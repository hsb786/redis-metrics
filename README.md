# redis-metrics
## 目的
想根据redis keyPattern进行统计，以评估每个key的访问量和rt，用来获取热key等信息，以便进行优化

### 现有的metrics
lettuce自带metrics，但只统计到command的纬度，没有具体的key tag

[lettuce metics](https://lettuce.io/core/release/api/io/lettuce/core/metrics/package-summary.html)

## 需求
1. 按照keyPattern统计
2. tag至少含command,key,exception
3. 尽可能的无侵入式

## 实现
### keyPattern聚合
假设keyPattern = user:%s，具体的key可能为user:1、user:2 ....，直接将具体的key统计成tag，会导致tag数量异常的多，最终导致prometheus内存不够，所以要按照keyPattern进行聚合

![RedisKeyPatternParse](./doc/image/RedisKeyPatternParse.png)
* RedisKeyPatternParse: 用于将原始key解析成keyPattern
* RedisKeyEnumPatternParse: 根据keyPattern枚举 将原始key解析成keyPattern

示例：

定义key pattern枚举
```
public enum RedisKeyEnum implements BaseRedisKey {
  USER_KEY("user:%s:%s", "user info"),
  ;

  private String pattern;

  private String desc;

  RedisKeyEnum(String pattern, String desc) {
    this.pattern = pattern;
    this.desc = desc;
  }

  @Override
  public String pattern() {
    return this.pattern;
  }

  @Override
  public String desc() {
    return this.desc;
  }
}
```

初始化RedisKeyPatternParse，解析原始key对应的keyPattern
```
    @Test
    void parse() {
        RedisKeyPatternParse redisPatternParse = RedisKeyEnumPatternParse.createRedisPatternParse(RedisKeyEnum.class);
        Optional<ParseResult> keyPattern = redisPatternParse.parse("user:11");
        assertEquals("user:.*", keyPattern.map(ParseResult::getPattern).orElse(null));
    }
```

### 集成到现有框架中
现有项目都是通过redisTemplate操作redis, 所以目标就是给redisTemplate增加metrics的功能

代码现状，基本都是获取一个valueOperation，再通过valueOperation进行操作
```
redisTemplate.opsForValue().set("user:1","aaaaaa")
```

redisTemplate源码
```
public class RedisTemplate<K, V> extends RedisAccessor implements RedisOperations<K, V>, BeanClassLoaderAware {

	private final ValueOperations<K, V> valueOps = new DefaultValueOperations<>(this);
	
	@Override
	public ValueOperations<K, V> opsForValue() {
		return valueOps;
	}
```

思考：
想无侵入式的对redisTemplate进行增强，可以生成ValueOperations的代理对象，redisTemplate.opsForValue()返回代理对象，
但valueOps被private final修饰，无法替换

所以换个思路：
1. 创建redisTemplate代理对象，拦截opsForValue方法，返回代理的ValueOperations
2. valueOperations也是代理对象，方法执行前后会调用redisMetrics进行统计
3. redisMetrics会调用RedisKeyPatternParse提取keyPattern进行统计



## 实现
通过实现BeanPostProcessor，用代理redisTemplate替换原生redisTemplate。 代理的redisTemplate执行opsForValue方法返回代理的valueOperations
```
public class RedisTemplateMetricsBeanPostProcessor implements BeanPostProcessor {
    ...

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RedisTemplate) {
            RedisMethodInterceptor redisMethodInterceptor = createRedisMethodInterceptor();
            ProxyFactory proxyFactory = new ProxyFactory(bean);
            proxyFactory.addAdvice(redisMethodInterceptor);
            proxyFactory.addAdvice(
                    RedisOperationMethodInterceptor.createRedisOperationMethodInterceptor(
                            redisMethodInterceptor));
            proxyFactory.setProxyTargetClass(true);
            return proxyFactory.getProxy();
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
    ...

```

创建ValueOperations的代理，ValueOperations执行前后会通过redisMetrics进行统计
```
public class RedisOperationMethodInterceptor implements MethodInterceptor {

    private final Map<Object, Object> operationProxyCache = new ConcurrentHashMap<>();

    public static RedisOperationMethodInterceptor createRedisOperationMethodInterceptor(
            RedisMethodInterceptor redisMethodInterceptor) {
        return new RedisOperationMethodInterceptor(redisMethodInterceptor);
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();

        try {
            if (needProxy(invocation, result)) {
                return operationProxyCache.computeIfAbsent(result, key -> wrapper(result));
            }
        } catch (Exception e) {
            LOG.warn("create redis proxy operation error", e);
            return result;
        }

        return result;
    }

    private Object wrapper(Object result) {
        ProxyFactory proxyFactory = new ProxyFactory(result);
        proxyFactory.addAdvice(redisMethodInterceptor);
        Object proxy = proxyFactory.getProxy();
        LOG.info(String.format("create proxy success, obj:%s", proxy));
        return proxy;
    }
    ...

```

RedisMetrics通过redisPatternParse提取keyPattern，进行统计
```
public class RedisMetrics {
   ...
    public static RedisMetrics createRedisMetrics(
            RedisKeyPatternParse redisPatternParse, MeterRegistry meterRegistry) {
        return new RedisMetrics(redisPatternParse, meterRegistry);
    }

    public void metrics(String originalKey, String command, long start, Exception e) {
        Optional<ParseResult> parseResultOptional = redisPatternParse.parse(originalKey);
        if (parseResultOptional.isPresent()) {
            ParseResult parseResult = parseResultOptional.get();
            doMetrics(parseResult.getPattern(), parseResult.getDesc(), command, start, e);
        } else {
            doMetrics("UNKNOWN", "UNKNOWN", command, start, e);
            LOG.debug(
                    "not found key, originalKey:{}, redisPatternParse:{}",
                    originalKey,
                    redisPatternParse.getClass().getSimpleName());
        }
    }
```


## 结果
只需定义枚举实现BaseRedisKey，就可以完成keyPattern的统计，无需修改业务代码
```
redis_seconds_count{command="set",desc="user info",exception="None",key="user:.*",} 1.0
```



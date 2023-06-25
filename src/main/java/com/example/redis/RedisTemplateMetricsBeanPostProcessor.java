package com.example.redis;

import com.example.redis.pattern.RedisKeyPatternParse;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author hushengbin
 * @date 2023-03-07 0:38
 */
public class RedisTemplateMetricsBeanPostProcessor implements BeanPostProcessor {

    private final ApplicationContext applicationContext;

    public RedisTemplateMetricsBeanPostProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

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

    private RedisMethodInterceptor createRedisMethodInterceptor() {
        RedisKeyPatternParse redisKeyPatternParse =
                applicationContext.getBean(RedisKeyPatternParse.class);
        MeterRegistry meterRegistry = applicationContext.getBean(MeterRegistry.class);
        RedisMetrics redisMetrics =
                RedisMetrics.createRedisMetrics(redisKeyPatternParse, meterRegistry);
        return RedisMethodInterceptor.createRedisMethodInterceptor(redisMetrics);
    }
}

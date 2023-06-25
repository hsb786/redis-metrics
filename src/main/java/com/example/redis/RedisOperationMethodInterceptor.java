package com.example.redis;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.redis.core.HashOperations;

/**
 * @author hushengbin
 * @date 2023-03-07 0:40
 */
public class RedisOperationMethodInterceptor implements MethodInterceptor {

    private static final Log LOG = LogFactory.getLog(RedisOperationMethodInterceptor.class);

    private final RedisMethodInterceptor redisMethodInterceptor;

    private final Map<Object, Object> operationProxyCache = new ConcurrentHashMap<>();

    private RedisOperationMethodInterceptor(RedisMethodInterceptor redisMethodInterceptor) {
        this.redisMethodInterceptor = redisMethodInterceptor;
    }

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

    private boolean needProxy(MethodInvocation invocation, Object result) {
        return isEmptyArguments(invocation)
                && Optional.ofNullable(result)
                .filter(obj -> !(obj instanceof HashOperations))
                .map(Object::getClass)
                .map(Class::getSuperclass)
                .map(Class::getName)
                .filter("org.springframework.data.redis.core.AbstractOperations"::equals)
                .isPresent();
    }

    private static boolean isEmptyArguments(MethodInvocation invocation) {
        return invocation.getArguments().length == 0;
    }
}

package com.example.redis;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisOperationMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RedisOperationMethodInterceptor.class);

    private final RedisMetrics redisMetrics;

    public RedisOperationMethodInterceptor(RedisMetrics redisMetrics) {
        this.redisMetrics = redisMetrics;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object[] arguments = invocation.getArguments();
        boolean needMetrics = arguments.length > 0 && arguments[0] instanceof String;
        if (!needMetrics) {
            return invocation.proceed();
        }

        long start = System.currentTimeMillis();
        Object result;
        String key = (String) arguments[0];
        String command = invocation.getMethod().getName();
        try {
            result = invocation.proceed();
        } catch (Exception e) {
            LOG.warn("redis execute error", e);
            redisMetrics.metrics(key, command, System.currentTimeMillis() - start, e);
            throw e;
        }
        redisMetrics.metrics(key, command, System.currentTimeMillis() - start, null);
        return result;
    }
}

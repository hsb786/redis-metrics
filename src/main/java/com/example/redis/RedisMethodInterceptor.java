package com.example.redis;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * @author hushengbin
 * @date 2023-02-10 20:47
 */
public class RedisMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RedisMethodInterceptor.class);

    private final RedisMetrics redisMetrics;

    private RedisMethodInterceptor(RedisMetrics redisMetrics) {
        this.redisMetrics = redisMetrics;
    }

    public static RedisMethodInterceptor createRedisMethodInterceptor(RedisMetrics redisMetrics) {
        return new RedisMethodInterceptor(redisMetrics);
    }

    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        Object[] arguments = invocation.getArguments();
        boolean needMetrics = arguments.length > 0 && arguments[0] instanceof String;
        if (!needMetrics) {
            return invocation.proceed();
        }

        long start = System.currentTimeMillis();
        Object result;
        try {
            result = invocation.proceed();
        } catch (Exception e) {
            LOG.warn("redis execute error", e);
            metrics((String) arguments[0], invocation.getMethod().getName(), start, e);
            throw e;
        }
        metrics((String) arguments[0], invocation.getMethod().getName(), start, null);
        return result;
    }

    private void metrics(String originalKey, String command, long start, Exception e) {
        try {
            redisMetrics.metrics(originalKey, command, start, e);
        } catch (Exception exception) {
            LOG.warn("redis metrics error", exception);
        }
    }
}

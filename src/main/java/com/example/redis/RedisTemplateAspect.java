package com.example.redis;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.redis.core.HashOperations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
public class RedisTemplateAspect {

    private final Map<Object, Object> operationProxyCache = new ConcurrentHashMap<>();

    private final RedisOperationMethodInterceptor redisOperationMethodInterceptor;

    public RedisTemplateAspect(RedisMetrics redisMetrics) {
        this.redisOperationMethodInterceptor = new RedisOperationMethodInterceptor(redisMetrics);
    }


    @Pointcut("execution(public * org.springframework.data.redis.core.RedisTemplate.opsFor*())")
    public void operationPointCut() {

    }

    @Around("operationPointCut()")
    public Object aroundOpsFor(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (needProxy(result)) {
            return operationProxyCache.computeIfAbsent(result, (re) -> {
                ProxyFactory proxyFactory = new ProxyFactory(result);
                proxyFactory.addAdvice(redisOperationMethodInterceptor);
                return proxyFactory.getProxy();
            });
        }
        return result;
    }

    private static boolean needProxy(Object result) {
        return "AbstractOperations".equals(result.getClass().getSuperclass().getSimpleName())
                && !(result instanceof HashOperations);
    }

}

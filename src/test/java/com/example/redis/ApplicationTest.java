package com.example.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@EnableAspectJAutoProxy
@SpringBootTest
@SpringBootApplication
public class ApplicationTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testOpsFor() {
        ValueOperations valueOperations = stringRedisTemplate.opsForValue();
        Assertions.assertTrue(AopUtils.isAopProxy(valueOperations));
    }


}

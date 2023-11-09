package com.example.redis.pattern;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultRedisKeyPatternParseTest {

    protected RedisKeyPatternParse redisPatternParse = new JdkRegexpRedisKeyPatternParse(RedisKeyConstantTest.class);

    @Test
    void exist_parse() {
        Optional<String> keyPattern = redisPatternParse.parse("user:11");
        assertEquals("user:.*", keyPattern.orElse(null));
    }

    @Test
    void not_exist_parse() {
        Optional<String> keyPattern = redisPatternParse.parse("aaa:11");
        assertEquals(false, keyPattern.isPresent());
    }
}
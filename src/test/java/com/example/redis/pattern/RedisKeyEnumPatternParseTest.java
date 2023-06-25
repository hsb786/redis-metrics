package com.example.redis.pattern;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisKeyEnumPatternParseTest {

    @Test
    void exist_parse() {
        RedisKeyPatternParse redisPatternParse = RedisKeyEnumPatternParse.createRedisPatternParse(RedisKeyEnum.class);
        Optional<ParseResult> keyPattern = redisPatternParse.parse("user:11");
        assertEquals("user:.*", keyPattern.map(ParseResult::getPattern).orElse(null));
    }

    @Test
    void not_exist_parse() {
        RedisKeyPatternParse redisPatternParse = RedisKeyEnumPatternParse.createRedisPatternParse(RedisKeyEnum.class);
        Optional<ParseResult> keyPattern = redisPatternParse.parse("aaa:11");
        assertEquals(false, keyPattern.isPresent());
    }
}
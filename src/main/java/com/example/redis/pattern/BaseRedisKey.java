package com.example.redis.pattern;

/**
 * @author hushengbin
 * @date 2023-02-09 14:22
 */
public interface BaseRedisKey {

    String pattern();

    String desc();

    default String pattern(Object... args) {
        return String.format(pattern(), args);
    }
}

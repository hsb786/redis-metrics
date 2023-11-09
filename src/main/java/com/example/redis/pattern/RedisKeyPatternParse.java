package com.example.redis.pattern;

import java.util.Optional;

/**
 * @author hushengbin
 * @date 2023-02-22 18:10
 */
public interface RedisKeyPatternParse {

    Optional<String> parse(String original);
}

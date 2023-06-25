package com.example.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author hushengbin
 * @date 2023-02-09 15:14
 */
@ConfigurationProperties(prefix = "custom.redis")
public class CustomRedisProperties {

    private String redisKeyClass;

    public String getRedisKeyClass() {
        return redisKeyClass;
    }

    public void setRedisKeyClass(String redisKeyClass) {
        this.redisKeyClass = redisKeyClass;
    }
}

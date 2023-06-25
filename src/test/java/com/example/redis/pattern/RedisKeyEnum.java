package com.example.redis.pattern;

/**
 * @author hushengbin
 * @date 2023-06-25 17:41
 */
public enum RedisKeyEnum implements BaseRedisKey {
    USER_KEY("user:%s", "user info"),
    ;

    private String pattern;

    private String desc;

    RedisKeyEnum(String pattern, String desc) {
        this.pattern = pattern;
        this.desc = desc;
    }

    @Override
    public String pattern() {
        return this.pattern;
    }

    @Override
    public String desc() {
        return this.desc;
    }
}
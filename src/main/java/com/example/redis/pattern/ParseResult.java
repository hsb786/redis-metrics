package com.example.redis.pattern;

/**
 * @author hushengbin
 * @date 2023-02-22 18:14
 */
public class ParseResult {

    private String pattern;

    private String desc;

    public ParseResult(String pattern, String desc) {
        this.pattern = pattern;
        this.desc = desc;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

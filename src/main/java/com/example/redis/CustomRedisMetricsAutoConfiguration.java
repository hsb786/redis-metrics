package com.example.redis;

import com.example.redis.pattern.JdkRegexpRedisKeyPatternParse;
import com.example.redis.pattern.RedisKeyPatternParse;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@AutoConfigureAfter(RedisAutoConfiguration.class)
public class CustomRedisMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedisKeyPatternParse.class)
    public RedisKeyPatternParse redisKeyPatternParse() {
        return new JdkRegexpRedisKeyPatternParse(Collections.emptyList());
    }

    @Bean
    public RedisMetrics redisMetrics(RedisKeyPatternParse redisPatternParse) {
        return new RedisMetrics(redisPatternParse);
    }

    @Bean
    public RedisTemplateAspect redisTemplateAspect(RedisMetrics redisMetrics) {
        return new RedisTemplateAspect(redisMetrics);
    }

}

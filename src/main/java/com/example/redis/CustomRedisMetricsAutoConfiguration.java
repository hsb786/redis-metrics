package com.example.redis;

import com.example.redis.pattern.RedisKeyEnumPatternParse;
import com.example.redis.pattern.RedisKeyPatternParse;
import io.lettuce.core.metrics.MicrometerCommandLatencyRecorder;
import io.lettuce.core.metrics.MicrometerOptions;
import io.lettuce.core.resource.DefaultClientResources;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.util.internal.PlatformDependent;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(CustomRedisProperties.class)
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class CustomRedisMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisKeyPatternParse redisKeyPatternParse(
            CustomRedisProperties platformRedisProperties) {
        return RedisKeyEnumPatternParse.createRedisPatternParse(
                platformRedisProperties.getRedisKeyClass());
    }

    @Bean
    public RedisTemplateMetricsBeanPostProcessor redisTemplateMetricsBeanPostProcessor(
            ApplicationContext applicationContext) {
        return new RedisTemplateMetricsBeanPostProcessor(applicationContext);
    }
}

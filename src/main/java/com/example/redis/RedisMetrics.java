package com.example.redis;

import com.example.redis.pattern.RedisKeyPatternParse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author hushengbin
 * @date 2023-02-09 14:26
 */
public class RedisMetrics implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(RedisMetrics.class);

    private final RedisKeyPatternParse redisPatternParse;

    private MeterRegistry meterRegistry;

    public RedisMetrics(RedisKeyPatternParse redisPatternParse) {
        this.redisPatternParse = redisPatternParse;
    }

    public void metrics(String originalKey, String command, long cost, Exception e) {
        Optional<String> parseResultOptional = redisPatternParse.parse(originalKey);
        if (parseResultOptional.isPresent()) {
            doMetrics(parseResultOptional.get(), command, cost, e);
        } else {
            doMetrics("UNKNOWN", command, cost, e);
            LOG.debug(
                    "not found key, originalKey:{}, redisPatternParse:{}",
                    originalKey,
                    redisPatternParse.getClass().getSimpleName());
        }
    }

    private void doMetrics(String key, String command, long cost, Exception e) {
        Timer.builder("redis")
                .publishPercentileHistogram(false)
                .tag("command", command)
                .tag("key", key)
                .tag(
                        "exception",
                        e == null
                                ? "None"
                                : Optional.ofNullable(e.getCause()).orElse(e).getClass().getSimpleName())
                .publishPercentileHistogram(true)
                .register(meterRegistry)
                .record(cost, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.meterRegistry = applicationContext.getBean(MeterRegistry.class);
    }
}

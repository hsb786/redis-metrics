package com.example.redis;

import com.example.redis.pattern.RedisKeyPatternParse;
import com.example.redis.pattern.ParseResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author hushengbin
 * @date 2023-02-09 14:26
 */
public class RedisMetrics {

    private static final Logger LOG = LoggerFactory.getLogger(RedisMetrics.class);

    private final RedisKeyPatternParse redisPatternParse;

    private final MeterRegistry meterRegistry;

    private RedisMetrics(RedisKeyPatternParse redisPatternParse, MeterRegistry meterRegistry) {
        this.redisPatternParse = redisPatternParse;
        this.meterRegistry = meterRegistry;
    }

    public static RedisMetrics createRedisMetrics(
            RedisKeyPatternParse redisPatternParse, MeterRegistry meterRegistry) {
        return new RedisMetrics(redisPatternParse, meterRegistry);
    }

    public void metrics(String originalKey, String command, long start, Exception e) {
        Optional<ParseResult> parseResultOptional = redisPatternParse.parse(originalKey);
        if (parseResultOptional.isPresent()) {
            ParseResult parseResult = parseResultOptional.get();
            doMetrics(parseResult.getPattern(), parseResult.getDesc(), command, start, e);
        } else {
            doMetrics("UNKNOWN", "UNKNOWN", command, start, e);
            LOG.debug(
                    "not found key, originalKey:{}, redisPatternParse:{}",
                    originalKey,
                    redisPatternParse.getClass().getSimpleName());
        }
    }

    private void doMetrics(String key, String desc, String command, long start, Exception e) {
        Timer.builder("redis")
                .publishPercentileHistogram(false)
                .tag("command", command)
                .tag("key", key)
                .tag("desc", desc)
                .tag(
                        "exception",
                        e == null
                                ? "None"
                                : Optional.ofNullable(e.getCause()).orElse(e).getClass().getSimpleName())
                .publishPercentileHistogram(true)
                .register(this.meterRegistry)
                .record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
    }
}

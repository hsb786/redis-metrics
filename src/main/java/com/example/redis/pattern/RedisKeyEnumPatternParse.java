package com.example.redis.pattern;

import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author hushengbin
 * @date 2023-02-09 14:20
 */
public class RedisKeyEnumPatternParse implements RedisKeyPatternParse {

    private static final Log LOG = LogFactory.getLog(RedisKeyEnumPatternParse.class);

    private List<Pair<String, String>> patternToDesc = new ArrayList<>();

    public RedisKeyEnumPatternParse() {
    }

    private RedisKeyEnumPatternParse(Class<? extends BaseRedisKey> clazz) {
        BaseRedisKey[] enumConstants = (BaseRedisKey[]) clazz.getEnumConstants();
        patternToDesc =
                Arrays.stream(enumConstants)
                        .map(i -> Pair.of(i.pattern().replace("%s", ".*"), i.desc()))
                        .collect(Collectors.toList());
    }

    public Optional<ParseResult> parse(String original) {
        return this.patternToDesc.stream()
                .filter(i -> Pattern.matches(i.getKey(), original))
                .findFirst()
                .map(i -> new ParseResult(i.getKey(), i.getValue()));
    }

    public static RedisKeyEnumPatternParse createRedisPatternParse(Class<? extends BaseRedisKey> clazzList) {
        return new RedisKeyEnumPatternParse(clazzList);
    }

    public static RedisKeyEnumPatternParse createRedisPatternParse(String classPath) {
        if (StringUtils.isBlank(classPath)) {
            return new RedisKeyEnumPatternParse();
        }

        try {
            return createRedisPatternParse((Class<? extends BaseRedisKey>) Class.forName(classPath));
        } catch (ClassNotFoundException e) {
            LOG.warn("class not found", e);
            throw new RuntimeException(e);
        }
    }
}

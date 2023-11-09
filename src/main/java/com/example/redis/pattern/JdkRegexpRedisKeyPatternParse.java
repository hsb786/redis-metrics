package com.example.redis.pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author hushengbin
 * @date 2023-02-09 14:20
 */
public class JdkRegexpRedisKeyPatternParse implements RedisKeyPatternParse {

    private static final Log LOG = LogFactory.getLog(JdkRegexpRedisKeyPatternParse.class);

    private final List<String> originals;

    public JdkRegexpRedisKeyPatternParse(List<String> originals) {
        this.originals = originals;
    }

    public JdkRegexpRedisKeyPatternParse(Class<?> clazz) {
        this.originals = Arrays.stream(clazz.getFields())
                .filter(field -> Modifier.isPublic(field.getModifiers())
                        && Modifier.isStatic(field.getModifiers())
                        && String.class.equals(field.getType()))
                .map(field -> {
                    try {
                        return String.valueOf(field.get(null));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(field -> field.replace("%s", ".*"))
                .collect(Collectors.toList());
    }

    public Optional<String> parse(String original) {
        return this.originals.stream()
                .filter(i -> Pattern.matches(i, original))
                .findFirst();
    }
}

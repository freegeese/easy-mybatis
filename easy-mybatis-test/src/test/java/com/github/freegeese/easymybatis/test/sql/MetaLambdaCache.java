package com.github.freegeese.easymybatis.test.sql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MetaLambdaCache {
    private static Map<String, MetaLambda> cache = new ConcurrentHashMap<>();

    public static MetaLambda get(Function function) {
        String name = function.getClass().getName();
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        MetaLambda value = MetaLambda.forFunction(function);
        cache.put(name, value);
        return value;
    }
}

package com.github.freegeese.easymybatis.meta;

import org.apache.ibatis.mapping.MappedStatement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 元数据缓存
 *
 * <p>通过此类获取元数据，将会被缓存，下次获取将会从缓存中获取
 *
 * @author zhangguangyong
 * @since 1.0
 */
public class MetaCache {
    private static Map<Class<?>, MetaEntityClass> metaEntityClassCache = new ConcurrentHashMap<>();
    private static Map<String, MetaMapperClass> metaMapperClassCache = new ConcurrentHashMap<>();

    public static MetaEntityClass getMetaEntityClass(Class<?> entityClass) {
        if (metaEntityClassCache.containsKey(entityClass)) {
            return metaEntityClassCache.get(entityClass);
        }
        MetaEntityClass metaEntityClass = MetaEntityClass.forClass(entityClass);
        metaEntityClassCache.put(entityClass, metaEntityClass);
        return metaEntityClass;
    }

    public static MetaMapperClass getMetaMapperClass(MappedStatement mappedStatement) {
        String id = mappedStatement.getId();
        if (metaMapperClassCache.containsKey(id)) {
            return metaMapperClassCache.get(id);
        }
        MetaMapperClass metaMapperClass = MetaMapperClass.forMappedStatement(mappedStatement);
        metaMapperClassCache.put(id, metaMapperClass);
        return metaMapperClass;
    }
}

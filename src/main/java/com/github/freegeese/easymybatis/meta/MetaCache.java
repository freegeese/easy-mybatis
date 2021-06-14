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
    private static Map<String, MetaEntityClass> metaEntityClassCache = new ConcurrentHashMap<>();
    private static Map<String, MetaMapperClass> metaMapperClassCache = new ConcurrentHashMap<>();

    public static MetaEntityClass getMetaEntityClass(Class<?> entityClass) {
        String entityClassName = entityClass.getName();
        if (metaEntityClassCache.containsKey(entityClassName)) {
            return metaEntityClassCache.get(entityClassName);
        }
        MetaEntityClass metaEntityClass = MetaEntityClass.forClass(entityClass);
        metaEntityClassCache.put(entityClassName, metaEntityClass);
        return metaEntityClass;
    }

    public static MetaMapperClass getMetaMapperClass(Class<?> mapperClass) {
        String mapperClassName = mapperClass.getName();
        if (metaMapperClassCache.containsKey(mapperClassName)) {
            return metaMapperClassCache.get(mapperClassName);
        }
        MetaMapperClass metaMapperClass = MetaMapperClass.forMapperClass(mapperClass);
        metaMapperClassCache.put(mapperClassName, metaMapperClass);
        return metaMapperClass;
    }

    public static MetaMapperClass getMetaMapperClass(MappedStatement mappedStatement) {
        String mapperClassName = toMapperClassName(mappedStatement.getId());
        if (metaMapperClassCache.containsKey(mapperClassName)) {
            return metaMapperClassCache.get(mapperClassName);
        }
        MetaMapperClass metaMapperClass = MetaMapperClass.forMappedStatement(mappedStatement);
        metaMapperClassCache.put(mapperClassName, metaMapperClass);
        return metaMapperClass;
    }

    private static String toMapperClassName(String mappedStatementId) {
        return mappedStatementId.substring(0, mappedStatementId.lastIndexOf("."));
    }
}

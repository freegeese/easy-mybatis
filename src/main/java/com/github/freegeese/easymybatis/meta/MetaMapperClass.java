package com.github.freegeese.easymybatis.meta;

import com.github.freegeese.easymybatis.util.RefUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mapper类元数据信息
 *
 * @author zhangguangyong
 * @since 1.0
 */
@Slf4j
@Getter
public class MetaMapperClass {
    /**
     * 获取 {@link MappedStatement#getId()}
     */
    private String id;
    /**
     * 实体类型
     */
    private Class<?> entityClass;
    /**
     * Mapper类型
     */
    private Class<?> mapperClass;
    /**
     * Mapper方法返回值类型
     */
    private Map<String, Class<?>> methodGenericReturnTypeMap;

    /**
     * 解析Mapper类型元信息
     *
     * @param mappedStatement
     * @return
     */
    public static MetaMapperClass forMappedStatement(MappedStatement mappedStatement) {
        MetaMapperClass meta = new MetaMapperClass();
        String id = mappedStatement.getId();
        meta.id = id;
        try {
            Class<?> mapperClass = Class.forName(id.substring(0, id.lastIndexOf(".")));
            meta.mapperClass = mapperClass;

            // 继承接口
            Type[] genericInterfaces = mapperClass.getGenericInterfaces();
            if (Objects.isNull(genericInterfaces) || genericInterfaces.length == 0) {
                return meta;
            }
            // 泛型参数
            ParameterizedType parameterizedType = (ParameterizedType) genericInterfaces[0];
            Type[] types = parameterizedType.getActualTypeArguments();
            if (Objects.isNull(types) || types.length == 0) {
                return meta;
            }
            meta.entityClass = (Class<?>) types[0];

            // 方法返回泛型类型
            Map<String, Class<?>> methodGenericReturnTypeMap = new HashMap<>();
            meta.methodGenericReturnTypeMap = methodGenericReturnTypeMap;

            // 泛型类型名称与泛型类型的映射
            Map<String, Class<?>> genericNameAndTypeMap = RefUtils.getGenericNameAndTypeMap(mapperClass);
            if (Objects.isNull(genericNameAndTypeMap) || genericNameAndTypeMap.isEmpty()) {
                return meta;
            }

            List<Method> methods = RefUtils.getAllDeclaredMethods(mapperClass);
            for (Method method : methods) {
                Type type = method.getGenericReturnType();
                if (type instanceof TypeVariable) {
                    String typeName = type.getTypeName();
                    if (genericNameAndTypeMap.containsKey(typeName)) {
                        methodGenericReturnTypeMap.put(method.getName(), genericNameAndTypeMap.get(typeName));
                    }
                }
                if (type instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) type;
                    Type[] actualTypeArguments = pt.getActualTypeArguments();
                    for (Type actualTypeArgument : actualTypeArguments) {
                        String typeName = actualTypeArgument.getTypeName();
                        if (genericNameAndTypeMap.containsKey(typeName)) {
                            methodGenericReturnTypeMap.put(method.getName(), genericNameAndTypeMap.get(typeName));
                        }
                    }
                }
            }

            return meta;
        } catch (ClassNotFoundException e) {
            log.error("获取 entityClass 和  mapperClass 发生异常[" + id + "]", e);
            throw new RuntimeException(e);
        }
    }
}

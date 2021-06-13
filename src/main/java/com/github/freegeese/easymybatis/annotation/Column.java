package com.github.freegeese.easymybatis.annotation;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于定义数据库列
 *
 * @author zhangguangyong
 * @see com.github.freegeese.easymybatis.meta.MetaEntityClass
 * @see com.github.freegeese.easymybatis.EasyMybatisConfiguration
 * @since 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Column {
    /**
     * 列名
     *
     * @return
     */
    String value() default "";

    /**
     * 列类型
     *
     * @return
     */
    JdbcType jdbcType() default JdbcType.UNDEFINED;

    /**
     * 列类型处理器
     *
     * @return
     */
    Class<? extends TypeHandler<?>> typeHandler() default UnknownTypeHandler.class;
}

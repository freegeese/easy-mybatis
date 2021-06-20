package com.github.freegeese.easymybatis.core.annotation;

import com.github.freegeese.easymybatis.core.meta.MetaEntityClass;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于标识实体对应的表名
 *
 * @author zhangguangyong
 * @see MetaEntityClass
 * @since 1.0
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Table {
    String value() default "";
}

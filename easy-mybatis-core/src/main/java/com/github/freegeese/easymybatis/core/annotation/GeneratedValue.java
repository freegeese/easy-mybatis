package com.github.freegeese.easymybatis.core.annotation;

import com.github.freegeese.easymybatis.core.meta.MetaEntityClass;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.github.freegeese.easymybatis.core.annotation.GenerationType.AUTO;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 列值生成策略
 *
 * @author zhangguangyong
 * @see MetaEntityClass
 * @since 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface GeneratedValue {
    GenerationType strategy() default AUTO;
}

package com.github.freegeese.easymybatis.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.github.freegeese.easymybatis.annotation.GenerationType.AUTO;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 列值生成策略
 *
 * @author zhangguangyong
 * @see com.github.freegeese.easymybatis.meta.MetaEntityClass
 * @since 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface GeneratedValue {
    GenerationType strategy() default AUTO;
}

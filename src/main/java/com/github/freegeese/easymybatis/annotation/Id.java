package com.github.freegeese.easymybatis.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于标识实体主键字段
 *
 * @author zhangguangyong
 * @see com.github.freegeese.easymybatis.meta.MetaEntityClass
 * @since 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Id {
}

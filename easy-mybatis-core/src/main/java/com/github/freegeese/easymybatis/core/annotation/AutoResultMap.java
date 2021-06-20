package com.github.freegeese.easymybatis.core.annotation;


import com.github.freegeese.easymybatis.core.mapper.SelectMapper;
import com.github.freegeese.easymybatis.core.mapper.TreeableMapper;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 该注解应用在 Mapper 的查询接口上，当 Mapper 查询接口未指定任何结果集映射的时候
 * 该注解将会根据 Mapper<T> 的泛型T来生成一个 {@link org.apache.ibatis.mapping.ResultMap}
 * 对象去覆盖 {@link org.apache.ibatis.mapping.MappedStatement} 原有的
 * {@link org.apache.ibatis.mapping.ResultMap} 对象
 *
 * <p>请确保 {@link AutoResultMap} 使用于查询接口，且返回值为一个POJO对象或{@link java.util.Collection<POJO>}
 * 不然将无法生成 {@link org.apache.ibatis.mapping.ResultMap} 对象
 *
 * @author zhangguangyong
 * @see SelectMapper
 * @see TreeableMapper
 * @since 1.0
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface AutoResultMap {
}

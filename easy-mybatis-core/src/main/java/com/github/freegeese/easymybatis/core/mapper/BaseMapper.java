package com.github.freegeese.easymybatis.core.mapper;

/**
 * 通用基础 Mapper
 *
 * <p>一般Mapper继承此接口可快速使用一般增删改查接口
 *
 * @param <T> 实体类型
 * @author zhangguangyong
 * @since 1.0
 */
public interface BaseMapper<T> extends
        SelectMapper<T>,
        InsertMapper<T>,
        UpdateMapper<T>,
        DeleteMapper<T> {
}

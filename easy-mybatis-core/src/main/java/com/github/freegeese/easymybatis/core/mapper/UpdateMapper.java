package com.github.freegeese.easymybatis.core.mapper;

import com.github.freegeese.easymybatis.core.mapper.provider.UpdateSqlProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 通用基础更新 Mapper
 *
 * @param <T> 实体类型
 * @author zhangguangyong
 * @since 1.0
 */
public interface UpdateMapper<T> {
    /**
     * 更新单个
     *
     * @param entity
     * @return
     */
    @UpdateProvider(type = UpdateSqlProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(T entity);

    /**
     * 更新单个，只更新非空字段
     *
     * @param entity
     * @return
     */
    @UpdateProvider(type = UpdateSqlProvider.class, method = "updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(T entity);

    /**
     * 更新多个(只更新非空字段)
     *
     * @param entities
     * @return
     */
    @UpdateProvider(type = UpdateSqlProvider.class, method = "updateBatchSelective")
    int updateBatchSelective(@Param("entities") List<T> entities);
}

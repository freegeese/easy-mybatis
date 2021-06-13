package com.github.freegeese.easymybatis.mapper;

import com.github.freegeese.easymybatis.mapper.provider.InsertSqlProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通用基础插入 Mapper
 *
 * @param <T> 实体类型
 * @author zhangguangyong
 * @since 1.0
 */
public interface InsertMapper<T> {
    /**
     * 插入单个
     *
     * @param entity
     * @return
     */
    @InsertProvider(type = InsertSqlProvider.class, method = "insert")
    int insert(T entity);

    /**
     * 插入单个，只插入非空字段
     *
     * @param entity
     * @return
     */
    @InsertProvider(type = InsertSqlProvider.class, method = "insertSelective")
    int insertSelective(T entity);

    /**
     * 批量插入(忽略 auto 字段)
     *
     * @param entities
     * @return
     */
    @InsertProvider(type = InsertSqlProvider.class, method = "insertBatch")
    int insertBatch(@Param("entities") List<T> entities);

    /**
     * 插入多个(只插入非空字段)
     *
     * @param entities
     * @return
     */
    @InsertProvider(type = InsertSqlProvider.class, method = "insertBatchSelective")
    int insertBatchSelective(@Param("entities") List<T> entities);
}

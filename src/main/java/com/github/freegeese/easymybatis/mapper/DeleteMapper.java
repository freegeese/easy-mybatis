package com.github.freegeese.easymybatis.mapper;

import com.github.freegeese.easymybatis.mapper.provider.DeleteSqlProvider;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通用基础删除 Mapper
 *
 * @param <T>
 * @author zhangguangyong
 * @since 1.0
 */
public interface DeleteMapper<T> {
    @DeleteProvider(type = DeleteSqlProvider.class, method = "deleteByEntity")
    int deleteByEntity(T record);

    @DeleteProvider(type = DeleteSqlProvider.class, method = "deleteByParameterMap")
    int deleteByParameterMap(Map<String, Object> parameterMap);

    @DeleteProvider(type = DeleteSqlProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(Object id);

    @DeleteProvider(type = DeleteSqlProvider.class, method = "deleteByPrimaryKeys")
    int deleteByPrimaryKeys(@Param("ids") List<?> id);
}

package com.github.freegeese.easymybatis.mapper;

import com.github.freegeese.easymybatis.annotation.AutoResultMap;
import com.github.freegeese.easymybatis.mapper.provider.SelectSqlProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

/**
 * 通用基础查询 Mapper
 *
 * @param <T> 实体类型
 * @author zhangguangyong
 * @since 1.0
 */
public interface SelectMapper<T> {
    @SelectProvider(type = SelectSqlProvider.class, method = "selectByEntity")
    @AutoResultMap
    List<T> selectByEntity(T record);

    @SelectProvider(type = SelectSqlProvider.class, method = "selectByParameterMap")
    @AutoResultMap
    List<T> selectByParameterMap(@Param("parameterMap") Map<String, Object> parameterMap, @Param("entityClass") Class<T> entityClass);

    @SelectProvider(type = SelectSqlProvider.class, method = "selectByPrimaryKey")
    @AutoResultMap
    T selectByPrimaryKey(@Param("id") Object id, @Param("entityClass") Class<T> entityClass);

    @SelectProvider(type = SelectSqlProvider.class, method = "selectByPrimaryKeys")
    @AutoResultMap
    List<T> selectByPrimaryKeys(@Param("ids") List<?> id, @Param("entityClass") Class<T> entityClass);

    @SelectProvider(type = SelectSqlProvider.class, method = "selectAll")
    @AutoResultMap
    List<T> selectAll(Class<T> entityClass);
}

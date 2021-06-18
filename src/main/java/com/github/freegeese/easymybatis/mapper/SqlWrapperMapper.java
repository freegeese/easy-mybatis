package com.github.freegeese.easymybatis.mapper;

import com.github.freegeese.easymybatis.annotation.AutoResultMap;
import com.github.freegeese.easymybatis.criterion.SqlWrapper;
import com.github.freegeese.easymybatis.mapper.provider.SqlWrapperProvider;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * SQL包装器 Mapper
 *
 * @author zhangguangyong
 * @since 1.0
 */
public interface SqlWrapperMapper<T> {
    @SelectProvider(type = SqlWrapperProvider.class, method = "selectByWrapper")
    @AutoResultMap
    List<T> selectByWrapper(SqlWrapper.Result result);
}

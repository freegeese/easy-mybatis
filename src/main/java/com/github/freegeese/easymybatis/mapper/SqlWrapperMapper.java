package com.github.freegeese.easymybatis.mapper;

import com.github.freegeese.easymybatis.annotation.AutoResultMap;
import com.github.freegeese.easymybatis.criterion.SqlWrapper;
import com.github.freegeese.easymybatis.mapper.provider.SqlWrapperProvider;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * SQL包装器 Mapper
 *
 * @author zhangguangyong
 * @since 1.0
 */
public interface SqlWrapperMapper<T> {
    /**
     * 查询
     *
     * @param result
     * @return
     */
    @SelectProvider(type = SqlWrapperProvider.class, method = "sql")
    @AutoResultMap
    List<T> selectByWrapper(SqlWrapper.Result result);

    /**
     * 更新
     *
     * @param result
     * @return
     */
    @UpdateProvider(type = SqlWrapperProvider.class, method = "sql")
    int updateByWrapper(SqlWrapper.Result result);

    /**
     * 删除
     *
     * @param result
     * @return
     */
    @DeleteProvider(type = SqlWrapperProvider.class, method = "sql")
    int deleteByWrapper(SqlWrapper.Result result);
}

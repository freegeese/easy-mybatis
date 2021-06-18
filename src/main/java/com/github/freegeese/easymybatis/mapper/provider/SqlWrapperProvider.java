package com.github.freegeese.easymybatis.mapper.provider;

import com.github.freegeese.easymybatis.criterion.SqlWrapper;

/**
 * 提供基础查询 SQL
 *
 * @author zhangguangyong
 * @since 1.0
 */
public class SqlWrapperProvider {
    /**
     * 查询
     *
     * @param result
     * @return
     */
    public String selectByWrapper(SqlWrapper.Result result) {
        return result.getSql();
    }

}

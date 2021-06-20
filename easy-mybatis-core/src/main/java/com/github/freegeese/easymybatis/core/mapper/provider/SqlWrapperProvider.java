package com.github.freegeese.easymybatis.core.mapper.provider;

import com.github.freegeese.easymybatis.core.criterion.SqlWrapper;

/**
 * 提供 从SqlWrapper中获取SQL
 *
 * @author zhangguangyong
 * @since 1.0
 */
public class SqlWrapperProvider {
    public String sql(SqlWrapper.Result result) {
        return result.getSql();
    }
}

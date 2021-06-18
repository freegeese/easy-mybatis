package com.github.freegeese.easymybatis.interceptor;

import com.github.freegeese.easymybatis.domain.Dateable;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * @author zhangguangyong
 * @see Dateable
 * @since 1.0
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
})
public class SqlWrapperInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
//        Object[] args = invocation.getArgs();
//        Object param = args[1];
//        if (Objects.nonNull(param) && param instanceof SqlWrapper) {
//            args[1] = ((SqlWrapper) param).unwrap().getParameterMap();
//        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}

package com.github.freegeese.easymybatis.interceptor;

import com.github.freegeese.easymybatis.domain.Dateable;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;

import java.util.*;

/**
 * 记录创建时间和修改时间拦截器
 *
 * <p>当插入类型为{@link Dateable}类型时，会自动填充创建时间和修改时间
 *
 * @author zhangguangyong
 * @see Dateable
 * @since 1.0
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
})
public class DateableInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object param = args[1];

        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Date now = new Date();

        // 设置创建时间
        if (sqlCommandType == SqlCommandType.INSERT) {
            Object param1 = Map.class.isAssignableFrom(param.getClass()) ? ((Map) param).get("param1") : param;
            if (Collection.class.isAssignableFrom(param1.getClass())) {
                Collection<?> items = (Collection<?>) param1;
                for (Object item : items) {
                    if (Dateable.class.isAssignableFrom(item.getClass()) && Objects.isNull(((Dateable) item).getCreatedDate())) {
                        ((Dateable) item).setCreatedDate(now);
                    }
                }
            }
            if (Dateable.class.isAssignableFrom(param1.getClass()) && Objects.isNull(((Dateable) param1).getCreatedDate())) {
                ((Dateable) param1).setCreatedDate(now);
            }
        }

        // 设置更新时间
        if (sqlCommandType == SqlCommandType.UPDATE) {
            Object param1 = Map.class.isAssignableFrom(param.getClass()) ? ((Map) param).get("param1") : param;
            if (Collection.class.isAssignableFrom(param1.getClass())) {
                Collection<?> items = (Collection<?>) param1;
                for (Object item : items) {
                    if (Dateable.class.isAssignableFrom(item.getClass()) && Objects.isNull(((Dateable) item).getLastModifiedDate())) {
                        ((Dateable) item).setLastModifiedDate(now);
                    }
                }
            }
            if (Dateable.class.isAssignableFrom(param1.getClass()) && Objects.isNull(((Dateable) param1).getLastModifiedDate())) {
                ((Dateable) param1).setLastModifiedDate(now);
            }
        }

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

package com.github.freegeese.easymybatis.core.mapper.provider;

import com.github.freegeese.easymybatis.core.meta.MetaEntityClass;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.AbstractSQL;
import org.apache.ibatis.jdbc.SQL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 提供基础更新 SQL
 *
 * @author zhangguangyong
 * @since 1.0
 */
public class UpdateSqlProvider extends BaseSqlProvider{
    /**
     * 更新单个
     *
     * @param entity
     * @return
     */
    public String updateByPrimaryKey(Object entity, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        List<MetaEntityClass.ResultMapping> resultMappings = meta.getResultMappingsWithoutNull(entity);
        MetaEntityClass.ResultMapping primaryKey = meta.checkPrimaryKey(entity);
        return updateSql(meta, resultMappings, primaryKey);
    }

    /**
     * 更新单个，只更新非空字段
     *
     * @param entity
     * @return
     */
    public String updateByPrimaryKeySelective(Object entity, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        List<MetaEntityClass.ResultMapping> resultMappings = meta.getResultMappingsWithoutPrimaryKeyOrNull(entity);
        MetaEntityClass.ResultMapping primaryKey = meta.checkPrimaryKey(entity);
        return updateSql(meta, resultMappings, primaryKey);
    }

    /**
     * 更新多个(只更新非空字段)
     *
     * @param entities
     * @return
     */
    public String updateBatchSelective(@Param("entities") List<?> entities, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        AtomicInteger index = new AtomicInteger(0);

        List<SQL> sqls = new ArrayList<>();
        for (Object entity : entities) {
            List<MetaEntityClass.ResultMapping> resultMappings = meta.getResultMappingsWithoutPrimaryKeyOrNull(entity);
            MetaEntityClass.ResultMapping primaryKey = meta.checkPrimaryKey(entity);
            int i = index.getAndIncrement();

            String sets = resultMappings.stream().map(v -> v.getColumn() + " = #{entities[" + i + "]." + v.getProperty() + "}").collect(Collectors.joining(","));
            SQL sql = new SQL().UPDATE(meta.getTable()).SET(sets).WHERE(primaryKey.getColumn() + " = #{entities[" + i + "]." + primaryKey.getProperty() + "}");

            sqls.add(sql);
        }

        return sqls.stream().map(AbstractSQL::toString).collect(Collectors.joining(";"));
    }

    private String updateSql(MetaEntityClass meta, List<MetaEntityClass.ResultMapping> resultMappings, MetaEntityClass.ResultMapping primaryKey) {
        String sets = resultMappings.stream().map(v -> v.getColumn() + " = #{" + v.getProperty() + "}").collect(Collectors.joining(","));
        SQL sql = new SQL().UPDATE(meta.getTable()).SET(sets).WHERE(primaryKey.getColumn() + " = #{" + primaryKey.getProperty() + "}");
        return sql.toString();
    }

}

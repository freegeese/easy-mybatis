package com.github.freegeese.easymybatis.mapper.provider;

import com.github.freegeese.easymybatis.meta.MetaCache;
import com.github.freegeese.easymybatis.meta.MetaEntityClass;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.AbstractSQL;
import org.apache.ibatis.jdbc.SQL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 提供基础插入 SQL
 *
 * @author zhangguangyong
 * @since 1.0
 */
public class InsertSqlProvider {
    /**
     * 插入单个
     *
     * @param entity
     * @return
     */
    public String insert(Object entity) {
        MetaEntityClass meta = MetaCache.getMetaEntityClass(entity.getClass());
        List<MetaEntityClass.ResultMapping> resultMappings = meta.getResultMappingsWithoutAutoAndNull(entity);
        return insertSql(meta, resultMappings);
    }

    /**
     * 插入单个，只插入非空字段
     *
     * @param entity
     * @return
     */
    public String insertSelective(Object entity) {
        MetaEntityClass meta = MetaCache.getMetaEntityClass(entity.getClass());
        List<MetaEntityClass.ResultMapping> resultMappings = meta.getResultMappingsWithoutNull(entity);
        return insertSql(meta, resultMappings);
    }

    private String insertSql(MetaEntityClass meta, List<MetaEntityClass.ResultMapping> resultMappings) {
        String columns = resultMappings.stream().map(MetaEntityClass.ResultMapping::getColumn).collect(Collectors.joining(","));
        String values = resultMappings.stream().map(v -> "#{" + v.getProperty() + "}").collect(Collectors.joining(","));
        SQL sql = new SQL().INSERT_INTO(meta.getTable()).INTO_COLUMNS(columns).INTO_VALUES(values);
        return sql.toString();
    }

    /**
     * 批量插入(忽略 auto 字段)
     *
     * @param entities
     * @return
     */
    public String insertBatch(@Param("entities") List<?> entities) {
        Class<?> entityClass = entities.get(0).getClass();
        MetaEntityClass meta = MetaCache.getMetaEntityClass(entityClass);
        List<MetaEntityClass.ResultMapping> resultMappings = meta.getResultMappingsWithoutAuto();

        String columns = resultMappings.stream().map(MetaEntityClass.ResultMapping::getColumn).collect(Collectors.joining(","));
        String valuesTemplate = resultMappings.stream().map(v -> "#{entities[i]." + v.getProperty() + "}").collect(Collectors.joining(","));
        List<String> multipleRowsValues = IntStream.range(0, entities.size()).mapToObj(v -> valuesTemplate.replace("[i]", "[" + v + "]")).collect(Collectors.toList());

        SQL sql = new SQL().INSERT_INTO(meta.getTable()).INTO_COLUMNS(columns);
        for (String values : multipleRowsValues) {
            sql.INTO_VALUES(values).ADD_ROW();
        }

        return sql.toString();
    }

    /**
     * 插入多个(只插入非空字段)
     *
     * @param entities
     * @return
     */
    public String insertBatchSelective(@Param("entities") List<?> entities) {
        Class<?> entityClass = entities.get(0).getClass();
        MetaEntityClass meta = MetaCache.getMetaEntityClass(entityClass);
        AtomicInteger index = new AtomicInteger(0);

        List<SQL> sqls = new ArrayList<>();
        for (Object entity : entities) {
            List<MetaEntityClass.ResultMapping> resultMappings = meta.getResultMappingsWithoutNull(entity);
            int i = index.getAndIncrement();
            String columns = resultMappings.stream().map(MetaEntityClass.ResultMapping::getColumn).collect(Collectors.joining(","));
            String values = resultMappings.stream().map(v -> "#{entities[" + i + "]." + v.getProperty() + "}").collect(Collectors.joining(","));
            sqls.add(new SQL().INSERT_INTO(meta.getTable()).INTO_COLUMNS(columns).INTO_VALUES(values));
        }

        return sqls.stream().map(AbstractSQL::toString).collect(Collectors.joining(";"));
    }

}

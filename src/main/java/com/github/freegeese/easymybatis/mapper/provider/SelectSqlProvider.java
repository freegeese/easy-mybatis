package com.github.freegeese.easymybatis.mapper.provider;

import com.github.freegeese.easymybatis.meta.MetaCache;
import com.github.freegeese.easymybatis.meta.MetaEntityClass;
import com.github.freegeese.easymybatis.util.RefUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 提供基础查询 SQL
 *
 * @author zhangguangyong
 * @since 1.0
 */
public class SelectSqlProvider {
    /**
     * 根据主键查询
     *
     * @param id
     * @param entityClass
     * @return
     */
    public String selectByPrimaryKey(@Param("id") Object id, @Param("entityClass") Class<?> entityClass) {
        MetaEntityClass meta = MetaCache.getMetaEntityClass(entityClass);
        MetaEntityClass.ResultMapping primaryKey = meta.getPrimaryKeyResultMapping();
        SQL sql = new SQL().SELECT(meta.getColumns()).FROM(meta.getTable()).WHERE(primaryKey.getColumn() + " = #{id}");
        return sql.toString();
    }

    /**
     * 根据多个主键查询
     *
     * @param ids
     * @param entityClass
     * @return
     */
    public String selectByPrimaryKeys(@Param("ids") List<?> ids, @Param("entityClass") Class<?> entityClass) {
        MetaEntityClass meta = MetaCache.getMetaEntityClass(entityClass);
        MetaEntityClass.ResultMapping primaryKey = meta.getPrimaryKeyResultMapping();
        String condition = IntStream.range(0, ids.size()).mapToObj(v -> "#{ids[" + v + "]}").collect(Collectors.joining(","));
        SQL sql = new SQL().SELECT(meta.getColumns()).FROM(meta.getTable()).WHERE(primaryKey.getColumn() + " in (" + condition + ")");
        return sql.toString();
    }

    /**
     * 根据记录查询
     *
     * @param record
     * @return
     */
    public String selectByEntity(Object record) {
        MetaEntityClass meta = MetaCache.getMetaEntityClass(record.getClass());
        SQL sql = new SQL().SELECT(meta.getColumns()).FROM(meta.getTable());
        List<MetaEntityClass.ResultMapping> resultMappings = meta.getResultMappings();

        for (MetaEntityClass.ResultMapping resultMapping : resultMappings) {
            if (Objects.isNull(RefUtils.invokeMethod(resultMapping.getGetMethod(), record))) {
                continue;
            }
            sql.WHERE(resultMapping.getColumn() + " = #{" + resultMapping.getProperty() + "}");
        }
        return sql.toString();
    }

    /**
     * 根据参数查询
     *
     * @param parameterMap
     * @param entityClass
     * @return
     */
    public String selectByParameterMap(@Param("parameterMap") Map<String, Object> parameterMap, @Param("entityClass") Class<?> entityClass) {
        MetaEntityClass meta = MetaCache.getMetaEntityClass(entityClass);
        SQL sql = new SQL().SELECT(meta.getColumns()).FROM(meta.getTable());
        List<MetaEntityClass.ResultMapping> resultMappings = meta.getResultMappings();

        for (MetaEntityClass.ResultMapping resultMapping : resultMappings) {
            if (Objects.isNull(parameterMap.getOrDefault(resultMapping.getProperty(), null))) {
                continue;
            }
            sql.WHERE(resultMapping.getColumn() + " ${parameterMap." + resultMapping.getProperty() + "}");
        }
        return sql.toString();
    }

    /**
     * 查询所有
     *
     * @param entityClass
     * @return
     */
    public String selectAll(Class<?> entityClass) {
        MetaEntityClass meta = MetaCache.getMetaEntityClass(entityClass);
        SQL sql = new SQL().SELECT(meta.getColumns()).FROM(meta.getTable());
        return sql.toString();
    }
}
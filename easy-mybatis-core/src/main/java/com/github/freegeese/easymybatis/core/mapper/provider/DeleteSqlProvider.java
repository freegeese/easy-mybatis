package com.github.freegeese.easymybatis.core.mapper.provider;

import com.github.freegeese.easymybatis.core.meta.MetaEntityClass;
import com.github.freegeese.easymybatis.util.RefUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 提供基础删除 SQL
 *
 * @author zhangguangyong
 * @since 1.0
 */
public class DeleteSqlProvider extends BaseSqlProvider {
    /**
     * 根据主键查询
     *
     * @param id
     * @return
     */
    public String deleteByPrimaryKey(Object id, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        MetaEntityClass.ResultMapping primaryKey = meta.getPrimaryKeyResultMapping();
        SQL sql = new SQL().DELETE_FROM(meta.getTable()).WHERE(primaryKey.getColumn() + " = #{id}");
        return sql.toString();
    }

    /**
     * 根据多个主键查询
     *
     * @param ids
     * @return
     */
    public String deleteByPrimaryKeys(@Param("ids") List<?> ids, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        MetaEntityClass.ResultMapping primaryKey = meta.getPrimaryKeyResultMapping();
        String condition = IntStream.range(0, ids.size()).mapToObj(i -> "#{ids[" + i + "]}").collect(Collectors.joining(","));
        SQL sql = new SQL().DELETE_FROM(meta.getTable()).WHERE(primaryKey.getColumn() + " in (" + condition + ")");
        return sql.toString();
    }

    /**
     * 根据记录查询
     *
     * @param record
     * @return
     */
    public String deleteByEntity(Object record, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        SQL sql = new SQL().DELETE_FROM(meta.getTable());
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
     * @return
     */
    public String deleteByParameterMap(Map<String, Object> parameterMap, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        SQL sql = new SQL().DELETE_FROM(meta.getTable());
        List<MetaEntityClass.ResultMapping> resultMappings = meta.getResultMappings();
        for (MetaEntityClass.ResultMapping resultMapping : resultMappings) {
            if (Objects.isNull(parameterMap.getOrDefault(resultMapping.getProperty(), null))) {
                continue;
            }
            sql.WHERE(resultMapping.getColumn() + " ${parameterMap." + resultMapping.getProperty() + "}");
        }
        return sql.toString();
    }
}

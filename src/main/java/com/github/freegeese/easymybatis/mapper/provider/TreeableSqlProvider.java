package com.github.freegeese.easymybatis.mapper.provider;

import com.github.freegeese.easymybatis.domain.Treeable;
import com.github.freegeese.easymybatis.meta.MetaEntityClass;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 提供基础层级结构 SQL
 *
 * @author zhangguangyong
 * @since 1.0
 */
public class TreeableSqlProvider extends BaseSqlProvider {
    private static final String EMPTY_SQL = "";

    /**
     * 交换位置
     *
     * @param source
     * @param target
     * @return
     */
    public String exchange(@Param("source") Object source, @Param("target") Object target, ProviderContext context) {
        Treeable sourceNode = (Treeable) source;
        Treeable targetNode = (Treeable) target;

        if (sourceNode.getId() != targetNode.getId() && sourceNode.getParentId() == targetNode.getParentId()) {
            MetaEntityClass meta = getMetaEntityClass(context);
            Map<String, MetaEntityClass.ResultMapping> getMethodAndThisMap = meta.getGetMethodAndThisMap();
            MetaEntityClass.TreeableGetMethod treeableGetMethod = MetaEntityClass.getTreeableGetMethod();

            MetaEntityClass.ResultMapping idProp = getMethodAndThisMap.get(treeableGetMethod.getGetId());
            MetaEntityClass.ResultMapping sortProp = getMethodAndThisMap.get(treeableGetMethod.getGetSort());
            String table = meta.getTable();

            return Stream.of(
                    new SQL().UPDATE(table).SET(sortProp.eq("target")).WHERE(idProp.eq("source")).toString(),
                    new SQL().UPDATE(table).SET(sortProp.eq("source")).WHERE(idProp.eq("target")).toString()
            ).collect(Collectors.joining(";"));
        }
        return EMPTY_SQL;
    }

    /**
     * 从 from 移动到 to
     *
     * @param from
     * @param to
     * @return
     */
    public String move(@Param("from") Object from, @Param("to") Object to, ProviderContext context) {
        Treeable fromNode = (Treeable) from;
        Treeable toNode = (Treeable) to;
        if (fromNode.getId() != toNode.getId() && fromNode.getParentId() == toNode.getParentId()) {
            MetaEntityClass meta = getMetaEntityClass(context);
            Map<String, MetaEntityClass.ResultMapping> getMethodAndThisMap = meta.getGetMethodAndThisMap();
            MetaEntityClass.TreeableGetMethod treeableGetMethod = MetaEntityClass.getTreeableGetMethod();

            MetaEntityClass.ResultMapping idProp = getMethodAndThisMap.get(treeableGetMethod.getGetId());
            MetaEntityClass.ResultMapping parentIdProp = getMethodAndThisMap.get(treeableGetMethod.getGetParentId());
            MetaEntityClass.ResultMapping sortProp = getMethodAndThisMap.get(treeableGetMethod.getGetSort());
            String table = meta.getTable();

            if (fromNode.getSort() > toNode.getSort()) {
                return String.join(";",
                        new SQL().UPDATE(table).SET(sortProp.getColumn() + " = " + sortProp.getColumn() + " + 1")
                                .WHERE(sortProp.ge("to")).AND()
                                .WHERE(parentIdProp.ge("from")).AND()
                                .WHERE(sortProp.lt("from")).toString(),
                        new SQL().UPDATE(table).SET(sortProp.eq("to")).WHERE(idProp.eq("from")).toString()
                );
            }

            if (fromNode.getSort() < toNode.getSort()) {
                return String.join(";",
                        new SQL().UPDATE(table).SET(sortProp.getColumn() + " = " + sortProp.getColumn() + " - 1")
                                .WHERE(sortProp.gt("from")).AND()
                                .WHERE(parentIdProp.eq("from")).AND()
                                .WHERE(sortProp.le("to")).toString(),
                        new SQL().UPDATE(table).SET(sortProp.eq("to")).WHERE(idProp.eq("from")).toString()
                );
            }
        }

        return EMPTY_SQL;
    }

    /**
     * 查询上一个兄弟节点
     *
     * @param target
     * @return
     */
    public String selectPreviousSibling(Object target, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        Map<String, MetaEntityClass.ResultMapping> getMethodAndThisMap = meta.getGetMethodAndThisMap();
        MetaEntityClass.TreeableGetMethod treeableGetMethod = MetaEntityClass.getTreeableGetMethod();

        String table = meta.getTable();
        MetaEntityClass.ResultMapping parentIdProp = getMethodAndThisMap.get(treeableGetMethod.getGetParentId());
        MetaEntityClass.ResultMapping sortProp = getMethodAndThisMap.get(treeableGetMethod.getGetSort());

        return new SQL().SELECT(meta.getColumns()).FROM(table)
                .WHERE(parentIdProp.eq()).AND()
                .WHERE(sortProp.getColumn() + " = (select max (" + sortProp.getColumn() + ") from table where " + parentIdProp.getColumn() + " = #{" + parentIdProp.getProperty() + "}) and " + sortProp.getColumn() + " < #{" + sortProp.getProperty() + "}").toString();
    }

    /**
     * 查询下一个兄弟节点
     *
     * @param target
     * @return
     */
    public String selectNextSibling(Object target, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        Map<String, MetaEntityClass.ResultMapping> getMethodAndThisMap = meta.getGetMethodAndThisMap();
        MetaEntityClass.TreeableGetMethod treeableGetMethod = MetaEntityClass.getTreeableGetMethod();

        String table = meta.getTable();
        MetaEntityClass.ResultMapping parentIdProp = getMethodAndThisMap.get(treeableGetMethod.getGetParentId());
        MetaEntityClass.ResultMapping sortProp = getMethodAndThisMap.get(treeableGetMethod.getGetSort());

        return new SQL().SELECT(meta.getColumns()).FROM(table)
                .WHERE(parentIdProp.eq()).AND()
                .WHERE(sortProp.getColumn() + " = (select min (" + sortProp.getColumn() + ") from " + table + " where " + parentIdProp.getColumn() + " = #{" + parentIdProp.getProperty() + "}) and " + sortProp.getColumn() + " > #{" + sortProp.getProperty() + "}").toString();
    }

    /**
     * 查询第一个兄弟节点
     *
     * @param target
     * @return
     */
    public String selectFirstSibling(Object target, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        Map<String, MetaEntityClass.ResultMapping> getMethodAndThisMap = meta.getGetMethodAndThisMap();
        MetaEntityClass.TreeableGetMethod treeableGetMethod = MetaEntityClass.getTreeableGetMethod();

        String table = meta.getTable();
        MetaEntityClass.ResultMapping parentIdProp = getMethodAndThisMap.get(treeableGetMethod.getGetParentId());
        MetaEntityClass.ResultMapping sortProp = getMethodAndThisMap.get(treeableGetMethod.getGetSort());

        return new SQL().SELECT(meta.getColumns()).FROM(table)
                .WHERE(parentIdProp.eq()).AND()
                .WHERE(sortProp.getColumn() + " = (select min (" + sortProp.getColumn() + ") from " + table + " where " + parentIdProp.getColumn() + " = #{" + parentIdProp.getProperty() + "}").toString();
    }

    /**
     * 查询最后一个兄弟节点
     *
     * @param target
     * @return
     */
    public String selectLastSibling(Object target, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        Map<String, MetaEntityClass.ResultMapping> getMethodAndThisMap = meta.getGetMethodAndThisMap();
        MetaEntityClass.TreeableGetMethod treeableGetMethod = MetaEntityClass.getTreeableGetMethod();

        String table = meta.getTable();
        MetaEntityClass.ResultMapping parentIdProp = getMethodAndThisMap.get(treeableGetMethod.getGetParentId());
        MetaEntityClass.ResultMapping sortProp = getMethodAndThisMap.get(treeableGetMethod.getGetSort());

        return new SQL().SELECT(meta.getColumns()).FROM(table)
                .WHERE(parentIdProp.eq()).AND()
                .WHERE(sortProp.getColumn() + " = (select max (" + sortProp.getColumn() + ") from " + table + " where " + parentIdProp.getColumn() + " = #{" + parentIdProp.getProperty() + "}").toString();
    }

    /**
     * 查询所有父节点
     *
     * @return
     */
    public String selectParents(ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        Map<String, MetaEntityClass.ResultMapping> getMethodAndThisMap = meta.getGetMethodAndThisMap();
        MetaEntityClass.TreeableGetMethod treeableGetMethod = MetaEntityClass.getTreeableGetMethod();

        String table = meta.getTable();
        MetaEntityClass.ResultMapping parentIdProp = getMethodAndThisMap.get(treeableGetMethod.getGetParentId());
        MetaEntityClass.ResultMapping sortProp = getMethodAndThisMap.get(treeableGetMethod.getGetSort());
        MetaEntityClass.ResultMapping idProp = getMethodAndThisMap.get(treeableGetMethod.getGetId());

        return new SQL().SELECT(meta.getColumns()).FROM(table)
                .WHERE(idProp.getColumn() + " in (select distinct " + parentIdProp.getColumn() + " from " + table + ") order by " + String.join(",", parentIdProp.getColumn(), sortProp.getColumn())).toString();
    }

    /**
     * 查询子节点
     *
     * @param target
     * @param deep
     * @return
     */
    public String selectChildren(@Param("target") Objects target, @Param("deep") boolean deep, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        Map<String, MetaEntityClass.ResultMapping> getMethodAndThisMap = meta.getGetMethodAndThisMap();
        MetaEntityClass.TreeableGetMethod treeableGetMethod = MetaEntityClass.getTreeableGetMethod();

        String table = meta.getTable();
        MetaEntityClass.ResultMapping parentIdProp = getMethodAndThisMap.get(treeableGetMethod.getGetParentId().toString());
        MetaEntityClass.ResultMapping sortProp = getMethodAndThisMap.get(treeableGetMethod.getGetSort());
        MetaEntityClass.ResultMapping idProp = getMethodAndThisMap.get(treeableGetMethod.getGetId());
        MetaEntityClass.ResultMapping pathProp = getMethodAndThisMap.get(treeableGetMethod.getGetPath());

        SQL sql = new SQL().SELECT(meta.getColumns()).FROM(table);
        if (deep) {
            return sql.WHERE(pathProp.getColumn() + " like concat(#{target." + pathProp.getProperty() + "},'%')").AND().WHERE(idProp.eq("target")).ORDER_BY(parentIdProp.getColumn(), sortProp.getColumn()).toString();
        }
        return sql.WHERE(parentIdProp.getColumn() + " = #{target." + idProp.getProperty() + "}").ORDER_BY(parentIdProp.getColumn(), sortProp.getColumn()).toString();
    }

    /**
     * 更新子节点路径
     *
     * @param target
     * @param oldPath
     * @return
     */
    public String updateChildrenPath(@Param("target") Object target, @Param("oldPath") String oldPath, ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        Map<String, MetaEntityClass.ResultMapping> getMethodAndThisMap = meta.getGetMethodAndThisMap();
        MetaEntityClass.TreeableGetMethod treeableGetMethod = MetaEntityClass.getTreeableGetMethod();

        String table = meta.getTable();
        MetaEntityClass.ResultMapping parentIdProp = getMethodAndThisMap.get(treeableGetMethod.getGetParentId());
        MetaEntityClass.ResultMapping idProp = getMethodAndThisMap.get(treeableGetMethod.getGetId());
        MetaEntityClass.ResultMapping pathProp = getMethodAndThisMap.get(treeableGetMethod.getGetPath());

        return new SQL().UPDATE(table).
                SET(parentIdProp.getColumn() + " = concat(#{target." + pathProp.getProperty() + "},substr(" + pathProp.getColumn() + ",length(#{oldPath}) + 1, length(" + pathProp.getColumn() + ")))")
                .WHERE(parentIdProp.getColumn() + " = #{target." + idProp.getProperty() + "}").toString();
    }

    /**
     * 查询根节点
     *
     * @return
     */
    public String selectRoot(ProviderContext context) {
        MetaEntityClass meta = getMetaEntityClass(context);
        Map<String, MetaEntityClass.ResultMapping> getMethodAndThisMap = meta.getGetMethodAndThisMap();
        MetaEntityClass.TreeableGetMethod treeableGetMethod = MetaEntityClass.getTreeableGetMethod();

        String table = meta.getTable();
        MetaEntityClass.ResultMapping pathProp = getMethodAndThisMap.get(treeableGetMethod.getGetPath());

        return new SQL().SELECT(meta.getColumns()).FROM(table)
                .WHERE(pathProp.getColumn() + " = '/'").toString();
    }
}

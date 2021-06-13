package com.github.freegeese.easymybatis.mapper;

import com.github.freegeese.easymybatis.annotation.AutoResultMap;
import com.github.freegeese.easymybatis.domain.Treeable;
import com.github.freegeese.easymybatis.mapper.provider.TreeableSqlProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 通用基础层级结构 Mapper
 *
 * @param <T> 实体类型
 * @author zhangguangyong
 * @since 1.0
 */
public interface TreeableMapper<T extends Treeable<ID>, ID> extends BaseMapper<T> {
    /**
     * 交换位置
     * <pre>
     *  条件：
     *      属于同一级节点 并且 不是同一个节点
     *      source.extNodeParentId == target.extNodeParentId && source.id != target.id
     *  操作：
     *      update table set ext_node_sort = target.extNodeSort where id = source.id;
     *      update table set ext_node_sort = source.extNodeSort where id = target.id;
     * </pre>
     *
     * @param source
     * @param target
     * @return
     */
    @SelectProvider(type = TreeableSqlProvider.class, method = "exchange")
    int exchange(@Param("source") T source, @Param("target") T target);

    /**
     * 从 from 移动到 to
     * <pre>
     *  条件：
     *      属于同一级节点 并且 不是同一个节点
     *      from.extNodeParentId == to.extNodeParentId && from.id != to.id
     *  操作：
     *      if from.extNodeSort > to.extNodeSort
     *          from 节点 在 to 节点 下方的位置
     *          update
     *              table set ext_node_sort = ext_node_sort + 1
     *          where
     *              ext_node_parent_id = from.extNodeParentId
     *              and ext_node_sort >= to.extNodeSort
     *              and ext_node_sort < from.extNodeSort;
     *          update table set ext_node_sort = to.extNodeSort where id = from.id;
     *      else
     *          from 节点 在 to 节点 下方的位置
     *          update
     *          table set ext_node_sort = ext_node_sort -1
     *          where
     *              ext_node_parent_id = from.extNodeParentId
     *              and ext_node_sort > from.extNodeSort
     *              and ext_node_sort <= to.extNodeSort;
     *          update table set ext_node_sort = to.extNodeSort where id = from.id;
     * </pre>
     *
     * @param from
     * @param to
     * @return
     */
    @SelectProvider(type = TreeableSqlProvider.class, method = "move")
    int move(@Param("from") T from, @Param("to") T to);

    /**
     * 查询目标节点的上一个节点
     * <pre>
     *     条件：
     *          属于同一级节点
     *     操作：
     *          select
     *              * from table
     *          where
     *              sort = ( select max(ext_node_sort) from table where ext_node_parent_id = target.extNodeParentId and ext_node_sort < target.extNodeSort )
     *              and ext_node_parent_id = target.extNodeParentId
     * </pre>
     *
     * @param target
     * @return
     */
    @SelectProvider(type = TreeableSqlProvider.class, method = "selectPreviousSibling")
    @AutoResultMap
    T selectPreviousSibling(T target);

    /**
     * 查询目标节点的下一个节点
     * <pre>
     *     条件：
     *          属于同一级节点
     *     操作：
     *          select
     *              * from table
     *          where
     *              sort = ( select min(ext_node_sort) from table where ext_node_parent_id = target.extNodeParentId and ext_node_sort > target.extNodeSort )
     *              and ext_node_parent_id = target.extNodeParentId
     * </pre>
     *
     * @param target
     * @return
     */
    @SelectProvider(type = TreeableSqlProvider.class, method = "selectNextSibling")
    @AutoResultMap
    T selectNextSibling(T target);

    /**
     * 查询目标节点同一级节点的第一个节点
     * <pre>
     *     条件：
     *          属于同一级节点
     *     操作：
     *          select
     *              * from table
     *          where
     *              sort = ( select min(ext_node_sort) from table where ext_node_parent_id = target.extNodeParentId )
     *              and ext_node_parent_id = target.extNodeParentId
     * </pre>
     *
     * @param target
     * @return
     */
    @SelectProvider(type = TreeableSqlProvider.class, method = "selectFirstSibling")
    @AutoResultMap
    T selectFirstSibling(T target);

    /**
     * 查询目标节点同一级节点的最后一个节点
     * <pre>
     *     条件：
     *          属于同一级节点
     *     操作：
     *          select
     *              * from table
     *          where
     *              sort = ( select max(ext_node_sort) from table where ext_node_parent_id = target.extNodeParentId )
     *              and ext_node_parent_id = target.extNodeParentId
     * </pre>
     *
     * @param target
     * @return
     */
    @SelectProvider(type = TreeableSqlProvider.class, method = "selectLastSibling")
    @AutoResultMap
    T selectLastSibling(T target);

    /**
     * 查询所有父节点
     * <pre>
     *     条件：
     *          自身必须是父节点
     *     操作：
     *          select
     *              * from table
     *          where
     *              id in ( select distinct ext_node_parent_id from table )
     *              order by ext_node_parent_id, ext_node_sort
     * </pre>
     *
     * @return
     */
    @SelectProvider(type = TreeableSqlProvider.class, method = "selectParents")
    @AutoResultMap
    List<T> selectParents();

    /**
     * 查询目标节点的子节点
     * <pre>
     *     条件：
     *          目标节点是父节点
     *          deep -> true -> 获取所有子孙节点
     *          deep -> false -> 只获取下一级子节点
     *     操作：
     *          select
     *              * from table
     *          where
     *            deep -> true
     *              ext_node_path like '%target.extNodeParentPath%'
     *              and id != target.id
     *            deep -> false
     *              ext_node_parent_id = target.id
     * </pre>
     *
     * @param target
     * @param deep
     * @return
     */
    @SelectProvider(type = TreeableSqlProvider.class, method = "selectChildren")
    @AutoResultMap
    List<T> selectChildren(@Param("target") T target, @Param("deep") boolean deep);

    /**
     * 更新子节点的层级路径
     * <pre>
     *     条件：
     *          目标节点是一个父节点
     *     操作：
     *          update
     *              table
     *          set
     *              ext_node_path = concat( target.extNodePath, substr(ext_node_path, length(oldPath) + 1, length(ext_node_path)) )
     *          where ext_node_parent_id = target.id
     *
     * </pre>
     *
     * @param target
     * @param oldPath
     * @return
     */
    @SelectProvider(type = TreeableSqlProvider.class, method = "updateChildrenPath")
    int updateChildrenPath(@Param("target") T target, @Param("oldPath") String oldPath);

    /**
     * 查询根节点
     *
     * @return
     */
    @SelectProvider(type = TreeableSqlProvider.class, method = "selectRoot")
    @AutoResultMap
    List<T> selectRoot(Class<T> entityClass);
}

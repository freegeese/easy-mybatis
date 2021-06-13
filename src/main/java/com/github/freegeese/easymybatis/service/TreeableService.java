package com.github.freegeese.easymybatis.service;

import com.github.freegeese.easymybatis.domain.Treeable;
import com.github.freegeese.easymybatis.mapper.TreeableMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 层级结构基础Service
 *
 * <p>对于实现{@link Treeable}实体对应的Service，可继承此类用于操作层级关系的表
 *
 * @param <T>  实体类型
 * @param <M>  Mapper类型
 * @param <ID> 实体主键类型
 * @author zhangguangyong
 * @since 1.0
 */
public abstract class TreeableService<T extends Treeable<ID>, M extends TreeableMapper<T, ID>, ID> extends BaseService<T, M> {
    private static final String PATH_SEPARATOR = "/";

    @Override
    public int insert(T entity) {
        beforeInsert(entity);
        int i = super.insert(entity);
        if (i > 0) {
            afterInsert(entity);
        }
        return i;
    }

    @Override
    public int insertSelective(T entity) {
        beforeInsert(entity);
        int i = super.insertSelective(entity);
        if (i > 0) {
            afterInsert(entity);
        }
        return i;
    }

    @Override
    public int insertBatch(List<T> entities) {
        int i = 0;
        for (T entity : entities) {
            i += this.insert(entity);
        }
        return i;
    }

    public int updateByPrimaryKey(T entity) {
        beforeUpdate(entity);
        return super.updateByPrimaryKey(entity);
    }

    @Override
    public int updateByPrimaryKeySelective(T entity) {
        beforeUpdate(entity);
        return super.updateByPrimaryKeySelective(entity);
    }

    @Override
    public int updateBatchSelective(List<T> entities) {
        int i = 0;
        for (T entity : entities) {
            i += this.updateByPrimaryKeySelective(entity);
        }
        return i;
    }


    @Override
    public int deleteByPrimaryKey(Object id) {
        final T e = selectByPrimaryKey(id);
        final List<T> children = selectChildren(e, true);
        if (Objects.nonNull(children) && !children.isEmpty()) {
            super.deleteByPrimaryKeys(children.stream().map(T::getId).collect(Collectors.toList()));
        }
        return super.deleteByPrimaryKey(e.getId());
    }

    @Override
    public int deleteByPrimaryKeys(List<?> ids) {
        int i = 0;
        for (Object id : ids) {
            i += this.deleteByPrimaryKey(id);
        }
        return i;
    }

    /**
     * 查询根节点
     *
     * @return
     */
    public List<T> selectRoot() {
        return mapper.selectRoot(entityClass);
    }

    /**
     * 交换位置
     *
     * @param source
     * @param target
     * @return
     */
    public int exchange(T source, T target) {
        return mapper.exchange(source, target);
    }

    /**
     * 把节点移到顶部
     *
     * @param from
     * @return
     */
    public int moveToTop(T from) {
        return move(from, Position.TOP);
    }

    /**
     * 向上移动节点
     *
     * @param from
     * @return
     */
    public int moveToUp(T from) {
        return move(from, Position.UP);
    }

    /**
     * 向下移动节点
     *
     * @param from
     * @return
     */
    public int moveToDown(T from) {
        return move(from, Position.DOWN);
    }

    /**
     * 把节点移动到底部
     *
     * @param from
     * @return
     */
    public int moveToBottom(T from) {
        return move(from, Position.BOTTOM);
    }

    private int move(T from, Position position) {
        T to = null;
        switch (position) {
            case TOP:
                to = mapper.selectFirstSibling(from);
                break;
            case UP:
                to = mapper.selectPreviousSibling(from);
                break;
            case DOWN:
                to = mapper.selectNextSibling(from);
                break;
            case BOTTOM:
                to = mapper.selectLastSibling(from);
                break;
        }
        // 没有找到目标行（两种可能：1.当前行在顶部，向上移动 2.当前行在底部，向下移动）
        if (null == to) {
            return 0;
        }
        // 同一行记录
        if (to.getId().equals(from.getId())) {
            return 0;
        }
        return mapper.move(from, to);
    }

    private enum Position {
        TOP,    // 移动到顶部
        UP,     // 向上移动
        DOWN,   // 向下移动
        BOTTOM  // 移动到底部
    }

    /**
     * 把 from 节点 移到 to 节点内部
     *
     * @param from
     * @param to
     */
    public void append(T from, T to) {
        // 同一个节点
        if (from.getId().equals(to.getId())) {
            return;
        }
        // 上级不能追加到下级
        String pathSeparator = to.getPath().substring(0, 1);
        List<String> pathItems = Arrays.asList(to.getPath().split(pathSeparator));
        if (pathItems.contains(from.getId().toString())) {
            return;
        }
        // 修改 from 的父节点为 to
        from.setParentId(to.getId());
        // 将 from 放置到 to 的最后一个节点，找到to的最后一个子节点, 将from的排序值修改为to的最后一个节点的sort+1
        T lastChildOfTo = mapper.selectLastSibling(to);
        if (Objects.nonNull(lastChildOfTo)) {
            Integer sort = lastChildOfTo.getSort();
            sort = Objects.isNull(sort) ? 1 : sort + 1;
            from.setSort(sort);
        }
        // 修改 from 的路径为 to 的路径加上ID
        String oldPathOfFrom = from.getPath();
        from.setPath(to.getPath() + from.getId() + pathSeparator);
        from.setParentId(to.getId());
        // 更新 from 子节点的 path
        mapper.updateChildrenPath(from, oldPathOfFrom);
        // 更新 from
        mapper.updateByPrimaryKeySelective(from);
    }

    /**
     * 查询目标节点的子节点
     *
     * @param target
     * @param deep
     * @return
     */
    public List<T> selectChildren(T target, boolean deep) {
        return mapper.selectChildren(target, deep);
    }

    /**
     * 查询所有父节点
     *
     * @return
     */
    public List<T> selectParents() {
        return mapper.selectParents();
    }

    /**
     * 插入前操作
     * <pre>
     *     条件：
     *          插入的节点没有父节点
     *     操作：
     *          1. 设置节点层级路径为根路径：/
     *          2. 设置节点位置为：1
     * </pre>
     *
     * @param entity
     */
    private void beforeInsert(T entity) {
        if (Objects.isNull(entity.getParentId())) {
            entity.setSort(1);
            entity.setPath(PATH_SEPARATOR);
        }
    }

    /**
     * 插入之后
     * <pre>
     *     1. 调整节点的位置，新增的节点在最后面
     *     2. 设置节点层级路径
     * </pre>
     *
     * @param entity
     */
    private void afterInsert(T entity) {
        T parent = mapper.selectByPrimaryKey(entity.getParentId(), entityClass);
        if (Objects.nonNull(parent)) {
            // 获取新增节点的最后一个兄弟节点
            T lastSibling = mapper.selectLastSibling(entity);
            // 调整节点的位置，新增的节点在最后面
            entity.setSort(Objects.isNull(lastSibling) ? 1 : lastSibling.getSort() + 1);
            // 设置节点的层级路径
            entity.setPath(parent.getPath() + entity.getId() + parent.getPath().substring(0, 1));
            // 更新节点
            updateByPrimaryKeySelective(entity);
        }
    }

    /**
     * 节点更新前的操作
     * <pre>
     *     条件：
     *          更新前和更新后不在同一层级
     *     操作：
     *          1. 设置新的层级路径
     *          2. 设置新的位置
     *          3. 更新子孙节点的层级路径
     * </pre>
     *
     * @param entity
     */
    private void beforeUpdate(T entity) {
        if (Objects.isNull(entity.getParentId())) {
            return;
        }
        // 获取更新前的实体
        T oldEntity = selectByPrimaryKey(entity.getId());
        // 如果更新节点与原节点不在同一层级
        if (!Objects.equals(entity.getParentId(), oldEntity.getParentId())) {
            // 获取新的父节点
            T newParent = selectByPrimaryKey(entity.getParentId());
            // 设置新的层级路径
            entity.setPath(newParent.getPath() + entity.getId() + newParent.getPath().substring(0, 1));
            // 设置新的位置
            T lastSibling = mapper.selectLastSibling(entity);
            entity.setSort(Objects.isNull(lastSibling) ? 1 : lastSibling.getSort() + 1);
            // 更新自身子孙节点的层级路径
            mapper.updateChildrenPath(entity, oldEntity.getPath());
        }
    }

}

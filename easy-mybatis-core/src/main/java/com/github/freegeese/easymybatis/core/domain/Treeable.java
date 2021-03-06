package com.github.freegeese.easymybatis.core.domain;

import com.github.freegeese.easymybatis.core.mapper.TreeableMapper;

/**
 * 用于标识有层级关系的实体
 *
 * @author zhangguangyong
 * @see TreeableMapper
 * @since 1.0
 */
public interface Treeable<ID> {
    /**
     * 节点ID
     *
     * @return
     */
    ID getId();

    void setId(ID id);

    /**
     * 父节点ID
     *
     * @return
     */
    ID getParentId();

    void setParentId(ID parentId);

    /**
     * 节点所在位置路径(/1/-1-1/-1-1-1)
     *
     * @return
     */
    String getPath();

    void setPath(String path);

    /**
     * 节点排序
     *
     * @return
     */
    Integer getSort();

    void setSort(Integer sort);
}

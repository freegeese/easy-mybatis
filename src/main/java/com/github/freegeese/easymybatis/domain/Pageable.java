package com.github.freegeese.easymybatis.domain;

import java.util.List;

/**
 * 用于记录分页相关参数
 *
 * @author zhangguangyong
 * @see Pagination
 * @since 1.0
 */
public interface Pageable {
    /**
     * 第几页
     *
     * @return
     */
    Integer getPageNumber();

    /**
     * 每页多少条记录
     *
     * @return
     */
    Integer getPageSize();

    /**
     * 分页起始位置
     *
     * @return
     */
    Integer getOffset();

    /**
     * 总页数
     *
     * @return
     */
    Integer getTotalPages();

    /**
     * 总记录数
     *
     * @return
     */
    Long getTotalRecords();

    void setTotalRecords(Long totalRecords);

    /**
     * 当前页数据
     *
     * @return
     */
    <T> List<T> getContent();

    <T> void setContent(List<T> content);
}

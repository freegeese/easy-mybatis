package com.github.freegeese.easymybatis.domain;

import com.google.common.base.Preconditions;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 分页参数默认实现，创建后可用于调用分页接口
 *
 * @author zhangguangyong
 * @see com.github.freegeese.easymybatis.service.BaseService#selectPage(Pageable)
 * @see com.github.freegeese.easymybatis.service.BaseService#selectPageByEntity(Pageable, Object)
 * @see com.github.freegeese.easymybatis.service.BaseService#selectPageByParameterMap(Pageable, Map)
 * @since 1.0
 */
@Data
public class Pagination<T> implements Pageable<T> {
    // 页码
    private Integer pageNumber;

    // 页面大小
    private Integer pageSize;

    // 总页数
    private Integer totalPages;

    // 总记录数
    private Long totalRecords;

    // 页面数据
    private List<T> content = new ArrayList<>();

    public Pagination() {
    }

    public Pagination(Integer pageNumber, Integer pageSize) {
        Preconditions.checkState(pageNumber > 0, "页码必须大于0");
        Preconditions.checkState(pageSize > 0, "每页的记录数必须大于0");
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public static Pagination create(Integer pageNumber, Integer pageSize) {
        return new Pagination(pageNumber, pageSize);
    }

    @Override
    public Integer getOffset() {
        return (getPageNumber() - 1) * getPageSize();
    }

    @Override
    public Integer getTotalPages() {
        if (Objects.isNull(getTotalRecords())) {
            return null;
        }
        Integer totalRecords = getTotalRecords().intValue();
        Integer pageSize = getPageSize();
        return (totalRecords % pageSize == 0) ? (totalRecords / pageSize) : (totalRecords / pageSize) + 1;
    }


}
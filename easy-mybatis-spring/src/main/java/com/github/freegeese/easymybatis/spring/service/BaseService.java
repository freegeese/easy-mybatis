package com.github.freegeese.easymybatis.spring.service;

import com.github.freegeese.easymybatis.core.criterion.SqlWrapper;
import com.github.freegeese.easymybatis.core.domain.Pageable;
import com.github.freegeese.easymybatis.core.mapper.BaseMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 基础Service
 *
 * <p>通用基础服务，用于常用的增删改查方法
 * 一般实体Service集成此类快速使用
 *
 * @param <T> 实体类型
 * @param <M> Mapper类型
 * @author zhangguangyong
 * @since 1.0
 */
public abstract class BaseService<T, M extends BaseMapper<T>> {
    @Autowired
    protected M mapper;

    // 增 =================================================================================
    public int insert(T entity) {
        return mapper.insert(entity);
    }

    public int insertSelective(T entity) {
        return mapper.insertSelective(entity);
    }

    public int insertBatch(List<T> entities) {
        return mapper.insertBatch(entities);
    }

    public int insertBatchSelective(List<T> entities) {
        return mapper.insertBatchSelective(entities);
    }

    public int insertBatch(List<T> entities, int batchSize) {
        int size = entities.size();
        int batchTimes = size / batchSize;
        int remaining = size % batchSize;
        int count = 0;
        for (int i = 0; i < batchTimes; i++) {
            count += mapper.insertBatch(entities.subList(i * batchSize, (i + 1) * batchSize));
        }
        if (remaining > 0) {
            count += mapper.insertBatch(entities.subList(size - remaining, size));
        }
        return count;
    }

    // 删 =================================================================================
    public int deleteByPrimaryKey(Object id) {
        return mapper.deleteByPrimaryKey(id);
    }

    public int deleteByPrimaryKeys(List<?> ids) {
        return mapper.deleteByPrimaryKeys(ids);
    }

    public int deleteByEntity(T record) {
        return mapper.deleteByEntity(record);
    }

    public int deleteByParameterMap(Map<String, Object> parameterMap) {
        return mapper.deleteByParameterMap(parameterMap);
    }

    // 改 =================================================================================
    public int updateByPrimaryKey(T entity) {
        return mapper.updateByPrimaryKey(entity);
    }

    public int updateByPrimaryKeySelective(T entity) {
        return mapper.updateByPrimaryKeySelective(entity);
    }

    public int updateBatchSelective(List<T> entities) {
        return mapper.updateBatchSelective(entities);
    }

    public int updateBatchSelective(List<T> entities, int batchSize) {
        int size = entities.size();
        int batchTimes = size / batchSize;
        int remaining = size % batchSize;
        int count = 0;
        for (int i = 0; i < batchTimes; i++) {
            count += mapper.updateBatchSelective(entities.subList(i * batchSize, (i + 1) * batchSize));
        }
        if (remaining > 0) {
            count += mapper.updateBatchSelective(entities.subList(size - remaining, size));
        }
        return count;
    }

    // 查 =================================================================================
    public T selectByPrimaryKey(Object id) {
        return mapper.selectByPrimaryKey(id);
    }

    public List<T> selectByPrimaryKeys(List<?> ids) {
        return mapper.selectByPrimaryKeys(ids);
    }

    public List<T> selectByEntity(T entity) {
        return mapper.selectByEntity(entity);
    }

    public List<T> selectByParameterMap(Map<String, Object> parameterMap) {
        return mapper.selectByParameterMap(parameterMap);
    }

    public List<T> selectByWrapper(SqlWrapper wrapper) {
        return mapper.selectByWrapper(wrapper.unwrap());
    }

    public List<T> selectAll() {
        return mapper.selectAll();
    }

    public Long selectCount() {
        return mapper.selectCount();
    }

    // 分页 =================================================================================
    public Pageable selectPage(Pageable page) {
        return copyPageInfo(page, PageHelper.offsetPage(page.getOffset(), page.getPageSize()).doSelectPageInfo(() -> mapper.selectAll()));
    }

    public Pageable selectPageByEntity(Pageable page, T entity) {
        return copyPageInfo(page, PageHelper.offsetPage(page.getOffset(), page.getPageSize()).doSelectPageInfo(() -> mapper.selectByEntity(entity)));
    }

    public Pageable selectPageByParameterMap(Pageable page, Map<String, Object> parameterMap) {
        return copyPageInfo(page, PageHelper.offsetPage(page.getOffset(), page.getPageSize()).doSelectPageInfo(() -> mapper.selectByParameterMap(parameterMap)));
    }

    public Pageable selectPageByWrapper(Pageable page, SqlWrapper wrapper) {
        return copyPageInfo(page, PageHelper.offsetPage(page.getOffset(), page.getPageSize()).doSelectPageInfo(() -> mapper.selectByWrapper(wrapper.unwrap())));
    }

    protected Pageable copyPageInfo(Pageable page, PageInfo<T> pageInfo) {
        page.setTotalRecords(pageInfo.getTotal());
        page.setContent(pageInfo.getList());
        return page;
    }
}

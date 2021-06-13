package com.github.freegeese.easymybatis.service;

import com.github.freegeese.easymybatis.domain.Pageable;
import com.github.freegeese.easymybatis.mapper.BaseMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import java.lang.reflect.ParameterizedType;
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
public class BaseService<T, M extends BaseMapper<T>> {
    protected final M mapper;

    protected final Class<T> entityClass;

    public BaseService(M mapper) {
        this.mapper = mapper;
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

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
        return mapper.deleteByPrimaryKey(id, entityClass);
    }

    public int deleteByPrimaryKeys(List<?> ids) {
        return mapper.deleteByPrimaryKeys(ids, entityClass);
    }

    public int deleteByEntity(T record) {
        return mapper.deleteByEntity(record);
    }

    public int deleteByParameterMap(Map<String, Object> parameterMap) {
        return mapper.deleteByParameterMap(parameterMap, entityClass);
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
        return mapper.selectByPrimaryKey(id, entityClass);
    }

    public List<T> selectByPrimaryKeys(List<?> ids) {
        return mapper.selectByPrimaryKeys(ids, entityClass);
    }

    public List<T> selectByEntity(T entity) {
        return mapper.selectByEntity(entity);
    }

    public List<T> selectByParameterMap(Map<String, Object> parameterMap) {
        return mapper.selectByParameterMap(parameterMap, entityClass);
    }

    public List<T> selectAll() {
        return mapper.selectAll(entityClass);
    }

    // 分页 =================================================================================
    public Pageable<T> selectPage(Pageable<T> page) {
        return copyPageInfo(page, PageHelper.offsetPage(page.getOffset(), page.getPageSize()).doSelectPageInfo(() -> mapper.selectAll(entityClass)));
    }

    public Pageable<T> selectPageByEntity(Pageable<T> page, T entity) {
        return copyPageInfo(page, PageHelper.offsetPage(page.getOffset(), page.getPageSize()).doSelectPageInfo(() -> mapper.selectByEntity(entity)));
    }

    public Pageable<T> selectPageByParameterMap(Pageable<T> page, Map<String, Object> parameterMap) {
        return copyPageInfo(page, PageHelper.offsetPage(page.getOffset(), page.getPageSize()).doSelectPageInfo(() -> mapper.selectByParameterMap(parameterMap, entityClass)));
    }

    private Pageable<T> copyPageInfo(Pageable<T> page, PageInfo<T> pageInfo) {
        page.setTotalRecords(pageInfo.getTotal());
        page.setContent(pageInfo.getList());
        return page;
    }
}

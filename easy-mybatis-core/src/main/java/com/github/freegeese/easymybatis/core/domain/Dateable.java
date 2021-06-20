package com.github.freegeese.easymybatis.core.domain;

import com.github.freegeese.easymybatis.core.interceptor.DateableInterceptor;

import java.util.Date;

/**
 * 用于标识有记录创建时间和修改的时间的实体
 *
 * @author zhangguangyong
 * @see DateableInterceptor
 * @since 1.0
 */
public interface Dateable {
    /**
     * 获取创建时间
     *
     * @return
     */
    Date getCreatedDate();

    /**
     * 设置创建时间
     *
     * @param createdDate
     */
    void setCreatedDate(Date createdDate);

    /**
     * 获取最近一次修改时间
     *
     * @return
     */
    Date getLastModifiedDate();

    /**
     * 设置最后一次修改时间
     *
     * @param lastModifiedDate
     */
    void setLastModifiedDate(Date lastModifiedDate);
}

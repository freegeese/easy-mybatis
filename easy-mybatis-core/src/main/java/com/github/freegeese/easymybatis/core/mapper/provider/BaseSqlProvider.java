package com.github.freegeese.easymybatis.core.mapper.provider;

import com.github.freegeese.easymybatis.core.meta.MetaCache;
import com.github.freegeese.easymybatis.core.meta.MetaEntityClass;
import com.github.freegeese.easymybatis.core.meta.MetaMapperClass;
import org.apache.ibatis.builder.annotation.ProviderContext;

/**
 * 基础SQL提供者
 */
public class BaseSqlProvider {
    /**
     * Entity 元信息
     *
     * @param context
     * @return
     */
    public MetaEntityClass getMetaEntityClass(ProviderContext context) {
        return MetaCache.getMetaEntityClass(getMetaMapperClass(context).getEntityClass());
    }

    /**
     * Mapper 元信息
     *
     * @param context
     * @return
     */
    public MetaMapperClass getMetaMapperClass(ProviderContext context) {
        return MetaCache.getMetaMapperClass(context.getMapperType());
    }
}

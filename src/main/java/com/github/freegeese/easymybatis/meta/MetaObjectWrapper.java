package com.github.freegeese.easymybatis.meta;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/**
 * 元数据对象包装器，可方便对元数据进行操作
 *
 * <p>可快速修改 {@link org.apache.ibatis.mapping.MappedStatement} 中的属性值
 *
 * @author zhangguangyong
 * @since 1.0
 */
public class MetaObjectWrapper {
    private MetaObject metaObject;

    private MetaObjectWrapper(MetaObject metaObject) {
        this.metaObject = metaObject;
    }

    public static MetaObjectWrapper forObject(Object target) {
        return new MetaObjectWrapper(MetaObject.forObject(target, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory()));
    }

    public MetaObject getMetaObject() {
        return metaObject;
    }

    public <T> T getValue(String name) {
        return (T) metaObject.getValue(name);
    }

    public MetaObjectWrapper setValue(String name, Object value) {
        metaObject.setValue(name, value);
        return this;
    }
}

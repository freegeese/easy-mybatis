package com.github.freegeese.easymybatis.generator.mybatis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.freegeese.easymybatis.core.annotation.GeneratedValue;
import com.github.freegeese.easymybatis.core.annotation.GenerationType;
import com.github.freegeese.easymybatis.core.annotation.Id;
import com.github.freegeese.easymybatis.core.domain.Dateable;
import com.github.freegeese.easymybatis.core.domain.Treeable;
import com.github.freegeese.easymybatis.core.mapper.BaseMapper;
import com.github.freegeese.easymybatis.core.mapper.TreeableMapper;
import com.github.freegeese.easymybatis.spring.service.BaseService;
import com.github.freegeese.easymybatis.spring.service.TreeableService;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 数据模型（用于配合模板生成代码）
 *
 * @author zhangguangyong
 * @since 1.0
 */
@Data
public class DataModel {
    private String tableName;
    private String remarks;
    private List<Column> columns;
    private Column primaryKey;

    // 输出文件是否已存在
    private boolean outputFileExists;

    // 实体相关的
    private String modelName;
    private String modelPackage;

    private String repositoryName;
    private String mapperPackage;

    private String serviceName;
    private String servicePackage;

    private String repositorySqlName;
    private String customRepositorySqlName;

    private List<String> extTypes;

    // 其他属性
    private Map<String, Object> properties;
    private MybatisConfiguration.ExtProperty extProperty;
    private String keepMarkStart;
    private String keepMarkEnd;


    @Data
    public static class Column {
        private String name;
        private String remarks;
        private String property;
        private String jdbcType;
        private Class javaType;
        private Boolean autoincrement;

        public String getProperty() {
            if (Objects.nonNull(property)) {
                return property;
            }
            property = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, getName().toLowerCase());
            return property;
        }
    }

    public String getPropertyType(String property) {
        return getColumns().stream().filter(v -> v.getProperty().equals(property)).findFirst().get().getJavaType().getSimpleName();
    }

    /**
     * 列与属性映射
     *
     * @return
     */
    public Map<String, String> getPropertyAndColumnMap() {
        return getColumns().stream().collect(Collectors.toMap(Column::getProperty, Column::getName));
    }

    /**
     * 获取需要重写的扩展字段属性
     *
     * @return
     */
    public Map<String, String> getOverriddenExtPropertyMap() {
        final MybatisConfiguration.ExtProperty extProperty = getExtProperty();
        Map<String, String> overriddenExtPropertyMap = Maps.newHashMap();
        Set<String> propertyNames = getPropertyAndColumnMap().keySet();
        List<Class> modelImplementTypes = getModelImplementTypes();

        for (Class type : modelImplementTypes) {
            Object ext = (type == Dateable.class) ? extProperty.getDateable() : (type == Treeable.class) ? extProperty.getTreeable() : null;
            if (Objects.isNull(ext)) {
                continue;
            }
            final JSONObject map = (JSONObject) JSON.toJSON(ext);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (propertyNames.contains(entry.getKey())) {
                    continue;
                }
                overriddenExtPropertyMap.put(entry.getKey(), (String) entry.getValue());
            }
        }

        return overriddenExtPropertyMap;
    }

    /**
     * 除主键列以外的其他列
     *
     * @return
     */
    public List<Column> getColumnsWithoutPrimaryKey() {
        if (Objects.isNull(getPrimaryKey())) {
            return getColumns();
        }
        return getColumns().stream().filter(column -> !Objects.equals(column.getName(), getPrimaryKey().getName())).collect(Collectors.toList());
    }

    /**
     * 获取所有列的名称
     *
     * @return
     */
    public String getColumnNames() {
        return getColumns().stream().map(Column::getName).collect(Collectors.joining(","));
    }

    /**
     * 获取除了主键外的所有列
     *
     * @return
     */
    public String getColumnNamesWithoutPrimaryKey() {
        return getColumnsWithoutPrimaryKey().stream().map(Column::getName).collect(Collectors.joining(","));
    }

    /**
     * Model 现实的接口
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public List<Class> getModelImplementTypes() {
        if (Objects.isNull(this.extTypes) || this.extTypes.isEmpty()) {
            return null;
        }
        return Stream.of(Dateable.class, Treeable.class).filter(v -> this.extTypes.contains(v.getSimpleName())).collect(Collectors.toList());
    }

    public List<String> getModelImportTypes() {
        Set<String> importTypes = new TreeSet<>();
        // 非java.lang包下面的类
        for (Column column : getColumns()) {
            if (!column.getJavaType().getPackage().getName().startsWith("java.lang")) {
                importTypes.add(column.getJavaType().getName());
            }
        }
        // 继承的接口
        importTypes.addAll(getModelImplementTypes().stream().map(Class::getName).collect(Collectors.toList()));

        if (Objects.nonNull(primaryKey)) {
            importTypes.add(Id.class.getName());
            if (Objects.equals(primaryKey.getAutoincrement(), true)) {
                importTypes.add(GeneratedValue.class.getName());
                importTypes.add(GenerationType.class.getName());
            }
        }

        return new ArrayList<>(importTypes);
    }

    public String getModelImplements() {
        return getModelImplementTypes().stream().map(Class::getSimpleName).collect(Collectors.joining(","));
    }

    public String getMapperName() {
        if (Objects.nonNull(this.repositoryName)) {
            return this.repositoryName;
        }
        return getModelName() + "Repository";
    }

    public String getMapperPackage() {
        if (Objects.nonNull(this.mapperPackage)) {
            return this.mapperPackage;
        }
        ArrayList<String> items = new ArrayList<>(Splitter.on(".").splitToList(getModelPackage()));
        items.remove(items.size() - 1);
        items.add(items.size(), "mapper");
        return Joiner.on(".").join(items);
    }

    public List<String> getMapperImportTypes() {
        Set<String> importTypes = new TreeSet<>();
        importTypes.add(getMapperExtendType().getName());
        importTypes.add(String.join(".", getModelPackage(), getModelName()));
        return new ArrayList<>(importTypes);
    }

    @SuppressWarnings("rawtypes")
    public Class getMapperExtendType() {
        for (Class type : getModelImplementTypes()) {
            if (Treeable.class == type) {
                return TreeableMapper.class;
            }
        }
        return BaseMapper.class;
    }

    public String getMapperExtends() {
        return getMapperExtendType().getSimpleName() + "<" + getModelName() + ">";
    }

    public String getServiceName() {
        if (Objects.nonNull(this.serviceName)) {
            return this.serviceName;
        }
        return getModelName() + "Service";
    }

    public String getServicePackage() {
        if (Objects.nonNull(this.servicePackage)) {
            return this.servicePackage;
        }
        ArrayList<String> items = new ArrayList<>(Splitter.on(".").splitToList(getModelPackage()));
        items.remove(items.size() - 1);
        items.add(items.size(), "service");
        return Joiner.on(".").join(items);
    }

    public List<String> getServiceImportTypes() {
        Set<String> importTypes = new TreeSet<>();
        importTypes.add(getServiceExtendType().getName());
        importTypes.add(String.join(".", getMapperPackage(), getMapperName()));
        importTypes.add(String.join(".", getModelPackage(), getModelName()));
        return new ArrayList<>(importTypes);
    }

    @SuppressWarnings("rawtypes")
    public Class getServiceExtendType() {
        for (Class type : getModelImplementTypes()) {
            if (Treeable.class == type) {
                return TreeableService.class;
            }
        }
        return BaseService.class;
    }

    public String getServiceExtends() {
        return getServiceExtendType().getSimpleName() + "<" + getModelName() + "," + getMapperName() + ">";
    }
}

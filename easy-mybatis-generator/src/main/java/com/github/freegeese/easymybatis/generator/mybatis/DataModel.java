package com.github.freegeese.easymybatis.generator.mybatis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.nuochen.framework.autocoding.domain.Auditable;
import com.nuochen.framework.autocoding.domain.Dateable;
import com.nuochen.framework.autocoding.domain.Entity;
import com.nuochen.framework.autocoding.domain.Treeable;
import com.nuochen.framework.autocoding.domain.mybatis.BaseRepository;
import com.nuochen.framework.autocoding.domain.mybatis.BaseService;
import com.nuochen.framework.autocoding.domain.mybatis.TreeableRepository;
import com.nuochen.framework.autocoding.domain.mybatis.TreeableService;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据模型（用于配合模板生成代码）
 *
 * @author Guangyong Zhang
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
    private String repositoryPackage;

    private String serviceName;
    private String servicePackage;

    private String repositorySqlName;
    private String customRepositorySqlName;

    private List<String> extTypes;

    // 其他属性
    private Map<String, Object> properties;
    private Configuration.ExtProperty extProperty;


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
    public Map<String, String> getPropertyWithColumnMap() {
        return getColumns().stream().collect(Collectors.toMap(Column::getProperty, Column::getName));
    }

    /**
     * 获取需要重写的扩展字段属性
     *
     * @return
     */
    public Map<String, String> getOverriddenExtPropertyMap() {
        final Configuration.ExtProperty extProperty = getExtProperty();
        Map<String, String> overriddenExtPropertyMap = Maps.newHashMap();
        Set<String> propertyNames = getPropertyWithColumnMap().keySet();
        List<Class> modelImplementTypes = getModelImplementTypes();

        for (Class type : modelImplementTypes) {
            Object ext = (type == Auditable.class) ? extProperty.getAuditable() : (type == Dateable.class) ? extProperty.getDateable() : (type == Treeable.class) ? extProperty.getTreeable() : null;
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
        return String.join(",", getColumns().stream().map(Column::getName).collect(Collectors.toList()));
    }

    /**
     * 获取除了主键外的所有列
     *
     * @return
     */
    public String getColumnNamesWithoutPrimaryKey() {
        return String.join(",", getColumnsWithoutPrimaryKey().stream().map(Column::getName).collect(Collectors.toList()));
    }

    /**
     * Model 现实的接口
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public List<Class> getModelImplementTypes() {
        if (Objects.isNull(this.extTypes) || this.extTypes.isEmpty()) {
            return Collections.singletonList(Entity.class);
        }

        List<Class<?>> types = Arrays.asList(Auditable.class, Dateable.class, Treeable.class);
        List<Class> modelTypes = types.stream().filter(v -> this.extTypes.contains(v.getSimpleName())).collect(Collectors.toList());
        if (modelTypes.stream().filter(Entity.class::isAssignableFrom).count() == 0) {
            modelTypes.add(0, Entity.class);
        }
        return modelTypes;
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
        return new ArrayList<>(importTypes);
    }

    public String getModelImplements() {
        final String primaryKeyJavaTypeSimpleName = getPrimaryKey().getJavaType().getSimpleName();
        return Joiner.on(",").join(
                getModelImplementTypes().stream().map(type -> {
                    if (Entity.class.isAssignableFrom(type)) {
                        return type.getSimpleName() + "<" + primaryKeyJavaTypeSimpleName + ">";
                    }
                    return type.getSimpleName();
                }).collect(Collectors.toList())
        );
    }

    public String getRepositoryName() {
        if (Objects.nonNull(this.repositoryName)) {
            return this.repositoryName;
        }
        return getModelName() + "Repository";
    }

    public String getRepositoryPackage() {
        if (Objects.nonNull(this.repositoryPackage)) {
            return this.repositoryPackage;
        }
        ArrayList<String> items = new ArrayList<>(Splitter.on(".").splitToList(getModelPackage()));
        items.remove(items.size() - 1);
        items.add(items.size(), "repository");
        return Joiner.on(".").join(items);
    }

    public List<String> getRepositoryImportTypes() {
        Set<String> importTypes = new TreeSet<>();
        importTypes.add(getRepositoryExtendType().getName());
        importTypes.add(String.join(".", getModelPackage(), getModelName()));
        return new ArrayList<>(importTypes);
    }

    @SuppressWarnings("rawtypes")
    public Class getRepositoryExtendType() {
        for (Class type : getModelImplementTypes()) {
            if (Entity.class == type) {
                continue;
            }
            if (Treeable.class == type) {
                return TreeableRepository.class;
            }
        }
        return BaseRepository.class;
    }

    public String getRepositoryExtends() {
        return getRepositoryExtendType().getSimpleName() + "<" + getModelName() + "," + getPrimaryKey().getJavaType().getSimpleName() + ">";
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
        importTypes.add(String.join(".", getRepositoryPackage(), getRepositoryName()));
        importTypes.add(String.join(".", getModelPackage(), getModelName()));
        return new ArrayList<>(importTypes);
    }

    @SuppressWarnings("rawtypes")
    public Class getServiceExtendType() {
        for (Class type : getModelImplementTypes()) {
            if (Entity.class == type) {
                continue;
            }
            if (Treeable.class == type) {
                return TreeableService.class;
            }
        }
        return BaseService.class;
    }

    public String getServiceExtends() {
        return getServiceExtendType().getSimpleName() + "<" + getRepositoryName() + "," + getModelName() + "," + getPrimaryKey().getJavaType().getSimpleName() + ">";
    }
}

package com.github.freegeese.easymybatis.generator.mybatis;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.util.*;

/**
 * 配置信息
 *
 * @author zhangguangyong
 * @since 1.0
 */
@Data
public class MybatisConfiguration {
    @Data
    public static class ExtProperty {
        private Dateable dateable = new Dateable();
        private Treeable treeable = new Treeable();

        @Data
        public static class Dateable {
            private String createdDate = "createdDate";
            private String lastModifiedDate = "lastModifiedDate";
        }

        @Data
        public static class Treeable {
            private String parentId = "parentId";
            private String path = "path";
            private String sort = "sort";
        }
    }

    /**
     * 属性配置
     */
    private Map<String, Object> properties;

    /**
     * 扩展属性
     */
    private ExtProperty extProperty = new ExtProperty();

    /**
     * JDBC 连接配置
     */
    private Jdbc jdbc;

    /**
     * 指定数据库（Mysql）
     */
    private String catalog;

    /**
     * 指定数据库（Oracle）
     */
    private String schemaPattern;

    /**
     * 实体生成配置
     */
    private GenerateJava model;

    /**
     * 数据库操作接口代码生成配置
     */
    private GenerateJava mapper;

    /**
     * 基础服务层代码生成配置
     */
    private GenerateJava service;

    /**
     * 数据库SQL代码生成配置
     */
    private GenerateXml mapperXml;

    /**
     * 保留标记开始
     */
    private String keepMarkStart;

    /**
     * 保留标记结束
     */
    private String keepMarkEnd;

    /**
     * 数据库表匹配
     */
    private String tableNamePattern;

    /**
     * 数据库表配置
     */
    private List<Table> tables;

    @Data
    public static class Jdbc {
        private String url;
        private String driver;
        private String user;
        private String password;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class GenerateJava extends GenerateFile {
        private String packageName;

        @Override
        public String getExtension() {
            return ".java";
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class GenerateXml extends GenerateFile {
        @Override
        public String getExtension() {
            return ".xml";
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class GenerateFile extends Template {
        private boolean override;
        private String prefix;
        private String suffix;
        private String extension;

        public String getPrefix() {
            return Objects.isNull(prefix) ? "" : prefix;
        }

        public String getSuffix() {
            return Objects.isNull(suffix) ? "" : suffix;
        }
    }

    @Data
    public static class Template {
        private File outputDirectory;
        private String name;
    }

    @Data
    public static class Table {
        private String tableName;
        private String modelName;
        private String mapperName;
        private String serviceName;
        private String mapperXmlName;
        private List<Column> columns;
        private List<String> extTypes;


        public Map<String, Column> getColumnMap() {
            if (Objects.isNull(columns)) {
                return Collections.emptyMap();
            }
            final Map<String, Column> map = new HashMap<>();
            for (Column column : columns) {
                map.put(column.getName(), column);
            }
            return map;
        }
    }

    @Data
    public static class Column {
        private String name;
        private String property;
    }

    @JSONField(serialize = false, deserialize = false)
    public Map<String, Table> getTableMap() {
        if (Objects.isNull(tables)) {
            return Collections.emptyMap();
        }
        final Map<String, Table> map = new HashMap<>();
        for (Table table : tables) {
            map.put(table.getTableName(), table);
        }
        return map;

    }

    MybatisConfiguration postConstruct() {
        if (Objects.nonNull(model)) {
            model.setName("model.ftl");
        }
        if (Objects.nonNull(mapper)) {
            mapper.setName("mapper.ftl");
        }
        if (Objects.nonNull(mapperXml)) {
            mapperXml.setName("mapperXml.ftl");
        }
        if (Objects.nonNull(service)) {
            service.setName("service.ftl");
        }
        if (Objects.isNull(extProperty)) {
            setExtProperty(new ExtProperty());
        }
        if (Objects.isNull(keepMarkStart)) {
            setKeepMarkStart("KEEP_MARK_START");
        }
        if (Objects.isNull(keepMarkEnd)) {
            setKeepMarkEnd("KEEP_MARK_END");
        }
        return this;
    }
}

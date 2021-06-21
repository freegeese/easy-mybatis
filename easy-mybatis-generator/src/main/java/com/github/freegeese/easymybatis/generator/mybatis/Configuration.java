package com.github.freegeese.easymybatis.generator.mybatis;

import com.alibaba.fastjson.annotation.JSONField;
import freemarker.template.TemplateModelException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static freemarker.template.Configuration.VERSION_2_3_28;

/**
 * 配置信息
 *
 * @author Guangyong Zhang
 * @since 1.0
 */
@Data
public class Configuration {
    @Data
    public static class ExtProperty {
        private Auditable auditable = new Auditable();
        private Dateable dateable = new Dateable();
        private Treeable treeable = new Treeable();

        @Data
        public static class Auditable {
            private String extCreatedBy = "extCreatedBy";
            private String extLastModifiedBy = "extLastModifiedBy";
        }

        @Data
        public static class Dateable {
            private String extCreatedDate = "extCreatedDate";
            private String extLastModifiedDate = "extLastModifiedDate";
        }

        @Data
        public static class Treeable {
            private String extNodeParentId = "extNodeParentId";
            private String extNodePath = "extNodePath";
            private String extNodeSort = "extNodeSort";
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
     * 模板目录
     */
    private File templateDirectory;

    /**
     * 模板配置
     */
    private freemarker.template.Configuration templateConfiguration;

    /**
     * 实体生成配置
     */
    private GenerateJava model;

    /**
     * 数据库操作接口代码生成配置
     */
    private GenerateJava repository;

    /**
     * 基础服务层代码生成配置
     */
    private GenerateJava service;

    /**
     * 数据库SQL代码生成配置
     */
    private GenerateXml repositorySql;

    /**
     * 自定义数据库SQL代码生成配置
     */
    private GenerateXml repositoryCustomSql;

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

    /**
     * 设置模板目录
     *
     * @param templateDirectory
     */
    public void setTemplateDirectory(File templateDirectory) {
        if (Objects.isNull(templateDirectory)) {
            return;
        }
        try {
            this.templateDirectory = templateDirectory;
            freemarker.template.Configuration templateConfiguration = new freemarker.template.Configuration(VERSION_2_3_28);
            templateConfiguration.setDirectoryForTemplateLoading(templateDirectory);
            setTemplateConfiguration(templateConfiguration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置模板配置信息
     *
     * @param templateConfiguration
     */
    public void setTemplateConfiguration(freemarker.template.Configuration templateConfiguration) {
        if (Objects.isNull(templateConfiguration)) {
            return;
        }
        this.templateConfiguration = templateConfiguration;
        try {
            this.templateConfiguration.setSharedVariable("configuration", this);
        } catch (TemplateModelException e) {
            throw new RuntimeException(e);
        }
    }

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
        private String template;
    }

    @Data
    public static class Table {
        private String tableName;
        private String modelName;
        private String repositoryName;
        private String serviceName;
        private String repositorySqlName;
        private String customRepositorySqlName;
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

}

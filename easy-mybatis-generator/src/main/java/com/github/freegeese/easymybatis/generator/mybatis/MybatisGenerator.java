package com.github.freegeese.easymybatis.generator.mybatis;

import com.alibaba.fastjson.JSON;
import com.github.freegeese.easymybatis.generator.CodeGenerator;
import com.github.freegeese.easymybatis.generator.database.DatabaseMetaDataHelper;
import com.github.freegeese.easymybatis.generator.database.JdbcConnectionBuilder;
import com.github.freegeese.easymybatis.generator.database.metadata.Column;
import com.github.freegeese.easymybatis.generator.database.metadata.PrimaryKey;
import com.github.freegeese.easymybatis.generator.database.metadata.Table;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mybatis 代码生成器
 *
 * @author zhangguangyong
 * @since 1.0
 */
@Slf4j
public class MybatisGenerator {
    private Configuration configuration;

    public MybatisGenerator(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 创建代码生成器，通过传递配置文件
     *
     * @param path
     * @return
     */
    public static MybatisGenerator create(Path path) {
        try {
            return create(new String(Files.readAllBytes(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据 JSON 配置信息，创建代码生成器，这里需要替换变量
     *
     * @param json
     * @return
     */
    private static MybatisGenerator create(String json) {
        final Map<String, Object> properties = JSON.parseObject(json, Configuration.class).getProperties();
        final Properties systemProperties = System.getProperties();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String value = (String) entry.getValue();
            for (Map.Entry<Object, Object> systemEntry : systemProperties.entrySet()) {
                value = value.replace("${" + systemEntry.getKey() + "}", systemEntry.getValue().toString());
            }
            properties.put(entry.getKey(), value);
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            json = json.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        json = json.replace("\\", "/");
        final Configuration configuration = JSON.parseObject(json, Configuration.class);
        configuration.setTemplateDirectory(new File(properties.get("templateDirectory").toString()));
        return create(configuration);
    }

    /**
     * 根据配置对象创建代码生成器
     *
     * @param configuration
     * @return
     */
    public static MybatisGenerator create(Configuration configuration) {
        if (Objects.isNull(configuration.getExtProperty())) {
            configuration.setExtProperty(new Configuration.ExtProperty());
        }
        return new MybatisGenerator(configuration);
    }

    /**
     * 执行代码生成
     */
    public void generate() {
        try {
            // JDBC
            final Configuration.Jdbc jdbc = configuration.getJdbc();
            final Connection connection = JdbcConnectionBuilder.create(jdbc.getUrl(), jdbc.getDriver())
                    .user(jdbc.getUser())
                    .password(jdbc.getPassword())
                    .builder();

            // 数据库元数据
            final DatabaseMetaData databaseMetaData = connection.getMetaData();

            // 数据库所有表元数据
            final List<Table> dbTables = DatabaseMetaDataHelper.tablesExtractor()
                    .catalog(configuration.getCatalog())
                    .schemaPattern(configuration.getSchemaPattern())
                    .extract(databaseMetaData);
            log.info("Table元数据 -> {}", JSON.toJSONString(dbTables, true));

            // 过滤(匹配配置信息里面的Table)
            final String tableNamePattern = configuration.getTableNamePattern();
            final Map<String, Configuration.Table> configTableMap = configuration.getTableMap();
            final List<Table> filteredDbTables = dbTables.stream().filter(table -> {
                final String tableName = table.getTableName();
                if (!configTableMap.isEmpty()) {
                    return configTableMap.containsKey(tableName);
                }
                if (Objects.nonNull(tableNamePattern)) {
                    return tableName.matches(tableNamePattern);
                }
                return true;
            }).collect(Collectors.toList());
            log.info("Table元数据(过滤后) -> {}", JSON.toJSONString(filteredDbTables, true));

            // 转换成Mybatis生成代码所需要的数据结构
            final List<DataModel> dataModels = filteredDbTables.stream().map(dbTable -> {
                Configuration.Table configTable = configTableMap.get(dbTable.getTableName());
                if (Objects.isNull(configTable)) {
                    configTable = new Configuration.Table();
                    configTable.setTableName(dbTable.getTableName());
                }

                if (Objects.isNull(configTable.getModelName())) {
                    final String tableName = configTable.getTableName();
                    if (tableName.contains("_")) {
                        configTable.setModelName(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.toUpperCase()));
                    } else {
                        configTable.setModelName(tableName.substring(0, 1).toUpperCase() + tableName.substring(1));
                    }
                }

                final DataModel dataModel = new DataModel();
                dataModel.setTableName(dbTable.getTableName());
                dataModel.setRemarks(dbTable.getRemarks());
                dataModel.setExtProperty(configuration.getExtProperty());
                dataModel.setExtTypes(configTable.getExtTypes());

                // Model
                final Configuration.GenerateJava model = configuration.getModel();
                if (Objects.nonNull(model)) {
                    dataModel.setModelName(configTable.getModelName());
                    dataModel.setModelPackage(model.getPackageName());
                }

                // Repository
                Configuration.GenerateJava repository = configuration.getRepository();
                if (Objects.nonNull(repository)) {
                    String repositoryName = configTable.getRepositoryName();
                    if (Objects.isNull(repositoryName)) {
                        repositoryName = repository.getPrefix() + dataModel.getModelName() + repository.getSuffix();
                    }
                    dataModel.setRepositoryName(repositoryName);
                    dataModel.setRepositoryPackage(repository.getPackageName());
                }

                // Service
                Configuration.GenerateJava service = configuration.getService();
                if (Objects.nonNull(service)) {
                    String serviceName = configTable.getServiceName();
                    if (Objects.isNull(serviceName)) {
                        serviceName = service.getPrefix() + dataModel.getModelName() + service.getSuffix();
                    }
                    dataModel.setServiceName(serviceName);
                    dataModel.setServicePackage(service.getPackageName());
                }

                // Repository Sql
                final Configuration.GenerateXml repositorySql = configuration.getRepositorySql();
                if (Objects.nonNull(repositorySql)) {
                    String repositorySqlName = configTable.getRepositorySqlName();
                    if (Objects.isNull(repositorySqlName)) {
                        repositorySqlName = repositorySql.getPrefix() + dataModel.getModelName() + repositorySql.getSuffix();
                    }
                    dataModel.setRepositorySqlName(repositorySqlName);
                }

                // Custom Repository Sql
                final Configuration.GenerateXml repositoryCustomSql = configuration.getRepositoryCustomSql();
                if (Objects.nonNull(repositoryCustomSql)) {
                    String customRepositorySqlName = configTable.getCustomRepositorySqlName();
                    if (Objects.isNull(customRepositorySqlName)) {
                        customRepositorySqlName = repositoryCustomSql.getPrefix() + dataModel.getModelName() + repositoryCustomSql.getSuffix();
                    }
                    dataModel.setCustomRepositorySqlName(customRepositorySqlName);
                }

                final Map<String, Configuration.Column> configColumnMap = configTable.getColumnMap();
                // 列
                dataModel.setColumns(
                        dbTable.getColumnList().stream().map(dbColumn -> {
                            final DataModel.Column column = new DataModel.Column();
                            final String columnName = dbColumn.getColumnName();
                            column.setRemarks(dbColumn.getRemarks());
                            column.setName(columnName);
                            setJdbcTypeAndJavaType(column, dbColumn);
                            column.setAutoincrement(Objects.equals("YES", dbColumn.getIsAutoincrement()));
                            if (configColumnMap.containsKey(columnName)) {
                                column.setProperty(configColumnMap.get(columnName).getProperty());
                            }
                            return column;
                        }).collect(Collectors.toList())
                );

                // 扩展类型自动匹配
                if (Objects.isNull(dataModel.getExtTypes()) || dataModel.getExtTypes().isEmpty()) {
                    List<String> props = dataModel.getColumns().stream().map(DataModel.Column::getProperty).collect(Collectors.toList());
                    Configuration.ExtProperty extProperty = dataModel.getExtProperty();

                    List<String> extTypes = new ArrayList<>();
                    Configuration.ExtProperty.Dateable dateable = extProperty.getDateable();
                    if (props.containsAll(Arrays.asList(dateable.getCreatedDate(), dateable.getLastModifiedDate()))) {
                        extTypes.add(dateable.getClass().getSimpleName());
                    }
                    Configuration.ExtProperty.Treeable treeable = extProperty.getTreeable();
                    if (props.containsAll(Arrays.asList(treeable.getParentId(), treeable.getPath(), treeable.getSort()))) {
                        extTypes.add(treeable.getClass().getSimpleName());
                    }
                    if (!extTypes.isEmpty()) {
                        dataModel.setExtTypes(extTypes);
                    }
                }

                // 主键列
                final List<PrimaryKey> dbPrimaryKeys = dbTable.getPrimaryKeyList();
                if (Objects.nonNull(dbPrimaryKeys) && dbPrimaryKeys.size() == 1) {
                    final PrimaryKey dbPrimaryKey = dbPrimaryKeys.get(0);
                    for (DataModel.Column column : dataModel.getColumns()) {
                        final String name = column.getName();
                        if (Objects.equals(dbPrimaryKey.getColumnName(), name)) {
                            dataModel.setPrimaryKey(column);
                            break;
                        }
                    }
                }

                return dataModel;
            }).collect(Collectors.toList());
            log.info("模板数据 -> {}", JSON.toJSONString(dataModels, true));

            for (DataModel dataModel : dataModels) {
                // 暂时不支持没有主键的表，或者有多个主键的表
                if (Objects.isNull(dataModel.getPrimaryKey())) {
                    log.warn("数据库表【{}】没有主键列，不支持代码生成", dataModel.getTableName());
                    continue;
                }
                dataModel.setProperties(configuration.getProperties());
                dataModel.setExtProperty(configuration.getExtProperty());
                generateModel(dataModel);
                generateRepository(dataModel);
                generateRepositorySql(dataModel);
                generateService(dataModel);
                generateCustomRepositorySql(dataModel);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 生成 Model
     *
     * @param dataModel
     */
    private void generateModel(DataModel dataModel) {
        final Configuration.GenerateJava model = configuration.getModel();
        if (Objects.isNull(model)) {
            return;
        }
        generateFile(dataModel, model, dataModel.getModelName());
    }

    /**
     * 生成 Repository
     *
     * @param dataModel
     */
    private void generateRepository(DataModel dataModel) {
        final Configuration.GenerateJava repository = configuration.getRepository();
        if (Objects.isNull(repository)) {
            return;
        }
        generateFile(dataModel, repository, dataModel.getRepositoryName());
    }

    /**
     * 生成 Service
     *
     * @param dataModel
     */
    private void generateService(DataModel dataModel) {
        final Configuration.GenerateJava service = configuration.getService();
        if (Objects.isNull(service)) {
            return;
        }
        generateFile(dataModel, service, dataModel.getServiceName());
    }

    /**
     * 生成 Repository Sql
     *
     * @param dataModel
     */
    private void generateRepositorySql(DataModel dataModel) {
        final Configuration.GenerateXml repositorySql = configuration.getRepositorySql();
        if (Objects.isNull(repositorySql)) {
            return;
        }
        generateFile(dataModel, repositorySql, dataModel.getRepositorySqlName());
    }

    /**
     * 生成 Custom Repository Sql
     *
     * @param dataModel
     */
    private void generateCustomRepositorySql(DataModel dataModel) {
        final Configuration.GenerateXml repositoryCustomSql = configuration.getRepositoryCustomSql();
        if (Objects.isNull(repositoryCustomSql)) {
            return;
        }
        generateFile(dataModel, repositoryCustomSql, dataModel.getCustomRepositorySqlName());
    }

    /**
     * 生成文件
     *
     * @param dataModel
     * @param generateFile
     * @param fileName
     */
    private void generateFile(DataModel dataModel, Configuration.GenerateFile generateFile, String fileName) {
        final String template = generateFile.getTemplate();
        final File output = new File(generateFile.getOutputDirectory(), fileName + generateFile.getExtension());
        dataModel.setOutputFileExists(output.exists());
        CodeGenerator.create(configuration.getTemplateConfiguration())
                .keepMarkStart(configuration.getKeepMarkStart())
                .keepMarkEnd(configuration.getKeepMarkEnd())
                .override(generateFile.isOverride())
                .generate(dataModel, template, output);
    }

    /**
     * 设置 JdbcType 与 JavaType
     *
     * @param column
     * @param dbColumn
     */
    private void setJdbcTypeAndJavaType(DataModel.Column column, Column dbColumn) {
        final String typeName = dbColumn.getTypeName();
        if (Objects.equals(typeName, "BIGINT")) {
            column.setJdbcType(JdbcType.BIGINT.name());
            column.setJavaType(Long.class);
            return;
        }

        if (Arrays.asList("INT", "INTEGER").contains(typeName)) {
            column.setJdbcType(JdbcType.INTEGER.name());
            column.setJavaType(Integer.class);
            return;
        }

        if (Arrays.asList("DOUBLE", "FLOAT", "DECIMAL").contains(typeName)) {
            column.setJdbcType(JdbcType.DECIMAL.name());
            column.setJavaType(BigDecimal.class);
            return;
        }

        if (Objects.equals("BIT", typeName)) {
            column.setJdbcType(JdbcType.BIT.name());
            column.setJavaType(Boolean.class);
            return;
        }

        // 数字类型
        if (Objects.equals(typeName, "NUMBER")) {
            column.setJdbcType(JdbcType.DECIMAL.name());
            Integer digits = dbColumn.getDecimalDigits();
            Integer size = dbColumn.getColumnSize();
            if (digits > 0 || size > 18) {
                column.setJavaType(BigDecimal.class);
                return;
            }
            if (size >= 10) {
                column.setJavaType(Long.class);
                return;
            }
            column.setJavaType(Integer.class);
            return;
        }

        // 日期类型
        if (Objects.equals("DATE", typeName) || Objects.equals("DATETIME", typeName) || typeName.startsWith("TIMESTAMP")) {
            column.setJdbcType(JdbcType.TIMESTAMP.name());
            column.setJavaType(Date.class);
            return;
        }

        // 其他类型均视为字符串类型
        column.setJdbcType(JdbcType.VARCHAR.name());
        column.setJavaType(String.class);
    }
}

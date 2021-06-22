package com.github.freegeese.easymybatis.generator.mybatis;

import com.alibaba.fastjson.JSON;
import com.github.freegeese.easymybatis.generator.Generator;
import com.github.freegeese.easymybatis.generator.database.DatabaseMetaDataHelper;
import com.github.freegeese.easymybatis.generator.database.JdbcConnectionBuilder;
import com.github.freegeese.easymybatis.generator.database.metadata.Column;
import com.github.freegeese.easymybatis.generator.database.metadata.PrimaryKey;
import com.github.freegeese.easymybatis.generator.database.metadata.Table;
import com.github.freegeese.easymybatis.generator.util.IoUtils;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;

import java.io.File;
import java.math.BigDecimal;
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
    private MybatisConfiguration configuration;

    public MybatisGenerator(MybatisConfiguration configuration) {
        this.configuration = configuration;
    }

    public MybatisConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 根据 JSON 配置信息，创建代码生成器，这里需要替换变量
     *
     * @param json
     * @return
     */
    public static MybatisGenerator create(String json) {
        final Map<String, Object> properties = JSON.parseObject(json, MybatisConfiguration.class).getProperties();
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
        return create(JSON.parseObject(json, MybatisConfiguration.class));
    }

    /**
     * 根据配置对象创建代码生成器
     *
     * @param configuration
     * @return
     */
    public static MybatisGenerator create(MybatisConfiguration configuration) {
        return new MybatisGenerator(configuration.postConstruct());
    }

    /**
     * 执行代码生成
     */
    public void generate() {
        try {
            // JDBC
            final MybatisConfiguration.Jdbc jdbc = configuration.getJdbc();
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
            final Map<String, MybatisConfiguration.Table> configTableMap = configuration.getTableMap();
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
                MybatisConfiguration.Table configTable = configTableMap.get(dbTable.getTableName());
                if (Objects.isNull(configTable)) {
                    configTable = new MybatisConfiguration.Table();
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
                final MybatisConfiguration.GenerateJava model = configuration.getModel();
                if (Objects.nonNull(model)) {
                    dataModel.setModelName(configTable.getModelName());
                    dataModel.setModelPackage(model.getPackageName());
                }

                // Mapper
                MybatisConfiguration.GenerateJava mapper = configuration.getMapper();
                if (Objects.nonNull(mapper)) {
                    String repositoryName = configTable.getMapperName();
                    if (Objects.isNull(repositoryName)) {
                        repositoryName = mapper.getPrefix() + dataModel.getModelName() + mapper.getSuffix();
                    }
                    dataModel.setRepositoryName(repositoryName);
                    dataModel.setMapperPackage(mapper.getPackageName());
                }

                // Service
                MybatisConfiguration.GenerateJava service = configuration.getService();
                if (Objects.nonNull(service)) {
                    String serviceName = configTable.getServiceName();
                    if (Objects.isNull(serviceName)) {
                        serviceName = service.getPrefix() + dataModel.getModelName() + service.getSuffix();
                    }
                    dataModel.setServiceName(serviceName);
                    dataModel.setServicePackage(service.getPackageName());
                }

                // MapperXml
                final MybatisConfiguration.GenerateXml mapperXml = configuration.getMapperXml();
                if (Objects.nonNull(mapperXml)) {
                    String repositorySqlName = configTable.getMapperXmlName();
                    if (Objects.isNull(repositorySqlName)) {
                        repositorySqlName = mapperXml.getPrefix() + dataModel.getModelName() + mapperXml.getSuffix();
                    }
                    dataModel.setRepositorySqlName(repositorySqlName);
                }

                final Map<String, MybatisConfiguration.Column> configColumnMap = configTable.getColumnMap();
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
                    MybatisConfiguration.ExtProperty extProperty = dataModel.getExtProperty();

                    List<String> extTypes = new ArrayList<>();
                    MybatisConfiguration.ExtProperty.Dateable dateable = extProperty.getDateable();
                    if (props.containsAll(Arrays.asList(dateable.getCreatedDate(), dateable.getLastModifiedDate()))) {
                        extTypes.add(dateable.getClass().getSimpleName());
                    }
                    MybatisConfiguration.ExtProperty.Treeable treeable = extProperty.getTreeable();
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
                dataModel.setProperties(configuration.getProperties());
                dataModel.setExtProperty(configuration.getExtProperty());
                dataModel.setKeepMarkStart(configuration.getKeepMarkStart());
                dataModel.setKeepMarkEnd(configuration.getKeepMarkEnd());

                generateModel(dataModel);
                generateMapper(dataModel);
                generateMapperXml(dataModel);
                generateService(dataModel);
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
        final MybatisConfiguration.GenerateJava model = configuration.getModel();
        if (Objects.isNull(model)) {
            return;
        }
        generateFile(dataModel, model, dataModel.getModelName());
    }

    /**
     * 生成 Mapper
     *
     * @param dataModel
     */
    private void generateMapper(DataModel dataModel) {
        final MybatisConfiguration.GenerateJava repository = configuration.getMapper();
        if (Objects.isNull(repository)) {
            return;
        }
        generateFile(dataModel, repository, dataModel.getMapperName());
    }

    /**
     * 生成 Service
     *
     * @param dataModel
     */
    private void generateService(DataModel dataModel) {
        final MybatisConfiguration.GenerateJava service = configuration.getService();
        if (Objects.isNull(service)) {
            return;
        }
        generateFile(dataModel, service, dataModel.getServiceName());
    }

    /**
     * 生成 Mapper.xml
     *
     * @param dataModel
     */
    private void generateMapperXml(DataModel dataModel) {
        final MybatisConfiguration.GenerateXml repositorySql = configuration.getMapperXml();
        if (Objects.isNull(repositorySql)) {
            return;
        }
        generateFile(dataModel, repositorySql, dataModel.getRepositorySqlName());
    }

    /**
     * 生成文件
     *
     * @param dataModel
     * @param generateFile
     * @param fileName
     */
    private void generateFile(DataModel dataModel, MybatisConfiguration.GenerateFile generateFile, String fileName) {
        String template = IoUtils.readString(getClass().getClassLoader().getResourceAsStream(generateFile.getName()));
        File output = new File(generateFile.getOutputDirectory(), fileName + generateFile.getExtension());
        dataModel.setOutputFileExists(output.exists());

        Generator.create()
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

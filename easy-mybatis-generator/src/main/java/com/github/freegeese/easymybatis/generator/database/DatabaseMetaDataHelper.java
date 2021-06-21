package com.github.freegeese.easymybatis.generator.database;

import com.alibaba.fastjson.JSON;
import com.nuochen.framework.autocoding.generator.database.metadata.Column;
import com.nuochen.framework.autocoding.generator.database.metadata.PrimaryKey;
import com.nuochen.framework.autocoding.generator.database.metadata.Table;
import lombok.Getter;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

/**
 * 元数据抽取
 */
public final class DatabaseMetaDataHelper {
    /**
     * 数据库元数据抽取
     *
     * @return
     */
    public static DatabaseMetaDataExtractor databaseMetaDataExtractor() {
        return new DatabaseMetaDataExtractor();
    }

    public static class DatabaseMetaDataExtractor implements Extractor {
        @Override
        public Object extract(DatabaseMetaData databaseMetaData) {
            try {
                Map<String, Object> data = new TreeMap<>();
                Method[] methods = DatabaseMetaData.class.getDeclaredMethods();
                for (Method method : methods) {
                    int parameterCount = method.getParameterCount();
                    if (Objects.equals(0, parameterCount)) {
                        Class<?> returnType = method.getReturnType();
                        // 连接对象
                        if (Connection.class.isAssignableFrom(returnType)) {
                            continue;
                        }

                        String name = method.getName();
                        Object retVal = method.invoke(databaseMetaData);

                        // 结果集
                        if (ResultSet.class.isAssignableFrom(returnType)) {
                            data.put(name, parseResultSet((ResultSet) retVal));
                            continue;
                        }

                        // 其他类型数据
                        data.put(name, retVal);
                    }
                }
                return data;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 数据库表格信息抽取
     *
     * @return
     */
    public static TablesExtractor tablesExtractor() {
        return new TablesExtractor();
    }


    @Getter
    public static class TablesExtractor implements Extractor {
        private String catalog;
        private String schemaPattern;
        private String tableNamePattern;
        private String[] types;

        public TablesExtractor catalog(String catalog) {
            this.catalog = catalog;
            return this;
        }

        public TablesExtractor schemaPattern(String schemaPattern) {
            this.schemaPattern = schemaPattern;
            return this;
        }

        public TablesExtractor tableNamePattern(String tableNamePattern) {
            this.tableNamePattern = tableNamePattern;
            return this;
        }

        public TablesExtractor types(String[] types) {
            this.types = types;
            return this;
        }

        @Override
        public List<Table> extract(DatabaseMetaData databaseMetaData) {
            try {
                ResultSet rs = databaseMetaData.getTables(catalog, schemaPattern, null == tableNamePattern ? "%" : tableNamePattern, null == types ? new String[]{"TABLE"} : types);
                final List<Table> tableList = JSON.parseArray(JSON.toJSONString(parseResultSet(rs)), Table.class);
                for (Table table : tableList) {
                    final String tableName = table.getTableName();
                    final List<Column> columnList = columnsExtractor().catalog(catalog).schemaPattern(schemaPattern).tableNamePattern(tableName).extract(databaseMetaData);
                    table.setColumnList(columnList);

                    final List<PrimaryKey> primaryKeyList = primaryKeysExtractor().catalog(catalog).schema(schemaPattern).table(tableName).extract(databaseMetaData);
                    table.setPrimaryKeyList(primaryKeyList);
                }
                return tableList;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 数据库表格列信息抽取
     *
     * @return
     */
    public static ColumnsExtractor columnsExtractor() {
        return new ColumnsExtractor();
    }

    @Getter
    public static class ColumnsExtractor implements Extractor {
        private String catalog;
        private String schemaPattern;
        private String tableNamePattern;
        private String columnNamePattern;

        public ColumnsExtractor catalog(String catalog) {
            this.catalog = catalog;
            return this;
        }

        public ColumnsExtractor schemaPattern(String schemaPattern) {
            this.schemaPattern = schemaPattern;
            return this;
        }

        public ColumnsExtractor tableNamePattern(String tableNamePattern) {
            this.tableNamePattern = tableNamePattern;
            return this;
        }

        public ColumnsExtractor columnNamePattern(String columnNamePattern) {
            this.columnNamePattern = columnNamePattern;
            return this;
        }

        @Override
        public List<Column> extract(DatabaseMetaData databaseMetaData) {
            try {
                ResultSet rs = databaseMetaData.getColumns(catalog, schemaPattern, tableNamePattern, null == columnNamePattern ? "%" : columnNamePattern);
                return JSON.parseArray(JSON.toJSONString(parseResultSet(rs)), Column.class);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 抽取主键信息
     *
     * @return
     */
    public static PrimaryKeysExtractor primaryKeysExtractor() {
        return new PrimaryKeysExtractor();
    }

    @Getter
    public static class PrimaryKeysExtractor implements Extractor {
        private String catalog;
        private String schema;
        private String table;

        public PrimaryKeysExtractor catalog(String catalog) {
            this.catalog = catalog;
            return this;
        }

        public PrimaryKeysExtractor schema(String schema) {
            this.schema = schema;
            return this;
        }

        public PrimaryKeysExtractor table(String table) {
            this.table = table;
            return this;
        }

        @Override
        public List<PrimaryKey> extract(DatabaseMetaData databaseMetaData) {
            try {
                ResultSet rs = databaseMetaData.getPrimaryKeys(catalog, schema, table);
                return JSON.parseArray(JSON.toJSONString(parseResultSet(rs)), PrimaryKey.class);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 解析结果集
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<Map> parseResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int columnCount = rsMetaData.getColumnCount();
        List<Map> rows = new ArrayList<>();
        while (rs.next()) {
            Map row = new TreeMap();
            for (int i = 1; i <= columnCount; i++) {
                String columnLabel = rsMetaData.getColumnLabel(i);
                Object columnValue = rs.getObject(i);
                row.put(columnLabel, columnValue);
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 抽取数据接口
     */
    private interface Extractor {
        Object extract(DatabaseMetaData databaseMetaData);
    }
}

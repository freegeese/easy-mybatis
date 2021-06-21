package com.github.freegeese.easymybatis.generator.database;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.freegeese.easymybatis.generator.database.metadata.Column;
import com.github.freegeese.easymybatis.generator.database.metadata.Table;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.DatabaseMetaData;
import java.util.*;

/**
 * 数据库比较器
 */
@Slf4j
public final class DatabaseComparator {

    private List<String> columnCompareFields = new ArrayList<>();

    public static DatabaseComparator create() {
        DatabaseComparator comparator = new DatabaseComparator();
        comparator.columnCompareFields.addAll(Arrays.asList("columnName", "typeName"));
        return comparator;
    }

    public DatabaseComparator columnCompareFields(String columnCompareField) {
        this.columnCompareFields.add(columnCompareField);
        return this;
    }

    public DatabaseComparator columnCompareFields(Collection<String> columnCompareFields) {
        this.columnCompareFields.addAll(Preconditions.checkNotNull(columnCompareFields));
        return this;
    }

    public CompareResult compare(DatabaseMetaData databaseMetaData, String firstDatabase, String secondDatabase) {
        List<Table> tables1 = DatabaseMetaDataHelper.tablesExtractor().catalog(firstDatabase).extract(databaseMetaData);
        if (null == tables1 || tables1.isEmpty()) {
            log.info("database -> {} 未获取到数据库表", firstDatabase);
        }

        List<Table> tables2 = DatabaseMetaDataHelper.tablesExtractor().catalog(secondDatabase).extract(databaseMetaData);
        if (null == tables2 || tables2.isEmpty()) {
            log.info("database -> {} 未获取到数据库表", secondDatabase);
        }

        Map<String, Table> tables1Map = new TreeMap<>();
        for (Table item : tables1) {
            tables1Map.put(item.getTableName(), item);
        }

        Map<String, Table> tables2Map = new TreeMap<>();
        for (Table item : tables2) {
            tables2Map.put(item.getTableName(), item);
        }

        CompareResult result = new CompareResult();
        // 交集 差集（more | less）
        List<String> retain = new ArrayList<>(tables1Map.keySet());
        List<String> more = new ArrayList<>(tables1Map.keySet());
        List<String> less = new ArrayList<>(tables2Map.keySet());
        // 交集
        retain.retainAll(less);
        // 差集 -> more
        more.removeAll(retain);
        // 差集 -> less
        less.removeAll(retain);
        result.setMore(more);
        result.setLess(less);

        // 处理交集
        if (!retain.isEmpty()) {
            Map<String, CompareResult> retainResult = new TreeMap<>();
            for (String tableName : retain) {
                CompareResult compareResult = compareTable(databaseMetaData, firstDatabase, secondDatabase, tableName);
                retainResult.put(tableName, compareResult);
            }
            result.setRetain(retainResult);
        }

        return result;
    }

    private CompareResult compareTable(DatabaseMetaData databaseMetaData, String firstDatabase, String secondDatabase, String tableName) {
        List<Column> columns1 = DatabaseMetaDataHelper.columnsExtractor().catalog(firstDatabase).tableNamePattern(tableName).extract(databaseMetaData);
        List<Column> columns2 = DatabaseMetaDataHelper.columnsExtractor().catalog(secondDatabase).tableNamePattern(tableName).extract(databaseMetaData);

        Map<String, Column> columns1Map = new TreeMap<>();
        for (Column item : columns1) {
            columns1Map.put(item.getColumnName(), item);
        }

        Map<String, Column> columns2Map = new TreeMap<>();
        for (Column item : columns2) {
            columns2Map.put(item.getColumnName(), item);
        }

        CompareResult result = new CompareResult();
        // 交集 差集（more | less）
        List<String> retain = new ArrayList<>(columns1Map.keySet());
        List<String> more = new ArrayList<>(columns1Map.keySet());
        List<String> less = new ArrayList<>(columns2Map.keySet());
        // 交集
        retain.retainAll(less);
        // 差集 -> more
        more.removeAll(retain);
        // 差集 -> less
        less.removeAll(retain);

        result.setMore(more);
        result.setLess(less);

        // 处理交集
        if (!retain.isEmpty()) {
            Map<String, Boolean> matchResult = new TreeMap<>();
            for (String columnName : retain) {
                matchResult.put(columnName, matches(columns1Map.get(columnName), columns2Map.get(columnName)));
            }
            result.setMatchResult(matchResult);
        }

        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean matches(Column column1, Column column2) {
        log.info("匹配 Column 是否一致 -> first column meta data -> {}, second column meta data -> {}", JSON.toJSONString(column1, true), JSON.toJSONString(column2));
        JSONObject column1Map = (JSONObject) JSON.toJSON(column1);
        JSONObject column2Map = (JSONObject) JSON.toJSON(column2);

        List column1CompareFields = new ArrayList();
        List column2CompareFields = new ArrayList();
        for (String columnCompareField : this.columnCompareFields) {
            column1CompareFields.add(column1Map.get(columnCompareField));
            column2CompareFields.add(column2Map.get(columnCompareField));
        }
        String column1CompareVal = Joiner.on(" ").join(column1CompareFields);
        String column2CompareVal = Joiner.on(" ").join(column2CompareFields);
        log.info("匹配 Column 是否一致 -> first column compare value -> {}, second column compare value -> {}", column1CompareVal, column2CompareVal);

        boolean result = Objects.equals(column1CompareFields, column2CompareFields);
        log.info("匹配 Column 是否一致 -> 结果 -> {}", result);

        return result;
    }

    @Data
    public static class CompareResult {
        private List<String> more;
        private List<String> less;
        private Map<String, CompareResult> retain;
        private Map<String, Boolean> matchResult;

        public boolean isMatched() {
            if (!more.isEmpty() || !less.isEmpty()) {
                return false;
            }

            if (null != matchResult && !matchResult.isEmpty()) {
                return matchResult.values().stream().allMatch((Predicate<Boolean>) input -> input);
            }

            if (null != retain && !retain.isEmpty()) {
                for (CompareResult compareResult : retain.values()) {
                    if (!compareResult.isMatched()) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

}

package com.github.freegeese.easymybatis.generator.database.metadata;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class Table {
    private String refGeneration;
    private String remarks;
    private String selfReferencingColName;
    private String tableCat;
    private String tableName;
    private String tableSchem;
    private String tableType;
    private String typeCat;
    private String typeName;
    private String typeSchem;

    private List<Column> columnList;
    private List<PrimaryKey> primaryKeyList;

    public Map<String, Column> getColumnMetaDataMap() {
        return columnList.stream().collect(Collectors.toMap(Column::getColumnName, Function.identity()));
    }

    public Map<String, PrimaryKey> getPrimaryKeyMetaDataMap() {
        return primaryKeyList.stream().collect(Collectors.toMap(PrimaryKey::getColumnName, Function.identity()));
    }
}

package com.github.freegeese.easymybatis.generator.database.metadata;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        final HashMap<String, Column> map = Maps.newHashMap();
        for (Column column : columnList) {
            map.put(column.getColumnName(), column);
        }
        return map;
    }

    public Map<String, PrimaryKey> getPrimaryKeyMetaDataMap() {
        final HashMap<String, PrimaryKey> map = Maps.newHashMap();
        for (PrimaryKey primaryKey : primaryKeyList) {
            map.put(primaryKey.getColumnName(), primaryKey);
        }
        return map;
    }
}

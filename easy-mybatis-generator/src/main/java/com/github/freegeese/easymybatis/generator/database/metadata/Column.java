package com.github.freegeese.easymybatis.generator.database.metadata;

import lombok.Data;

@Data
public class Column {
    private String tableCat;
    private String tableSchem;
    private String tableName;
    private String columnName;
    private Integer dataType;
    private String typeName;
    private Integer columnSize;
    private Integer bufferLength;
    private Integer decimalDigits;
    private Integer numPrecRadix;
    private Integer nullable;
    private String remarks;
    private String columnDef;
    private Integer sqlDataType;
    private Integer sqlDatetimeSub;
    private Integer charOctetLength;
    private Integer ordinalPosition;
    private String isNullable;
    private String scopeCatalog;
    private String scopeSchema;
    private String scopeTable;
    private Integer sourceDataType;
    private String isAutoincrement;
    private String isGeneratedcolumn;
}

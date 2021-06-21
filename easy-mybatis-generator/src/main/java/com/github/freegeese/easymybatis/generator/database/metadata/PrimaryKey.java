package com.github.freegeese.easymybatis.generator.database.metadata;

import lombok.Data;

@Data
public class PrimaryKey {
    private String tableCat;
    private String tableSchem;
    private String tableName;
    private String columnName;
    private Integer keySeq;
    private String pkName;
}

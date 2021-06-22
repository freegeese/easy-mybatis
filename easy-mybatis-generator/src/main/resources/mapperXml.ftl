<#assign modelClass="${modelPackage}.${modelName}">
<#assign mapperClass="${mapperPackage}.${modelName}Mapper">
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${mapperPackage}.${mapperName}">
    <resultMap id="BaseResultMap" type="${modelPackage}.${modelName}">
        <#if primaryKey??>
        <id column="${primaryKey.name}" jdbcType="${primaryKey.jdbcType}" property="${primaryKey.property}"/>
        </#if>
        <#list columnsWithoutPrimaryKey as column>
        <result column="${column.name}" jdbcType="${column.jdbcType}" property="${column.property}"/>
        </#list>
    </resultMap>

    <sql id="Base_Column_List">
        ${columnNames}
    </sql>
</mapper>
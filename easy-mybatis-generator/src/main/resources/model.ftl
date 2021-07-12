<#-- package -->
package ${modelPackage};

<#-- imports -->
<#list modelImportTypes as importType>
import ${importType};
</#list>
import lombok.Data;

/**
 * ${tableName}
 */
@Data
public class ${modelName} implements ${modelImplements} {
<#-- fields -->
<#list columns as column>
    <#if primaryKey?? && primaryKey.name == column.name>
    @Id
        <#if primaryKey.autoincrement??>
    @GeneratedValue(strategy = GenerationType.AUTO)
        </#if>
    </#if>
    <#if column.remarks?length gt 1>
    // ${column.remarks}
    </#if>
    private ${column.javaType.simpleName} ${column.property};
</#list>
<#-- ext property -->
<#if overriddenExtPropertyMap?? && (overriddenExtPropertyMap?size>0)>
    <#list overriddenExtPropertyMap?keys as key>
    @Override
    public ${getPropertyType(overriddenExtPropertyMap[key])} get${key?cap_first}() {
        return ${overriddenExtPropertyMap[key]};
    }
    @Override
    public void set${key?cap_first}(${getPropertyType(overriddenExtPropertyMap[key])} ${overriddenExtPropertyMap[key]}) {
        this.${overriddenExtPropertyMap[key]} = ${overriddenExtPropertyMap[key]};
    }
    </#list>
</#if>
<#-- keep -->
<#if !(outputFileExists)>
    /* ${keepMarkStart} */
    /* ${keepMarkEnd} */
</#if>
}
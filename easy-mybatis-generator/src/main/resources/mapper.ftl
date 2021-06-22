<#-- package -->
package ${mapperPackage};

<#-- imports -->
<#list mapperImportTypes as importType>
import ${importType};
</#list>
import org.springframework.stereotype.Repository;

@Repository
public interface ${mapperName} extends ${mapperExtends} {
}
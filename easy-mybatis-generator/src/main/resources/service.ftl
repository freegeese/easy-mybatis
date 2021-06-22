<#-- package -->
package ${servicePackage};

<#-- imports -->
<#list serviceImportTypes as importType>
import ${importType};
</#list>
import org.springframework.stereotype.Service;

@Service
public class ${serviceName} extends ${serviceExtends} {
}
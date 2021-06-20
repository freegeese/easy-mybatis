package com.github.freegeese.easymybatis.core.meta;

import com.github.freegeese.easymybatis.core.EasyMybatisConfiguration;
import com.github.freegeese.easymybatis.core.annotation.*;
import com.github.freegeese.easymybatis.util.RefUtils;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 实体类元数据信息
 *
 * @author zhangguangyong
 * @since 1.0
 */
@Data
public class MetaEntityClass {
    /**
     * get方法
     */
    private List<Method> getMethods;
    /**
     * get方法对应的字段
     */
    private List<Field> getFields;
    /**
     * 实体对应的结果集
     */
    private List<ResultMapping> resultMappings;
    /**
     * 实体对应的表名
     */
    private String table;
    /**
     * 层级结构get方法
     */
    private static final TreeableGetMethod treeableGetMethod = new TreeableGetMethod();

    @Data
    public static class TreeableGetMethod {
        private String getId = "getId";
        private String getParentId = "getParentId";
        private String getPath = "getPath";
        private String getSort = "getSort";
    }

    /**
     * 列映射信息
     */
    @Data
    public static class ResultMapping {
        /**
         * 属性
         */
        private String property;
        /**
         * 列
         */
        private String column;
        /**
         * 属性类型
         */
        private Class<?> javaType;
        /**
         * 列类型
         */
        private JdbcType jdbcType;
        /**
         * 类型转换处理
         */
        private TypeHandler<?> typeHandler;
        /**
         * 是否为主键
         */
        private boolean primaryKey;
        /**
         * 是否为自增列
         */
        private boolean auto;
        /**
         * get方法
         */
        private Method getMethod;

        public String ne(String prefix) {
            return sql(Condition.ne, prefix);
        }

        public String ne() {
            return sql(Condition.ne);
        }

        public String eq(String prefix) {
            return sql(Condition.eq, prefix);
        }

        public String eq() {
            return sql(Condition.eq);
        }

        public String gt(String prefix) {
            return sql(Condition.gt, prefix);
        }

        public String gt() {
            return sql(Condition.gt);
        }

        public String lt(String prefix) {
            return sql(Condition.lt, prefix);
        }

        public String lt() {
            return sql(Condition.lt);
        }

        public String ge(String prefix) {
            return sql(Condition.ge, prefix);
        }

        public String ge() {
            return sql(Condition.ge);
        }

        public String le(String prefix) {
            return sql(Condition.le, prefix);
        }

        public String le() {
            return sql(Condition.le);
        }

        private String sql(Condition condition, String prefix) {
            return String.join(" ", column, condition.symbol, "#{", prefix + "." + property, "}");
        }

        private String sql(Condition condition) {
            return String.join(" ", column, condition.symbol, "#{" + property + "}");
        }

        private enum Condition {
            ne("!="),
            eq("="),
            gt(">"),
            lt("<"),
            ge(">="),
            le("<=");
            private final String symbol;

            Condition(String symbol) {
                this.symbol = symbol;
            }
        }

    }

    public static TreeableGetMethod getTreeableGetMethod() {
        return treeableGetMethod;
    }

    /**
     * 获取实体元数据
     *
     * @param clazz
     * @return
     */
    public static MetaEntityClass forClass(Class<?> clazz) {
        EasyMybatisConfiguration cfg = EasyMybatisConfiguration.getInstance();
        Function<Class<?>, String> entityClassToTable = cfg.getEntityClassToTable();
        Function<Method, String> entityGetMethodToColumn = cfg.getEntityGetMethodToColumn();

        MetaEntityClass meta = new MetaEntityClass();

        // 表名
        meta.setTable(entityClassToTable.apply(clazz));

        // getMethods
        List<Method> getMethods = RefUtils.getAllDeclaredMethods(clazz).stream().filter(v -> v.getName().startsWith("get") && Objects.isNull(v.getAnnotation(Ignore.class))).collect(Collectors.toList());
        Map<String, Method> getMethodMap = getMethods.stream().collect(Collectors.toMap(MetaEntityClass::toProperty, Function.identity()));

        // fields
        List<Field> fields = RefUtils.getAllDeclaredFields(clazz).stream().filter(v -> Objects.isNull(v.getAnnotation(Ignore.class))).collect(Collectors.toList());

        List<ResultMapping> resultMappings = new ArrayList<>();
        for (Field field : fields) {
            String property = field.getName();
            if (!getMethodMap.containsKey(property)) {
                continue;
            }
            Method getMethod = getMethodMap.get(property);

            ResultMapping resultMapping = new ResultMapping();
            resultMapping.setGetMethod(getMethod);
            resultMapping.setProperty(property);
            resultMapping.setJavaType(getMethod.getReturnType());

            // column
            Column column = getMethod.getAnnotation(Column.class);
            column = Objects.isNull(column) ? field.getAnnotation(Column.class) : column;
            if (Objects.nonNull(column)) {
                JdbcType jdbcType = column.jdbcType();
                if (jdbcType != JdbcType.UNDEFINED) {
                    resultMapping.setJdbcType(jdbcType);
                }

                Class<? extends TypeHandler<?>> typeHandler = column.typeHandler();
                if (typeHandler != UnknownTypeHandler.class) {
                    resultMapping.setTypeHandler(getInstance(resultMapping.getJavaType(), typeHandler));
                }
            }
            // 列名
            resultMapping.setColumn(Objects.isNull(column) || column.value().isEmpty() ? entityGetMethodToColumn.apply(getMethod) : column.value());

            // primaryKey
            resultMapping.setPrimaryKey(Objects.nonNull(getMethod.getAnnotation(Id.class)) || Objects.nonNull(field.getAnnotation(Id.class)));
            GeneratedValue generatedValue = getMethod.getAnnotation(GeneratedValue.class);
            generatedValue = Objects.isNull(generatedValue) ? field.getAnnotation(GeneratedValue.class) : generatedValue;
            resultMapping.setAuto(Objects.nonNull(generatedValue) && generatedValue.strategy() == GenerationType.AUTO);

            resultMappings.add(resultMapping);
        }

        meta.setGetMethods(getMethods);
        meta.setGetFields(fields.stream().filter(v -> getMethodMap.containsKey(v.getName())).collect(Collectors.toList()));
        meta.setResultMappings(resultMappings);

        return meta;
    }

    /**
     * 获取所有列名
     *
     * @return
     */
    public String getColumns() {
        return resultMappings.stream().map(MetaEntityClass.ResultMapping::getColumn).collect(Collectors.joining(","));
    }

    /**
     * 获取主键列
     *
     * @return
     */
    public ResultMapping getPrimaryKeyResultMapping() {
        return resultMappings.stream().filter(ResultMapping::isPrimaryKey).findFirst().orElse(null);
    }

    /**
     * 获取 auto 之外的所有列
     *
     * @return
     */
    public List<ResultMapping> getResultMappingsWithoutAuto() {
        return resultMappings.stream().filter(v -> !v.isAuto()).collect(Collectors.toList());
    }

    /**
     * 获取 auto 列
     *
     * @return
     */
    public List<ResultMapping> getAutoResultMappings() {
        return resultMappings.stream().filter(ResultMapping::isAuto).collect(Collectors.toList());
    }

    /**
     * 获取所有非空列
     *
     * @param target
     * @return
     */
    public List<ResultMapping> getResultMappingsWithoutNull(Object target) {
        return resultMappings.stream().filter(v -> Objects.nonNull(RefUtils.invokeMethod(v.getGetMethod(), target))).collect(Collectors.toList());
    }

    /**
     * 获取所有非（auto 且 null） 列
     *
     * @param target
     * @return
     */
    public List<ResultMapping> getResultMappingsWithoutAutoAndNull(Object target) {
        return resultMappings.stream().filter(v -> !(v.isAuto() && Objects.isNull(RefUtils.invokeMethod(v.getGetMethod(), target)))).collect(Collectors.toList());
    }

    /**
     * 获取所有非 (primaryKey 或 null) 列
     *
     * @param target
     * @return
     */
    public List<ResultMapping> getResultMappingsWithoutPrimaryKeyOrNull(Object target) {
        return resultMappings.stream().filter(v -> !(v.isPrimaryKey() || Objects.isNull(RefUtils.invokeMethod(v.getGetMethod(), target)))).collect(Collectors.toList());
    }

    public ResultMapping checkPrimaryKey(Object target) {
        Optional<ResultMapping> primaryKeyOptional = resultMappings.stream().filter(MetaEntityClass.ResultMapping::isPrimaryKey).findFirst();
        if (!primaryKeyOptional.isPresent()) {
            throw new RuntimeException("主键字段不存在[" + target.getClass() + "]");
        }
        ResultMapping primaryKeyResultMapping = primaryKeyOptional.get();
        if (Objects.isNull(RefUtils.invokeMethod(primaryKeyResultMapping.getGetMethod(), target))) {
            throw new RuntimeException("主键字段值为空[" + primaryKeyResultMapping.getProperty() + ", " + target.getClass() + "]");
        }
        return primaryKeyResultMapping;
    }

    public Map<String, ResultMapping> getGetMethodAndResultMappingMap() {
        return resultMappings.stream().collect(Collectors.toMap(v -> v.getGetMethod().getName(), Function.identity()));
    }

    /**
     * 将 get 方法名称转换属性名称
     *
     * @param getMethod
     * @return
     */
    private static String toProperty(Method getMethod) {
        String name = getMethod.getName().substring(3);
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    /**
     * 反射构造 typeHandler 来自Mybatis源码
     *
     * @param javaTypeClass
     * @param typeHandlerClass
     * @param <T>
     * @return
     */
    private static <T> TypeHandler<T> getInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
        if (javaTypeClass != null) {
            try {
                Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
                return (TypeHandler<T>) c.newInstance(javaTypeClass);
            } catch (NoSuchMethodException ignored) {
                // ignored
            } catch (Exception e) {
                throw new TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
            }
        }
        try {
            Constructor<?> c = typeHandlerClass.getConstructor();
            return (TypeHandler<T>) c.newInstance();
        } catch (Exception e) {
            throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
        }
    }
}

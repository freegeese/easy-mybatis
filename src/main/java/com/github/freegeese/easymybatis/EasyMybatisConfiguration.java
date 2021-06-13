package com.github.freegeese.easymybatis;

import com.github.freegeese.easymybatis.annotation.AutoResultMap;
import com.github.freegeese.easymybatis.annotation.Column;
import com.github.freegeese.easymybatis.annotation.Table;
import com.github.freegeese.easymybatis.meta.MetaCache;
import com.github.freegeese.easymybatis.meta.MetaEntityClass;
import com.github.freegeese.easymybatis.meta.MetaMapperClass;
import com.github.freegeese.easymybatis.meta.MetaObjectWrapper;
import com.github.freegeese.easymybatis.util.RefUtils;
import com.google.common.base.CaseFormat;
import lombok.Data;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 框架配置信息
 *
 * <p>{@link #javaCaseFormat} 标识java命名格式，{@link #databaseCaseFormat} 标识数据库命名格式
 * {@link #doWithKeyGenerator(Configuration)} 处理存在自动生成值得列
 * {@link #doWithAutoResultMapAnnotation(Configuration)}} 处理标识有{@link AutoResultMap}的注解
 *
 * @author zhangguangyong
 * @since 1.0
 */
@Data
public class EasyMybatisConfiguration {
    private CaseFormat javaCaseFormat = CaseFormat.LOWER_CAMEL;
    private CaseFormat databaseCaseFormat = CaseFormat.LOWER_UNDERSCORE;

    /**
     * 从 entity-class 获取 table
     */
    private Function<Class<?>, String> entityClassToTable = entityClass -> {
        Table table = entityClass.getAnnotation(Table.class);
        if (Objects.isNull(table) || table.value().isEmpty()) {
            return format(entityClass.getSimpleName());
        }
        return table.value();
    };

    /**
     * 从 entity-class get-method 获取 column
     */
    private Function<Method, String> entityGetMethodToColumn = method -> {
        Column column = method.getAnnotation(Column.class);
        if (Objects.isNull(column) || column.value().isEmpty()) {
            return format(method.getName().substring(3));
        }
        return column.value();
    };

    /**
     * 从 entity-class field 获取 column
     */
    private Function<Field, String> entityFieldToColumn = field -> {
        Column column = field.getAnnotation(Column.class);
        if (Objects.isNull(column) || column.value().isEmpty()) {
            return format(field.getName());
        }
        return column.value();
    };


    /**
     * KeyGenerate 未定义处理
     *
     * @param configuration
     */
    private void doWithKeyGenerator(Configuration configuration) {
        String[] EMPTY_ARRAY = new String[0];
        Collection<MappedStatement> mappedStatements = configuration.getMappedStatements();
        for (Object ms : mappedStatements) {
            if (!(ms instanceof MappedStatement)) {
                continue;
            }
            MappedStatement mappedStatement = (MappedStatement) ms;

            SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
            KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
            // 非 INSERT 接口 或 已声明了KeyGenerator
            if (sqlCommandType != SqlCommandType.INSERT || keyGenerator != NoKeyGenerator.INSTANCE) {
                continue;
            }
            // 未找到Mapper的泛型实体类型
            MetaEntityClass meta = MetaCache.getMetaEntityClass(MetaCache.getMetaMapperClass(mappedStatement).getEntityClass());
            if (Objects.isNull(meta)) {
                continue;
            }
            // 未找到自增字段
            List<MetaEntityClass.ResultMapping> autoResultMappings = meta.getAutoResultMappings();
            if (Objects.isNull(autoResultMappings) || autoResultMappings.isEmpty()) {
                continue;
            }

            // 设置自增字段
            String[] autoProperties = autoResultMappings.stream().map(MetaEntityClass.ResultMapping::getProperty).collect(Collectors.toList()).toArray(EMPTY_ARRAY);
            String[] autoColumns = autoResultMappings.stream().map(MetaEntityClass.ResultMapping::getProperty).collect(Collectors.toList()).toArray(EMPTY_ARRAY);
            MetaObjectWrapper wrapper = MetaObjectWrapper.forObject(mappedStatement);
            wrapper.setValue("useCache", true);
            wrapper.setValue("keyGenerator", Jdbc3KeyGenerator.INSTANCE);
            wrapper.setValue("keyProperties", autoProperties);
            wrapper.setValue("keyColumns", autoColumns);
        }
    }

    /**
     * 处理 {@link AutoResultMap} 注解
     *
     * @param configuration
     */
    private void doWithAutoResultMapAnnotation(Configuration configuration) {
        Collection<?> mappedStatements = configuration.getMappedStatements();
        // Mapper所有声明接口
        Map<String, Map<String, Method>> mapperMethodMap =
                mappedStatements.stream().filter(v -> v instanceof MappedStatement).map(v -> {
                    String id = ((MappedStatement) v).getId();
                    return id.substring(0, id.lastIndexOf("."));
                }).distinct().collect(Collectors.toMap(v -> v, v -> RefUtils.getAllDeclaredMethods(RefUtils.classForName(v)).stream().collect(Collectors.toMap(Method::getName, Function.identity()))));

        // 注解信息
        Class<AutoResultMap> autoResultMapClass = AutoResultMap.class;
        String autoResultMapName = autoResultMapClass.getSimpleName();

        Map<String, List<ResultMap>> mapperResultMapsCache = new HashMap<>();
        // 遍历处理每一个MappedStatement
        for (Object obj : mappedStatements) {
            // 过滤掉有歧义的 mappedStatement，也就是父Mapper定义的接口
            if (!(obj instanceof MappedStatement)) {
                continue;
            }
            MappedStatement mappedStatement = (MappedStatement) obj;

            // 从 MappedStatement-id 获取Mapper类和方法
            String id = mappedStatement.getId();
            int index = id.lastIndexOf(".");
            String mapperClassName = id.substring(0, index);
            String mapperMethodName = id.substring(index + 1);

            // 先从缓存获取
            if (mapperResultMapsCache.containsKey(mapperClassName)) {
                MetaObjectWrapper.forObject(mappedStatement).setValue("resultMaps", mapperResultMapsCache.get(mapperClassName));
                continue;
            }

            // 查看 MappedStatement-method 是否声明了注解：@AutoResultMap，没有则不进行处理
            Method method = mapperMethodMap.get(mapperClassName).get(mapperMethodName);
            if (Objects.isNull(method.getAnnotation(autoResultMapClass))) {
                continue;
            }

            List<ResultMap> resultMaps = mappedStatement.getResultMaps();
            /* 已经声明 ResultMap (XML 或者 {@link org.apache.ibatis.annotations.Results} 或 {@link org.apache.ibatis.annotations.ResultMap} */
            if (!resultMaps.isEmpty() && !resultMaps.get(0).getResultMappings().isEmpty()) {
                continue;
            }

            // ResultMap Type
            MetaMapperClass metaMapperClass = MetaCache.getMetaMapperClass(mappedStatement);
            Class<?> resultMapType = resultMaps.isEmpty() ? metaMapperClass.getMethodGenericReturnTypeMap().get(method.getName()) : resultMaps.get(0).getType();

            // ResultMap Type 元数据
            MetaEntityClass meta = MetaCache.getMetaEntityClass(resultMapType);
            List<MetaEntityClass.ResultMapping> metaResultMappings = meta.getResultMappings();

            // 通过 MappedStatement-method 返回类型元数据构建 ResultMapping
            List<ResultMapping> resultMappings = new ArrayList<>();
            for (MetaEntityClass.ResultMapping rm : metaResultMappings) {
                Class<?> returnType = rm.getGetMethod().getReturnType();
                ResultMapping.Builder builder = new ResultMapping.Builder(configuration, rm.getProperty(), rm.getColumn(), returnType);
                builder.jdbcType(rm.getJdbcType());
                builder.typeHandler(rm.getTypeHandler());
                if (rm.isPrimaryKey()) {
                    builder.flags(Collections.singletonList(ResultFlag.ID));
                }
                resultMappings.add(builder.build());
            }

            // 构建Mybatis需要的ResultMap
            ResultMap autoResultMap = new ResultMap.Builder(configuration, mapperClassName + "." + autoResultMapName, resultMapType, resultMappings).build();
            List<ResultMap> autoResultMaps = Collections.singletonList(autoResultMap);
            mapperResultMapsCache.put(mapperClassName, autoResultMaps);

            // 通过反射修改 mappedStatement-resultMaps
            MetaObjectWrapper.forObject(mappedStatement).setValue("resultMaps", autoResultMaps);
        }
    }

    private String format(String value) {
        return javaCaseFormat.to(databaseCaseFormat, value);
    }

    /**
     * 在初始化SqlSessionFactory后再初始化自身框架
     *
     * @param sqlSessionFactory
     */
    public void init(SqlSessionFactory sqlSessionFactory) {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        doWithKeyGenerator(configuration);
        doWithAutoResultMapAnnotation(configuration);
    }

    // TODO 这里有待改善，为了在静态类里面使用

    private static EasyMybatisConfiguration instance;

    public EasyMybatisConfiguration() {
        instance = this;
    }

    public static EasyMybatisConfiguration getInstance() {
        return instance;
    }
}

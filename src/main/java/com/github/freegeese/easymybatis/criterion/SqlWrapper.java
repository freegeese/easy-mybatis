package com.github.freegeese.easymybatis.criterion;

import com.github.freegeese.easymybatis.meta.MetaCache;
import com.github.freegeese.easymybatis.meta.MetaEntityClass;
import com.github.freegeese.easymybatis.meta.MetaLambda;
import com.github.freegeese.easymybatis.meta.MetaLambdaCache;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * SQL 包装器
 */

public class SqlWrapper {
    private Class<?> selectFrom;
    private List<SerializableFunction> selectProperties;

    private Class<?> updateFrom;
    private Map<SerializableFunction, Object> updatePropertyValueMap;

    private Class<?> deleteFrom;

    private List<Condition> conditions;
    private Condition next;

    public SqlWrapper() {
        this.conditions = new ArrayList<>();
        this.and();
    }

    private SqlWrapper setSelectFrom(Class<?> selectFrom) {
        this.selectFrom = selectFrom;
        return this;
    }

    private SqlWrapper setSelectProperties(List<SerializableFunction> selectProperties) {
        this.selectProperties = selectProperties;
        return this;
    }

    private SqlWrapper setUpdateFrom(Class<?> updateFrom) {
        this.updateFrom = updateFrom;
        return this;
    }

    private SqlWrapper setUpdatePropertyValueMap(Map<SerializableFunction, Object> updatePropertyValueMap) {
        this.updatePropertyValueMap = updatePropertyValueMap;
        return this;
    }

    private SqlWrapper setDeleteFrom(Class deleteFrom) {
        this.deleteFrom = deleteFrom;
        return this;
    }

    /**
     * 查询
     *
     * @param properties 属性集合
     * @param <T>        属性get方法
     * @param <R>        列名
     * @return SqlWrapper
     */
    @SafeVarargs
    public static <T, R> SqlWrapper select(SerializableFunction<T, R>... properties) {
        return new SqlWrapper().setSelectProperties(Arrays.asList(properties));
    }

    /**
     * 查询
     *
     * @param from       类
     * @param properties 属性集合
     * @param <T>        属性get方法
     * @param <R>        列名
     * @return SqlWrapper
     */
    @SafeVarargs
    public static <T, R> SqlWrapper select(Class<?> from, SerializableFunction<T, R>... properties) {
        return select(properties).setSelectFrom(from);
    }


    public static SqlWrapper update() {
        return new SqlWrapper().setUpdatePropertyValueMap(Maps.newHashMap());
    }

    public static SqlWrapper update(Class<?> from) {
        return update().setUpdateFrom(from);
    }

    public static SqlWrapper delete(Class<?> from) {
        return new SqlWrapper().setDeleteFrom(from);
    }

    public <T, R> SqlWrapper setNull(SerializableFunction<T, R> property) {
        return set(property, null);
    }

    public <T, R> SqlWrapper set(SerializableFunction<T, R> property, Object value) {
        updatePropertyValueMap.put(property, value);
        return this;
    }

    public <T, R> SqlWrapper where(SerializableFunction<T, R> property, Option option, Object value) {
        return add(new Expression(property, option, value));
    }

    public <T, R> SqlWrapper where(SerializableFunction<T, R> property, Option option) {
        return add(new Expression(property, option));
    }

    private SqlWrapper add(Expression expression) {
        this.next.add(expression);
        return this;
    }

    public SqlWrapper or() {
        this.next = Condition.or();
        this.conditions.add(next);
        return this;
    }

    public SqlWrapper and() {
        this.next = Condition.and();
        this.conditions.add(next);
        return this;
    }

    private static final String PARAMETER_NAME_PREFIX = "parameterMap";

    /**
     * 解包装，生成SQL和SQL对应的参数
     *
     * @return
     */
    public Result unwrap() {
        Result result = assembleSqlPrefix();
        MetaEntityClass metaEntityClass = result.getMetaEntityClass();
        Map<String, MetaEntityClass.ResultMapping> getMethodAndResultMappingMap = metaEntityClass.getGetMethodAndResultMappingMap();
        StringBuilder sql = new StringBuilder(result.getSql());

        List<String> conditionItems = new ArrayList<>();
        for (int i = 0; i < conditions.size(); i++) {
            Condition condition = conditions.get(i);

            List<Expression> expressions = condition.getExpressions();
            List<String> expressionItems = new ArrayList<>(expressions.size());
            for (int j = 0; j < expressions.size(); j++) {
                Expression expression = expressions.get(j);
                Option option = expression.getOption();
                Object value = expression.getValue();

                MetaEntityClass.ResultMapping resultMapping = getMethodAndResultMappingMap.get(MetaLambdaCache.get(expression.getProperty()).getImplMethodName());
                String column = resultMapping.getColumn();
                String property = resultMapping.getProperty();

                // 参数名称
                String key = Joiner.on("_").join(i, j, property);
                // 参数占位 #{key}
                String placeholder = toPlaceholder(key);

                if (option == Option.between) {
                    List values = (List) value;
                    Object low = values.get(0);
                    Object high = values.get(1);
                    String lowKey = key + "_low";
                    String highKey = key + "_high";
                    result.addParameter(lowKey, low);
                    result.addParameter(highKey, high);
                    expressionItems.add(Joiner.on(" ").join(column, option.format(toPlaceholder(lowKey), toPlaceholder(highKey))));
                    continue;
                }

                result.addParameter(key, value);

                if (option == Option.in) {
                    expressionItems.add(Joiner.on(" ").join(column, IntStream.range(0, ((Collection<?>) value).size()).mapToObj(v -> toPlaceholder(key + ".[" + v + "]")).collect(Collectors.joining(","))));
                    continue;
                }

                if (option == Option.isNull || option == Option.isNotNull) {
                    expressionItems.add(Joiner.on(" ").join(column, option.getValue()));
                    continue;
                }

                expressionItems.add(Joiner.on(" ").join(column, option.format(placeholder)));
            }

            String join = condition.getJoin().name();
            conditionItems.add((conditionItems.isEmpty() ? "where" : join) + " (" + Joiner.on(" " + join + " ").join(expressionItems) + ")");
        }

        if (!conditionItems.isEmpty()) {
            sql.append(" ").append(Joiner.on(" ").join(conditionItems));
        }

        return result.setSql(sql.toString());
    }

    /**
     * 组装SQL前缀
     *
     * @return
     */
    private Result assembleSqlPrefix() {
        Result result = new Result();

        // select
        if (Objects.nonNull(selectProperties)) {
            List<MetaLambda> lambdas = selectProperties.stream().map(MetaLambdaCache::get).collect(Collectors.toList());
            MetaEntityClass metaEntityClass = Objects.nonNull(selectFrom) ? MetaCache.getMetaEntityClass(selectFrom) : MetaCache.getMetaEntityClass(lambdas.get(0).getImplClass().replace("/", "."));
            Map<String, MetaEntityClass.ResultMapping> getMethodAndResultMappingMap = metaEntityClass.getGetMethodAndResultMappingMap();
            String columns = lambdas.stream().map(v -> getMethodAndResultMappingMap.get(v.getImplMethodName()).getColumn()).collect(Collectors.joining(", "));
            return result.setSql("select " + columns + " from " + metaEntityClass.getTable()).setMetaEntityClass(metaEntityClass);
        }

        // update
        if (Objects.nonNull(updatePropertyValueMap)) {
            MetaEntityClass meta = Objects.nonNull(updateFrom) ? MetaCache.getMetaEntityClass(updateFrom) : MetaCache.getMetaEntityClass(MetaLambdaCache.get(updatePropertyValueMap.keySet().iterator().next()).getImplClass().replace("/", "."));
            Map<String, MetaEntityClass.ResultMapping> getMethodAndResultMappingMap = meta.getGetMethodAndResultMappingMap();

            List<String> sets = new ArrayList<>();
            for (Map.Entry<SerializableFunction, Object> entry : updatePropertyValueMap.entrySet()) {
                MetaLambda lambda = MetaLambdaCache.get(entry.getKey());
                MetaEntityClass.ResultMapping resultMapping = getMethodAndResultMappingMap.get(lambda.getImplMethodName());
                sets.add(resultMapping.getColumn() + "=" + toPlaceholder(resultMapping.getProperty()));
                result.addParameter(resultMapping.getProperty(), entry.getValue());
            }
            return result.setSql("update " + meta.getTable() + " set " + Joiner.on(", ").join(sets)).setMetaEntityClass(meta);
        }

        // delete
        MetaEntityClass meta = MetaCache.getMetaEntityClass(deleteFrom);
        return result.setSql("delete from " + meta.getTable()).setMetaEntityClass(meta);
    }

    private String toPlaceholder(String key) {
        return "#{" + PARAMETER_NAME_PREFIX + "." + key + "}";
    }

    @Getter
    public static class Result {
        private String sql;
        private Map<String, Object> parameterMap;
        private MetaEntityClass metaEntityClass;

        private Result() {
            this.parameterMap = Maps.newHashMap();
        }

        private Result setSql(String sql) {
            this.sql = sql;
            return this;
        }

        private Result setMetaEntityClass(MetaEntityClass metaEntityClass) {
            this.metaEntityClass = metaEntityClass;
            return this;
        }

        private Result addParameter(String key, Object value) {
            parameterMap.put(key, value);
            return this;
        }
    }
}

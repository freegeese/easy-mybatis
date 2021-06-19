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
 * SQL包装器，面向实体的SQL操作
 * <P> 提供面向实体属性的 select,update,delete 操作
 * 提示：复杂的SQL请不要使用此类进行编写，最好写在XML中，以便于后期维护
 *
 * @author zhangguangyong
 * @since 1.0
 */
public class SqlWrapper {
    /**
     * 查询表
     */
    private Class<?> selectFrom;

    /**
     * 查询列
     */
    private List<SerializableFunction> selectProperties;

    /**
     * 更新表
     */
    private Class<?> updateFrom;

    /**
     * 更新列
     */
    private Map<SerializableFunction, Object> updatePropertyValueMap;

    /**
     * 删除表
     */
    private Class<?> deleteFrom;

    /**
     * 条件组集合
     */
    private List<ConditionGroup> conditionGroups;

    /**
     * 下一个条件组
     */
    private ConditionGroup nextConditionGroup;

    public SqlWrapper() {
        this.conditionGroups = new ArrayList<>();
        this.andGroup();
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

    /**
     * 更新操作
     *
     * @return
     */
    public static SqlWrapper update() {
        return new SqlWrapper().setUpdatePropertyValueMap(Maps.newHashMap());
    }

    /**
     * 更新操作
     *
     * @param from 类
     * @return
     */
    public static SqlWrapper update(Class<?> from) {
        return update().setUpdateFrom(from);
    }

    /**
     * 删除操作
     *
     * @param from 类
     * @return
     */
    public static SqlWrapper delete(Class<?> from) {
        return new SqlWrapper().setDeleteFrom(from);
    }

    /**
     * 更新操作，设置列为null
     *
     * @param property
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper setNull(SerializableFunction<T, R> property) {
        return set(property, null);
    }

    /**
     * 更新操作，设置列值
     *
     * @param property
     * @param value
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper set(SerializableFunction<T, R> property, Object value) {
        updatePropertyValueMap.put(property, value);
        return this;
    }

    /**
     * 过滤条件
     *
     * @param property
     * @param option
     * @param value
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper where(SerializableFunction<T, R> property, Option option, Object value) {
        return and(new Expression(property, option, value));
    }

    /**
     * 过滤条件
     *
     * @param property
     * @param option
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper where(SerializableFunction<T, R> property, Option option) {
        return and(new Expression(property, option));
    }

    /**
     * and 过滤条件
     *
     * @param property
     * @param option
     * @param value
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper and(SerializableFunction<T, R> property, Option option, Object value) {
        return and(new Expression(property, option, value));
    }

    /**
     * and 过滤条件
     *
     * @param property
     * @param option
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper and(SerializableFunction<T, R> property, Option option) {
        return and(new Expression(property, option));
    }

    /**
     * or 过滤条件
     *
     * @param property
     * @param option
     * @param value
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper or(SerializableFunction<T, R> property, Option option, Object value) {
        return or(new Expression(property, option, value));
    }

    /**
     * or 过滤条件
     *
     * @param property
     * @param option
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper or(SerializableFunction<T, R> property, Option option) {
        return or(new Expression(property, option));
    }

    /**
     * 一组 or 过滤条件
     *
     * @param property
     * @param option
     * @param value
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper orGroup(SerializableFunction<T, R> property, Option option, Object value) {
        return orGroup().or(new Expression(property, option, value));
    }

    /**
     * 一组 or 过滤条件
     *
     * @param property
     * @param option
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper orGroup(SerializableFunction<T, R> property, Option option) {
        return orGroup().or(new Expression(property, option));
    }

    /**
     * 一组 and 过滤条件
     *
     * @param property
     * @param option
     * @param value
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper andGroup(SerializableFunction<T, R> property, Option option, Object value) {
        return andGroup().and(new Expression(property, option, value));
    }

    /**
     * 一组 and 过滤条件
     *
     * @param property
     * @param option
     * @param <T>
     * @param <R>
     * @return
     */
    public <T, R> SqlWrapper andGroup(SerializableFunction<T, R> property, Option option) {
        return andGroup().and(new Expression(property, option));
    }

    private SqlWrapper and(Expression expression) {
        this.nextConditionGroup.add(Condition.and(expression));
        return this;
    }

    private SqlWrapper or(Expression expression) {
        this.nextConditionGroup.add(Condition.and(expression));
        return this;
    }


    private SqlWrapper orGroup() {
        return addGroup(ConditionGroup.or());
    }

    private SqlWrapper andGroup() {
        return addGroup(ConditionGroup.and());
    }

    private SqlWrapper addGroup(ConditionGroup group) {
        this.nextConditionGroup = group;
        this.conditionGroups.add(nextConditionGroup);
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


        List<String> groupSqls = new ArrayList<>();
        for (int i = 0; i < conditionGroups.size(); i++) {
            // 一组条件
            ConditionGroup group = conditionGroups.get(i);
            List<Condition> conditions = group.getConditions();
            List<String> conditionSqls = new ArrayList<>(conditions.size());

            for (int j = 0; j < conditions.size(); j++) {
                Condition condition = conditions.get(j);
                Expression expression = condition.getExpression();
                Join join = condition.getJoin();

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
                    conditionSqls.add(Joiner.on(" ").join(column, option.format(toPlaceholder(lowKey), toPlaceholder(highKey))));
                    continue;
                }

                result.addParameter(key, value);

                if (option == Option.in) {
                    appendSql(conditionSqls, join, Joiner.on(" ").join(column, IntStream.range(0, ((Collection<?>) value).size()).mapToObj(v -> toPlaceholder(key + ".[" + v + "]")).collect(Collectors.joining(","))));
                    continue;
                }

                if (option == Option.isNull || option == Option.isNotNull) {
                    appendSql(conditionSqls, join, Joiner.on(" ").join(column, option.getValue()));
                    continue;
                }

                appendSql(conditionSqls, join, Joiner.on(" ").join(column, option.format(placeholder)));
            }

            // 记录每一组条件
            String groupSql = Joiner.on(" ").join(conditionSqls);
            if (groupSqls.isEmpty()) {
                groupSqls.add(groupSql);
                continue;
            }
            groupSqls.add(group.getJoin().name() + " (" + groupSql + ")");
        }

        if (!groupSqls.isEmpty()) {
            sql.append(" where ").append(Joiner.on(" ").join(groupSqls));
        }

        return result.setSql(sql.toString());
    }

    private void appendSql(List<String> sqls, Join join, String sql) {
        sqls.add(sqls.isEmpty() ? sql : join.name() + " " + sql);
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

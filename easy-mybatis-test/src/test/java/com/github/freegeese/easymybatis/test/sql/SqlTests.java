package com.github.freegeese.easymybatis.test.sql;

import com.alibaba.fastjson.JSON;
import com.github.freegeese.easymybatis.meta.MetaCache;
import com.github.freegeese.easymybatis.meta.MetaEntityClass;
import com.github.freegeese.easymybatis.test.db1.domain.User;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SqlTests {

    public static class Wrapper {
        @SneakyThrows
        public static <T, R> String select(SerializableFunction<T, R>... functions) {
            // com.github.freegeese.easymybatis.test.sql.WrapperTests$$Lambda$268/2109874862
            // class com.github.freegeese.easymybatis.test.sql.WrapperTests$$Lambda$268/1361289747
            // class com.github.freegeese.easymybatis.test.sql.WrapperTests$$Lambda$269/999609945
            for (SerializableFunction<T, R> function : functions) {
                System.out.println(function.getClass().getName());
                Method method = function.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                SerializedLambda lambda = (SerializedLambda) method.invoke(function);
                System.out.println(JSON.toJSONString(lambda, true));
            }

            return null;
        }
    }

    @Test
    void test2() {
        SqlWrapper
                .select(User::getName)
                .where(User::getName, SqlWrapper.Option.eq, "zhangsan")
                .where(User::getCreatedDate, SqlWrapper.Option.ge, "2020-12-31")
                .or()
                .where(User::getPhone, SqlWrapper.Option.between.not(), Arrays.asList("133", "135"))
                .where(User::getName, SqlWrapper.Option.between.not(), Arrays.asList("a", "b"))
                .build();
    }

    static class SqlWrapper {
        private Class selectFrom;
        private List<SerializableFunction> selectProperties;

        private Class updateFrom;
        private Map<SerializableFunction, Object> updatePropertyValueMap;

        private Class deleteFrom;

        private SqlWrapper(Class deleteFrom) {
            this.deleteFrom = deleteFrom;
        }

        private SqlWrapper(Class selectFrom, List<SerializableFunction> selectProperties) {
            this.selectFrom = selectFrom;
            this.selectProperties = selectProperties;
        }

        private SqlWrapper(List<SerializableFunction> selectProperties) {
            this.selectProperties = selectProperties;
        }

        private SqlWrapper(Class updateFrom, Map<SerializableFunction, Object> updatePropertyValueMap) {
            this.updateFrom = updateFrom;
            this.updatePropertyValueMap = updatePropertyValueMap;
        }

        private SqlWrapper(Map<SerializableFunction, Object> updatePropertyValueMap) {
            this.updatePropertyValueMap = updatePropertyValueMap;
        }

        public static <T, R> WhereBuilder select(SerializableFunction<T, R>... properties) {
            return new WhereBuilder(new SqlWrapper(Arrays.asList(properties)));
        }


        static class WhereBuilder {
            private SqlWrapper wrapper;
            private List<Condition> conditions;
            private Condition next;

            public WhereBuilder(SqlWrapper wrapper) {
                this.wrapper = wrapper;
                this.conditions = new ArrayList<>();
                this.and();
            }

            public <T, R> WhereBuilder where(SerializableFunction<T, R> property, Option option, Object value) {
                return add(new Expression(property, option, value));
            }

            public <T, R> WhereBuilder where(SerializableFunction<T, R> property, Option option) {
                return add(new Expression(property, option));
            }

            private WhereBuilder add(Expression expression) {
                this.next.add(expression);
                return this;
            }

            public WhereBuilder or() {
                this.next = Condition.or();
                this.conditions.add(next);
                return this;
            }

            public WhereBuilder and() {
                this.next = Condition.and();
                this.conditions.add(next);
                return this;
            }

            public void build() {
//                Map<String, Object> updateParameterMap = Maps.newHashMap();
//                Map<SerializableFunction, Object> updatePropertyValueMap = wrapper.updatePropertyValueMap;
//                MetaEntityClass meta = MetaCache.getMetaEntityClass(MetaLambdaCache.get(updatePropertyValueMap.keySet().iterator().next()).getImplClass().replace("/", "."));
//
//                for (Map.Entry<SerializableFunction, Object> entry : updatePropertyValueMap.entrySet()) {
//                    MetaLambda lambda = MetaLambdaCache.get(entry.getKey());
//                    lambda.getImplMethodName();
//                }


                List<MetaLambda> lambdas = wrapper.selectProperties.stream().map(MetaLambdaCache::get).collect(Collectors.toList());
                String className = lambdas.get(0).getImplClass().replace("/", ".");
                MetaEntityClass metaEntityClass = MetaCache.getMetaEntityClass(className);
                Map<String, MetaEntityClass.ResultMapping> getMethodAndResultMappingMap = metaEntityClass.getGetMethodAndResultMappingMap();

                String table = metaEntityClass.getTable();
                String columns = lambdas.stream().map(v -> getMethodAndResultMappingMap.get(v.getImplMethodName()).getColumn()).collect(Collectors.joining(","));
                StringBuilder sql = new StringBuilder("select " + columns + " from " + table);
                Map<String, Object> parameterMap = Maps.newHashMap();

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
                            parameterMap.put(lowKey, low);
                            parameterMap.put(highKey, high);
                            expressionItems.add(Joiner.on(" ").join(column, option.format(toPlaceholder(lowKey), toPlaceholder(highKey))));
                            continue;
                        }

                        parameterMap.put(key, value);

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
                    sql.append(" " + Joiner.on(" ").join(conditionItems));
                }
                System.out.println(sql);
                System.out.println(JSON.toJSONString(parameterMap, true));
            }

            private String toPlaceholder(String key) {
                return "#{" + key + "}";
            }
        }

        @Getter
        static class Expression {
            private SerializableFunction property;
            private Option option;
            private Object value;

            public Expression(SerializableFunction property, Option option, Object value) {
                this(property, option);
                this.value = value;
            }

            public Expression(SerializableFunction property, Option option) {
                this.property = property;
                this.option = option;
            }
        }

        @Getter
        static class Condition {
            private List<Expression> expressions;
            private Join join;

            public static Condition or() {
                return create(Join.or);
            }

            public static Condition and() {
                return create(Join.and);
            }

            private static Condition create(Join join) {
                Condition condition = new Condition();
                condition.join = join;
                condition.expressions = new ArrayList<>();
                return condition;
            }

            public Condition add(Expression expression) {
                this.expressions.add(expression);
                return this;
            }

            public Condition add(Collection<Expression> expressions) {
                this.expressions.addAll(expressions);
                return this;
            }
        }

        enum Join {
            and,
            or
        }

        enum Option {
            eq("= %s"),
            ne("!= %s"),

            gt("> %s"),
            ge(">= %s"),

            lt("< %s"),
            le("<= %s"),

            in("in (%s)"),

            like("like %s"),
            startLike("like concat(%s,'%')"),
            endLike("like concat('%',%s)"),
            fullLike("like concat('%',%s,'%')"),

            between("between %s and %s"),

            isNull("is null"),
            isNotNull("is not null");


            private String value;

            Option(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

            public Option not() {
                this.value = "not " + this.value;
                return this;
            }


            public String format(Object... args) {
                return String.format(getValue(), args);
            }
        }
    }

    interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
    }
}

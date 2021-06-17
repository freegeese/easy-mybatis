package com.github.freegeese.easymybatis.test.sql;

import com.alibaba.fastjson.JSON;
import com.github.freegeese.easymybatis.meta.MetaCache;
import com.github.freegeese.easymybatis.meta.MetaEntityClass;
import com.github.freegeese.easymybatis.test.db1.domain.User;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        SqlWrapper.select(User::getName).where(User::getName, SqlWrapper.Option.eq, "zhangsan").build();
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
                List<MetaLambda> lambdas = wrapper.selectProperties.stream().map(MetaLambdaCache::get).collect(Collectors.toList());
                String className = lambdas.get(0).getImplClass().replace("/", ".");
                MetaEntityClass metaEntityClass = MetaCache.getMetaEntityClass(className);
                Map<String, MetaEntityClass.ResultMapping> getMethodAndResultMappingMap = metaEntityClass.getGetMethodAndResultMappingMap();

                String table = metaEntityClass.getTable();
                String columns = lambdas.stream().map(v -> getMethodAndResultMappingMap.get(v.getImplMethodName()).getColumn()).collect(Collectors.joining(","));
                String sql = "select " + columns + " from " + table;
                System.out.println(sql);
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
            eq("="),
            ne("!="),
            isNull("is null");


            private final String value;

            Option(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }
    }

    interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
    }
}

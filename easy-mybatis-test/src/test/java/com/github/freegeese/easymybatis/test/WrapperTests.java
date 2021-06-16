package com.github.freegeese.easymybatis.test;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

public class WrapperTests {

    public static class Wrapper<T> {
        @SneakyThrows
        public <R> String select(SerializableFunction<T, R>... functions) {
            // class com.github.freegeese.easymybatis.test.WrapperTests$$Lambda$268/1361289747
            // class com.github.freegeese.easymybatis.test.WrapperTests$$Lambda$269/999609945
            for (SerializableFunction<T, R> function : functions) {
                System.out.println(function.getClass());
                Method method = function.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                SerializedLambda lambda = (SerializedLambda) method.invoke(function);
                System.out.println(JSON.toJSONString(lambda, true));
            }
            return null;
        }
    }

    static class EntitySqlWrapper<T> {
        @SneakyThrows
        public static <T, R> EntitySqlWrapper<T> select(SerializableFunction<T, R>... functions) {
            return new EntitySqlWrapper<T>();
        }
    }

    @Test
    void test() {


    }


    interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
    }
}

package com.github.freegeese.easymybatis.core.meta;

import lombok.Getter;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

@Getter
public class MetaLambda {
    private String implClass;
    private String implMethodName;

    public static MetaLambda forFunction(Function function) {
        if (!(function instanceof Serializable)) {
            throw new RuntimeException("函数接口必须继承 Serializable 接口");
        }

        try {
            Method method = function.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) method.invoke(function);
            MetaLambda meta = new MetaLambda();
            meta.implClass = lambda.getImplClass();
            meta.implMethodName = lambda.getImplMethodName();
            return meta;
        } catch (Exception e) {
            throw new RuntimeException("解析 Function(" + function + ") 失败");
        }
    }
}

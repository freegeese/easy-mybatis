package com.github.freegeese.easymybatis.criterion;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 可序列化的函数，用于可以获取函数元数据{@link java.lang.invoke.SerializedLambda}
 *
 * @param <T> 输入类型
 * @param <R> 返回类型
 * @author zhangguangyong
 * @since 1.0
 */
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}

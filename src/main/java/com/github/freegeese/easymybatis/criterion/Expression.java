package com.github.freegeese.easymybatis.criterion;

import lombok.Getter;

/**
 * SQL 条件表达式
 * <p> 例如：where name = 'xiaoming' 其中，name = 'xiaoming' 就是一个条件表达式
 *
 * @author zhangguangyong
 * @see Condition
 * @since 1.0
 */
@Getter
public class Expression {
    /**
     * 对应实体属性
     */
    private SerializableFunction property;
    /**
     * 关系运算
     */
    private Option option;
    /**
     * 值
     */
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

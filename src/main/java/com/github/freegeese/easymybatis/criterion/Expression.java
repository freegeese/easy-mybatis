package com.github.freegeese.easymybatis.criterion;

import lombok.Getter;

/**
 * 条件表达式
 */
@Getter
public class Expression {
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

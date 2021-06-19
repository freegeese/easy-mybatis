package com.github.freegeese.easymybatis.criterion;

import lombok.Getter;

/**
 * SQL的一个条件语句
 * <p> 跟在 where 后面的一个and或者or条件
 *
 * @author zhangguangyong
 * @see ConditionGroup
 * @since 1.0
 */
@Getter
public class Condition {
    private Expression expression;
    private Join join;

    public static Condition or(Expression expression) {
        return new Condition(expression, Join.or);
    }

    public static Condition and(Expression expression) {
        return new Condition(expression, Join.and);
    }

    private Condition(Expression expression, Join join) {
        this.expression = expression;
        this.join = join;
    }
}
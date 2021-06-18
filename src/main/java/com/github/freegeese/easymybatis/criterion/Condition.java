package com.github.freegeese.easymybatis.criterion;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 条件
 */
@Getter
public class Condition {
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
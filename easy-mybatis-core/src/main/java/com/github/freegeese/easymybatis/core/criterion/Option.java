package com.github.freegeese.easymybatis.core.criterion;

import com.google.common.base.Strings;

/**
 * SQL 条件运算符
 *
 * @author zhangguangyong
 * @see Expression
 * @since 1.0
 */
public enum Option {
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
        return Strings.lenientFormat(getValue(), args);
    }
}

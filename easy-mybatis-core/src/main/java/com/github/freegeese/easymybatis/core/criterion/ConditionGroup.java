package com.github.freegeese.easymybatis.core.criterion;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SQL的一个条件语句
 * <p> 跟在 where 后面的一个and(一组条件) 或者or (一组条件) ，例如：select * from user where (name = 'zs' and age > 10) or (name = 'ls' and age > 18)
 *
 * @author zhangguangyong
 * @see Condition
 * @since 1.0
 */
@Getter
public class ConditionGroup {
    private List<Condition> conditions;
    private Join join;

    public static ConditionGroup or() {
        return create(Join.or);
    }

    public static ConditionGroup and() {
        return create(Join.and);
    }

    private static ConditionGroup create(Join join) {
        ConditionGroup group = new ConditionGroup();
        group.join = join;
        group.conditions = new ArrayList<>();
        return group;
    }

    public ConditionGroup add(Condition condition) {
        this.conditions.add(condition);
        return this;
    }

    public ConditionGroup add(Collection<Condition> conditions) {
        this.conditions.addAll(conditions);
        return this;
    }
}
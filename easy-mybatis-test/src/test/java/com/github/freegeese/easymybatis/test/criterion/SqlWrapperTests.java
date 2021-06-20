package com.github.freegeese.easymybatis.test.criterion;

import com.github.freegeese.easymybatis.core.criterion.Option;
import com.github.freegeese.easymybatis.core.criterion.SqlWrapper;
import com.github.freegeese.easymybatis.test.db1.domain.User;
import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

public class SqlWrapperTests {
    @Test
    void testSelect() {
        SqlWrapper wrapper = SqlWrapper
                .select(User::getName, User::getPhone)
                .where(User::getName, Option.fullLike.not(), "zhangsan");
        SqlWrapper.Result result = wrapper.unwrap();
        System.out.println(result);
    }

    @Test
    void test2() {
        System.out.println(Strings.lenientFormat(Option.fullLike.getValue(), "sdf"));
    }
}

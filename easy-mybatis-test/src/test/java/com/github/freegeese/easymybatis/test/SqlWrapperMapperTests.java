package com.github.freegeese.easymybatis.test;

import com.github.freegeese.easymybatis.criterion.Option;
import com.github.freegeese.easymybatis.criterion.SqlWrapper;
import com.github.freegeese.easymybatis.test.db1.domain.User;
import com.github.freegeese.easymybatis.test.db1.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SqlWrapperMapperTests {

    @Autowired
    UserMapper mapper;

    @Test
    void testSelect() {
        mapper.selectByWrapper(
                SqlWrapper
                        .select(User::getId, User::getName, User::getPhone)
                        .where(User::getId, Option.eq, 1)
                        .or()
                        .where(User::getPhone, Option.fullLike, "133")
                        .unwrap()
        );
    }

    @Test
    void testUpdate() {
        mapper.updateByWrapper(
                SqlWrapper
                        .update()
                        .set(User::getName, "lisi")
                        .set(User::getPhone, "123")
                        .setNull(User::getCreatedDate)
                        .where(User::getId, Option.eq, 1)
                        .unwrap()
        );
    }

    @Test
    void testDelete() {
        mapper.updateByWrapper(
                SqlWrapper
                        .delete(User.class)
                        .where(User::getId, Option.eq, 1)
                        .unwrap()
        );
    }
}

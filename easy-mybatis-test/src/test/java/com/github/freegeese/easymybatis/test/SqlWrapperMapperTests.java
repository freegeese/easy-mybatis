package com.github.freegeese.easymybatis.test;

import com.github.freegeese.easymybatis.core.criterion.Option;
import com.github.freegeese.easymybatis.core.criterion.SqlWrapper;
import com.github.freegeese.easymybatis.test.db1.mapper.UserMapper;
import com.github.freegeese.easymybatis.test.db1.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SqlWrapperMapperTests {

    @Autowired
    UserMapper mapper;

    @Test
    void testSelect() {
        List<User> users = mapper.selectByWrapper(
                SqlWrapper
                        .select(User::getId, User::getName, User::getPhone)
                        .where(User::getId, Option.gt, 1000)
                        .orGroup(User::getPhone, Option.fullLike, "133")
                        .and(User::getName, Option.fullLike.not(), "张")
                        .unwrap()
        );
        for (User user : users) {
            System.out.println(user.getName());
        }
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

package com.github.freegeese.easymybatis.test;

import com.github.freegeese.easymybatis.test.domain.User;
import com.github.freegeese.easymybatis.test.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

@SpringBootTest
class UserServiceTests {
    @Autowired
    UserService service;

    @Test
    void testInsert() {
        User user = new User();
        user.setName("张三");
        user.setPhone("13380383333");
        service.insert(user);
        assert Objects.nonNull(user.getId());
    }

    @Test
    void testSelectAll() {
        assert !service.selectAll().isEmpty();
    }
}

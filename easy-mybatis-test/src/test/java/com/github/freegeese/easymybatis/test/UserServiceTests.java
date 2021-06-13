package com.github.freegeese.easymybatis.test;

import com.github.freegeese.easymybatis.test.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceTests {
    @Autowired
    UserService service;

    @Test
    void testSelectAll() {
        assert !service.selectAll().isEmpty();
    }
}

package com.github.freegeese.easymybatis.test;

import com.alibaba.fastjson.JSON;
import com.github.freegeese.easymybatis.test.db1.domain.User;
import com.github.freegeese.easymybatis.test.db1.mapper.UserMapper;
import com.github.freegeese.easymybatis.test.db1.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Objects;

@SpringBootTest
class UserServiceTests {
    @Autowired
    UserService service;

    @Autowired
    UserMapper mapper;

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

    @Test
    void testSelectOne() {
        System.out.println(JSON.toJSONString(mapper.selectByPrimaryKeys(Arrays.asList(1, 2, 3))));
    }
}
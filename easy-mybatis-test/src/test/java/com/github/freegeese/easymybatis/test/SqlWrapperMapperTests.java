package com.github.freegeese.easymybatis.test;

import com.alibaba.fastjson.JSON;
import com.github.freegeese.easymybatis.criterion.Option;
import com.github.freegeese.easymybatis.criterion.SqlWrapper;
import com.github.freegeese.easymybatis.test.db1.domain.User;
import com.github.freegeese.easymybatis.test.db1.mapper.UserMapper;
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
        List<User> users = mapper.selectByWrapper(SqlWrapper.select(User::getName).where(User::getId, Option.eq, 1).unwrap());
        System.out.println(JSON.toJSONString(users, true));
    }
}

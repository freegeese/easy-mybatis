package com.github.freegeese.easymybatis.test;

import com.github.freegeese.easymybatis.test.db2.domain.Account;
import com.github.freegeese.easymybatis.test.db2.mapper.AccountMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccountServicesTests {
    @Autowired
    AccountMapper mapper;

    @Test
    void testInsert() {
        Account account = new Account();
        account.setUsername("zhangsan");
        account.setPassword("123456");
        assert mapper.insert(account) > 0;
    }
}

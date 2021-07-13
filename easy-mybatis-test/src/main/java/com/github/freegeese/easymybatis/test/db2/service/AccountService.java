package com.github.freegeese.easymybatis.test.db2.service;

import com.github.freegeese.easymybatis.spring.service.BaseService;
import com.github.freegeese.easymybatis.test.db2.mapper.AccountMapper;
import com.github.freegeese.easymybatis.test.db2.model.Account;
import org.springframework.stereotype.Service;

@Service
public class AccountService extends BaseService<Account, AccountMapper> {
}
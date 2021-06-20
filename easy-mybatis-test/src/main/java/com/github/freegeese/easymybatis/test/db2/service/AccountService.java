package com.github.freegeese.easymybatis.test.db2.service;

import com.github.freegeese.easymybatis.spring.service.BaseService;
import com.github.freegeese.easymybatis.test.db2.domain.Account;
import com.github.freegeese.easymybatis.test.db2.mapper.AccountMapper;
import org.springframework.stereotype.Service;

@Service
public class AccountService extends BaseService<Account, AccountMapper> {
}
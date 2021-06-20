package com.github.freegeese.easymybatis.test.db1.service;

import com.github.freegeese.easymybatis.spring.service.BaseService;
import com.github.freegeese.easymybatis.test.db1.domain.User;
import com.github.freegeese.easymybatis.test.db1.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService<User, UserMapper> {
}
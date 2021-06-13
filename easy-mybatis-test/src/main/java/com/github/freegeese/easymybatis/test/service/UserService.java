package com.github.freegeese.easymybatis.test.service;

import com.github.freegeese.easymybatis.service.BaseService;
import com.github.freegeese.easymybatis.test.domain.User;
import com.github.freegeese.easymybatis.test.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService<User, UserMapper> {
    // com.github.freegeese.easymybatis.test.mapper
}
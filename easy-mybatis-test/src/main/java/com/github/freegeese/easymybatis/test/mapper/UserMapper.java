package com.github.freegeese.easymybatis.test.mapper;


import com.github.freegeese.easymybatis.mapper.BaseMapper;
import com.github.freegeese.easymybatis.test.domain.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {
}

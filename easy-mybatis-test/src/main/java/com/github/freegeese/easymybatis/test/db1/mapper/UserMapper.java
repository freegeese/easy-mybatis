package com.github.freegeese.easymybatis.test.db1.mapper;


import com.github.freegeese.easymybatis.core.mapper.BaseMapper;
import com.github.freegeese.easymybatis.test.db1.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {
}

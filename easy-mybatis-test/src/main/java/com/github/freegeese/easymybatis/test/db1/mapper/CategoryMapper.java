package com.github.freegeese.easymybatis.test.db1.mapper;

import com.github.freegeese.easymybatis.core.mapper.TreeableMapper;
import com.github.freegeese.easymybatis.test.db1.model.Category;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryMapper extends TreeableMapper<Category, Long> {
}
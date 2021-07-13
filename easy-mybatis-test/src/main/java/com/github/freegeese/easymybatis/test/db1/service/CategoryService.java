package com.github.freegeese.easymybatis.test.db1.service;

import com.github.freegeese.easymybatis.spring.service.TreeableService;
import com.github.freegeese.easymybatis.test.db1.mapper.CategoryMapper;
import com.github.freegeese.easymybatis.test.db1.model.Category;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends TreeableService<Category, CategoryMapper, Long> {
}
package com.github.freegeese.easymybatis.test;

import cn.binarywang.tools.generator.ChineseMobileNumberGenerator;
import cn.binarywang.tools.generator.ChineseNameGenerator;
import com.alibaba.fastjson.JSON;
import com.github.freegeese.easymybatis.domain.Pageable;
import com.github.freegeese.easymybatis.domain.Pagination;
import com.github.freegeese.easymybatis.test.db1.domain.User;
import com.github.freegeese.easymybatis.test.db1.service.UserService;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootTest
class UserServiceTests {
    @Autowired
    UserService service;

    ChineseNameGenerator nameGenerator = ChineseNameGenerator.getInstance();
    ChineseMobileNumberGenerator mobileNumberGenerator = ChineseMobileNumberGenerator.getInstance();

    @Test
    void testInsert() {
        assert service.insert(createUser()) == 1;
    }

    @Test
    void testInsertBatch() {
        int size = 1000;
        assert service.insertBatch(createUser(size)) == size;
    }

    @Test
    void testInsertBatchSelective() {
        int size = 1000;
        long beforeCount = service.selectCount();
        assert service.insertBatchSelective(createUser(size)) > 0;
        long afterCount = service.selectCount();
        assert beforeCount + size == afterCount;

    }

    @Test
    void testUpdate() {
        Pageable page = service.selectPage(Pagination.create(1, 1));
        User user = (User) page.getContent().get(0);
        user.setName(nameGenerator.generate());
        user.setPhone(mobileNumberGenerator.generate());
        assert service.updateByPrimaryKey(user) == 1;
    }

    @Test
    void testUpdateBatchSelective() {
        Pageable page = service.selectPage(Pagination.create(1, 1000));
        List<User> content = page.getContent();
        for (User user : content) {
            user.setName(nameGenerator.generate());
            user.setPhone(mobileNumberGenerator.generate());
        }
        assert service.updateBatchSelective(content) > 0;

        Map<Long, User> insertMap = content.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<User> updatedItems = service.selectByPrimaryKeys(content.stream().map(User::getId).collect(Collectors.toList()));
        assert updatedItems.stream().allMatch(v -> Objects.equals(v.getName(), insertMap.get(v.getId()).getName()) && Objects.equals(v.getPhone(), insertMap.get(v.getId()).getPhone()));
    }

    @Test
    void testSelectByEntity() {
        User entity = createUser();
        service.insertSelective(entity);
        // 这里会出现纳秒
        entity.setCreatedDate(null);
        assert !service.selectByEntity(entity).isEmpty();
    }

    @Test
    void testSelectByParameterMap() {
        Map<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("name", "like '%张%'");
        parameterMap.put("phone", "like '133%'");
        List<User> users = service.selectByParameterMap(parameterMap);
        System.out.println(JSON.toJSONString(users, true));
    }

    @Test
    void testSelectByPrimaryKeys() {
        List<User> users = service.selectPage(Pagination.create(1, 10)).getContent();
        List<User> users2 = service.selectByPrimaryKeys(users.stream().map(User::getId).collect(Collectors.toList()));
        assert users.size() == users2.size();
    }

    @Test
    void testDatetime() {
        Date date = DateTime.parse("2021-06-19 10:24:26", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        User user = new User();
        user.setCreatedDate(date);
        List<User> users = service.selectByEntity(user);
        System.out.println(users.size());
    }

    @Test
    void testSelectAll() {
        assert !service.selectAll().isEmpty();
    }

    @Test
    void testSelectCount() {
        assert service.selectCount() > 0;
    }

    private User createUser() {
        return createUser(1).get(0);
    }

    private List<User> createUser(int size) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            User user = new User();
            user.setName(nameGenerator.generate());
            user.setPhone(mobileNumberGenerator.generate());
            users.add(user);
        }
        return users;
    }
}

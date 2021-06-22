package com.github.freegeese.easymybatis.generator.test;

import com.github.freegeese.easymybatis.generator.mybatis.MybatisGenerator;
import com.github.freegeese.easymybatis.generator.util.IoUtils;
import org.junit.jupiter.api.Test;

/**
 * 代码生成测试
 */
public class MybatisGeneratorTests {
    ClassLoader classLoader = getClass().getClassLoader();

    @Test
    void test() {
        MybatisGenerator.create(load("generatorConfiguration.json")).generate();
    }

    String load(String filename) {
        return IoUtils.readString(classLoader.getResourceAsStream(filename));
    }
}

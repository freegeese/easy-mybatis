package com.github.freegeese.easymybatis.spring.boot.autoconfigure;

import com.google.common.base.CaseFormat;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = EasyMybatisProperties.EASY_MYBATIS_PREFIX)
public class EasyMybatisProperties {
    public static final String EASY_MYBATIS_PREFIX = "easy-mybatis";
    private CaseFormat javaCaseFormat;
    private CaseFormat databaseCaseFormat;
}

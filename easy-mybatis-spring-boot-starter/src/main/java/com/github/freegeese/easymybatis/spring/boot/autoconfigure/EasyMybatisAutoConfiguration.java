/*
 *    Copyright 2015-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.freegeese.easymybatis.spring.boot.autoconfigure;

import com.github.freegeese.easymybatis.core.EasyMybatisConfiguration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;
import java.util.Objects;


@org.springframework.context.annotation.Configuration
@ConditionalOnBean({SqlSessionFactory.class})
@EnableConfigurationProperties(EasyMybatisProperties.class)
public class EasyMybatisAutoConfiguration implements InitializingBean {

    private final EasyMybatisProperties properties;
    private final List<SqlSessionFactory> sqlSessionFactories;

    public EasyMybatisAutoConfiguration(EasyMybatisProperties properties, List<SqlSessionFactory> sqlSessionFactories) {
        this.properties = properties;
        this.sqlSessionFactories = sqlSessionFactories;
    }


    @Override
    public void afterPropertiesSet() {
        if (Objects.nonNull(sqlSessionFactories) && !sqlSessionFactories.isEmpty()) {
            EasyMybatisConfiguration configuration = new EasyMybatisConfiguration();
            if (Objects.nonNull(properties.getJavaCaseFormat())) {
                configuration.setJavaCaseFormat(properties.getJavaCaseFormat());
            }
            if (Objects.nonNull(properties.getDatabaseCaseFormat())) {
                configuration.setDatabaseCaseFormat(properties.getDatabaseCaseFormat());
            }

            for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
                configuration.init(sqlSessionFactory);
            }
        }
    }
}

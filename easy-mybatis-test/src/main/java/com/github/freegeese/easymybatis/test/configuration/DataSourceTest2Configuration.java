package com.github.freegeese.easymybatis.test.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.freegeese.easymybatis.core.interceptor.DateableInterceptor;
import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {DataSourceTest2Configuration.MAPPER_PACKAGE}, sqlSessionFactoryRef = "test2SqlSessionFactory")
public class DataSourceTest2Configuration {
    static final String MAPPER_PACKAGE = "com.github.freegeese.easymybatis.test.db2.mapper";
    private final String CONFIGURATION_PREFIX = "datasource.test2";
    private final String MAPPER_LOCATION_PATTERN = "classpath:" + MAPPER_PACKAGE.replace(".", "/") + "/**/*.xml";

    @Bean
    @ConfigurationProperties(prefix = CONFIGURATION_PREFIX)
    public DataSource test2DataSource() {
        return new DruidDataSource();
    }

    @Bean
    public DataSourceTransactionManager test2TransactionManager() {
        return createDataSourceTransactionManager((test2DataSource()));
    }

    @Bean
    public SqlSessionFactory test2SqlSessionFactory() {
        return createSqlSessionFactory(test2DataSource(), MAPPER_LOCATION_PATTERN);
    }

    /**
     * 创建事务管理器
     *
     * @param dataSource
     * @return
     */
    public DataSourceTransactionManager createDataSourceTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 创建数据库连接工厂
     *
     * @param dataSource
     * @param mapperLocationPattern
     * @return
     */
    public SqlSessionFactory createSqlSessionFactory(DataSource dataSource, String mapperLocationPattern) {
        try {
            SqlSessionFactoryBean sf = new SqlSessionFactoryBean();
            sf.setDataSource(dataSource);
            sf.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperLocationPattern));

            // 插件
            sf.setPlugins(new PageInterceptor(), new DateableInterceptor());

            // 默认属性
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            configuration.setCallSettersOnNulls(true);

            // 驼峰标识转换
            configuration.setMapUnderscoreToCamelCase(true);

            sf.setConfiguration(configuration);
            return sf.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}

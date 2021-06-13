package com.github.freegeese.easymybatis.test.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.freegeese.easymybatis.EasyMybatisConfiguration;
import com.github.freegeese.easymybatis.interceptor.DateableInterceptor;
import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {DataSourceTestConfiguration.MAPPER_PACKAGE}, sqlSessionFactoryRef = "testSqlSessionFactory")
public class DataSourceTestConfiguration {
    static final String MAPPER_PACKAGE = "com.github.freegeese.easymybatis.test.mapper";
    private final String CONFIGURATION_PREFIX = "datasource.test";
    private final String MAPPER_LOCATION_PATTERN = "classpath:" + MAPPER_PACKAGE.replace(".", "/") + "/**/*.xml";

    @Primary
    @Bean
    @ConfigurationProperties(prefix = CONFIGURATION_PREFIX)
    public DataSource testDataSource() {
        return createDataSource();
    }

    @Primary
    @Bean
    public DataSourceTransactionManager testTransactionManager() {
        return createDataSourceTransactionManager((testDataSource()));
    }

    @Primary
    @Bean
    public SqlSessionFactory testSqlSessionFactory() {
        return createSqlSessionFactory(testDataSource(), MAPPER_LOCATION_PATTERN);
    }

    /**
     * 创建数据源
     *
     * @return
     */
    public DataSource createDataSource() {
        return new DruidDataSource();
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
            SqlSessionFactory factory = sf.getObject();

            // 初始化
            new EasyMybatisConfiguration().init(factory);

            return factory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}

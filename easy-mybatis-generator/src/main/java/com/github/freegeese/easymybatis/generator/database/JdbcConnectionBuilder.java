package com.github.freegeese.easymybatis.generator.database;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Properties;

/**
 * 数据源构建
 */
@Slf4j
public final class JdbcConnectionBuilder {
    private Properties properties = new Properties();
    private String url;
    private String driver;

    public JdbcConnectionBuilder(String url, String driver) {
        this.url = url;
        this.driver = driver;
    }

    public static JdbcConnectionBuilder create(String url, String driver) {
        return new JdbcConnectionBuilder(url, driver);
    }

    public JdbcConnectionBuilder user(String user) {
        this.properties.put("user", user);
        return this;
    }

    public JdbcConnectionBuilder password(String password) {
        this.properties.put("password", password);
        return this;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public JdbcConnectionBuilder properties(Properties properties) {
        this.properties.putAll((Map) properties);
        return this;
    }

    public Connection builder() {
        try {
            Class.forName(driver);
            properties.setProperty("remarksReporting", "true");
            properties.setProperty("useInformationSchema", "true");
            log.info("JDBC Connection 配置参数 -> {}", JSON.toJSONString(properties, true));
            return DriverManager.getConnection(url, properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

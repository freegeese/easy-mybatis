# easy-mybatis

快速上手，简单使用

### 实现功能

 * 基础 BaseMapper<T> 通用基础方法(增删改查)
 * 基础 BaseService<T> 通用基础接口(增删改查+分页)
 * 基础 TreeableService<T> 通用层级结构操作接口(增删改查节点)

## 使用
```java
    // 配置对象
    EasyMybatisConfiguration configuration = new EasyMybatisConfiguration();
    // java命名格式
    configuration.setJavaCaseFormat(CaseFormat.LOWER_CAMEL);
    // 数据库命名格式
    configuration.setDatabaseCaseFormat(CaseFormat.LOWER_UNDERSCORE);
    // 传入 SqlSessionFactory 进行初始化
    configuration.init(sqlSessionFactory);
```

## 文档

- [wiki](https://github.com/freegeese/easy-mybatis/wiki)

## Maven

* 单独使用
```xml
<dependency>
    <groupId>com.github.freegeese</groupId>
    <artifactId>easy-mybatis</artifactId>
    <version>1.0</version>
</dependency>
```

* spring-boot-starter
```xml
<dependency>
    <groupId>com.github.freegeese</groupId>
    <artifactId>easy-mybatis-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```

# easy-mybatis

快速上手，简单使用

### 实现功能

 * 基础 BaseMapper<T> 通用基础方法(增删改查)
 * 基础 BaseService<T> 通用基础接口(增删改查+分页)
 * 基础 TreeableService<T> 通用层级结构操作接口(增删改查节点)

## 使用
[source,java,indent=0]
----
	// 创建 SqlSessionFactory
    SqlSessionFactory sqlSessionFactory = null;
    // 初始化配置
    EasyMybatisConfiguration configuration = new EasyMybatisConfiguration();
    // java命名格式
    configuration.setJavaCaseFormat(CaseFormat.LOWER_CAMEL);
    // 数据库命名格式
    configuration.setDatabaseCaseFormat(CaseFormat.LOWER_UNDERSCORE);
    configuration.init(sqlSessionFactory);
----

## 文档

- [wiki](https://github.com/freegeese/easy-mybatis/wiki)

## Maven

```xml
<dependency>
    <groupId>com.github.freegeese</groupId>
    <artifactId>easy-mybatis</artifactId>
    <version>1.0</version>
</dependency>
```


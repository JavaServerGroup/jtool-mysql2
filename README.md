# jtool-mysql2  
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Build Status](https://travis-ci.org/JavaServerGroup/jtool-mysql2.svg?branch=master)](https://travis-ci.org/JavaServerGroup/jtool-mysql2)
[![Coverage Status](https://coveralls.io/repos/github/JavaServerGroup/jtool-mysql2/badge.svg?branch=master)](https://coveralls.io/github/JavaServerGroup/jtool-mysql2?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ff8f22ab678248e2a7c12b26dd8fd8c3)](https://www.codacy.com/app/jiale-chan/jtool-mysql2?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=JavaServerGroup/jtool-mysql2&amp;utm_campaign=Badge_Grade)   
   
## Quick start:
### 第一步：引入repository到pom.xml
```xml
<repositories>
    <repository>
        <id>jtool-mvn-repository</id>
        <url>https://raw.github.com/JavaServerGroup/jtool-mvn-repository/master/releases</url>
    </repository>
</repositories>
```
### 第二步：添加dependency到pom.xml
```xml
<dependency>
    <groupId>com.jtool</groupId>
    <artifactId>jtool-mysql2</artifactId>
    <version>0.0.5</version>
</dependency>
```
### 第三步：dataSource配置（和平时spring jdbc一样）
```xml
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
    <property name="driverClassName" value="${jdbc.driverClassName}"/>
    <property name="url" value="${jdbc.url}"/>
    <property name="username" value="${jdbc.username}"/>
    <property name="password" value="${jdbc.password}"/>
</bean>
```
### 第四步：准备数据库表
```sql
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `age` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) 
```
### 第五步：建立pojo
```java
public class Users {
    private long id;
    private String name;
    private int age;
    ... 
    getter and setter
}
```
### 第五步：编写users表的DAO
```java
@Repository
@Table(tableName = "users")
@DataSource("dataSource")
public class UserDAO extends AbstractDAO<Users> {
}
```
注：AbstractDAO<Users>这里的<Users>是必须的。

### 第六步：直接使用AbstractDAO提供的方法
```java
public class MainApp {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"application-context.xml"}); 
	UserDAO userDAO = (UserDAO)context.getBean("userDAO");
		
	Users users = new Users();
	users.setAge(1);
	users.setName("Tim");
	userDAO.add(users);//直接添加
    }
}
```
## 接下来可以做什么？
* <a href="https://github.com/JavaServerGroup/jtool-mysql2/wiki/%E4%BA%86%E8%A7%A3select%E7%9A%84%E7%94%A8%E6%B3%95" target="_blank">了解select的用法</a>
* <a href="https://github.com/JavaServerGroup/jtool-mysql2/wiki/%E4%BA%86%E8%A7%A3where%E7%9A%84%E7%94%A8%E6%B3%95" target="_blank">了解where的用法</a>
* <a href="https://github.com/JavaServerGroup/jtool-mysql2/wiki/%E4%BA%86%E8%A7%A3limit%E7%9A%84%E7%94%A8%E6%B3%95" target="_blank">了解limit的用法</a>
* <a href="https://github.com/JavaServerGroup/jtool-mysql2/wiki/ORDERBY%E7%9A%84%E4%BD%BF%E7%94%A8" target="_blank">了解orderby的用法</a>
* <a href="https://github.com/JavaServerGroup/jtool-mysql2/wiki/COUNT%E7%9A%84%E7%94%A8%E6%B3%95" target="_blank">了解count的用法</a>
* <a href="https://github.com/JavaServerGroup/jtool-mysql2/wiki/DELETE%E7%9A%84%E7%94%A8%E6%B3%95" target="_blank">了解delete的用法</a>
* <a href="https://github.com/JavaServerGroup/jtool-mysql2/wiki/UPDATE%E7%9A%84%E7%94%A8%E6%B3%95" target="_blank">了解update的用法</a>
* <a href="https://github.com/JavaServerGroup/jtool-mysql2/wiki/%E6%89%B9%E9%87%8F%E6%B7%BB%E5%8A%A0%E7%9A%84%E7%94%A8%E6%B3%95" target="_blank">批量添加的用法</a>

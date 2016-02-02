# jtool-mysql2  [![Build Status](https://travis-ci.org/JavaServerGroup/jtool-mysql2.svg?branch=master)](https://travis-ci.org/JavaServerGroup/jtool-mysql2)[![Coverage Status](https://coveralls.io/repos/github/JavaServerGroup/jtool-mysql2/badge.svg?branch=master)](https://coveralls.io/github/JavaServerGroup/jtool-mysql2?branch=master) 
## 例子数据库表结构
```sql
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `age` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) 
```
## 例子users表的DAO
```java
@Repository
@Table(tableName = "users", primaryKeyName = "id")
@DataSource("dataSource")
public class UserDAO extends AbstractDAO {
	@Override
	protected RowMapper<?> makeRowMapperInstance() {
		return (rs, rowNum) -> {
            Users o = new Users();
            o.setId(rs.getInt("id"));
            o.setName(rs.getString("name"));
            o.setAge(rs.getInt("age"));
            return o;
    };
	}
}
```
## dataSource配置（和平时spring jdbc一样）
```xml
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
    <property name="driverClassName" value="${jdbc.driverClassName}"/>
    <property name="url" value="${jdbc.url}"/>
    <property name="username" value="${jdbc.username}"/>
    <property name="password" value="${jdbc.password}"/>
</bean>
```
### ADD的使用
```java
Users users = new Users();
users.setAge(1);
users.setName("Tim");
userDAO.add(users);//直接添加
long id = userDAO.addAndReturnKey(users);//返回生成的id
```
### SELECT的使用
```java
List<Users> result = userDAO.select().execAsList();
List<Map<String, Object>> result = userDAO.select().execAsRows();
```
```shell
+----+-----------+-----+  
| id | name      | age |  
+----+-----------+-----+  
|  1 | jialechan |   8 |  
|  2 | KKL       |  18 |  
|  3 | Ken       |  28 |  
+----+-----------+-----+    
```
### WHERE的使用
```java
List<Users> result = userDAO.select().where("age < ?", 20).execAsList();
List<Map<String, Object>> result = userDAO.select().where("age < ?", 20).execAsRows();
```
```shell
+----+-----------+-----+
| id | name      | age |
+----+-----------+-----+
|  1 | jialechan |   8 |
|  2 | KKL       |  18 |
+----+-----------+-----+
```
### SELECT特定用户
```java
Optional<Users> userFromDB = userDAO.select().where("id = ?", 1).execAsPojoOpt();
```
```shell
+----+-----------+-----+
| id | name      | age |
+----+-----------+-----+
|  1 | jialechan |   8 |
+----+-----------+-----+
```
### LIMIT的使用
```java
List<Users> result = userDAO.select().where("age < ?", 20).limit(0, 1).execAsList();
List<Map<String, Object>> result = userDAO.select().where("age < ?", 20).limit(0, 1).execAsRows();
```
```shell
+----+-----------+-----+
| id | name      | age |
+----+-----------+-----+
|  1 | jialechan |   8 |
+----+-----------+-----+
```
### ORDERBY的使用
```java
List<Users> result = userDAO.select().where("age < ?", 20).orderByDesc("id").limit(0, 1).execAsList();
List<Map<String, Object>> result = userDAO.select().where("age < ?", 20).orderByDesc("id").limit(0, 1).execAsRows();
```
```shell
+----+------+-----+
| id | name | age |
+----+------+-----+
|  2 | KKL  |  18 |
+----+------+-----+
```
### COUNT的用法
```java
int result = userDAO.select().count();
```
```shell
+----------+
| count(*) |
+----------+
|        3 |
+----------+
```
### DELETE的用法
```java
int deleted = userDAO.select().where("age > ?", 20).delete();
```
### UPDATE的用法
```java
String sql = "update " + userDAO.getTableName() + " set name = ? where id = ?";
int updated = userDAO.execUpdate(sql, "KKL2", 1);
```


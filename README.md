# jtool-mysql2  [![Build Status](https://travis-ci.org/JavaServerGroup/jtool-mysql2.svg?branch=master)](https://travis-ci.org/JavaServerGroup/jtool-mysql2)[![Coverage Status](https://coveralls.io/repos/github/JavaServerGroup/jtool-mysql2/badge.svg?branch=master)](https://coveralls.io/github/JavaServerGroup/jtool-mysql2?branch=master) 

<a href="https://github.com/JavaServerGroup/jtool-mysql2/wiki/%E4%BD%BF%E7%94%A8maven%E5%BC%95%E5%85%A5jtool-mysql2">使用maven引入jtool mysql2</a>   
<a href="https://github.com/JavaServerGroup/jtool-mysql2/wiki/Quick-start">Quick start</a>

## 批量添加
修改DAO
```java
@Repository
@Table(tableName = "users")
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

	public int[] batchUpdate(final List<Users> userses) {
		String sql = "insert into " + getTableName() + " (name, age) values(?, ?);";
		return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setString(1, userses.get(i).getName());
				ps.setInt(2, userses.get(i).getAge());
			}

			public int getBatchSize() {
				return userses.size();
			}
		});
	}

}
```
使用
```java
public class BatchUpdateTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Resource
	private UserDAO userDAO;

	@Test
	public void testBatchUpdate() {
		long begin = System.currentTimeMillis();
		List<Users> userses = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			userses.add(genUserPojo(0, i + "", i));
		}
		userDAO.batchUpdate(userses);
		long end = System.currentTimeMillis();
		System.out.println(end - begin);
		
		Assert.assertEquals(10003, userDAO.select().count());
	}

	private Users genUserPojo(int id, String name, int age) {
		Users users = new Users();
		users.setAge(age);
		users.setName(name);
		users.setId(id);
		return users;
	}

}
```

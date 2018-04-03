package com.test.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.*;

@ContextConfiguration(locations = "/testDB-config.xml")
public class UserDAOTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Resource
	private UserDAO userDAO;

	@Before
	public void before() {
		userDAO.add(new Users(){{
			setName("jialechan");
			setAge(8);
			setHeight(1.73);
		}});
		userDAO.add(new Users(){{
			setName("KKL");
			setAge(18);
		}});
		userDAO.add(new Users(){{
			setName("Ken");
			setAge(28);
		}});
	}

	@Test
	public void testIfWhere() {

		Users users = genUserPojo(1L, "jialechan", 8, 1.73);

		Optional<Users> userFromDB = userDAO.select().ifWhere(users.getName() != null, "name = ?", () -> "jialechan").execAsPojoOpt();

		Assert.assertTrue(userFromDB.isPresent());
		Assert.assertEquals(users, userFromDB.get());

	}

	@Test
	public void testIfWhere2() {
		Users users = genUserPojo(1L, "KKL", 18);

		Optional<Users> userFromDB = userDAO.select().where("name = ?", "KKL").ifWhere(users.getAge() != 0, "age = ?", () -> 18).execAsPojoOpt();

		Assert.assertTrue(userFromDB.isPresent());
		Assert.assertEquals(users, userFromDB.get());
	}

	@Test
	public void testIfWhere3() {

		Users users = genUserPojo(1L, null, 8);

		List<Users> usersList = userDAO.select().ifWhere(users.getName() != null, "name = ?", () -> users.getName().toLowerCase()).execAsList();

		Assert.assertEquals(3, usersList.size());
	}


	@Test
	public void testSelectAll() {
		List<Users> userses = new ArrayList<>();
		userses.add(genUserPojo(1L, "jialechan", 8, 1.73));
		userses.add(genUserPojo(2L, "KKL", 18));
		userses.add(genUserPojo(3L, "Ken", 28));
		
		List<Users> result = userDAO.select().execAsList();

		Assert.assertEquals(userses, result);
	}

	@Test
	public void testSelectAllByFields() {
		List<Users> userses = new ArrayList<>();
		userses.add(genUserPojo(1L, "jialechan", 8));
		userses.add(genUserPojo(2L, "KKL", 18));
		userses.add(genUserPojo(3L, "Ken", 28));

		List<Users> result = userDAO.select("id, name, age").execAsList();

		Assert.assertEquals(userses, result);
	}

	@Test
	public void testSelectAllWithNoData() {
		int deleted = userDAO.select().delete();
		Assert.assertEquals(3, deleted);

		List<Users> userses = userDAO.select().execAsList();
		Assert.assertEquals(0, userses.size());
	}

	@Test
	public void testCountTotal() {
		Assert.assertEquals(3, userDAO.select().count());
	}
	
	@Test
	public void addAndReturnKey() {
		Users users = new Users();
		users.setAge(1);
		users.setName("Tim");

		long id = userDAO.addAndReturnPrimaryKey(users);
		
		users.setId(id);
		
		Optional<Users> userFromDb = userDAO.selectByPrimaryKeyOpt(id);
		
		Assert.assertTrue(userFromDb.isPresent());
		
		Assert.assertEquals(users, userFromDb.get());
	}

	@Test
	public void testAdd() {
		Users users = new Users();
		users.setAge(1);
		users.setName("Tim");
		userDAO.add(users);
		Assert.assertEquals(4, userDAO.select().count());
	}
	
	@Test
	public void testSelectFilterBy() {
		Users users = genUserPojo(1L, "jialechan", 8, 1.73);
		
		Optional<Users> userFromDB = userDAO.select().where("name = ?", "jialechan").execAsPojoOpt();
		
		Assert.assertTrue(userFromDB.isPresent());
		Assert.assertEquals(users, userFromDB.get());
	}
	
	@Test
	public void testSelectFilterByWithNoData() {
		Assert.assertFalse(userDAO.select().where("name = ?", "nobody").execAsPojoOpt().isPresent());
	}
	
	@Test(expected=IncorrectResultSizeDataAccessException.class)
	public void testSelectFilterByWithMoreThanOneResult() {
		userDAO.select().where("age > ?", 1).execAsPojoOpt();
	}
	
	@Test
	public void testSelectFilterByAsList() {
		List<Users> userses = new ArrayList<>();
		userses.add(genUserPojo(1L, "jialechan", 8, 1.73));
		userses.add(genUserPojo(2L, "KKL", 18));
		
		List<Users> usersFromDB = userDAO.select().where("age < ?", 20).execAsList();
		
		Assert.assertEquals(userses, usersFromDB);
	}
	
	@Test
	public void testSelectFilterByAsListWithNoData() {
		List<Users> usersFromDB = userDAO.select().where("age < ?", 0).execAsList();
		Assert.assertEquals(0, usersFromDB.size());
	}
	
	@Test
	public void testSelectFilterByAsRowsWithNoData() {
		List<Map<String, Object>> userFromDB = userDAO.select().where("age < ?", 0).execAsRows();
		Assert.assertEquals(0, userFromDB.size());
	}
	
	@Test
	public void testSelectFilterByStartAndLimitOrderBy() {
		List<Users> usersFromDB = userDAO.select().where("age > ?", 16).orderByDesc("id").orderByAsc("age").execAsList();

		Assert.assertEquals(2, usersFromDB.size());
		Assert.assertEquals("KKL", usersFromDB.get(1).getName());
		Assert.assertEquals(18, usersFromDB.get(1).getAge().longValue());
	}
	
	@Test
	public void testSelectByStartAndLimit() {
		List<Map<String, Object>> userFromDB = userDAO.select().limit(0, 1).execAsRows();

		Assert.assertEquals(1, userFromDB.size());
		Assert.assertEquals("jialechan", userFromDB.get(0).get("name"));
		Assert.assertEquals(8, userFromDB.get(0).get("age"));
	}
	
	@Test
	public void testSelectByStartAndLimitOrderBy() {
		List<Map<String, Object>> userFromDB = userDAO.select().limit(0, 1).orderByDesc("id").execAsRows();
		
		Assert.assertEquals(1, userFromDB.size());
		Assert.assertEquals("Ken", userFromDB.get(0).get("name"));
		Assert.assertEquals(28, userFromDB.get(0).get("age"));
	}
	
	@Test
	public void testSelectFilterByStartAndLimitOrderByWithNoData() {
		List<Map<String, Object>> userFromDB = userDAO.select().where("age > ?", 100).limit(0, 100).orderByDesc("age").execAsRows();
		Assert.assertEquals(0, userFromDB.size());
	}
	
	@Test
	public void testSelectFilterByStartAndLimitOrderByAsList() {
		Users users = genUserPojo(2L, "KKL", 18);
		
		List<Users> usersFromDB = userDAO.select().where("age < ?", 20).orderByDesc("id").limit(0, 1).execAsList();
		
		Assert.assertEquals(1, usersFromDB.size());
		Assert.assertTrue(usersFromDB.contains(users));
	}
	
	@Test
	public void testSelectFilterByStartAndLimitOrderByAsListWithNoData() {
		List<Users> usersFromDB = userDAO.select().where("age < ?", 0).orderByDesc("id").limit(0, 1).execAsList();
		Assert.assertEquals(0, usersFromDB.size());
	}
	
	@Test
	public void testExecSelectSql() {
		String sql = "select * from " + userDAO.getTableName() + " where name = ?";
		
		List<Map<String, Object>> userFromDB = userDAO.execSelectSqlAsRows(sql, "KKL");
		
		Assert.assertEquals(1, userFromDB.size());
		Assert.assertEquals("KKL", userFromDB.get(0).get("name"));
		Assert.assertEquals(18, userFromDB.get(0).get("age"));
	}
	
	@Test
	public void testExecSelectSqlNotExit() {
		String sql = "select * from " + userDAO.getTableName() + " where name = ?";
		List<Map<String, Object>> userFromDB = userDAO.execSelectSqlAsRows(sql, "KKL2");
		Assert.assertEquals(0, userFromDB.size());
	}
	
	@Test
	public void testExecUpdate() {
		Users user = new Users();
		user.setName("Jacket");
		user.setAge(25);

		user.setId(userDAO.addAndReturnPrimaryKey(user));
		
		String sql = "update " + userDAO.getTableName() + " set name = ? where id = ?";
		
		int updated = userDAO.execUpdate(sql, "KKL2", user.getId());
		
		Assert.assertEquals(1, updated);

		user.setName("KKL2");

		Optional<Users> userFromDB = userDAO.selectByPrimaryKeyOpt(user.getId());
		Assert.assertTrue(userFromDB.isPresent());
		Assert.assertEquals(user, userFromDB.get());
	}
	
	@Test
	public void testExecUpdateButNotMatch() {
		String sql = "update " + userDAO.getTableName() + " set name = 'nomatch' where id = -1";
		int updated = userDAO.execUpdate(sql);
		
		Assert.assertEquals(0, updated);
	}
	
	@Test
	public void testExecSelectSqlAsObject() {
		Users users = genUserPojo(1L, "jialechan", 8, 1.73);
		
		String sql = "select * from " + userDAO.getTableName() + " where name = ?";
		
		Optional<Users> userFromDB = userDAO.execSelectSqlAsPojoOpt(sql, "jialechan");

		Assert.assertTrue(userFromDB.isPresent());
		Assert.assertEquals(users, userFromDB.get());
	}
	
	@Test
	public void testExecSqlAsObjectList() {
		List<Users> userses = new ArrayList<>();
		userses.add(genUserPojo(1L, "jialechan", 8, 1.73));
		userses.add(genUserPojo(2L, "KKL", 18));
		
		String sql = "select * from " + userDAO.getTableName() + " where age < ?";
		
		List<Users> usersFromDB = userDAO.execSelectSqlAsList(sql, 20);
		
		Assert.assertEquals(userses, usersFromDB);
	}
	
	@Test
	public void testDeleteById() {
		Users users = new Users(){{
			setName("iamjiale");
			setAge(100);
		}};
		users.setId(userDAO.addAndReturnPrimaryKey(users));
		int deleted = userDAO.deleteByPrimaryKey(users.getId());
		Assert.assertEquals(1, deleted);
		
		Optional<Users> shouldBeEmpty = userDAO.selectByPrimaryKeyOpt(userDAO.selectByPrimaryKeyOpt(users.getId()));
		Assert.assertFalse(shouldBeEmpty.isPresent());
	}
	
	@Test
	public void testDeleteBy() {
		int deleted = userDAO.select().where("age > ?", 1).delete();
		Assert.assertEquals(3, deleted);
		
		List<Users> userses = userDAO.select().where("age > ?", 1).execAsList();
		Assert.assertEquals(0, userses.size());
	}
	
	@Test
	public void testHasOnlyOneRecord() {
		Assert.assertTrue(userDAO.select().where("name = ?", "Ken").hasOnlyOneRecord());
		Assert.assertFalse(userDAO.select().where("name != ?", "Ken").hasOnlyOneRecord());
	}
	
	@Test
	public void testHasRecord() {
		Assert.assertTrue(userDAO.select().where("name = ?", "Ken").hasRecord());
		Assert.assertFalse(userDAO.select().where("name = ?", "nobody").hasRecord());
	}

	@Test
	public void testSelectByStartAndLimitOrderByOrderBy() {
		List<Map<String, Object>> userFromDB = userDAO.select().limit(0, 1).orderByDesc("id").orderByDesc("age").execAsRows();

		Assert.assertEquals(1, userFromDB.size());
		Assert.assertEquals("Ken", userFromDB.get(0).get("name"));
		Assert.assertEquals(28, userFromDB.get(0).get("age"));
	}

	@Test
	public void testSelectByStartAndLimitOrderByOrderByAcs() {
		List<Map<String, Object>> userFromDB = userDAO.select().limit(0, 1).orderByAsc("id").orderByAsc("age").execAsRows();

		Assert.assertEquals(1, userFromDB.size());
		Assert.assertEquals("jialechan", userFromDB.get(0).get("name"));
		Assert.assertEquals(8, userFromDB.get(0).get("age"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLimit0() {
		userDAO.select().limit(0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLimitSmall1() {
		userDAO.select().limit(0, -1);
	}

	private Users genUserPojo(Long id, String name, Integer age) {
		Users users = new Users();
		users.setAge(age);
		users.setName(name);
		users.setId(id);

		return users;
	}

	private Users genUserPojo(Long id, String name, Integer age, Double height) {
		Users users = new Users();
		users.setAge(age);
		users.setName(name);
		users.setId(id);

		users.setHeight(height);

		return users;
	}

	@Test
	public void testUserWithBirthday() {
		Date now = new Date();
		Users users = new Users();
		users.setName("jiale");
		users.setAge(32);
		users.setBirthday(getSecondTimestamp(now));

		long id = userDAO.addAndReturnPrimaryKey(users);
		users.setId(id);

		Optional<Users> usersOptional = userDAO.selectByPrimaryKeyOpt(users.getId());
		Assert.assertTrue(usersOptional.isPresent());
		Assert.assertEquals(users, usersOptional.get());
	}

	public static Date getSecondTimestamp(Date date){
		String timestamp = String.valueOf(date.getTime()/1000);
		return new Date(Long.valueOf(timestamp + "000"));
	}


}
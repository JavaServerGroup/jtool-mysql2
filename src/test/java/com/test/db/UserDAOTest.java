package com.test.db;

import org.junit.Assert;
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
	
	@Test
	public void testSelectAll() {
		List<Users> userses = new ArrayList<>();
		userses.add(genUserPojo(1, "jialechan", 8));
		userses.add(genUserPojo(2, "KKL", 18));
		userses.add(genUserPojo(3, "Ken", 28));
		
		List<Users> result = userDAO.select().execAsList();

		Assert.assertEquals(userses, result);
	}

	@Test
	public void testSelectAllByFields() {
		List<Users> userses = new ArrayList<>();
		userses.add(genUserPojo(1, "jialechan", 8));
		userses.add(genUserPojo(2, "KKL", 18));
		userses.add(genUserPojo(3, "Ken", 28));

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
	public void testSelectById() {
		
		Users users = genUserPojo(1, "jialechan", 8);
		
		Optional<Users> userFromDB = userDAO.selectByPrimaryKey(1);
		
		Assert.assertTrue(userFromDB.isPresent());
		Assert.assertEquals(users, userFromDB.get());
	}
	
	@Test
	public void testCountTotal() {
		Assert.assertEquals(3, userDAO.select().count());
	}
	
	@Test
	public void testAdd() {
		Users users = new Users();
		users.setAge(1);
		users.setName("Tim");

		long id = userDAO.addAndReturnKey(users);
		
		users.setId(id);
		
		Optional<Users> userFromDb = userDAO.selectByPrimaryKey(id);
		
		Assert.assertTrue(userFromDb.isPresent());
		
		Assert.assertEquals(users, userFromDb.get());
	}
	
	@Test
	public void testSelectFilterBy() {
		Users users = genUserPojo(1, "jialechan", 8);
		
		Optional<Users> userFromDB = userDAO.select().where("name = ?", "jialechan").execAsPojoOpt();
		
		Assert.assertTrue(userFromDB.isPresent());
		Assert.assertEquals(users, userFromDB.get());
	}
	
	@Test
	public void testSelectFilterByWithNoData() {
		Optional<Users> userFromDB = userDAO.select().where("name = ?", "nobody").execAsPojoOpt();
		Assert.assertFalse(userFromDB.isPresent());
	}
	
	@Test(expected=IncorrectResultSizeDataAccessException.class)
	public void testSelectFilterByWithMoreThanOneResult() {
		userDAO.select().where("age > ?", 1).execAsPojoOpt();
	}
	
	@Test
	public void testSelectFilterByAsList() {
		List<Users> userses = new ArrayList<>();
		userses.add(genUserPojo(1, "jialechan", 8));
		userses.add(genUserPojo(2, "KKL", 18));
		
		List<Users> usersFromDB = userDAO.select().where("age < ?", 20).execAsList();
		
		Assert.assertEquals(userses, usersFromDB);
	}
	
	@Test
	public void testSelectFilterByAsListWithNoData() {
		List<Users> usersFromDB = userDAO.select().where("age < ?", 0).execAsList();
		Assert.assertEquals(0, usersFromDB.size());
	}
	
	@Test
	public void testSelectFilterByAsRows() {
		Map<String, Object> user1 = genUserMap(1, "jialechan", 8);
		Map<String, Object> user2 = genUserMap(2, "KKL", 18); 
		
		List<Map<String, Object>> userFromDB = userDAO.select().where("age < ?", 20).execAsRows();
		
		Assert.assertEquals(2, userFromDB.size());
		Assert.assertTrue(userFromDB.contains(user1));
		Assert.assertTrue(userFromDB.contains(user2));
	}
	
	@Test
	public void testSelectFilterByAsRowsWithNoData() {
		List<Map<String, Object>> userFromDB = userDAO.select().where("age < ?", 0).execAsRows();
		Assert.assertEquals(0, userFromDB.size());
	}
	
	@Test
	public void testSelectFilterByStartAndLimitOrderBy() {
		Users users2 = genUserPojo(2, "KKL", 18);
		
		List<Users> usersFromDB = userDAO.select().where("age > ?", 16).orderByDesc("id").orderByAsc("age").execAsList();

		Assert.assertEquals(2, usersFromDB.size());
		Assert.assertEquals(usersFromDB.get(1), users2);
	}
	
	@Test
	public void testSelectByStartAndLimit() {
		Map<String, Object> user = genUserMap(1, "jialechan", 8); 
		
		List<Map<String, Object>> userFromDB = userDAO.select().limit(0, 1).execAsRows();
		
		Assert.assertEquals(1, userFromDB.size());
		Assert.assertTrue(userFromDB.contains(user));
	}
	
	@Test
	public void testSelectByStartAndLimitOrderBy() {
		Map<String, Object> user = genUserMap(3, "Ken", 28); 
		
		List<Map<String, Object>> userFromDB = userDAO.select().limit(0, 1).orderByDesc("id").execAsRows();
		
		Assert.assertEquals(1, userFromDB.size());
		Assert.assertTrue(userFromDB.contains(user));
	}
	
	@Test
	public void testSelectFilterByStartAndLimitOrderByWithNoData() {
		List<Map<String, Object>> userFromDB = userDAO.select().where("age > ?", 100).limit(0, 100).orderByDesc("age").execAsRows();
		Assert.assertEquals(0, userFromDB.size());
	}
	
	@Test
	public void testSelectFilterByStartAndLimitOrderByAsList() {
		Users users = genUserPojo(2, "KKL", 18);
		
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
		Map<String, Object> user2 = genUserMap(2, "KKL", 18); 
		
		String sql = "select * from " + userDAO.getTableName() + " where name = ?";
		
		List<Map<String, Object>> userFromDB = userDAO.execSelectSqlRows(sql, "KKL");
		
		Assert.assertEquals(1, userFromDB.size());
		Assert.assertTrue(userFromDB.contains(user2));
	}
	
	@Test
	public void testExecSelectSqlNotExit() {
		String sql = "select * from " + userDAO.getTableName() + " where name = ?";
		List<Map<String, Object>> userFromDB = userDAO.execSelectSqlRows(sql, "KKL2");
		Assert.assertEquals(0, userFromDB.size());
	}
	
	@Test
	public void testExecUpdate() {
		Users users = genUserPojo(1, "jialechan", 8);
		
		String sql = "update " + userDAO.getTableName() + " set name = ? where id = ?";
		
		int updated = userDAO.execUpdate(sql, "KKL2", 1);
		
		Assert.assertEquals(1, updated);
		
		users.setName("KKL2");
		
		Assert.assertEquals(users, userDAO.selectByPrimaryKey(1).get());
	}
	
	@Test
	public void testExecUpdateButNotMatch() {
		String sql = "update " + userDAO.getTableName() + " set name = 'nomatch' where id = -1";
		int updated = userDAO.execUpdate(sql);
		
		Assert.assertEquals(0, updated);
	}
	
	@Test
	public void testExecSelectSqlAsObject() {
		Users users = genUserPojo(1, "jialechan", 8);
		
		String sql = "select * from " + userDAO.getTableName() + " where name = ?";
		
		Optional<Users> userFromDB = userDAO.execSelectSqlAsPojoOpt(sql, "jialechan");
		
		Assert.assertEquals(users, userFromDB.get());
	}
	
	@Test
	public void testExecSqlAsObjectList() {
		List<Users> userses = new ArrayList<>();
		userses.add(genUserPojo(1, "jialechan", 8));
		userses.add(genUserPojo(2, "KKL", 18));
		
		String sql = "select * from " + userDAO.getTableName() + " where age < ?";
		
		List<Users> usersFromDB = userDAO.execSelectSqlAsList(sql, 20);
		
		Assert.assertEquals(userses, usersFromDB);
	}
	
	@Test
	public void testDeleteById() {
		int deleted = userDAO.deleteByPrimaryKey(1);
		Assert.assertEquals(1, deleted);
		
		Optional<Users> shouldBeEmpty = userDAO.selectByPrimaryKey(1);
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
		Map<String, Object> user = genUserMap(3, "Ken", 28);

		List<Map<String, Object>> userFromDB = userDAO.select().limit(0, 1).orderByDesc("id").orderByDesc("age").execAsRows();

		Assert.assertEquals(1, userFromDB.size());
		Assert.assertTrue(userFromDB.contains(user));
	}

	@Test
	public void testSelectByStartAndLimitOrderByOrderByAcs() {
		Map<String, Object> user = genUserMap(1, "jialechan", 8);

		List<Map<String, Object>> userFromDB = userDAO.select().limit(0, 1).orderByAsc("id").orderByAsc("age").execAsRows();

		System.out.println(userFromDB);

		Assert.assertEquals(1, userFromDB.size());
		Assert.assertTrue(userFromDB.contains(user));
	}

	private Users genUserPojo(int id, String name, int age) {
		Users users = new Users();
		users.setAge(age);
		users.setName(name);
		users.setId(id);
		return users;
	}
	
	private Map<String, Object> genUserMap(int id, String name, int age) {
		Map<String, Object> result = new HashMap<>();
		result.put("age", age);
		result.put("id", id);
		result.put("name", name);
		
		return result;
	}


}
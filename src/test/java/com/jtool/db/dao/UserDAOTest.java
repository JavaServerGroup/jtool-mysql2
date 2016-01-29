package com.jtool.db.dao;

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
		List<User> users = new ArrayList<>();
		users.add(genUserPojo(1, "jialechan", 8));
		users.add(genUserPojo(2, "KKL", 18));
		users.add(genUserPojo(3, "Ken", 28));
		
		List<User> result = userDAO.select().execAsList();
		
		Assert.assertEquals(users, result);
	}

	@Test
	public void testSelectAllByFields() {
		List<User> users = new ArrayList<>();
		users.add(genUserPojo(1, "jialechan", 8));
		users.add(genUserPojo(2, "KKL", 18));
		users.add(genUserPojo(3, "Ken", 28));

		List<User> result = userDAO.select("id, name, age").execAsList();

		Assert.assertEquals(users, result);
	}

	@Test
	public void testSelectAllWithNoData() {
		int deleted = userDAO.select().delete();
		Assert.assertEquals(3, deleted);

		List<User> users = userDAO.select().execAsList();
		Assert.assertEquals(0, users.size());
	}

	@Test
	public void testSelectById() {
		
		User user = genUserPojo(1, "jialechan", 8);
		
		Optional<User> userFromDB = userDAO.selectByPrimaryKey(1);
		
		Assert.assertTrue(userFromDB.isPresent());
		Assert.assertEquals(user, userFromDB.get());
	}
	
	@Test
	public void testCountTotal() {
		Assert.assertEquals(3, userDAO.select().count());
	}
	
	@Test
	public void testAdd() {
		User user = new User();
		user.setAge(1);
		user.setName("Tim");

		int id = userDAO.add(user);
		
		user.setId(id);
		
		Optional<User> userFromDb = userDAO.selectByPrimaryKey(id);
		
		Assert.assertTrue(userFromDb.isPresent());
		
		Assert.assertEquals(user, userFromDb.get());
	}
	
	@Test
	public void testSelectFilterBy() {
		User user = genUserPojo(1, "jialechan", 8);
		
		Optional<User> userFromDB = userDAO.select().where("name = ?", "jialechan").execAsPojoOpt();
		
		Assert.assertTrue(userFromDB.isPresent());
		Assert.assertEquals(user, userFromDB.get());
	}
	
	@Test
	public void testSelectFilterByWithNoData() {
		Optional<User> userFromDB = userDAO.select().where("name = ?", "nobody").execAsPojoOpt();
		Assert.assertFalse(userFromDB.isPresent());
	}
	
	@Test(expected=IncorrectResultSizeDataAccessException.class)
	public void testSelectFilterByWithMoreThanOneResult() {
		userDAO.select().where("age > ?", 1).execAsPojoOpt();
	}
	
	@Test
	public void testSelectFilterByAsList() {
		List<User> users = new ArrayList<>();
		users.add(genUserPojo(1, "jialechan", 8));
		users.add(genUserPojo(2, "KKL", 18));
		
		List<User> userFromDB = userDAO.select().where("age < ?", 20).execAsList();
		
		Assert.assertEquals(users, userFromDB);
	}
	
	@Test
	public void testSelectFilterByAsListWithNoData() {
		List<User> userFromDB = userDAO.select().where("age < ?", 0).execAsList();
		Assert.assertEquals(0, userFromDB.size());
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
		Map<String, Object> user2 = genUserMap(2, "KKL", 18); 
		
		List<Map<String, Object>> userFromDB = userDAO.select().where("age < ?", 20).limit(0, 1).orderBy("id desc").execAsRows();
		
		Assert.assertEquals(1, userFromDB.size());
		Assert.assertTrue(userFromDB.contains(user2));
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
		
		List<Map<String, Object>> userFromDB = userDAO.select().limit(0, 1).orderBy("id desc").execAsRows();
		
		Assert.assertEquals(1, userFromDB.size());
		Assert.assertTrue(userFromDB.contains(user));
	}
	
	@Test
	public void testSelectFilterByStartAndLimitOrderByWithNoData() {
		List<Map<String, Object>> userFromDB = userDAO.select().where("age > ?", 100).limit(0, 100).orderBy("age desc").execAsRows();
		Assert.assertEquals(0, userFromDB.size());
	}
	
	@Test
	public void testSelectFilterByStartAndLimitOrderByAsList() {
		User user = genUserPojo(2, "KKL", 18); 
		
		List<User> userFromDB = userDAO.select().where("age < ?", 20).orderBy("id desc").limit(0, 1).execAsList();
		
		Assert.assertEquals(1, userFromDB.size());
		Assert.assertTrue(userFromDB.contains(user));
	}
	
	@Test
	public void testSelectFilterByStartAndLimitOrderByAsListWithNoData() {
		List<User> userFromDB = userDAO.select().where("age < ?", 0).orderBy("id desc").limit(0, 1).execAsList();
		Assert.assertEquals(0, userFromDB.size());
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
		User user = genUserPojo(1, "jialechan", 8);
		
		String sql = "update " + userDAO.getTableName() + " set name = ? where id = ?";
		
		int updated = userDAO.execUpdate(sql, "KKL2", 1);
		
		Assert.assertEquals(1, updated);
		
		user.setName("KKL2");
		
		Assert.assertEquals(user, userDAO.selectByPrimaryKey(1).get());
	}
	
	@Test
	public void testExecUpdateButNotMatch() {
		String sql = "update " + userDAO.getTableName() + " set name = 'nomatch' where id = -1";
		int updated = userDAO.execUpdate(sql);
		
		Assert.assertEquals(0, updated);
	}
	
	@Test
	public void testExecSelectSqlAsObject() {
		User user = genUserPojo(1, "jialechan", 8);
		
		String sql = "select * from " + userDAO.getTableName() + " where name = ?";
		
		Optional<User> userFromDB = userDAO.execSelectSqlAsPojoOpt(sql, "jialechan");
		
		Assert.assertEquals(user, userFromDB.get());
	}
	
	@Test
	public void testExecSqlAsObjectList() {
		List<User> users = new ArrayList<>();
		users.add(genUserPojo(1, "jialechan", 8));
		users.add(genUserPojo(2, "KKL", 18));
		
		String sql = "select * from " + userDAO.getTableName() + " where age < ?";
		
		List<User> userFromDB = userDAO.execSelectSqlAsList(sql, 20);
		
		Assert.assertEquals(users, userFromDB);
	}
	
	@Test
	public void testDeleteById() {
		int deleted = userDAO.deleteByPrimaryKey(1);
		Assert.assertEquals(1, deleted);
		
		Optional<User> shouldBeEmpty = userDAO.selectByPrimaryKey(1);
		Assert.assertFalse(shouldBeEmpty.isPresent());
	}
	
	@Test
	public void testDeleteBy() {
		int deleted = userDAO.select().where("age > ?", 1).delete();
		Assert.assertEquals(3, deleted);
		
		List<User> users = userDAO.select().where("age > ?", 1).execAsList();
		Assert.assertEquals(0, users.size());
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
	
	private User genUserPojo(int id, String name, int age) {
		User user = new User();
		user.setAge(age);
		user.setName(name);
		user.setId(id);
		return user;
	}
	
	private Map<String, Object> genUserMap(int id, String name, int age) {
		Map<String, Object> result = new HashMap<>();
		result.put("age", age);
		result.put("id", id);
		result.put("name", name);
		
		return result;
	}


}
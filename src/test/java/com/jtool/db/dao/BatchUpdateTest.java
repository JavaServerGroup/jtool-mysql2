package com.jtool.db.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(locations = "/testDB-config.xml")
public class BatchUpdateTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Resource
	private UserDAO userDAO;

	@Test
	public void testBatchUpdate() {
		long begin = System.currentTimeMillis();
		List<User> users = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			users.add(genUserPojo(0, i + "", i));
		}
		userDAO.batchUpdate(users);
		long end = System.currentTimeMillis();
		System.out.println(end - begin);
		
		Assert.assertEquals(10003, userDAO.select().count());
	}


	private User genUserPojo(int id, String name, int age) {
		User user = new User();
		user.setAge(age);
		user.setName(name);
		user.setId(id);
		return user;
	}

}
package com.test.db;

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
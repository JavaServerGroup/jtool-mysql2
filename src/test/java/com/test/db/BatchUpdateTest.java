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
		List<Users> users = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			users.add(genUserPojo(0l, i + "", i));
		}
		userDAO.batchUpdate(users);

		Assert.assertEquals(10000, userDAO.select().count());
	}

	private Users genUserPojo(Long id, String name, Integer age) {
		Users users = new Users();
		users.setAge(age);
		users.setName(name);
		users.setId(id);
		return users;
	}

}
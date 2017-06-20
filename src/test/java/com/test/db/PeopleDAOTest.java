package com.test.db;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import javax.annotation.Resource;

@ContextConfiguration(locations = "/testDB-config.xml")
public class PeopleDAOTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Before
	public void before() {
		peopleDAO.add(new People(){{
			setName("jiale");
		}});
	}

	@Resource
	private PeopleDAO peopleDAO;
	
	@Test(expected= IllegalStateException.class)
	public void testSelectByPrimaryKey() {
		peopleDAO.selectByPrimaryKeyOpt("1");
	}

	@Test(expected= IllegalStateException.class)
	public void testDeleteByPrimaryKey() {
		peopleDAO.deleteByPrimaryKey("1");
	}

	@Test(expected= IllegalStateException.class)
	public void testAddAndReturnPrimaryKey() {
		peopleDAO.addAndReturnPrimaryKey(null);
	}


}
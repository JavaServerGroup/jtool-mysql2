package com.test.db;

import com.jtool.db.mysql.annotation.DataSource;
import com.jtool.db.mysql.annotation.Table;
import com.jtool.db.mysql.dao.AbstractDAO;
import org.springframework.stereotype.Repository;

@Repository
@Table(tableName = "people")
@DataSource("dataSource")
public class PeopleDAO extends AbstractDAO<People> {

}
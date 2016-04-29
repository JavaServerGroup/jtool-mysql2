package com.test.db;

import com.jtool.db.mysql.annotation.DataSource;
import com.jtool.db.mysql.annotation.Table;
import com.jtool.db.mysql.dao.AbstractDAO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Table(tableName = "people")
@DataSource("dataSource")
public class PeopleDAO extends AbstractDAO {

	@Override
	protected RowMapper<?> makeRowMapperInstance() {
		return new RowMapper() {
			@Override
			public People mapRow(ResultSet rs, int i) throws SQLException {
				People o = new People();
				o.setId(rs.getInt("id"));
				o.setName(rs.getString("name"));

				return o;
			}
		};
	}
}
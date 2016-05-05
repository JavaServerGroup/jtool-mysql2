package com.test.db;

import com.jtool.db.mysql.annotation.DataSource;
import com.jtool.db.mysql.annotation.Table;
import com.jtool.db.mysql.dao.AbstractDAO;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@Table(tableName = "users", primaryKeyName = "id")
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

	public int[] batchUpdate(final List<Users> usersList) {
		String sql = "insert into " + getTableName() + " (name, age) values(?, ?);";
		return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setString(1, usersList.get(i).getName());
				ps.setInt(2, usersList.get(i).getAge());
			}

			public int getBatchSize() {
				return usersList.size();
			}
		});
	}

}
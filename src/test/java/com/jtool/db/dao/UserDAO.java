package com.jtool.db.dao;

import com.jtool.db.annotation.DataSource;
import com.jtool.db.annotation.Table;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@Table(tableName = "user", primaryKeyName = "id")
@DataSource("dataSource")
public class UserDAO extends AbstractDAO {

	@Override
	protected RowMapper<?> makeRowMapperInstance() {
		return (rs, rowNum) -> {
            User o = new User();
            o.setId(rs.getInt("id"));
            o.setName(rs.getString("name"));
            o.setAge(rs.getInt("age"));

            return o;
        };
	}

	public int[] batchUpdate(final List<User> users) {
		String sql = "insert into " + getTableName() + " (name, age) values(?, ?);";
		return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setString(1, users.get(i).getName());
				ps.setInt(2, users.get(i).getAge());
			}

			public int getBatchSize() {
				return users.size();
			}
		});
	}

}
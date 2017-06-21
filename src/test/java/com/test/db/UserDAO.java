package com.test.db;

import com.jtool.db.mysql.annotation.DataSource;
import com.jtool.db.mysql.annotation.Table;
import com.jtool.db.mysql.dao.AbstractDAO;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;

@Repository
@Table(tableName = "users", primaryKeyName = "id")
@DataSource("dataSource")
public class UserDAO extends AbstractDAO<Users> {

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
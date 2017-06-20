package com.test.db;

import com.jtool.db.mysql.annotation.DataSource;
import com.jtool.db.mysql.annotation.Table;
import com.jtool.db.mysql.dao.AbstractDAO;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;

@Repository
@Table(tableName = "users", primaryKeyName = "id")
@DataSource("dataSource")
public class UserDAO extends AbstractDAO {

    @Override
    protected RowMapper<?> makeRowMapperInstance() {
        return new RowMapper<Users>() {
            @Override
            public Users mapRow(ResultSet rs, int i) throws SQLException {
                Users o = new Users();
                o.setId(rs.getInt("id"));
                o.setName(rs.getString("name"));
                o.setAge(rs.getInt("age"));

                return o;
            }
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

    public void myTest() throws SQLException {

//		Connection connection = this.jdbcTemplate.getDataSource().getConnection();
//
//		String catalog = connection.getCatalog();
//
//		DatabaseMetaData dmd = connection.getMetaData();
//
//		ResultSet rs = dmd.getTables(null, "%", "(?i)" + this.getTableName(), new String[] { "TABLE" });
//
//		while (rs.next()) {
//			System.out.println(rs.getString("TABLE_NAME"));
//		}


        Connection connection = this.jdbcTemplate.getDataSource().getConnection();
        DatabaseMetaData dmd = connection.getMetaData();

        String catalog = connection.getCatalog(); //catalog 其实也就是数据库名
        ResultSet tablesResultSet = dmd.getTables(catalog, null, this.getTableName().toUpperCase(), new String[]{"TABLE"});
        while (tablesResultSet.next()) {

            ResultSet crs = dmd.getColumns(null, "%", this.getTableName().toUpperCase(), "%");


            while (crs.next()) {
                String columnname = crs.getString("COLUMN_NAME");
                String columntype = crs.getString("TYPE_NAME");
                System.out.println("--------------------------"+ columnname + "\t" + columntype);
            }
        }

    }

}
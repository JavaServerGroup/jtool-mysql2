package com.jtool.db.mysql.dao;

import com.jtool.db.mysql.annotation.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Date;
import java.util.*;

public abstract class AbstractDAO<T> implements ApplicationContextAware {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert simpleJdbcInsert;

    private DataSource dataSource;

    private String tableName;
    private String primaryKeyName;
    private Class<T> dbPojoClass;
    private Map<String, Method> methodMap = new HashMap<>();
    private Map<String, Field> fieldMap = new HashMap<>();

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @PostConstruct
    private void init() throws NoSuchFieldException, SQLException, NoSuchMethodException, ClassNotFoundException {
        initTableAnnotation();
        initDataSource();

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName(tableName).usingGeneratedKeyColumns(primaryKeyName);

        initReflectionDbPojoClass();
        initReflectionBinding();

        checkPojoClass(dbPojoClass);
    }

    private void checkPojoClass(Class<T> dbPojoClass) {
        for(Field field : dbPojoClass.getDeclaredFields()) {
            if (field.getType().isPrimitive()) {
                throw new IllegalStateException(dbPojoClass.getName() + "不应该使用简单类型: " + field.getName());
            }
        }
    }

    private void initTableAnnotation() {
        tableName = this.getClass().getAnnotation(Table.class).tableName();
        primaryKeyName = this.getClass().getAnnotation(Table.class).primaryKeyName();
    }

    private void initReflectionDbPojoClass() throws ClassNotFoundException {
        dbPojoClass = (Class<T>)ReflectionUtil.getClass(ReflectionUtil.getParameterizedTypes(this)[0]);
    }

    private void initReflectionBinding() throws SQLException, NoSuchFieldException, NoSuchMethodException {

        try(final Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            final DatabaseMetaData dmd = connection.getMetaData();

            final String catalog = connection.getCatalog(); //catalog 其实也就是数据库名

            final ResultSet tablesResultSet = dmd.getTables(catalog, null, tableName, new String[]{"TABLE"});

            if (tablesResultSet.next()) {

                final ResultSet crs = dmd.getColumns(catalog, "%", tableName, "%");

                while (crs.next()) {
                    final String columnName = crs.getString("COLUMN_NAME");
                    final String methodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);

                    final Field field = dbPojoClass.getDeclaredField(columnName);

                    if (field == null) {
                        throw new IllegalStateException("pojo里面找不到对应数据库的字段");
                    } else {
                        Method method = dbPojoClass.getDeclaredMethod(methodName, new Class[]{field.getType()});
                        methodMap.put(columnName, method);
                        fieldMap.put(columnName, field);
                    }
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private void initDataSource() {
        String dataSourceString = this.getClass().getAnnotation(com.jtool.db.mysql.annotation.DataSource.class).value();
        dataSource = context.getBean(dataSourceString, DataSource.class);
    }

    public String getTableName() {
        return tableName;
    }

    public Select<T> select() {
        return new Select<>("*", this);
    }

    public Select<T> select(String fields) {
        return new Select<>(fields, this);
    }

    public Optional<T> selectByPrimaryKeyOpt(Object id) {
        if (primaryKeyName == null || "".equals(primaryKeyName)) {
            throw new IllegalStateException("需要使用selectByPrimaryKey方法,必须在dao的@Table注解设置primaryKeyName的值");
        }
        try {
            String selectByIdSQL = "select * from " + tableName + " where " + primaryKeyName + " = ?";
            log.debug("准备根据ID查找：{} \t {}", selectByIdSQL, id);
            T t = jdbcTemplate.queryForObject(selectByIdSQL, makeRowMapperInstance(), id.toString());
            log.debug("根据ID查找到：{}", t);
            return Optional.of(t);
        } catch (EmptyResultDataAccessException e) {
            log.debug("根据ID({})查找不到对象", id);
            return Optional.empty();
        }
    }

    protected RowMapper<T> makeRowMapperInstance() {
        return new RowMapper<T>() {
            @Override
            public T mapRow(ResultSet rs, int i) throws SQLException {

                try {

                    T object = dbPojoClass.newInstance();

                    ResultSetMetaData resultSetMetaData = rs.getMetaData();

                    for (int j = 1; j <= resultSetMetaData.getColumnCount(); j++) {

                        String columnName = resultSetMetaData.getColumnName(j);

                        switch (fieldMap.get(columnName).getGenericType().getTypeName()) {
                            case "byte" :
                            case "short" :
                            case "int" :
                            case "long" :
                            case "float" :
                            case "double" :
                                throw new IllegalStateException(dbPojoClass.getName() + "不应该使用简单类型: " + columnName);
                            case "java.lang.Byte" :
                                byte b = rs.getByte(j);
                                if(!rs.wasNull()) {
                                    methodMap.get(columnName).invoke(object, b);
                                }
                                break;
                            case "java.lang.Short" :
                                short s = rs.getShort(j);
                                if(!rs.wasNull()) {
                                    methodMap.get(columnName).invoke(object, s);
                                }
                                break;
                            case "java.lang.Integer" :
                                int anInt = rs.getInt(j);
                                if(!rs.wasNull()) {
                                    methodMap.get(columnName).invoke(object, anInt);
                                }
                                break;
                            case "java.lang.Long" :
                                long l = rs.getLong(j);
                                if(!rs.wasNull()) {
                                    methodMap.get(columnName).invoke(object, l);
                                }
                                break;
                            case "java.lang.Float" :
                                float f = rs.getFloat(j);
                                if(!rs.wasNull()) {
                                    methodMap.get(columnName).invoke(object, f);
                                }
                                break;
                            case "java.lang.Double" :
                                double d = rs.getDouble(j);
                                if (!rs.wasNull()) {
                                    methodMap.get(columnName).invoke(object, d);
                                }
                                break;
                            case "java.lang.String" :
                                String str = rs.getString(j);
                                if(!rs.wasNull()) {
                                    methodMap.get(columnName).invoke(object, str);
                                }
                                break;
                            case "java.sql.Timestamp" :
                            case "java.util.Date" :
                                Timestamp t = rs.getTimestamp(j);
                                if(!rs.wasNull()) {
                                    methodMap.get(columnName).invoke(object, new Date(t.getTime()));
                                }
                                break;
                            default:
                                throw new IllegalStateException();
                        }
                    }
                    return object;
                } catch (Exception e) {
                    log.error("反射绑定的时候发生错误", e);
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    public int deleteByPrimaryKey(Object id) {
        if (primaryKeyName == null || "".equals(primaryKeyName)) {
            throw new IllegalStateException("需要使用deleteByPrimaryKey方法,必须在dao的@Table注解设置primaryKeyName的值");
        }
        String sql = "delete from " + tableName + " where " + primaryKeyName + " = ?";
        log.debug("准备根据ID删除记录：{} \t {}", sql, id);
        int i = jdbcTemplate.update(sql, id);
        log.debug("删除记录条数：{}", i);
        return i;
    }

    public long addAndReturnPrimaryKey(Object object) {
        if (primaryKeyName == null || "".equals(primaryKeyName)) {
            throw new IllegalStateException("需要使用addAndReturnPrimaryKey方法,必须在dao的@Table注解设置primaryKeyName的值");
        }
        SqlParameterSource sps = new BeanPropertySqlParameterSource(object);
        log.debug("准备插入对象: {}", object);
        long id = simpleJdbcInsert.executeAndReturnKey(sps).longValue();
        log.debug("插入成功: {} \t primary key为:{}", object, id);
        return id;
    }

    public void add(Object object) {
        SqlParameterSource sps = new BeanPropertySqlParameterSource(object);
        log.debug("准备插入对象：{}", object);
        simpleJdbcInsert.execute(sps);
        log.debug("插入成功: {}", object);
    }

    public List<Map<String, Object>> execSelectSqlAsRows(String sql, Object... args) {
        log.debug("准备查找数据：{} \t {}", sql, args);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, args);
        log.debug("查找到符合条件记录条数：{}", result.size());
        return result;
    }

    public Optional<T> execSelectSqlAsPojoOpt(String sql, Object... args) {
        try {
            log.debug("准备查找数据：{} \t {}", sql, args);
            T t = jdbcTemplate.queryForObject(sql, makeRowMapperInstance(), args);
            log.debug("查找到记录：{}", t);
            return Optional.of(t);
        } catch (EmptyResultDataAccessException e) {
            log.debug("没有查找到数据");
            return Optional.empty();
        }
    }

    public List<T> execSelectSqlAsList(String sql, Object... args) {
        log.debug("准备查找数据：{} \t {}", sql, args);
        List<T> result = jdbcTemplate.query(sql, makeRowMapperInstance(), args);
        log.debug("查找到符合条件记录条数：{}", result.size());
        return result;
    }

    public int execUpdate(String sql, Object... args) {
        log.debug("执行修改操作：{} \t {}", sql, args);
        int result = jdbcTemplate.update(sql, args);
        log.debug("执行修改操作条数：{}", result);
        return result;
    }

    Logger getLog() {
        return log;
    }
}
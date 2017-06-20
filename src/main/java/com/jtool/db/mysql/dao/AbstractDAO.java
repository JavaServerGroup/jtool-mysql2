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
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractDAO implements ApplicationContextAware {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	protected JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert simpleJdbcInsert;

	private DataSource dataSource;

	private String tableName;
	private String primaryKeyName;

	private ApplicationContext context;

	protected abstract RowMapper makeRowMapperInstance();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		context = applicationContext;
	}

	@PostConstruct
	private void init() {
		initTableAnnotation();
		initDataSource();

		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName(tableName).usingGeneratedKeyColumns(primaryKeyName);
	}

	private void initTableAnnotation() {
		tableName = this.getClass().getAnnotation(Table.class).tableName();
		primaryKeyName = this.getClass().getAnnotation(Table.class).primaryKeyName();
	}

	private void initDataSource() {
		String dataSourceString = this.getClass().getAnnotation(com.jtool.db.mysql.annotation.DataSource.class).value();
		dataSource = context.getBean(dataSourceString, DataSource.class);
	}

	public String getTableName() {
		return tableName;
	}

	public Select select() {
		return new Select("*", this);
	}

	public Select select(String fields){
		return new Select(fields, this);
	}

	public <T> Optional<T> selectByPrimaryKeyOpt(Object id) {
		if(primaryKeyName == null || "".equals(primaryKeyName)) {
			throw new IllegalStateException("需要使用selectByPrimaryKey方法,必须在dao的@Table注解设置primaryKeyName的值");
		}
		try {
			String selectByIdSQL = "select * from " + tableName + " where " + primaryKeyName + " = ?";
			log.debug("准备根据ID查找：{} \t {}", selectByIdSQL, id);
			@SuppressWarnings("unchecked")
			T t = (T) jdbcTemplate.queryForObject(selectByIdSQL, makeRowMapperInstance(), id.toString());
			log.debug("根据ID查找到：{}", t);
			return Optional.of(t);
		} catch (EmptyResultDataAccessException e) {
			log.debug("根据ID({})查找不到对象", id);
			return Optional.empty();
		}
	}

	public int deleteByPrimaryKey(Object id) {
		if(primaryKeyName == null || "".equals(primaryKeyName)) {
			throw new IllegalStateException("需要使用deleteByPrimaryKey方法,必须在dao的@Table注解设置primaryKeyName的值");
		}
		String sql = "delete from " + tableName + " where " + primaryKeyName + " = ?";
		log.debug("准备根据ID删除记录：{} \t {}", sql, id);
		int i = jdbcTemplate.update(sql, id);
		log.debug("删除记录条数：{}", i);
		return i;
	}

	public long addAndReturnPrimaryKey(Object object) {
		if(primaryKeyName == null || "".equals(primaryKeyName)) {
			throw new IllegalStateException("需要使用addAndReturnPrimaryKey方法,必须在dao的@Table注解设置primaryKeyName的值");
		}
		SqlParameterSource sps = new BeanPropertySqlParameterSource(object);
		log.debug("准备插入对象: {}", object);
		@SuppressWarnings("unchecked")
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

	public <T> Optional<T> execSelectSqlAsPojoOpt(String sql, Object... args) {
		try {
			RowMapper rowMapper = makeRowMapperInstance();
			@SuppressWarnings("unchecked")
			T t = (T) jdbcTemplate.queryForObject(sql, rowMapper, args);
			log.debug("查找到记录：{}", t);
			return Optional.of(t);
		} catch (EmptyResultDataAccessException e) {
			log.debug("没有查找到数据");
			return Optional.empty();
		}
	}

	public <T> List<T> execSelectSqlAsList(String sql, Object... args) {
		log.debug("准备查找数据：{} \t {}", sql, args);
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) jdbcTemplate.query(sql, makeRowMapperInstance(), args);
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
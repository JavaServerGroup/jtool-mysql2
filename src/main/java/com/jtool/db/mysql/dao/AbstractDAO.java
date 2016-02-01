package com.jtool.db.mysql.dao;

import com.jtool.db.mysql.annotation.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
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
import java.util.*;

public abstract class AbstractDAO implements ApplicationContextAware {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert simpleJdbcInsert;

	private DataSource dataSource;

	private String tableName;
	private String primaryKeyName;

	private ApplicationContext context;

	protected abstract RowMapper<?> makeRowMapperInstance();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
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
		Class<?> clazz = this.getClass();
		Table table = clazz.getAnnotation(Table.class);
		tableName = table.tableName();
		primaryKeyName = table.primaryKeyName();
	}

	private void initDataSource() {
		Class<?> clazz = this.getClass();
		String dataSourceString = clazz.getAnnotation(com.jtool.db.mysql.annotation.DataSource.class).value();
		dataSource = context.getBean(dataSourceString, DataSource.class);
	}

	public String getTableName() {
		return tableName;
	}

	public class Select {

		private final List<Object> args = new ArrayList<>();

		private String fields;
		private String whereStr;
		private String orderByStr;
		private Integer start;
		private Integer len;

		private String action = "select";

		public Select(String fields) {
			this.fields = fields;
		}

		public Select where(String where, Object... args) {
			System.out.println(args.getClass());
			this.whereStr = where;
			Collections.addAll(this.args, args);
			return this;
		}

		public Select orderByAsc(String orderBy) {
			if(this.orderByStr == null) {
				this.orderByStr = orderBy;
			} else {
				this.orderByStr += ", " + orderBy;
			}
			return this;
		}

		public Select orderByDesc(String orderBy) {
			if(this.orderByStr == null) {
				this.orderByStr = orderBy + " desc";
			} else {
				this.orderByStr += ", " + orderBy + " desc";
			}
			return this;
		}

		public Select limit(int start, int len) {
//			if(len == 0) {
//				return this;
//			}
			this.start = start;
			this.len = len;
			return this;
		}

		public <T> List<T> execAsList() {
			return execAsList(makeSQL());
		}

		public <T> List<T> execAsList(String sql) {
			return AbstractDAO.this.execSelectSqlAsList(sql, args.toArray());
		}

		public List<Map<String, Object>> execAsRows() {
			return execAsRows(makeSQL());
		}

		public List<Map<String, Object>> execAsRows(String sql) {
			return AbstractDAO.this.execSelectSqlRows(sql, args.toArray());
		}

		public <T> Optional<T> execAsPojoOpt() {
			return execAsPojoOpt(makeSQL());
		}

		public <T> Optional<T> execAsPojoOpt(String sql) {
			return AbstractDAO.this.execSelectSqlAsPojoOpt(sql, args.toArray());
		}

		public int count() {
			this.fields = "count(1)";
			String sql = makeSQL();
			Object[] params = args.toArray();
			logger.debug("准备计算记录条数：" + sql + "\t" + Arrays.toString(params));
			int result = jdbcTemplate.queryForObject(sql, params, Integer.class);
			logger.debug("计算记录条数为：" + result);
			return result;
		}

		public boolean hasOnlyOneRecord() {
			return 1  == this.count();
		}

		public boolean hasRecord() {
			this.fields = "1";
			List<Map<String, Object>> rows = AbstractDAO.this.execSelectSqlRows(makeSQL(), args.toArray());
			boolean result = rows != null && rows.size() > 0;
			logger.debug("是否有纪录：" + result);
			return result;
		}

		public int delete() {
			this.action = "delete";
			this.fields = "";
			return AbstractDAO.this.execUpdate(makeSQL(), args.toArray());
		}

		private String makeSQL(){
			String sql = action + " " + fields + " from " + tableName;
			if(whereStr != null) {
				sql += " where " + whereStr;
			}
			if(orderByStr != null) {
				sql += " order by " + orderByStr;
			}
			if(start != null && len != null){
				sql += " limit " + start + ", " + len;
			}
			return sql;
		}
	}

	public Select select() {
		return new Select("*");
	}

	public Select select(String fields){
		return new Select(fields);
	}

	public <T> Optional<T> selectByPrimaryKey(Object id) {
		try {
			String selectByIdSQL = "select * from " + tableName + " where " + primaryKeyName + " = ?";
			logger.debug("准备根据ID查找：" + selectByIdSQL + "\t" + id.toString());
			@SuppressWarnings("unchecked")
			T t = (T) jdbcTemplate.queryForObject(selectByIdSQL, makeRowMapperInstance(), id.toString());
			logger.debug("根据ID查找到：" + t);
			return Optional.of(t);
		} catch (EmptyResultDataAccessException e) {
			logger.debug("根据ID(" + id + ")查找不到对象");
			return Optional.empty();
		}
	}

	public int deleteByPrimaryKey(Object id) {
		String sql = "delete from " + tableName + " where " + primaryKeyName + " = ?";
		logger.debug("准备根据ID删除记录：" + sql + "\t" + id);
		int i = jdbcTemplate.update(sql, id);
		logger.debug("删除记录条数：" + i);
		return i;
	}

	public long addAndReturnKey(Object object) {
		SqlParameterSource sps = new BeanPropertySqlParameterSource(object);
		logger.debug("准备插入对象：" + object);
		@SuppressWarnings("unchecked")
		long id = simpleJdbcInsert.executeAndReturnKey(sps).longValue();
		logger.debug("插入成功:" + object + "\t" + "primary key为:" + id);
		return id;
	}

	public void add(Object object) {
		SqlParameterSource sps = new BeanPropertySqlParameterSource(object);
		logger.debug("准备插入对象：" + object);
		simpleJdbcInsert.execute(sps);
		logger.debug("插入成功:" + object);
	}

	public List<Map<String, Object>> execSelectSqlRows(String sql, Object... args) {
		logger.debug("准备查找数据：" + sql + "\t" + Arrays.toString(args));
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, args);
		logger.debug("查找到符合条件记录条数：" + result.size());
		return result;
	}

	public <T> Optional<T> execSelectSqlAsPojoOpt(String sql, Object... args) {
		try {
			@SuppressWarnings("unchecked")
			T t = (T) jdbcTemplate.queryForObject(sql, makeRowMapperInstance(), args);
			logger.debug("查找到记录：" + t);
			return Optional.of(t);
		} catch (EmptyResultDataAccessException e) {
			logger.debug("没有查找到数据");
			return Optional.empty();
		}
	}

	public <T> List<T> execSelectSqlAsList(String sql, Object... args) {
		logger.debug("准备查找数据：" + sql + "\t" + Arrays.toString(args));
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) jdbcTemplate.query(sql, makeRowMapperInstance(), args);
		logger.debug("查找到符合条件记录条数：" + result.size());
		return result;
	}

	public int execUpdate(String sql, Object... args) {
		logger.debug("执行修改操作：" + sql + "\t" + Arrays.toString(args));
		int result = jdbcTemplate.update(sql, args);
		logger.debug("执行修改操作条数：" + result);
		return result;
	}
}
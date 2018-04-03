package com.jtool.db.mysql.dao;

import java.util.*;

public class Select<T> {

    private final List<Object> args = new ArrayList<>();

    private AbstractDAO<T> abstractDAO;

    private String fields;
    private String whereStr;
    private String orderByStr;
    private Integer start;
    private Integer len;

    private String action = "select";

    Select(String fields, AbstractDAO<T> abstractDAO) {
        this.fields = fields;
        this.abstractDAO = abstractDAO;
    }

    public Select<T> where(String where, Object... args) {
        this.whereStr = where;
        Collections.addAll(this.args, args);
        return this;
    }

    public Select<T> ifWhere(boolean runIfTrue, String where, ParamFunctionalInterface... args) {
        if(runIfTrue) {
            if(this.whereStr == null) {
                this.whereStr = where;
            } else {
                this.whereStr = this.whereStr + " and " + where;
            }

            for(ParamFunctionalInterface paramFunctionalInterface : args) {
                this.args.add(paramFunctionalInterface.calculation());
            }
        }
        return this;
    }

    public Select<T> orderByAsc(String orderBy) {
        if(this.orderByStr == null) {
            this.orderByStr = orderBy;
        } else {
            this.orderByStr += ", " + orderBy;
        }
        return this;
    }

    public Select<T> orderByDesc(String orderBy) {
        if(this.orderByStr == null) {
            this.orderByStr = orderBy + " desc";
        } else {
            this.orderByStr += ", " + orderBy + " desc";
        }
        return this;
    }

    public Select<T> limit(int start, int len) {
        if(len <= 0){
            throw new IllegalArgumentException("limit的长度应该大于0");
        }
        this.start = start;
        this.len = len;
        return this;
    }

    public List<T> execAsList() {
        return execAsList(makeSQL());
    }

    public List<T> execAsList(String sql) {
        return abstractDAO.execSelectSqlAsList(sql, args.toArray());
    }

    public List<Map<String, Object>> execAsRows() {
        return execAsRows(makeSQL());
    }

    public List<Map<String, Object>> execAsRows(String sql) {
        return abstractDAO.execSelectSqlAsRows(sql, args.toArray());
    }

    public Optional<T> execAsPojoOpt() {
        return execAsPojoOpt(makeSQL());
    }

    public Optional<T> execAsPojoOpt(String sql) {
        return abstractDAO.execSelectSqlAsPojoOpt(sql, args.toArray());
    }

    public int count() {
        this.fields = "count(1)";
        String sql = makeSQL();
        abstractDAO.getLog().debug("准备计算记录条数：{} \t {}", sql, args);
        int result = abstractDAO.jdbcTemplate.queryForObject(sql, args.toArray(), Integer.class);
        abstractDAO.getLog().debug("计算记录条数为：{}", result);
        return result;
    }

    public boolean hasOnlyOneRecord() {
        return 1 == this.count();
    }

    public boolean hasRecord() {
        this.fields = "1";
        final boolean result = !abstractDAO.execSelectSqlAsRows(makeSQL(), args.toArray()).isEmpty();
        abstractDAO.getLog().debug("是否有纪录：{}", result);
        return result;
    }

    public int delete() {
        this.action = "delete";
        this.fields = "";
        return abstractDAO.execUpdate(makeSQL(), args.toArray());
    }

    private String makeSQL(){
        String sql = action + " " + fields + " from " + abstractDAO.getTableName();
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
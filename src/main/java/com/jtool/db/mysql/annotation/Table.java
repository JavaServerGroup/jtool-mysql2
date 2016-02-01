package com.jtool.db.mysql.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Table {
	String tableName();
	String primaryKeyName();
}

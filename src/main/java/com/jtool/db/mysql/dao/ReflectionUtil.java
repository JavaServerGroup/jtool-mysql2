package com.jtool.db.mysql.dao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by jialechan on 2017/6/21.
 */
class ReflectionUtil {

    private static final String TYPE_NAME_PREFIX = "class ";

    private static String getClassName(Type type) {
        if (type==null) {
            return "";
        }
        String className = type.toString();
        if (className.startsWith(TYPE_NAME_PREFIX)) {
            className = className.substring(TYPE_NAME_PREFIX.length());
        }
        return className;
    }

    static Class<?> getClass(Type type)
            throws ClassNotFoundException {
        String className = getClassName(type);
        if (className==null || className.isEmpty()) {
            return null;
        }
        return Class.forName(className);
    }

    static Type[] getParameterizedTypes(Object object) {
        Type superclassType = object.getClass().getGenericSuperclass();
        if (!ParameterizedType.class.isAssignableFrom(superclassType.getClass())) {
            throw new IllegalStateException("申明类的时候请带上泛型名称");
        }
        return ((ParameterizedType)superclassType).getActualTypeArguments();
    }
}

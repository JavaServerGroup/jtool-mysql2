package com.test.db;

import java.util.Date;

public class Users {
	private long id;
	private String name;
	private int age;
	private Date birthday;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "Users{" +
				"id=" + id +
				", name='" + name + '\'' +
				", age=" + age +
				", birthday=" + birthday +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Users users = (Users) o;

		if (age != users.age) return false;
		return name != null ? name.equals(users.name) : users.name == null;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + age;
		return result;
	}
}

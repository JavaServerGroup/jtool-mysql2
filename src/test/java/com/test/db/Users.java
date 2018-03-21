package com.test.db;

import java.util.Date;
import java.util.Objects;

public class Users {
	private Long id;
	private String name;
	private Integer age;
	private Date birthday;
	private Double height;

	public Long getId() {
		return id;
	}

	public Users setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Users setName(String name) {
		this.name = name;
		return this;
	}

	public Integer getAge() {
		return age;
	}

	public Users setAge(Integer age) {
		this.age = age;
		return this;
	}

	public Date getBirthday() {
		return birthday;
	}

	public Users setBirthday(Date birthday) {
		this.birthday = birthday;
		return this;
	}

	public Double getHeight() {
		return height;
	}

	public Users setHeight(Double height) {
		this.height = height;
		return this;
	}

	@Override
	public String toString() {
		return "Users{" +
				"id=" + id +
				", name='" + name + '\'' +
				", age=" + age +
				", birthday=" + birthday +
				", height=" + height +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Users users = (Users) o;
		return age == users.age &&
				Objects.equals(name, users.name) &&
				Objects.equals(birthday, users.birthday) &&
				Objects.equals(height, users.height);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, age, birthday, height);
	}
}

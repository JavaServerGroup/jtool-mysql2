package com.test.db;

public class People {
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public People setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public People setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return "People{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

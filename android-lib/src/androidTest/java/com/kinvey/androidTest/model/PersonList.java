package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

import java.util.List;

public class PersonList extends Person{

    @Key("list")
    private List<PersonList> list;

    public PersonList() {
    }

    public PersonList(String name) {
        this.username = name;
    }

    public List<PersonList> getList() {
        return list;
    }

    public void setList(List<PersonList> list) {
        this.list = list;
    }
}

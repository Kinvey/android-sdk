package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by yuliya on 12/06/17.
 */
public class PersonWithPersonAndList extends GenericJson {

    @Key
    private PersonWithPersonAndList person;

    @Key
    private List<PersonWithPersonAndList> list;

    @Key
    private String name;

    public PersonWithPersonAndList(String name) {
        this.name = name;
    }

    public PersonWithPersonAndList() {
    }

    public PersonWithPersonAndList getPerson() {
        return person;
    }

    public void setPerson(PersonWithPersonAndList person) {
        this.person = person;
    }

    public List<PersonWithPersonAndList> getList() {
        return list;
    }

    public void setList(List<PersonWithPersonAndList> list) {
        this.list = list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

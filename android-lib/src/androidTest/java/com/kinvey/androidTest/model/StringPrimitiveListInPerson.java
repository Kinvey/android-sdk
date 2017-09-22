package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by yuliya on 09/20/17.
 */

public class StringPrimitiveListInPerson extends Person {

    @Key("stringList")
    private List<String> stringList;

    public StringPrimitiveListInPerson() {
    }

    public StringPrimitiveListInPerson(String username) {
        this.username = username;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }
}

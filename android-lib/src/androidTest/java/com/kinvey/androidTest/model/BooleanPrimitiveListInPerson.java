package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by yuliya on 09/20/17.
 */

public class BooleanPrimitiveListInPerson extends Person {

    @Key("booleanList")
    private List<Boolean> booleanList;

    public BooleanPrimitiveListInPerson() {
    }

    public BooleanPrimitiveListInPerson(String username) {
        this.username = username;
    }

    public List<Boolean> getBooleanList() {
        return booleanList;
    }

    public void setBooleanList(List<Boolean> booleanList) {
        this.booleanList = booleanList;
    }
}

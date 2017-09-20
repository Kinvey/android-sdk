package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by yuliya on 09/20/17.
 */

public class IntegerPrimitiveListInPerson extends Person {

    @Key("integerList")
    private List<Integer> integerList;

    public IntegerPrimitiveListInPerson() {
    }

    public IntegerPrimitiveListInPerson(String username) {
        this.username = username;
    }

    public List<Integer> getIntegerList() {
        return integerList;
    }

    public void setIntegerList(List<Integer> integerList) {
        this.integerList = integerList;
    }
}

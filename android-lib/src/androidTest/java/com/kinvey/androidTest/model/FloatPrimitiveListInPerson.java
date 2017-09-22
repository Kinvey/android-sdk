package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by yuliya on 09/20/17.
 */

public class FloatPrimitiveListInPerson extends Person {

    @Key("floatList")
    private List<Float> floatList;

    public FloatPrimitiveListInPerson() {
    }

    public FloatPrimitiveListInPerson(String username) {
        this.username = username;
    }

    public List<Float> getFloatList() {
        return floatList;
    }

    public void setFloatList(List<Float> floatList) {
        this.floatList = floatList;
    }
}

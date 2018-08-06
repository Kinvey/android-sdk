package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

import java.util.List;

public class LongPrimitiveListInPerson extends Person {

    @Key("longList")
    private List<Long> longList;

    public LongPrimitiveListInPerson() {
    }

    public LongPrimitiveListInPerson(String username) {
        this.username = username;
    }

    public List<Long> getLongList() {
        return longList;
    }

    public void setLongList(List<Long> longList) {
        this.longList = longList;
    }
}

package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by yuliya on 09/20/17.
 */

public class ObjectListInPerson extends Person {

    @Key("stringGenericJsons")
    private List<StringGenericJson> stringGenericJsons;

    public ObjectListInPerson() {
    }

    public ObjectListInPerson(String username) {
        this.username = username;
    }

    public List<StringGenericJson> getStringGenericJsons() {
        return stringGenericJsons;
    }

    public void setStringGenericJsons(List<StringGenericJson> stringGenericJsons) {
        this.stringGenericJsons = stringGenericJsons;
    }
}

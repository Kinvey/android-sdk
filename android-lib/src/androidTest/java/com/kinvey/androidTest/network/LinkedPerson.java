package com.kinvey.androidTest.network;

import com.google.api.client.util.Key;
import com.kinvey.java.LinkedResources.LinkedGenericJson;

public class LinkedPerson extends LinkedGenericJson {

    public static final String COLLECTION = "LinkedPersonCollection";
    @Key
    private String _id;
    @Key
    private String username;

    public String getId() {
        return _id;
    }

    public LinkedPerson() {
        putFile("attachment");
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

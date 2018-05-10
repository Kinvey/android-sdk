package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class InternalUserEntity extends GenericJson {

    @Key
    private String street;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}

package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

/**
 * Created by yuliya on 11/23/17.
 */

public class PersonWithAddress extends Person {

    @Key
    private Address address;

    public PersonWithAddress() {
    }

    public PersonWithAddress(String username) {
        this.username = username;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

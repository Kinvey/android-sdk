package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by yuliya on 11/23/17.
 */

public class Address extends GenericJson {

    @Key
    private Person person;

    @Key
    private String addressField;

    public Address(String addressField) {
        this.addressField = addressField;
    }

    public Address() {
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}

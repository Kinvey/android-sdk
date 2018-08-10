package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

import java.util.List;

public class PersonArray extends Person {

    public static final String COLLECTION = "PersonArray";

    @Key
    private PersonArray[] array;

    @Key
    private PersonArray personArray;

    public PersonArray() {
    }

    public PersonArray(String name) {
        this.username = name;
    }

    public PersonArray[] getArray() {
        return array;
    }

    public void setArray(PersonArray[] array) {
        this.array = array;
    }

    public PersonArray getPersonArray() {
        return personArray;
    }

    public void setPersonArray(PersonArray personArray) {
        this.personArray = personArray;
    }
}

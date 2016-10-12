package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.java.dto.User;


public class Person extends User {

    public static final String COLLECTION = "Persons";

    public Person() {
    }

    @Key
    private String age;

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}

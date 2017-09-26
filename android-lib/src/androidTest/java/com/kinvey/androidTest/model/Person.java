package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.List;

public class Person extends GenericJson{

    public static final String COLLECTION = "Persons";

    public static final String LONG_NAME = "LoremIpsumissimplydummytextoftheprintingandtypesettingindustry";

    public Person() {
    }

    public Person(String username) {
        this.username = username;
    }

    @Key
    protected String age;

    @Key("_id")
    protected String id;

    @Key("username")
    protected String username;

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

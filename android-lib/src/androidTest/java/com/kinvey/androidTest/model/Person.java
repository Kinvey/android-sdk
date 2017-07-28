package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;


public class Person extends GenericJson{

    public static final String COLLECTION = "Persons";

    public static final String LONG_NAME = "LoremIpsumissimplydummytextoftheprintingandtypesettingindustry";

    public Person() {
    }

    @Key
    private String age;

    @Key("_id")
    private String id;

    @Key("username")
    private String username;

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

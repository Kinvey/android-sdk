package com.kinvey.androidTest.model;


import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class Author extends GenericJson{

    @Key
    private String name;

    public Author() {
    }

    public Author(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

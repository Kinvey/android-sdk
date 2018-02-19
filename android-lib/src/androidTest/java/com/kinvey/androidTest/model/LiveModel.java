package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class LiveModel extends GenericJson{

    public static final String COLLECTION = "Live Collection";

    public LiveModel() {
    }

    public LiveModel(String username) {
        this.username = username;
    }

    @Key
    protected String age;

    @Key("_id")
    protected String id;

    @Key
    private float height;

    @Key
    private Author author;

    @Key
    private long weight ;

    @Key
    private Integer carNumber ;

    @Key
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

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public Integer getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(Integer carNumber) {
        this.carNumber = carNumber;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}

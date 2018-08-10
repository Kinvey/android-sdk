package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.Date;

public class ModelWithDifferentTypeFields extends GenericJson {

    public static final String COLLECTION = "CustomCollection";

    @Key("_id")
    protected String id;


    @Key
    protected String username;

    @Key
    private int carNumber ;

    @Key
    private boolean isUseAndroid;

    @Key
    private Date date;

    @Key
    private float height;

    @Key
    private double time;

    public ModelWithDifferentTypeFields() {
    }

    public ModelWithDifferentTypeFields(String username, int carNumber, boolean isUseAndroid, Date date, float height, double time) {
        this.username = username;
        this.carNumber = carNumber;
        this.isUseAndroid = isUseAndroid;
        this.date = date;
        this.height = height;
        this.time = time;
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

    public int getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(int carNumber) {
        this.carNumber = carNumber;
    }

    public boolean isUseAndroid() {
        return isUseAndroid;
    }

    public void setUseAndroid(boolean useAndroid) {
        isUseAndroid = useAndroid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}

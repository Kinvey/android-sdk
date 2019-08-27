package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.Date;

public class DateExample extends GenericJson {

    public static final String COLLECTION = "DateExample";

    @Key("_id")
    protected String id;

    @Key
    private String field;

    @Key
    private Date date;

    public DateExample() {
        }


    public DateExample(String field, Date date) {
        this.field = field;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

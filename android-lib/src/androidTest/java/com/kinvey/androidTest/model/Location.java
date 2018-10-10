package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class Location  extends GenericJson {
    public static final String COLLECTION = "Location";

    public Location() {
    }

    @Key
    protected String age;

    @Key("_id")
    protected String id;

    @Key
    private String description;

    @Key
    private String address;

    @Key
    private Double[] _geoloc ;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double[] getGeo() {
        return _geoloc;
    }

    public void setGeo(Double[] geoloc) {
        this._geoloc = geoloc;
    }
}

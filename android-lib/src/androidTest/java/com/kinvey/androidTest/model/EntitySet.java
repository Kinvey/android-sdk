package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class EntitySet  extends GenericJson {
    public static final String COLLECTION = "EntitySet";

    public EntitySet() {
    }

    @Key("_id")
    protected String id;

    @Key
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

package com.kinvey.java.model;

import com.google.api.client.util.Key;

public class KinveyErrorResponse {

    @Key
    private String error;
    @Key
    private String description;
    @Key
    private String debug;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }
}

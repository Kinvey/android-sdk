package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.io.IOException;

public class KinveyUpdateSingleItemError extends IOException {

    public KinveyUpdateSingleItemError(Exception e, GenericJson entity) {
        this.entity = entity;
        this.errorMessage = e.getMessage();
    }

    @Key
    private GenericJson entity;
    @Key
    private long code;
    @Key
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errmsg) {
        this.errorMessage = errmsg;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public GenericJson getEntity() {
        return entity;
    }

    public void setIndex(GenericJson entity) {
        this.entity = entity;
    }
}

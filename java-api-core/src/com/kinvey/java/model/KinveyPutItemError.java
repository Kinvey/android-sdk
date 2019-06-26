package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.io.IOException;

public class KinveyPutItemError extends IOException {

    public KinveyPutItemError(IOException e, GenericJson entity) {
        this.entity = entity;
        this.errmsg = e.getMessage();
    }

    @Key
    private GenericJson entity;
    @Key
    private long code;
    @Key
    private String errmsg;

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
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

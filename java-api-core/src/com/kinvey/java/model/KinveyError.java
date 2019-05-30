package com.kinvey.java.model;

import com.google.api.client.util.Key;

public class KinveyError {

    @Key
    private long index;
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

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }
}

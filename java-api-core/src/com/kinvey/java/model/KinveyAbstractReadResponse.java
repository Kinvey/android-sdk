package com.kinvey.java.model;

import java.util.List;

/**
 * Created by yuliya on 10/30/17.
 */

public class KinveyAbstractReadResponse<T> extends KinveyAbstractResponse {

    private String lastREquest;

    private List<T> result;

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public String getLastREquest() {
        return lastREquest;
    }

    public void setLastREquest(String lastREquest) {
        this.lastREquest = lastREquest;
    }
}

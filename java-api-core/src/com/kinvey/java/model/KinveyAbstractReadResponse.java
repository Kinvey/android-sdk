package com.kinvey.java.model;

import java.util.List;

/**
 * Created by yuliya on 10/30/17.
 */

public class KinveyAbstractReadResponse<T> extends KinveyAbstractResponse {

    private String lastRequest;

    private List<T> result;

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public String getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(String lastRequest) {
        this.lastRequest = lastRequest;
    }
}

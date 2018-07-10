package com.kinvey.java.model;

import java.util.List;

/**
 * Created by yuliya on 10/30/17.
 */

public class KinveyReadResponse<T> extends AbstractKinveyExceptionsListResponse {

    private String lastRequestTime;

    private List<T> result;

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public String getLastRequestTime() {
        return lastRequestTime;
    }

    public void setLastRequestTime(String lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }
}

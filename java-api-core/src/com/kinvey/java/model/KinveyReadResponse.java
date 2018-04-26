package com.kinvey.java.model;

import java.util.List;

/**
 * Created by yuliya on 10/30/17.
 */

public class KinveyReadResponse<T> extends KinveyAbstractResponse {

    private List<T> result;

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

}

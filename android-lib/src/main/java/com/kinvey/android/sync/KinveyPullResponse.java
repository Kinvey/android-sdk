package com.kinvey.android.sync;

import com.kinvey.java.model.KinveyAbstractResponse;

import java.util.List;

public class KinveyPullResponse<T> extends KinveyAbstractResponse {

    private List<T> result;

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }
}

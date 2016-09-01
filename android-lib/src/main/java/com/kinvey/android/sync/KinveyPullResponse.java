package com.kinvey.android.sync;

import java.util.List;

public class KinveyPullResponse<T> {

    private List<T> result;

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }
}

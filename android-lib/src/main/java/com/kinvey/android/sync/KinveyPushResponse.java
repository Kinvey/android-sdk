package com.kinvey.android.sync;

import java.util.List;

public class KinveyPushResponse {

    private int successCount;
    private List<Exception> listOfExceptions;

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successSyncCount) {
        this.successCount = successSyncCount;
    }

    public List<Exception> getListOfExceptions() {
        return listOfExceptions;
    }

    public void setListOfExceptions(List<Exception> listOfExceptions) {
        this.listOfExceptions = listOfExceptions;
    }
}

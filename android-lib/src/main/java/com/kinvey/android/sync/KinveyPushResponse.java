package com.kinvey.android.sync;

import java.util.List;

public class KinveyPushResponse {

    private int successSyncCount;
    private List<String> listOfExceptions;

    public int getSuccessSyncCount() {
        return successSyncCount;
    }

    public void setSuccessSyncCount(int successSyncCount) {
        this.successSyncCount = successSyncCount;
    }

    public List<String> getListOfExceptions() {
        return listOfExceptions;
    }

    public void setListOfExceptions(List<String> listOfExceptions) {
        this.listOfExceptions = listOfExceptions;
    }
}

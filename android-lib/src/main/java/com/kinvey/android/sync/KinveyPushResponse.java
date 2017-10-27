package com.kinvey.android.sync;

import com.kinvey.java.model.KinveyAbstractResponse;

public class KinveyPushResponse extends KinveyAbstractResponse {

    private int successCount;

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successSyncCount) {
        this.successCount = successSyncCount;
    }

}

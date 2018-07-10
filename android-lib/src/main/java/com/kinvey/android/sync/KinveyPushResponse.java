package com.kinvey.android.sync;

import com.kinvey.java.model.AbstractKinveyExceptionsListResponse;

public class KinveyPushResponse extends AbstractKinveyExceptionsListResponse {

    private int successCount;

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successSyncCount) {
        this.successCount = successSyncCount;
    }

}

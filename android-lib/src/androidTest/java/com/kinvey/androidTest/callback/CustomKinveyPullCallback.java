package com.kinvey.androidTest.callback;

import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.java.model.KinveyPullResponse;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 09/14/17.
 */

public class CustomKinveyPullCallback implements KinveyPullCallback {

    private CountDownLatch latch;
    private KinveyPullResponse result;
    private Throwable error;

    public CustomKinveyPullCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(KinveyPullResponse result) {
        this.result = result;
        finish();
    }

    @Override
    public void onFailure(Throwable error) {
        this.error = error;
        finish();
    }

    void finish() {
        latch.countDown();
    }

    public KinveyPullResponse getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}

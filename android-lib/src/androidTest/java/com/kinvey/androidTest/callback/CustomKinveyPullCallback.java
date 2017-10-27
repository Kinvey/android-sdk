package com.kinvey.androidTest.callback;

import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.java.model.KinveyPullResponse;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 09/14/17.
 */

public class CustomKinveyPullCallback<T> implements KinveyPullCallback<T> {

    private CountDownLatch latch;
    private KinveyPullResponse<T> result;
    private Throwable error;

    public CustomKinveyPullCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(KinveyPullResponse<T> result) {
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

    public KinveyPullResponse<T> getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}

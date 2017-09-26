package com.kinvey.androidTest.callback;

import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 09/14/17.
 */

public class DefaultKinveyPushCallback implements KinveyPushCallback {

    private CountDownLatch latch;
    private KinveyPushResponse result;
    private Throwable error;

    public DefaultKinveyPushCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(KinveyPushResponse result) {
        this.result = result;
        finish();
    }

    @Override
    public void onFailure(Throwable error) {
        this.error = error;
        finish();
    }

    @Override
    public void onProgress(long current, long all) {

    }

    void finish() {
        latch.countDown();
    }

    public KinveyPushResponse getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}

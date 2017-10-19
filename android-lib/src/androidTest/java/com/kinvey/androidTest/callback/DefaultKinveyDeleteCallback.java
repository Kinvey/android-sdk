package com.kinvey.androidTest.callback;

import com.kinvey.android.callback.KinveyDeleteCallback;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 10/10/17.
 */

public class DefaultKinveyDeleteCallback implements KinveyDeleteCallback{

    private CountDownLatch latch;
    private Integer result;
    private Throwable error;

    public DefaultKinveyDeleteCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(Integer result) {
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

    public Integer getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }


}

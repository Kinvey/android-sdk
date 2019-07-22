package com.kinvey.androidTest.store.user;

import com.kinvey.java.core.KinveyClientCallback;

import java.util.concurrent.CountDownLatch;

public class DefaultKinveyVoidCallback implements KinveyClientCallback<Void> {

    private CountDownLatch latch;
    Void result;
    Throwable error;

    DefaultKinveyVoidCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(Void result) {
        this.result = result;
        finish();
    }

    @Override
    public void onFailure(Throwable error) {
        this.error = error;
        finish();
    }

    private void finish() {
        latch.countDown();
    }
}
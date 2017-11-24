package com.kinvey.androidTest.callback;

import com.kinvey.java.core.KinveyClientCallback;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 09/20/17.
 */

public class CustomKinveyClientCallback<T> implements KinveyClientCallback<T> {

    private CountDownLatch latch;
    private T result;
    private Throwable error;

    public CustomKinveyClientCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(T result) {
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

    public T getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}

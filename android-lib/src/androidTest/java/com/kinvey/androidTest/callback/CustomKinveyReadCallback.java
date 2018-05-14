package com.kinvey.androidTest.callback;

import com.kinvey.android.callback.KinveyReadCallback;
import com.kinvey.java.model.KinveyReadResponse;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 09/20/17.
 */

public class CustomKinveyReadCallback<T> implements KinveyReadCallback<T> {

    private CountDownLatch latch;
    private KinveyReadResponse<T> result;
    private Throwable error;

    public CustomKinveyReadCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(KinveyReadResponse<T> result) {
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

    public KinveyReadResponse<T> getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}
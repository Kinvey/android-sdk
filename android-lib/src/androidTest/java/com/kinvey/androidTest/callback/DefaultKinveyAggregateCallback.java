package com.kinvey.androidTest.callback;

import com.kinvey.java.core.KinveyAggregateCallback;
import com.kinvey.java.model.Aggregation;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 10/10/17.
 */

public class DefaultKinveyAggregateCallback extends KinveyAggregateCallback {

    private CountDownLatch latch;
    private Aggregation result;
    private Throwable error;

    public DefaultKinveyAggregateCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onFailure(Throwable error) {
        this.error = error;
        finish();
    }

    @Override
    public void onSuccess(Aggregation response) {
        this.result = response;
        finish();
    }

    void finish() {
        latch.countDown();
    }

    public Aggregation getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}

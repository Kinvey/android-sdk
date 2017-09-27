package com.kinvey.androidTest.callback;

import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.androidTest.model.Person;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 09/20/17.
 */

public class CustomKinveyListCallback<T> implements KinveyListCallback<T> {

    private CountDownLatch latch;
    private List<T> result;
    private Throwable error;

    public CustomKinveyListCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(List<T> result) {
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

    public List<T> getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}
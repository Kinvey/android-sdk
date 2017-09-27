package com.kinvey.androidTest.callback;

import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.androidTest.model.Person;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 09/14/17.
 */

public class DefaultKinveyListCallback implements KinveyListCallback<Person> {

    private CountDownLatch latch;
    private List<Person> result;
    private Throwable error;

    public DefaultKinveyListCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(List<Person> result) {
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

    public List<Person> getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}
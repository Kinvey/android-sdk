package com.kinvey.androidTest.callback;

import com.kinvey.android.callback.KinveyReadCallback;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.model.KinveyReadResponse;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 09/14/17.
 */

public class DefaultKinveyReadCallback implements KinveyReadCallback<Person> {

    private CountDownLatch latch;
    private KinveyReadResponse<Person> result;
    private Throwable error;

    public DefaultKinveyReadCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(KinveyReadResponse<Person> result) {
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

    public KinveyReadResponse<Person> getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}
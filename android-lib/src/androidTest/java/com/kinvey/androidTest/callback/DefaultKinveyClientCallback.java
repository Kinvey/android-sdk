package com.kinvey.androidTest.callback;

import com.kinvey.androidTest.model.Person;
import com.kinvey.java.core.KinveyClientCallback;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 09/14/17.
 */

public class DefaultKinveyClientCallback implements KinveyClientCallback<Person> {

    private CountDownLatch latch;
    private Person result;
    private Throwable error;

    public DefaultKinveyClientCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(Person result) {
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

    public Person getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}

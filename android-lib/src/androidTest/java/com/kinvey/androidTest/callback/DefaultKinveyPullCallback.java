package com.kinvey.androidTest.callback;

import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.android.sync.KinveyPullResponse;
import com.kinvey.androidTest.model.Person;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 09/14/17.
 */

public class DefaultKinveyPullCallback implements KinveyPullCallback<Person> {

    private CountDownLatch latch;
    private KinveyPullResponse<Person> result;
    private Throwable error;

    public DefaultKinveyPullCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(KinveyPullResponse<Person> result) {
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

    public KinveyPullResponse<Person> getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}

package com.kinvey.androidTest.store.user;

import com.kinvey.android.model.User;
import com.kinvey.java.core.KinveyClientCallback;

import java.util.concurrent.CountDownLatch;

public class DefaultKinveyClientCallback implements KinveyClientCallback<User> {

    private CountDownLatch latch;
    User result;
    Throwable error;

    DefaultKinveyClientCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(User user) {
        this.result = user;
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
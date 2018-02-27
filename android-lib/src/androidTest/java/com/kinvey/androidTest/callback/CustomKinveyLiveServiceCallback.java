package com.kinvey.androidTest.callback;

import com.kinvey.java.store.KinveyDataStoreLiveServiceCallback;
import com.kinvey.java.store.KinveyLiveServiceStatus;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuliya on 02/26/17.
 */

public class CustomKinveyLiveServiceCallback<T> implements KinveyDataStoreLiveServiceCallback<T> {

    private CountDownLatch latch;
    private T result;
    private KinveyLiveServiceStatus status;
    private Throwable error;

    public CustomKinveyLiveServiceCallback(CountDownLatch latch) {
        this.latch = latch;
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

    @Override
    public void onNext(T next) {
        this.result = next;
        finish();
    }

    @Override
    public void onError(Exception e) {
        this.error = e;
        finish();
    }

    @Override
    public void onStatus(KinveyLiveServiceStatus status) {
        this.status = status;
        finish();
    }
}

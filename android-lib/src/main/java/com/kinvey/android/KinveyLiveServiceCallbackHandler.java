package com.kinvey.android;

import android.os.Handler;

import com.kinvey.java.store.KinveyDataStoreLiveServiceCallback;
import com.kinvey.java.store.KinveyLiveServiceStatus;

public class KinveyLiveServiceCallbackHandler<T> extends Handler {

    public void onNext(final T t, final KinveyDataStoreLiveServiceCallback<T> callback) {

        this.post(new Runnable() {
            @Override
            public void run() {
                callback.onNext(t);
            }
        });
    }

    public void onError(final Exception exception, final KinveyDataStoreLiveServiceCallback<T> callback) {
        this.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(exception);
            }
        });
    }

    public void onStatus(final KinveyLiveServiceStatus status, final KinveyDataStoreLiveServiceCallback<T> callback) {
        this.post(new Runnable() {
            @Override
            public void run() {
                callback.onStatus(status);
            }
        });
    }

}

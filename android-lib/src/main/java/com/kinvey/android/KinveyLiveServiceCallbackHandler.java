package com.kinvey.android;

import android.os.Handler;

import com.kinvey.java.store.KinveyDataStoreRealtimeCallback;
import com.kinvey.java.store.KinveyRealtimeStatus;

public class KinveyLiveServiceCallbackHandler<T> extends Handler {

    public void onNext(final T t, final KinveyDataStoreRealtimeCallback<T> callback) {

        this.post(new Runnable() {
            @Override
            public void run() {
                callback.onNext(t);
            }
        });
    }

    public void onError(final Exception exception, final KinveyDataStoreRealtimeCallback<T> callback) {
        this.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(exception);
            }
        });
    }

    public void onStatus(final KinveyRealtimeStatus status, final KinveyDataStoreRealtimeCallback<T> callback) {
        this.post(new Runnable() {
            @Override
            public void run() {
                callback.onStatus(status);
            }
        });
    }

}

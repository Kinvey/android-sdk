package com.kinvey.android;

import android.os.Handler;

import com.kinvey.java.core.KinveyCancellableCallback;
import com.kinvey.java.core.KinveyClientCallback;

public class KinveyCallbackHandler<T> extends Handler {

    public void onResult(final T t, final KinveyClientCallback<T> callback) {

        this.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(t);
            }
        });
    }

    public void onFailure(final Throwable error, final KinveyClientCallback<T> callback) {
        this.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(error);
            }
        });
    }

    public void onCancel(final KinveyCancellableCallback<T> callback) {
        this.post(new Runnable() {
            @Override
            public void run() {
                callback.onCancelled();
            }
        });
    }

}

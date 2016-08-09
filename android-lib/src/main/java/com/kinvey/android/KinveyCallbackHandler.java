package com.kinvey.android;

import android.os.Handler;
import android.os.Looper;

import com.kinvey.java.core.KinveyCancellableCallback;
import com.kinvey.java.core.KinveyClientCallback;

public class KinveyCallbackHandler<T> extends Handler {

    Handler uiHandler;

    public KinveyCallbackHandler() {
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public KinveyCallbackHandler(Looper looper) {
        super(looper);
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public void onResult(final T t, final KinveyClientCallback callback) {

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(t);
            }
        });
    }

    public void onFailure(final Throwable error, final KinveyClientCallback callback) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(error);
            }
        });
    }

    public void onCancel(final KinveyCancellableCallback callback) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onCancelled();
            }
        });
    }

    public void onProgress() {

    }

}

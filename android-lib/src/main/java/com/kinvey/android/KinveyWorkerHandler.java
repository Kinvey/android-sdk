package com.kinvey.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.kinvey.java.core.KinveyClientCallback;

public class KinveyWorkerHandler<T> extends Handler {

    public KinveyWorkerHandler(Looper looper) {
        super(looper);
    }

    public void onResult(final T t, final KinveyClientCallback callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(t);
            }
        });
    }

    public void onFailure(final Throwable error, final KinveyClientCallback callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(error);
            }
        });

    }

    public void onProgress() {

    }

    @Override
    public String getMessageName(Message message) {
        return super.getMessageName(message);
    }
}

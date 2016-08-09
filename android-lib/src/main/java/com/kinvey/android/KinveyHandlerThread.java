package com.kinvey.android;

import android.os.Handler;
import android.os.HandlerThread;

public class KinveyHandlerThread extends HandlerThread {

    private Handler mWorkerHandler;

    public KinveyHandlerThread(String name, int priority) {
        super(name, priority);
    }

    public KinveyHandlerThread(String name) {
        super(name);
    }

    public void postTask(Runnable task){
        mWorkerHandler.post(task);
    }

    @Override
    protected void onLooperPrepared() {
        mWorkerHandler = new Handler(getLooper());
    }

}

package com.kinvey.android;

import android.os.Handler;
import android.os.HandlerThread;

public class KinveyWorkerThread extends HandlerThread {

    private KinveyWorkerHandler mWorkerHandler;

    public KinveyWorkerThread(String name, int priority) {
        super(name, priority);
    }

    public KinveyWorkerThread(String name) {
        super(name);
    }

    public void postTask(Runnable task){
        mWorkerHandler.post(task);
    }

    public void prepareHandler(){
        mWorkerHandler = new KinveyWorkerHandler(getLooper());
    }

    public KinveyWorkerHandler getWorkerHandler() {
        return mWorkerHandler;
    }
}

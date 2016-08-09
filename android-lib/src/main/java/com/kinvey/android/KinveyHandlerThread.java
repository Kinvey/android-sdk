package com.kinvey.android;

import android.os.HandlerThread;

public class KinveyHandlerThread extends HandlerThread {

    private KinveyCallbackHandler mCallbackHandler;

    public KinveyHandlerThread(String name, int priority) {
        super(name, priority);
    }

    public KinveyHandlerThread(String name) {
        super(name);
    }

    public void postTask(Runnable task){
        mCallbackHandler.post(task);
    }

    public void prepareHandler(){
        mCallbackHandler = new KinveyCallbackHandler(getLooper());
    }
}

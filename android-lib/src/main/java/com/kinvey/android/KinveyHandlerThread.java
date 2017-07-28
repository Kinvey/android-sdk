package com.kinvey.android;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.ConcurrentLinkedQueue;

public class KinveyHandlerThread extends HandlerThread {

    private Handler mWorkerHandler;
    private ConcurrentLinkedQueue<Runnable> pendingQueue;

    public KinveyHandlerThread(String name, int priority) {
        super(name, priority);
        pendingQueue = new ConcurrentLinkedQueue<>();
    }

    public KinveyHandlerThread(String name) {
        super(name);
        pendingQueue = new ConcurrentLinkedQueue<>();
    }

    public synchronized void postTask(Runnable task){
        if (mWorkerHandler != null) {
            mWorkerHandler.post(task);
        } else {
            pendingQueue.add(task);
        }
    }

    @Override
    protected synchronized void onLooperPrepared() {
        mWorkerHandler = new Handler(getLooper());
        if (pendingQueue.size() > 0) {
            for (Runnable task:pendingQueue) {
                postTask(task);
            }
        }
    }

    synchronized void stopHandlerThread() {
        if (mWorkerHandler != null) {
            mWorkerHandler.post(new Runnable() {
                @Override
                public void run() {
                    Looper.myLooper().quit();
                    mWorkerHandler.removeCallbacksAndMessages(null);
                }
            });
        }
    }

}

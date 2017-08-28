package com.kinvey.androidTest;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by yuliya on 08/15/17.
 */

public class LooperThread extends Thread {
    public Handler mHandler;
    private Runnable runnable;

    public LooperThread(Runnable runnable) {
        super();
        this.runnable = runnable;
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (Looper.myLooper() != null) {
                    Looper.myLooper().quit();
                    mHandler.removeCallbacksAndMessages(null);
                }
            }
        };
        runnable.run();
        Looper.loop();
    }

}

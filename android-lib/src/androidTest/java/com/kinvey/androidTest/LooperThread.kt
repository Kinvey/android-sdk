package com.kinvey.androidTest

import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * Created by yuliya on 08/15/17.
 */

class LooperThread(private val runnable: Runnable) : Thread() {
    var mHandler: Handler? = null

    override fun run() {
        Looper.prepare()
        mHandler = object : Handler() {
            override fun handleMessage(msg: Message?) {
                if (Looper.myLooper() != null) {
                    Looper.myLooper()!!.quit()
                    mHandler!!.removeCallbacksAndMessages(null)
                }
            }
        }
        runnable.run()
        Looper.loop()
    }
}
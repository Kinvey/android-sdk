package com.kinvey.androidTest.callback

import com.kinvey.android.callback.KinveyDeleteCallback
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 10/10/17.
 */

class DefaultKinveyDeleteCallback(private val latch: CountDownLatch) : KinveyDeleteCallback {
    var result: Int? = null
        private set
    var error: Throwable? = null
        private set

    override fun onSuccess(result: Int?) {
        this.result = result
        finish()
    }

    override fun onFailure(error: Throwable?) {
        this.error = error
        finish()
    }

    internal fun finish() {
        latch.countDown()
    }

}
package com.kinvey.androidTest.callback

import com.kinvey.java.core.KinveyClientCallback
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 09/20/17.
 */

class CustomKinveyClientCallback<T>(private val latch: CountDownLatch) : KinveyClientCallback<T> {
    var result: T? = null
        private set
    var error: Throwable? = null
        private set

    override fun onSuccess(result: T?) {
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
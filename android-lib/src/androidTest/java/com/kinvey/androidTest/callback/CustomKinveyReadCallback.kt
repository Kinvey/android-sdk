package com.kinvey.androidTest.callback

import com.kinvey.android.callback.KinveyReadCallback
import com.kinvey.java.model.KinveyReadResponse
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 09/20/17.
 */

class CustomKinveyReadCallback<T>(private val latch: CountDownLatch) : KinveyReadCallback<T> {
    var result: KinveyReadResponse<T>? = null
        private set
    var error: Throwable? = null
        private set

    override fun onSuccess(result: KinveyReadResponse<T>?) {
        this.result = result
        finish()
    }

    override fun onFailure(error: Throwable) {
        this.error = error
        finish()
    }

    internal fun finish() {
        latch.countDown()
    }

}
package com.kinvey.androidTest.callback

import com.kinvey.android.callback.KinveyListCallback
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 04/10/18.
 */

class CustomKinveyListCallback<T>(private val latch: CountDownLatch) : KinveyListCallback<T> {

    private var result: List<T>? = null
    var error: Throwable? = null
        private set

    override fun onSuccess(result: List<T>) {
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

    fun getResult(): List<T>? {
        return result
    }

}
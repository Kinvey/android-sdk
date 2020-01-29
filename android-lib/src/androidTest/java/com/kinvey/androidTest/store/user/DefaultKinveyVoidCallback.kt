package com.kinvey.androidTest.store.user

import com.kinvey.java.core.KinveyClientCallback
import java.util.concurrent.CountDownLatch

class DefaultKinveyVoidCallback(private val latch: CountDownLatch) : KinveyClientCallback<Void> {

    var result: Void? = null
    var error: Throwable? = null

    override fun onSuccess(result: Void?) {
        this.result = result
        finish()
    }

    override fun onFailure(error: Throwable?) {
        this.error = error
        finish()
    }

    private fun finish() {
        latch.countDown()
    }
}
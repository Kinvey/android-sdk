package com.kinvey.androidTest.store.user

import com.kinvey.android.model.User
import com.kinvey.java.core.KinveyClientCallback
import java.util.concurrent.CountDownLatch

class DefaultKinveyClientCallback(private val latch: CountDownLatch) : KinveyClientCallback<User> {

    var result: User? = null
    var error: Throwable? = null

    override fun onSuccess(result: User?) {
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
package com.kinvey.androidTest.callback

import com.kinvey.android.sync.KinveyPullCallback
import com.kinvey.java.model.KinveyPullResponse
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 09/14/17.
 */

class CustomKinveyPullCallback(private val latch: CountDownLatch) : KinveyPullCallback {
    var result: KinveyPullResponse? = null
        private set
    var error: Throwable? = null
        private set

    override fun onSuccess(result: KinveyPullResponse) {
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
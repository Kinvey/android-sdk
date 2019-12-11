package com.kinvey.androidTest.callback

import com.kinvey.android.sync.KinveyPushCallback
import com.kinvey.android.sync.KinveyPushResponse
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 09/14/17.
 */

class DefaultKinveyPushCallback(private val latch: CountDownLatch) : KinveyPushCallback {
    var result: KinveyPushResponse? = null
        private set
    var error: Throwable? = null
        private set

    override fun onSuccess(result: KinveyPushResponse) {
        this.result = result
        finish()
    }

    override fun onFailure(error: Throwable) {
        this.error = error
        finish()
    }

    override fun onProgress(current: Long, all: Long) {}
    internal fun finish() {
        latch.countDown()
    }

}
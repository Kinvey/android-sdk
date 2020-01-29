package com.kinvey.androidTest.callback

import com.kinvey.android.sync.KinveyPushResponse
import com.kinvey.android.sync.KinveySyncCallback
import com.kinvey.java.model.KinveyPullResponse
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 12/27/17.
 */

class CustomKinveySyncCallback(private val latch: CountDownLatch) : KinveySyncCallback {
    var result: KinveyPullResponse? = null
    var kinveyPushResponse: KinveyPushResponse? = null
    var error: Throwable? = null
    override fun onSuccess(kinveyPushResponse: KinveyPushResponse?, kinveyPullResponse: KinveyPullResponse?) {
        result = kinveyPullResponse
        this.kinveyPushResponse = kinveyPushResponse
        finish()
    }

    override fun onPullStarted() {}
    override fun onPushStarted() {}
    override fun onPullSuccess(kinveyPullResponse: KinveyPullResponse?) {}
    override fun onPushSuccess(kinveyPushResponse: KinveyPushResponse?) {}
    override fun onFailure(t: Throwable?) {
        this.error = error
        finish()
    }

    private fun finish() {
        latch.countDown()
    }

}
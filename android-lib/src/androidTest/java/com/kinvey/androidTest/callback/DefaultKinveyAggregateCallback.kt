package com.kinvey.androidTest.callback

import com.kinvey.java.core.KinveyAggregateCallback
import com.kinvey.java.model.Aggregation
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 10/10/17.
 */

class DefaultKinveyAggregateCallback(private val latch: CountDownLatch) : KinveyAggregateCallback() {
    var result: Aggregation? = null
        private set
    var error: Throwable? = null
        private set

    override fun onFailure(error: Throwable?) {
        this.error = error
        finish()
    }

    override fun onSuccess(response: Aggregation?) {
        result = response
        finish()
    }

    internal fun finish() {
        latch.countDown()
    }

}
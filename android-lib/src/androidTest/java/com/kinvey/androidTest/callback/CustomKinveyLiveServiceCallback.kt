package com.kinvey.androidTest.callback

import com.kinvey.java.store.KinveyDataStoreLiveServiceCallback
import com.kinvey.java.store.KinveyLiveServiceStatus
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 02/26/17.
 */

class CustomKinveyLiveServiceCallback<T>(private val latch: CountDownLatch) : KinveyDataStoreLiveServiceCallback<T> {
    var result: T? = null
        private set
    private var status: KinveyLiveServiceStatus? = null
    var error: Throwable? = null
        private set

    internal fun finish() {
        latch.countDown()
    }

    override fun onNext(next: T) {
        result = next
        finish()
    }

    override fun onError(e: Exception) {
        this.error = e
        finish()
    }

    override fun onStatus(status: KinveyLiveServiceStatus) {
        this.status = status
        finish()
    }

}
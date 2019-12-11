package com.kinvey.androidTest.callback

import com.kinvey.android.callback.KinveyReadCallback
import com.kinvey.androidTest.model.Person
import com.kinvey.java.model.KinveyReadResponse
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 09/14/17.
 */

class DefaultKinveyReadCallback(private val latch: CountDownLatch) : KinveyReadCallback<Person> {
    var result: KinveyReadResponse<Person>? = null
        private set
    var error: Throwable? = null
        private set

    override fun onSuccess(result: KinveyReadResponse<Person>?) {
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
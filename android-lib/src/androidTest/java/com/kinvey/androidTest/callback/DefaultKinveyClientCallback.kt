package com.kinvey.androidTest.callback

import com.kinvey.androidTest.model.Person
import com.kinvey.java.core.KinveyClientCallback
import java.util.concurrent.CountDownLatch

/**
 * Created by yuliya on 09/14/17.
 */

class DefaultKinveyClientCallback(private val latch: CountDownLatch) : KinveyClientCallback<Person> {
    var result: Person? = null
        private set
    var error: Throwable? = null
        private set

    override fun onSuccess(result: Person) {
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
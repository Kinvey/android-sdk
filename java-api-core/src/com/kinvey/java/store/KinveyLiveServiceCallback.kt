package com.kinvey.java.store

/**
 * Created by yuliya on 2/20/17.
 */

interface KinveyLiveServiceCallback<T> {

    fun onNext(next: T)

    fun onError(e: Exception)

    fun onStatus(status: KinveyLiveServiceStatus)

}

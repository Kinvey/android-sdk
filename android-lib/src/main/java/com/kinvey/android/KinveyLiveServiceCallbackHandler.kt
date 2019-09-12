package com.kinvey.android

import android.os.Handler

import com.kinvey.java.store.KinveyDataStoreLiveServiceCallback
import com.kinvey.java.store.KinveyLiveServiceStatus

class KinveyLiveServiceCallbackHandler<T> : Handler() {

    fun onNext(t: T, callback: KinveyDataStoreLiveServiceCallback<T>) {
        this.post { callback.onNext(t) }
    }

    fun onError(exception: Exception, callback: KinveyDataStoreLiveServiceCallback<T>) {
        this.post { callback.onError(exception) }
    }

    fun onStatus(status: KinveyLiveServiceStatus, callback: KinveyDataStoreLiveServiceCallback<T>) {
        this.post { callback.onStatus(status) }
    }
}

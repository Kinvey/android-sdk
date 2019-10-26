package com.kinvey.android

import android.os.Handler

import com.kinvey.java.core.KinveyCancellableCallback
import com.kinvey.java.core.KinveyClientCallback

open class KinveyCallbackHandler<T> : Handler() {

    fun onResult(t: T, callback: KinveyClientCallback<T>?) {
        this.post { callback?.onSuccess(t) }
    }

    fun onFailure(error: Throwable, callback: KinveyClientCallback<T>?) {
        this.post { callback?.onFailure(error) }
    }

    fun onCancel(callback: KinveyCancellableCallback<T>?) {
        this.post { callback?.onCancelled() }
    }
}

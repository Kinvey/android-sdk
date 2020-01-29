package com.kinvey.android.callback

import com.kinvey.java.core.KinveyCancellableCallback
import com.kinvey.java.core.MediaHttpUploader
import com.kinvey.java.core.UploaderProgressListener
import java.io.IOException

interface AsyncUploaderProgressListener<T> : UploaderProgressListener, KinveyCancellableCallback<T> {
    override fun onSuccess(result: T?)
    override fun onFailure(error: Throwable?)
    @Throws(IOException::class)
    override fun progressChanged(uploader: MediaHttpUploader?)

    override fun onCancelled()
    override var isCancelled: Boolean
}
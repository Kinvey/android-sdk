package com.kinvey.android.callback

import com.kinvey.java.core.DownloaderProgressListener
import com.kinvey.java.core.KinveyCancellableCallback
import com.kinvey.java.core.MediaHttpDownloader
import java.io.IOException

interface AsyncDownloaderProgressListener<T> : DownloaderProgressListener, KinveyCancellableCallback<T> {
    override fun onSuccess(result: T?)
    override fun onFailure(error: Throwable?)
    @Throws(IOException::class)
    override fun progressChanged(uploader: MediaHttpDownloader)

    override fun onCancelled()
    override var isCancelled: Boolean
}
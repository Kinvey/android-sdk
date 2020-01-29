package com.kinvey.android.async


import com.kinvey.android.callback.AsyncDownloaderProgressListener
import com.kinvey.java.Logger
import com.kinvey.java.core.DownloaderProgressListener
import com.kinvey.java.core.MediaHttpDownloader

import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class AsyncDownloadRequest<T>(scope: Any, method: Method?, callback: AsyncDownloaderProgressListener<T>?, vararg args: Any?) : AsyncRequest<T>(scope, method, callback, *args) {

    var listener: DownloaderProgressListener? = null

    init {
        listener = object : DownloaderProgressListener {
            @Throws(IOException::class)
            override fun progressChanged(downloader: MediaHttpDownloader?) {
                val myRunnable = Runnable {
                    if (callback != null) {
                        Logger.INFO("notifying callback")
                        try {
                            callback?.progressChanged(downloader)
                        } catch (e: IOException) {
                            callback?.onFailure(e)
                        }

                    }
                }
                kinveyCallbackHandler.post(myRunnable)
            }
        }
    }

    @Throws(IOException::class, InvocationTargetException::class, IllegalAccessException::class)
    override fun executeAsync(): T {
        val newArgs = arrayOf(*args, listener)
        return mMethod?.invoke(scope, *newArgs) as T
    }
}

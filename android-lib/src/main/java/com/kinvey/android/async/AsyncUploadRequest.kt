package com.kinvey.android.async


import com.kinvey.android.callback.AsyncUploaderProgressListener
import com.kinvey.java.Logger
import com.kinvey.java.core.MediaHttpUploader
import com.kinvey.java.core.UploaderProgressListener

import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Arrays

class AsyncUploadRequest<T>(scope: Any, method: Method?, callback: AsyncUploaderProgressListener<T>?, vararg args: Any?) : AsyncRequest<T>(scope, method, callback, *args) {

    var listener: UploaderProgressListener? = null

    init {
        listener = object : UploaderProgressListener {
            @Throws(IOException::class)
            override fun progressChanged(uploader: MediaHttpUploader) {
                val myRunnable = Runnable {
                    if (getCallback() != null) {
                        Logger.INFO("notifying callback")
                        try {
                            callback?.progressChanged(uploader)
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

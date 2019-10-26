/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */
package com.kinvey.android

import android.util.Log
import com.kinvey.android.Client.Companion.kinveyHandlerThread
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger.Companion.INFO
import com.kinvey.java.core.AsyncExecutor
import com.kinvey.java.core.KinveyCancellableCallback
import com.kinvey.java.core.KinveyClientCallback
import java.io.IOException
import java.lang.reflect.InvocationTargetException

/**
 *
 * Abstract AsyncClientRequest class - used for Kinvey Callbacks.
 *
 * @author edwardf
 * @since 2.0
 * @version $Id: $
 */
abstract class AsyncClientRequest<Result>(callback: KinveyClientCallback<Result>?)
    : Runnable, AsyncExecutor<Result> {
    private var error: Throwable? = null
    /**
     * Get the callback for this request
     *
     * @return the callback for this request, or `null` if one hasn't been set.
     */
    open var callback: KinveyClientCallback<Result>? = null
    protected var kinveyCallbackHandler: KinveyCallbackHandler<Result>
//    private static final Executor KINVEY_SERIAL_EXECUTOR = new KinveySerialExecutor();

    fun execute() {
        INFO("Calling AsyncClientRequest#execute")
        kinveyHandlerThread?.postTask(this)
    }

    override fun run() {
        INFO("Calling AsyncClientRequest#run")
        var result: Result? = null
        if (callback == null) {
            return
        }
        try {
            if (!hasCancelled()) {
                INFO("Start executeAsync")
                result = executeAsync()
                INFO("Finish executeAsync")
            }
        } catch (e: Throwable) {
//            e.printStackTrace();
            error = if (e is InvocationTargetException) {
                e.targetException
            } else {
                e
            }
            Log.d("TEST", "test", e)
        }
//        KinveyCallbackHandler kinveyCallbackHandler = new KinveyCallbackHandler();

        if (hasCancelled()) {
            INFO("Calling kinveyCallbackHandler.onCancel")
            kinveyCallbackHandler.onCancel(callback as KinveyCancellableCallback<Result>)
        } else if (error != null){
            INFO("Calling kinveyCllbackHandler.onFailure")
            kinveyCallbackHandler.onFailure(error as Throwable, callback)
        } else {
            INFO("Calling kinveyCllbackHandler.onResult")
            kinveyCallbackHandler.onResult(result as Result, callback)
        }
    }

    /**
     * This method will be executed Asynchronously.
     *
     * @return a T object.
     * @throws java.io.IOException if any.
     */

    @Throws(IOException::class, InvocationTargetException::class, IllegalAccessException::class, InstantiationException::class, KinveyException::class)
    protected abstract fun executeAsync(): Result?

    override fun notify(obj: Result) {
        INFO("notifying async client request")
        val myRunnable = Runnable {
            if (callback != null) {
                INFO("notifying callback")
                callback?.onSuccess(obj)
            }
        }
        kinveyCallbackHandler.post(myRunnable)
    }

    /**
     * Check if there is callback, it is cancellable, and finally, if it has cancelled.
     *
     * @return
     */
    private fun hasCancelled(): Boolean {
        return callback != null && callback is KinveyCancellableCallback
                && (callback as KinveyCancellableCallback<Result>?)?.isCancelled == true
    }

    /**
     *
     * Constructor for AsyncClientRequest.
     *
     * @param callback a [com.kinvey.java.core.KinveyClientCallback] object.
     */

    init {
        this.callback = callback
        kinveyCallbackHandler = KinveyCallbackHandler()
    }
}
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
package com.kinvey.android;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.kinvey.java.Logger;
import com.kinvey.java.core.AsyncExecutor;
import com.kinvey.java.core.KinveyCancellableCallback;
import com.kinvey.java.core.KinveyClientCallback;

/**
 * <p>Abstract AsyncClientRequest class - used for Kinvey Callbacks.</p>
 *
 * @author edwardf
 * @since 2.0
 * @version $Id: $
 */
public abstract class AsyncClientRequest<Result> implements Runnable, AsyncExecutor<Result> {

    public enum ExecutorType {
        KINVEYSERIAL,
        ANDROIDSERIAL,
        ANDROIDTHREADPOOL
    }

    private Throwable error;
    private KinveyClientCallback callback;
    protected KinveyCallbackHandler kinveyCallbackHandler;
//    private static final Executor KINVEY_SERIAL_EXECUTOR = new KinveySerialExecutor();

    /**
     * <p>Constructor for AsyncClientRequest.</p>
     *
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     */
    public AsyncClientRequest(KinveyClientCallback callback) {
        this.callback = callback;
        //todo
        kinveyCallbackHandler = new KinveyCallbackHandler();
    }

    public void execute() {
        Client.sharedInstance().getKinveyHandlerThread().postTask(this);
    }

    public void execute(ExecutorType executorType) {
        Client.sharedInstance().getKinveyHandlerThread().postTask(this);
    }

    @Override
    public void run() {
        Result result = null;
        if(callback == null){
            return;
        }
        try{
            if (!hasCancelled()){
                result = executeAsync();
            }
        }catch(Throwable e){
//            e.printStackTrace();
            error = e;
            Log.d("TEST","test", e);
        }
//        KinveyCallbackHandler kinveyCallbackHandler = new KinveyCallbackHandler();
        if (hasCancelled()){
            kinveyCallbackHandler.onCancel(((KinveyCancellableCallback) callback));
        }else if(error != null){
            kinveyCallbackHandler.onFailure(error, callback);
        }else{
            kinveyCallbackHandler.onResult(result, callback);
        }
    }

    /**
     * This method will be executed Asynchronously.
     *
     * @return a T object.
     * @throws java.io.IOException if any.
     */

    protected abstract Result executeAsync() throws IOException, InvocationTargetException, IllegalAccessException;

    /**
     * Get the callback for this request
     *
     * @return the callback for this request, or {@code null} if one hasn't been set.
     */
    public KinveyClientCallback getCallback(){
        return callback;
    }

    @Override
    public void notify(final Result object){
    	Logger.INFO("notifying async client request");

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if (getCallback() != null) {
                	Logger.INFO("notifying callback");
                    getCallback().onSuccess(object);
                }

            }
        };
        kinveyCallbackHandler.post(myRunnable);
    }


    /**
     * Check if there is callback, it is cancellable, and finally, if it has cancelled.
     *
     * @return
     */
    private boolean hasCancelled(){
        return (callback != null && (callback instanceof KinveyCancellableCallback) && ((KinveyCancellableCallback) callback).isCancelled());
    }

}

/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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

import android.os.AsyncTask;
import android.os.Build;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.concurrent.Executor;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.kinvey.java.core.AsyncExecutor;
import com.kinvey.java.core.KinveyClientCallback;

/**
 * <p>Abstract AsyncClientRequest class - used for Kinvey Callbacks.</p>
 *
 * @author edwardf
 * @since 2.0
 * @version $Id: $
 */
public abstract class AsyncClientRequest<T> extends AsyncTask<Object, Void, T> implements AsyncExecutor<T> {

    public enum ExecutorType {
        KINVEYSERIAL,
        ANDROIDSERIAL,
        ANDROIDTHREADPOOL
    }

    private Throwable error;

    private KinveyClientCallback callback;

    private static final Executor KINVEY_SERIAL_EXECUTOR = new KinveySerialExecutor();

    /**
     * <p>Constructor for AsyncClientRequest.</p>
     *
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     */
    public AsyncClientRequest(KinveyClientCallback callback) {
        this.callback = callback;
    }


    /** {@inheritDoc} */
    @Override
    protected T doInBackground(Object ... params) {
        T result = null;

        try{
            result = executeAsync();
        }catch(Throwable e){
            e.printStackTrace();
            error = e;
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    protected void onPostExecute(T response) {
        if(callback == null){
            return;
        }

        if(error != null){
            callback.onFailure(error);
        }else{
            callback.onSuccess(response);
        }
    }

    /**
     * This method will be executed Asynchronously.
     *
     * @return a T object.
     * @throws java.io.IOException if any.
     */
    protected abstract T executeAsync() throws IOException, InvocationTargetException, IllegalAccessException;


    public AsyncTask<Object, Void, T> execute(ExecutorType type, Object... params) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            switch(type) {
                case KINVEYSERIAL:
                    return super.executeOnExecutor(KINVEY_SERIAL_EXECUTOR, params);
                case ANDROIDSERIAL:
                    return super.executeOnExecutor(SERIAL_EXECUTOR, params);
                case ANDROIDTHREADPOOL:
                    return super.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
                default:
                    throw new RuntimeException("Invalid Executor defined");
            }
        } else {
            return super.execute(params);
        }
    }

    /**
     * Get the callback for this request
     *
     * @return the callback for this request, or {@code null} if one hasn't been set.
     */
    public KinveyClientCallback getCallback(){
        return callback;
    }

    /**
     * This class is a copy of the native SerialExecutor from AOSP.  It is duplicated here to allow
     * AsyncClientRequest to run in their own Thread Pool seperate from the main application's AsyncTasks, but keep
     * the concurrency benefits of only having a single AsyncTask thread.  AndroidClientRequest will run in
     * its own thread pool of one.
     */
    private static class KinveySerialExecutor implements Executor {
        final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
        Runnable mActive;

        public synchronized void execute(final Runnable r) {
            mTasks.offer(new Runnable() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        scheduleNext();
                    }
                }
            });
            if (mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            if ((mActive = mTasks.poll()) != null) {
                THREAD_POOL_EXECUTOR.execute(mActive);
            }
        }
    }

    @Override
    public void notify(final T object){
        Log.i(Client.TAG, "notifying async client request");
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if (getCallback() != null) {
                    Log.i(Client.TAG, "notifying callback");

                    getCallback().onSuccess(object);
                }

            }
        };
        mainHandler.post(myRunnable);



    }

}

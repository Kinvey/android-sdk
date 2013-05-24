/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.android;

import android.os.AsyncTask;
import android.os.Build;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.concurrent.Executor;

import com.kinvey.java.core.KinveyClientCallback;

/**
 * <p>Abstract AsyncClientRequest class - used for Kinvey Callbacks.</p>
 *
 * @author edwardf
 * @since 2.0
 * @version $Id: $
 */
public abstract class AsyncClientRequest<T> extends AsyncTask<Object, Void, T> {

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
    protected abstract T executeAsync() throws IOException;


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

}

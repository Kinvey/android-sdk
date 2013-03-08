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

import java.io.IOException;

import com.kinvey.java.core.KinveyClientCallback;

/**
 * <p>Abstract AsyncClientRequest class.</p>
 *
 * @author edwardf
 * @since 2.0
 * @version $Id: $
 */
public abstract class AsyncClientRequest<T> extends AsyncTask<Object, Void, T> {

    private Throwable error;

    private KinveyClientCallback callback;

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
     * <p>executeAsync</p>
     *
     * @return a T object.
     * @throws java.io.IOException if any.
     */
    protected abstract T executeAsync() throws IOException;

}

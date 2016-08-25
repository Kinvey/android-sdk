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

package com.kinvey.android.async;

import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.dto.User;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Class represents internal implementation of Async push request that is used to create push
 */
public class AsyncPushRequest extends AsyncClientRequest<Integer> {
    private final String collection;
    private final SyncManager manager;
    private final AbstractClient client;
    private KinveyPushCallback callback;

    /**
     * Async push request constructor
     * @param collection Collection name that we want to push
     * @param manager sync manager that is used
     * @param client Kinvey client instance to be used to execute network requests
     * @param callback async callbacks to be invoked when job is done
     */
    public AsyncPushRequest(String collection,
                            SyncManager manager,
                            AbstractClient client,
                            KinveyPushCallback callback){
        super(callback);
        this.collection = collection;
        this.manager = manager;
        this.client = client;
        this.callback = callback;
    }


    @Override
    protected User executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
        SyncRequest syncRequest = null;
        int progress = 0;
        while ((syncRequest = manager.popSingleQueue(collection)) != null){
            manager.executeRequest(client, syncRequest);
            publishProgress(++progress);
        }
        return progress;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        callback.onProgress(values[0], manager.getCount(this.collection));
    }
}

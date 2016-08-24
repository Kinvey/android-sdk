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
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.core.KinveyJsonError;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class represents internal implementation of Async push request that is used to create push
 */
public class AsyncPushRequest extends AsyncClientRequest<KinveyPushResponse> {

    private final String collection;
    private final SyncManager manager;
    private List<SyncRequest> requests = null;
    private final AbstractClient client;
    private KinveyPushCallback callback;

    /**
     * Async push request constructor
     *
     * @param collection Collection name that we want to push
     * @param manager    sync manager that is used
     * @param client     Kinvey client instance to be used to execute network requests
     * @param callback   async callbacks to be invoked when job is done
     */
    public AsyncPushRequest(String collection,
                            SyncManager manager,
                            AbstractClient client,
                            KinveyPushCallback callback) {
        super(callback);
        this.collection = collection;
        this.manager = manager;
        this.client = client;
        this.callback = callback;
    }

    @Override
    protected KinveyPushResponse executeAsync() throws IOException, InvocationTargetException {
        KinveyPushResponse pushResponse = new KinveyPushResponse();
        List<Exception> errors = new ArrayList<>();
        requests = manager.popSingleQueue(collection);
        int progress = 0;

        for(SyncRequest syncRequest: requests){

            try {
                manager.executeRequest(client, syncRequest);
                pushResponse.setSuccessCount(++progress);
            } catch (AccessControlException | KinveyException e) { //TODO check Exception
                errors.add(e);
            }
            publishProgress(pushResponse);
        }

        pushResponse.setListOfExceptions(errors);
        return pushResponse;
    }

    @Override
    protected void onPostExecute(KinveyPushResponse response) {
        callback.onSuccess(response);
    }

    @Override
    protected void onProgressUpdate(KinveyPushResponse... values) {
        super.onProgressUpdate(values);
        callback.onProgress(values[0].getSuccessCount(), requests.size());
    }
}

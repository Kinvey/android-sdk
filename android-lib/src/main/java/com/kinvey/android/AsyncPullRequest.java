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

import com.google.api.client.json.GenericJson;
import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.android.sync.KinveyPullResponse;
import com.kinvey.java.Query;
import com.kinvey.java.model.KinveyAbstractReadResponse;
import com.kinvey.java.store.BaseDataStore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Class represents internal implementation of Async pull request that is used to create pull
 */
public class AsyncPullRequest<T extends GenericJson> extends AsyncClientRequest<KinveyPullResponse<T>> {

    private final BaseDataStore<T> store;
    private Query query;
    private int pageSize;
    private boolean isAutoPagination = false;

    /**
     * Async pull request constructor
     * @param query Query that is used to fetch data from network
     * @param store Kinvey data store instance to be used to execute network requests
     * @param callback async callbacks to be invoked when job is done
     */
    public AsyncPullRequest(BaseDataStore<T>  store,
                            Query query,
                            KinveyPullCallback<T> callback) {
        super(callback);
        this.query = query;
        this.store = store;
    }

    /**
     * Async pull request constructor
     * @param query Query that is used to fetch data from network
     * @param pageSize Page size for auto-pagination
     * @param store Kinvey data store instance to be used to execute network requests
     * @param callback async callbacks to be invoked when job is done
     */
    public AsyncPullRequest(BaseDataStore<T> store,
                            Query query,
                            int pageSize,
                            KinveyPullCallback<T> callback) {
        super(callback);
        this.query = query;
        this.store = store;
        this.isAutoPagination = true;
        this.pageSize = pageSize;
    }


    @Override
    protected KinveyPullResponse<T> executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
        KinveyPullResponse<T> kinveyPullResponse = new KinveyPullResponse<>();
        KinveyAbstractReadResponse<T> pullResponse;
        if (isAutoPagination) {
            pullResponse = store.pullBlocking(query, pageSize);
        } else {
            pullResponse = store.pullBlocking(query);
        }
        kinveyPullResponse.setListOfExceptions(pullResponse.getListOfExceptions());
        kinveyPullResponse.setResult(pullResponse.getResult());
        return kinveyPullResponse;
    }
}

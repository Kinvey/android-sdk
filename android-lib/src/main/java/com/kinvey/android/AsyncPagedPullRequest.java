/*
 *  Copyright (c) 2017, Kinvey, Inc. All rights reserved.
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

import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.android.sync.KinveyPullResponse;
import com.kinvey.java.Query;
import com.kinvey.java.store.BaseDataStore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class represents internal implementation of async paged pull request that is used to automate
 * pull request pagination.
 */
public class AsyncPagedPullRequest<T> extends AsyncClientRequest<KinveyPullResponse<T>> {
    private final BaseDataStore store;
    private Query query;

    /**
     * Async pull request constructor
     * @param query Query that is used to fetch data from network
     * @param store Kinvey data store instance to be used to execute network requests
     * @param callback async callbacks to be invoked when job is done
     */
    public AsyncPagedPullRequest(BaseDataStore store,
                            Query query,
                            KinveyPullCallback<T> callback){
        super(callback);
        this.query = query;
        this.store = store;
    }


    @Override
    protected KinveyPullResponse<T> executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
        KinveyPullResponse<T> kinveyPullResponse = new KinveyPullResponse<T>();

        int skipCount = 0;
        int pageSize = 10000;

        // First, get the count of all the items to pull
        int totalItemCount = 0; // TODO implement _count call to KCS

        List<T> totalPullResults = new ArrayList<>();
        do {
            query.setSkip(skipCount).setLimit(pageSize);
            totalPullResults.addAll(store.pullBlocking(query));
            skipCount += pageSize;
        } while (skipCount < totalItemCount);

        kinveyPullResponse.setResult(totalPullResults);

        return kinveyPullResponse;
    }
}

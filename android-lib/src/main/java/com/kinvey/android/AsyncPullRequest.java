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

import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.store.DataStore;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Class represents internal implementation of Async pull request that is used to create pull
 */
public class AsyncPullRequest extends AsyncClientRequest<Integer> {
    private final DataStore store;
    private Query query;

    /**
     * Async pull request constructor
     * @param query Query that is used to fetch data from network
     * @param store Kinvey data store instance to be used to execute network requests
     * @param callback async callbacks to be invoked when job is done
     */
    public AsyncPullRequest(DataStore store,
                            Query query,
                            KinveyPullCallback callback){
        super(callback);
        this.query = query;
        this.store = store;
    }


    @Override
    protected Integer executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
        store.pullBlocking(query);
        return 0;
    }
}

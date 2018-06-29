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

package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;

/**
 * Created by Prots on 2/15/16.
 */
public class DeleteQueryRequest<T extends GenericJson> extends AbstractDeleteRequest<T> {

    private final Query query;

    public DeleteQueryRequest(ICache<T> cache, NetworkManager<T> networkManager, WritePolicy writePolicy,
                              Query query, SyncManager syncManager) {
        super(cache, writePolicy, networkManager, syncManager);
        this.query = query;
    }

    @Override
    protected Integer deleteCached() {
        return cache.delete(query);
    }

    @Override
    protected NetworkManager.Delete deleteNetwork() throws IOException {
        return networkManager.deleteBlocking(query);
    }

    @Override
    protected void enqueueRequest(String collectionName, NetworkManager<T> networkManager) throws IOException {
        syncManager.enqueueDeleteRequests(collectionName, networkManager, cache.get(query));
    }
}

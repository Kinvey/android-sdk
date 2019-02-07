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

package com.kinvey.java.store.requests.data;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.KinveyReadResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.ReadPolicy;

import java.io.IOException;

/**
 * Created by Prots on 2/8/16.
 */
public class ReadRequest<T extends GenericJson> extends AbstractKinveyReadRequest<T> {
    private final ICache<T> cache;
    private final Query query;
    private final ReadPolicy readPolicy;
    private final long maxValue;
    private NetworkManager<T> networkManager;

    public ReadRequest(ICache<T> cache, Query query, ReadPolicy readPolicy, long maxValue,
                       NetworkManager<T> networkManager) {
        this.cache = cache;
        this.query = query == null ? new Query(new MongoQueryFilter.MongoQueryFilterBuilder()) : query;
        this.readPolicy = readPolicy;
        this.maxValue = maxValue;
        this.networkManager = networkManager;
    }

    @Override
    public KinveyReadResponse<T> execute() throws IOException {
        query.setLimit((int) maxValue);
        KinveyReadResponse<T> ret = null;
        switch (readPolicy){
            case FORCE_LOCAL:
                KinveyReadResponse<T> response = new KinveyReadResponse<>();
                response.setResult(cache.get(query));
                ret = response;
                break;
            case FORCE_NETWORK:
            case BOTH:
                ret = networkManager.getBlocking(query).execute();
                break;
            case NETWORK_OTHERWISE_LOCAL:
                try {
                    ret = networkManager.getBlocking(query).execute();
                } catch (Exception e) {
                    KinveyReadResponse<T> res = new KinveyReadResponse<>();
                    res.setResult(cache.get(query));
                    ret = res;
                }
        }
        return ret;
    }

    @Override
    public void cancel() {

    }
}

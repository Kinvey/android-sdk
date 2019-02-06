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

package com.kinvey.java.store.requests.data.read;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.requests.data.AbstractKinveyDataRequest;

import java.io.IOException;

/**
 * Created by Prots on 2/8/16.
 */
public class ReadSingleRequest<T extends GenericJson> extends AbstractKinveyDataRequest<T> {

    private final ICache<T> cache;
    private String id;
    private final ReadPolicy readPolicy;
    private NetworkManager<T> networkManager;

    public ReadSingleRequest(ICache<T> cache, String id, ReadPolicy readPolicy, NetworkManager<T> networkManager) {

        this.cache = cache;
        this.id = id;
        this.readPolicy = readPolicy;
        this.networkManager = networkManager;
    }

    @Override
    public T execute() throws IOException {
        T ret = null;
        switch (readPolicy){
            case FORCE_LOCAL:
                ret = cache.get(id);
                break;
            case FORCE_NETWORK: // Logic for getting cached data implemented before running Request
                ret = networkManager.getEntityBlocking(id).execute();
                break;
            case BOTH:
                ret = networkManager.getEntityBlocking(id).execute();
                cache.save(ret);
                break;
            case NETWORK_OTHER_WISE_LOCAL:
                try {
                    ret = networkManager.getEntityBlocking(id).execute();
                    cache.save(ret);
                } catch (Exception e) {
                    ret = cache.get(id);
                }
                break;
        }
        return ret;
    }

    @Override
    public void cancel() {

    }
}

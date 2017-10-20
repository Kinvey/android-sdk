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

package com.kinvey.java.store.requests.data.read;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.model.KinveyCountResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.store.requests.data.PushRequest;
import com.kinvey.java.sync.SyncManager;

import java.io.IOException;

public abstract class AbstractReadCountRequest<T extends GenericJson> implements IRequest<Integer> {
    protected final ICache<T> cache;
    private final ReadPolicy readPolicy;
    protected NetworkManager<T> networkManager;
    private SyncManager syncManager;

    public AbstractReadCountRequest(ICache<T> cache, ReadPolicy readPolicy, NetworkManager<T> networkManager,
                                 SyncManager syncManager) {

        this.cache = cache;
        this.readPolicy = readPolicy;
        this.networkManager = networkManager;
        this.syncManager = syncManager;
    }

    @Override
    public Integer execute() throws IOException {
        Integer ret = 0;
        AbstractKinveyJsonClientRequest<KinveyCountResponse> request = null;
        try {
            request = countNetwork();
        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (readPolicy){
            case FORCE_LOCAL:
                ret = countCached();
                break;
            case FORCE_NETWORK:
                KinveyCountResponse response = request.execute();
                ret = response.getCount();
                break;
            case BOTH:
                PushRequest<T> pushRequest = new PushRequest<T>(networkManager.getCollectionName(),
                        cache, networkManager, networkManager.getClient());
                try {
                    pushRequest.execute();
                } catch (Throwable t){
                    // silent fall, will be synced next time
                }

                ret = countCached();
                ret = request.execute().getCount();
                break;
        }
        return ret;
    }

    @Override
    public void cancel() {

    }

    abstract protected Integer countCached();

    abstract protected AbstractKinveyJsonClientRequest<KinveyCountResponse> countNetwork() throws IOException;
}

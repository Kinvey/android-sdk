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

package com.kinvey.java.store.requests.data.save;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.sync.SyncManager;

import java.io.IOException;

/**
 * Created by Prots on 2/5/16.
 */
public class SaveRequest<T extends GenericJson> implements IRequest<T> {
    private final ICache<T> cache;
    private final T object;
    private final WritePolicy writePolicy;
    private SyncManager syncManager;
    private NetworkManager<T> networkManager;

    public SaveRequest(ICache<T> cache, NetworkManager<T> networkManager,
                       WritePolicy writePolicy, T object,
                       SyncManager syncManager) {
        this.networkManager = networkManager;
        this.cache = cache;
        this.object = object;
        this.writePolicy = writePolicy;
        this.syncManager = syncManager;
    }

    @Override
    public T execute() throws IOException {
        T ret = null;
        switch (writePolicy){
            case FORCE_LOCAL:
                ret = cache.save(object);
                break;
            case FORCE_NETWORK:
                NetworkManager<T>.Save save = networkManager.saveBlocking(object);
                ret = save.execute();
                break;
            case LOCAL_THEN_NETWORK:
                //write to local and push to sync
                ret = cache.save(object);
                syncManager.enqueueRequest(networkManager.getCollectionName(),
                        networkManager.saveBlocking(object));

                break;
        }
        return ret;
    }

    @Override
    public void cancel() {
    }
}

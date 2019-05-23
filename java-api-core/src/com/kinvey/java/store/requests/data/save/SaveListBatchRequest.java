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
import com.kinvey.java.Logger;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.store.requests.data.PushRequest;
import com.kinvey.java.sync.SyncManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SaveListBatchRequest<T extends GenericJson> implements IRequest<List<T>> {
    private final ICache<T> cache;
    private NetworkManager<T> networkManager;
    private final Iterable<T> objects;
    private final WritePolicy writePolicy;
    private SyncManager syncManager;

    public SaveListBatchRequest(ICache<T> cache, NetworkManager<T> networkManager, WritePolicy writePolicy, Iterable<T> objects,
                                SyncManager syncManager) {

        this.cache = cache;
        this.networkManager = networkManager;
        this.objects = objects;
        this.writePolicy = writePolicy;
        this.syncManager = syncManager;
    }

    @Override
    public List<T> execute() throws IOException {
        List<T> ret = new ArrayList<>();
        List<T> items = (List<T>) objects;
        switch (writePolicy) {
            case FORCE_LOCAL:
                ret = cache.save(objects);
                syncManager.enqueueSaveRequests(networkManager.getCollectionName(), networkManager, ret);
                break;
            case LOCAL_THEN_NETWORK:
                PushRequest<T> pushRequest = new PushRequest<>(networkManager.getCollectionName(), cache, networkManager,
                        networkManager.getClient());
                try {
                    pushRequest.execute();
                } catch (Throwable t) {
                    t.printStackTrace();
                    // silent fall, will be synced next time
                }
                ret = cache.save(objects);
                networkManager.saveBatchBlocking(items).execute();
                cache.save(ret);
                break;
            case FORCE_NETWORK:
                Logger.INFO("Start saving entities");
                networkManager.saveBatchBlocking(items).execute();
                Logger.INFO("Finish saving entities");
                break;
        }
        return ret;
    }

    @Override
    public void cancel() {
        //TODO: put async and track cancel
    }
}

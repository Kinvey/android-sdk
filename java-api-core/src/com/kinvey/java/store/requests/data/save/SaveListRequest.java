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
import com.kinvey.java.Constants;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.store.requests.data.PushRequest;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.util.List;

import java.util.ArrayList;

/**
 * Created by Prots on 2/5/16.
 */
public class SaveListRequest<T extends GenericJson> implements IRequest<List<T>> {
    private final ICache<T> cache;
    private NetworkManager<T> networkManager;
    private final Iterable<T> objects;
    private final WritePolicy writePolicy;
    private SyncManager syncManager;

    public SaveListRequest(ICache<T> cache, NetworkManager<T> networkManager, WritePolicy writePolicy, Iterable<T> objects,
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
                IOException exception = null;
                cache.save(objects);
                for (T object : objects) {
                    try {
                        ret.add(networkManager.saveBlocking(object).execute());
                    } catch (IOException e) {
                        syncManager.enqueueRequest(networkManager.getCollectionName(),
                                networkManager, networkManager.isTempId(object) ? SyncRequest.HttpVerb.POST : SyncRequest.HttpVerb.PUT, (String)object.get(Constants._ID));
                        exception = e;
                    }
                }
                cache.save(ret);
                if (exception != null) {
                    throw exception;
                }
                break;
            case FORCE_NETWORK:
                for (T obj : objects){
                    SaveRequest<T> save = new SaveRequest<>(
                            cache, networkManager , writePolicy, obj, syncManager);
                    ret.add(save.execute());
                }
                break;
        }
        return ret;
    }

    @Override
    public void cancel() {
        //TODO: put async and track cancel
    }
}

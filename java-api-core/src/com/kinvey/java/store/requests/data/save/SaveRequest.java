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
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.store.requests.data.PushRequest;
import com.kinvey.java.sync.RequestMethod;
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
                syncManager.enqueueRequest(networkManager.getCollectionName(),
                        networkManager, RequestMethod.SAVE, (String)object.get("_id"));
                break;
            case LOCAL_THEN_NETWORK:
                PushRequest<T> pushRequest = new PushRequest<T>(networkManager.getCollectionName(), cache, networkManager,
                        networkManager.getClient());
                try {
                    pushRequest.execute();
                } catch (Throwable t){
                    // silent fall, will be synced next time
                }

                ret = cache.save(object);
                try{
                    ret = networkManager.saveBlocking(object).execute();
                } catch (IOException e) {
                    syncManager.enqueueRequest(networkManager.getCollectionName(),
                            networkManager, RequestMethod.SAVE, (String)object.get("_id"));
                    throw e;
                }
                cache.save(ret);
                break;
            case FORCE_NETWORK:
                ret = networkManager.saveBlocking(object).execute();
                break;
        }
        return ret;
    }

    @Override
    public void cancel() {
    }
}

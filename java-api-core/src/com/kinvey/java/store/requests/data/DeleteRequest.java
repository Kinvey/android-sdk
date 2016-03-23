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
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;

import java.io.IOException;

/**
 * Created by Prots on 2/8/16.
 */
public class DeleteRequest<T extends GenericJson> extends AbstractKinveyDataRequest<T> {
    private final ICache<T> cache;
    private final Query query;
    private final String id;
    private final WritePolicy writePolicy;
    private NetworkManager<T> networkManager;

    public DeleteRequest(ICache<T> cache, String id, WritePolicy writePolicy, NetworkManager<T> networkManager) {
        this.networkManager = networkManager;
        query = null;
        this.cache = cache;
        this.id = id;
        this.writePolicy = writePolicy;
    }

    public DeleteRequest(AbstractClient client, String collectionName, Class<T> clazz,
                         ICache<T> cache, Query query, WritePolicy writePolicy) {
        id = null;
        this.cache = cache;
        this.query = query;
        this.writePolicy = writePolicy;
    }

    @Override
    public T execute() {
        switch (writePolicy){
            case FORCE_LOCAL:
                cache.delete(query);
                //TODO: write to sync
                break;
            case FORCE_NETWORK:

                try {
                    networkManager.deleteBlocking(query);
                } catch (IOException e) {
                    //TODO: add to sync
                    e.printStackTrace();
                }

                //write to network, fallback to sync
                break;
            case LOCAL_THEN_NETWORK:
                //write to local and network, push to sync if network fails
                cache.delete(query);
                try {
                    networkManager.deleteBlocking(query);
                } catch (IOException e) {
                    //TODO: add to sync
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    @Override
    public void cancel() {

    }
}

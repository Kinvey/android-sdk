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
import com.kinvey.java.Constants;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.KinveyJsonResponseException;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.sync.RequestMethod;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncItem;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.util.List;


/**
 * Created by Prots on 2/8/16.
 */
public class PushRequest<T extends GenericJson> extends AbstractKinveyExecuteRequest<T> {

    private static final String IGNORED_EXCEPTION_MESSAGE = "EntityNotFound";
    private static final int IGNORED_EXCEPTION_CODE = 404;

    private ICache<T> cache;
    private NetworkManager<T> networkManager;
    private final SyncManager syncManager;
    private AbstractClient client;

    public PushRequest(String collectionName, ICache<T> cache, NetworkManager<T> networkManager, AbstractClient client){
        this.collection = collectionName;
        this.cache = cache;
        this.networkManager = networkManager;
        this.syncManager = client.getSyncManager();
        this.client = client;
    }

    @Override
    public Void execute() throws IOException {
        List<SyncRequest> requests = syncManager.popSingleQueue(collection);
        for (SyncRequest syncRequest : requests) {
            syncManager.executeRequest(client, syncRequest);
        }

        List<SyncItem> syncItems = syncManager.popSingleItemQueue(collection);
        SyncRequest syncRequest = null;

        if (syncItems != null) {
            T t;
            for (SyncItem syncItem : syncItems) {
                if (syncItem.getRequestMethod() != null) {
                    switch (syncItem.getRequestMethod()) {
                        case POST:
                        case PUT:
                            t = cache.get(syncItem.getEntityID().id);
                            if (t == null) {
                                // check that item wasn't deleted before
                                syncManager.deleteCachedItems(client.query().equals("meta.id", syncItem.getEntityID().id).notEqual(Constants.REQUEST_METHOD, Constants.DELETE));
                                continue;
                            }
                            syncRequest = syncManager.createSyncRequest(collection, networkManager.saveBlocking(cache.get(syncItem.getEntityID().id)));
                            break;
                        case DELETE:
                            syncRequest = syncManager.createSyncRequest(collection, networkManager.deleteBlocking(syncItem.getEntityID().id));
                            break;
                    }
                }
                try {
                    syncManager.executeRequest(client, syncRequest);
                } catch (KinveyJsonResponseException e) {
                    if (e.getStatusCode() != IGNORED_EXCEPTION_CODE && !e.getMessage().contains(IGNORED_EXCEPTION_MESSAGE)) {
                        throw e;
                    }
                }
                syncManager.deleteCachedItem((String) syncItem.get(Constants._ID));
            }
        }
        return null;
    }

    @Override
    public void cancel() {

    }
}

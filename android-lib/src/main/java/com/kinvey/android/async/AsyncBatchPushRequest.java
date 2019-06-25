/*
 *  Copyright (c) 2019, Kinvey, Inc. All rights reserved.
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

package com.kinvey.android.async;

import android.support.annotation.Nullable;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.sync.KinveyPushBatchResponse;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Constants;
import com.kinvey.java.KinveyException;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.KinveyJsonResponseException;
import com.kinvey.java.model.KinveySyncSaveBatchResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncItem;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class represents internal implementation of Async push request that is used to create push
 */
public class AsyncBatchPushRequest<T extends GenericJson> extends AsyncClientRequest<KinveyPushResponse> {

    private static final String IGNORED_EXCEPTION_MESSAGE = "EntityNotFound";
    private static final int IGNORED_EXCEPTION_CODE = 404;

    private final String collection;
    private final SyncManager manager;
    private final AbstractClient client;
    private StoreType storeType;
    private NetworkManager<T> networkManager;
    private Class<T> storeItemType;
    private KinveyPushCallback callback;
    private int progress = 0;
    private int fullCount = 0;
    private List<Exception> errors = new ArrayList<>();

    /**
     * Async push request constructor
     *
     * @param collection Collection name that we want to push
     * @param manager    sync manager that is used
     * @param client     Kinvey client instance to be used to execute network requests
     * @param callback   async callbacks to be invoked when job is done
     */
    public AsyncBatchPushRequest(String collection,
                                 SyncManager manager,
                                 AbstractClient client,
                                 StoreType storeType,
                                 NetworkManager<T> networkManager,
                                 Class<T> storeItemType,
                                 KinveyPushCallback callback) {
        super(callback);
        this.collection = collection;
        this.manager = manager;
        this.client = client;
        this.storeType = storeType;
        this.networkManager = networkManager;
        this.storeItemType = storeItemType;
        this.callback = callback;
    }

    @Override
    protected KinveyPushBatchResponse executeAsync() throws IOException, InvocationTargetException {

        com.google.common.base.Preconditions.checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType");

        KinveyPushBatchResponse pushResponse = new KinveyPushBatchResponse();
        List<SyncRequest> requests = manager.popSingleQueue(collection);
        List<SyncItem> syncItems = manager.popSingleItemQueue(collection);

        processQueuedSyncRequests(requests, pushResponse);

        fullCount = requests != null ? requests.size() : 0;
        fullCount += syncItems != null ? syncItems.size() : 0;

        List<GenericJson> resultAllItems = new ArrayList<>();
        List<GenericJson> resultSingleItems = new ArrayList<>();
        List<SyncItem> batchSyncItems = new ArrayList<>();

        String id;
        T item;
        ICache<T> cache = client.getCacheManager().getCache(collection, storeItemType, Long.MAX_VALUE);
        SyncRequest syncRequest = null;

        if (syncItems != null) {
            for (SyncItem syncItem : syncItems) {
                id = syncItem.getEntityID().id;
                SyncItem.HttpVerb requestMethod = syncItem.getRequestMethod();
                switch (requestMethod) {
                    case SAVE: // the SAVE case need for backward compatibility
                    case POST:
                    case PUT:
                        item = cache.get(id);
                        if (item == null) {
                            // check that item wasn't deleted before
                            // if item wasn't found, it means that the item was deleted from the Cache by Delete request and the item will be deleted in case:DELETE
                            manager.deleteCachedItems(client.query().equals("meta.id", syncItem.getEntityID().id).notEqual(Constants.REQUEST_METHOD, Constants.DELETE));
                            continue;
                        }
                        if (requestMethod != SyncRequest.HttpVerb.POST) {
                            syncRequest = manager.createSyncRequest(collection, networkManager.saveBlocking(item));
                        }
                        break;
                    case DELETE:
                        syncRequest = manager.createSyncRequest(collection, networkManager.deleteBlocking(id));
                        break;
                }
                try {
                    if (SyncRequest.HttpVerb.POST.equals(requestMethod)) {
                        batchSyncItems.add(syncItem);
                    } else {
                        GenericJson resultItem = runSingleSyncRequest(syncRequest);
                        resultSingleItems.add(resultItem);
                        pushResponse.setSuccessCount(++progress);
                    }
                } catch (AccessControlException | KinveyException e) { //TODO check Exception
                    errors.add(e);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
                manager.deleteCachedItem((String) syncItem.get(Constants._ID));
                callback.onProgress(pushResponse.getSuccessCount(), fullCount);
            }
        }
        KinveySyncSaveBatchResponse batchResponse = null;
        if (!batchSyncItems.isEmpty()) {
            List<T> saveItems = getSaveItems(batchSyncItems, cache);
            batchResponse = processBatchSyncRequest(saveItems);
            removeBatchTempItems(batchSyncItems);
        }
        resultAllItems.addAll(resultSingleItems);
        if (batchResponse != null) {
            if (batchResponse.getEntityList() != null) {
                resultAllItems.addAll(batchResponse.getEntityList());
                progress += batchResponse.getEntityList().size();
                pushResponse.setSuccessCount(++progress);
            }
            if (batchResponse.getErrors() != null) {
                pushResponse.setErrors(batchResponse.getErrors());
            }
        }
        pushResponse.setEntities(resultAllItems);
        pushResponse.setListOfExceptions(errors);
        return pushResponse;
    }

    private void removeBatchTempItems(List<SyncItem> batchSyncItems) {
        String tempID = "";
        for (SyncItem item : batchSyncItems) {
            tempID = (String) item.get(Constants._ID);
            if (tempID != null && !tempID.isEmpty()) {
                manager.deleteCachedItem(tempID);
            }
        }
    }

    private List<T> getSaveItems(List<SyncItem> batchSyncItems, ICache<T> cache) throws IOException {
        List<T> saveItems = new ArrayList<>();
        T cachedItem;
        for (SyncItem s : batchSyncItems) {
            cachedItem = cache.get(s.getEntityID().id);
            if (cachedItem != null) {
                saveItems.add(cachedItem);
            }
        }
        return saveItems;
    }

    private KinveySyncSaveBatchResponse processBatchSyncRequest(List<T> saveItems) throws IOException {
        SyncRequest syncRequest = manager.createSyncRequest(collection, networkManager.saveBatchBlocking(saveItems));
        GenericJson resultItem = manager.executeRequest(client, syncRequest);
        if (resultItem instanceof KinveySyncSaveBatchResponse) {
            return ((KinveySyncSaveBatchResponse) resultItem);
        }
        return null;
    }

    private GenericJson runSingleSyncRequest(@Nullable SyncRequest syncRequest) throws IOException {
        GenericJson resultItem = null;
        try {
            if (syncRequest.getHttpVerb() == SyncRequest.HttpVerb.POST) {
                String tempID = syncRequest.getEntityID().id;
                resultItem = manager.executeRequest(client, syncRequest);
                ICache<T> cache = client.getCacheManager().getCache(syncRequest.getCollectionName(), this.storeItemType, Long.MAX_VALUE);
                T temp = cache.get(tempID);
                temp.set("_id", resultItem.get("_id"));
                cache.delete(tempID);
                cache.save(temp);
            } else {
                resultItem = manager.executeRequest(client, syncRequest);
            }
        } catch (KinveyJsonResponseException e) {
            if (e.getStatusCode() != IGNORED_EXCEPTION_CODE && !e.getMessage().contains(IGNORED_EXCEPTION_MESSAGE)) {
                throw e;
            }
        }
        return resultItem;
    }

    private void processQueuedSyncRequests(List<SyncRequest> requests, KinveyPushResponse pushResponse) {
        if (requests != null) {
            for (SyncRequest syncRequest : requests) {
                try {
                    manager.executeRequest(client, syncRequest);
                    pushResponse.setSuccessCount(++progress);
                } catch (AccessControlException | KinveyException e) { //TODO check Exception
                    errors.add(e);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
                callback.onProgress(pushResponse.getSuccessCount(), fullCount);
            }
        }
    }
}


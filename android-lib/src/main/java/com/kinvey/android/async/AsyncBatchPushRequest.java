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

package com.kinvey.android.async;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Constants;
import com.kinvey.java.KinveyException;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.KinveyJsonResponseException;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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
    protected KinveyPushResponse executeAsync() throws IOException, InvocationTargetException {

        com.google.common.base.Preconditions.checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType");

        KinveyPushResponse pushResponse = new KinveyPushResponse();
        List<SyncRequest> requests = manager.popSingleQueue(collection);
        List<SyncItem> syncItems = manager.popSingleItemQueue(collection);

        processSyncRequests(requests, pushResponse);

        fullCount = requests != null ? requests.size() : 0;
        fullCount += syncItems != null ? syncItems.size() : 0;

//        if (syncItems != null) {
//            String id;
//            T t;
//            SyncRequest syncRequest = null;
//            int totalNumberOfPendingEntities = 0;
//            totalNumberOfPendingEntities = syncItems.size();
//
//
//                for (int j = 0; j < batchSize && j+i < totalNumberOfPendingEntities; j++) {
//                    SyncItem syncItem = syncItems.get(j+i);
//                    id = syncItem.getEntityID().id;
//
//                    switch (syncItem.getRequestMethod()) {
//                        case SAVE: // the SAVE case need for backward compatibility
//                        case POST:
//                        case PUT:
//                            t = client.getCacheManager().getCache(collection, storeItemType, Long.MAX_VALUE).get(id);
//                            if (t == null) {
//                                // check that item wasn't deleted before
//                                // if item wasn't found, it means that the item was deleted from the Cache by Delete request and the item will be deleted in case:DELETE
//                                manager.deleteCachedItems(client.query().equals("meta.id", syncItem.getEntityID().id).notEqual(Constants.REQUEST_METHOD, Constants.DELETE));
//                                continue;
//                            }
//                            syncRequest = manager.createSyncRequest(collection, networkManager.saveBlocking(t));
//                            break;
//                        case DELETE:
//                            syncRequest = manager.createSyncRequest(collection, networkManager.deleteBlocking(id));
//                            break;
//                    }
//
//                    try {
//                        FutureTask ft = new FutureTask(new CallableAsyncPushRequestHelper<T>(client, manager, syncRequest, syncItem, storeItemType));
//                        tasks.add(ft);
//                        executor.execute(ft);
//                        pushResponse.setSuccessCount(++progress);
//                    } catch (AccessControlException | KinveyException e) { //TODO check Exception
//                        errors.add(e);
//                    } catch (Exception e) {
//                        callback.onFailure(e);
//                    }
//                    callback.onProgress(pushResponse.getSuccessCount(), fullCount);
//                }
//
//                for (FutureTask<CallableAsyncPushRequestHelper> task : tasks) {
//                    try {
//                        task.get();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                executor.shutdown();
//            }
//        }
//
//        pushResponse.setListOfExceptions(errors);
        return pushResponse;
    }

    private void rusSyncRequest(SyncItem syncItem, SyncRequest syncRequest) throws IOException {
        try {
            if (syncRequest.getHttpVerb() == SyncRequest.HttpVerb.POST) {
                String tempID = syncRequest.getEntityID().id;
                GenericJson result = manager.executeRequest(client, syncRequest);
                ICache<T> cache = client.getCacheManager().getCache(syncRequest.getCollectionName(), this.storeItemType, Long.MAX_VALUE);
                T temp = cache.get(tempID);
                temp.set("_id", result.get("_id"));
                cache.delete(tempID);
                cache.save(temp);
            } else {
                manager.executeRequest(client, syncRequest);
            }
        } catch (KinveyJsonResponseException e) {
            if (e.getStatusCode() != IGNORED_EXCEPTION_CODE && !e.getMessage().contains(IGNORED_EXCEPTION_MESSAGE)) {
                throw e;
            }
        }
        manager.deleteCachedItem((String) syncItem.get(Constants._ID));
    }

    private void processSyncRequests(List<SyncRequest> requests, KinveyPushResponse pushResponse) {
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


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

import android.util.Log;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.sync.RequestMethod;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncItem;
import com.kinvey.java.sync.dto.SyncRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ExecutionException;

import static com.kinvey.android.Client.TAG;

/**
 * Class represents internal implementation of Async push request that is used to create push
 */
public class AsyncPushRequest<T extends GenericJson> extends AsyncClientRequest<KinveyPushResponse> {

    private final String collection;
    private final SyncManager manager;
    private final AbstractClient client;
    private StoreType storeType;
    private NetworkManager<T> networkManager;
    private Class<T> storeItemType;
    private KinveyPushCallback callback;

    /**
     * Async push request constructor
     *
     * @param collection Collection name that we want to push
     * @param manager    sync manager that is used
     * @param client     Kinvey client instance to be used to execute network requests
     * @param callback   async callbacks to be invoked when job is done
     */
    public AsyncPushRequest(String collection,
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
        List<Exception> errors = new ArrayList<>();
        List<SyncRequest> requests = manager.popSingleQueue(collection);
        List<SyncItem> syncItems = manager.popSingleItemQueue(collection);

        int batchSize = 10; // batch size for concurrent push requests

        int progress = 0;
        int fullCount = requests != null ? requests.size() : 0;
        fullCount += syncItems != null ? syncItems.size() : 0;
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

        if (syncItems != null) {
            String id;
            T t;
            SyncRequest syncRequest = null;
            int totalNumberOfPendingEntities = 0;
            totalNumberOfPendingEntities = syncItems.size();

            for (int i = 0; i < totalNumberOfPendingEntities; i+=batchSize) {
                ExecutorService executor = Executors.newFixedThreadPool(batchSize);
                List<FutureTask> tasks = new ArrayList<>();

                for (int j = 0; j < batchSize && j+i < totalNumberOfPendingEntities; j++) {
                    SyncItem syncItem = syncItems.get(j+i);
                    id = syncItem.getEntityID().id;

                    switch (RequestMethod.fromString(syncItem.getRequestMethod())) {
                        case SAVE:
                            t = client.getCacheManager().getCache(collection, storeItemType, Long.MAX_VALUE).get(id);
                            if (t == null) {
                                // check that item wasn't deleted before
//                            manager.deleteCachedItem((String) syncItem.get("_id"));
                                manager.deleteCachedItems(new Query().equals("meta.id", syncItem.getEntityID().id));
                                continue;
                            }
                            syncRequest = manager.createSyncRequest(collection, networkManager.saveBlocking(t));
                            break;
                        case DELETE:
                            syncRequest = manager.createSyncRequest(collection, networkManager.deleteBlocking(id));
                            break;
                    }

                    try {
                        FutureTask ft = new FutureTask(new CallableAsyncPushRequestHelper(client, manager, syncRequest, syncItem));
                        tasks.add(ft);
                        executor.execute(ft);
                        pushResponse.setSuccessCount(++progress);
                    } catch (AccessControlException | KinveyException e) { //TODO check Exception
                        errors.add(e);
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                    callback.onProgress(pushResponse.getSuccessCount(), fullCount);
                }

                for (FutureTask<CallableAsyncPushRequestHelper> task : tasks) {
                    try {
                        task.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                executor.shutdown();
            }
        }

        pushResponse.setListOfExceptions(errors);
        return pushResponse;
    }
}


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
import com.kinvey.java.Constants;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Logger;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.store.requests.data.PushRequest;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.List;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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
                Logger.INFO("Start saving entities");
                ExecutorService executor;
                List<FutureTask<T>> tasks;
                FutureTask<T> ft;
                List<T> items = (List<T>) objects;
                executor = Executors.newFixedThreadPool(networkManager.getClient().getNumberThreadsForDataStoreSaveList());
                tasks = new ArrayList<>();
                for (T obj : objects) {
                    try {
                        SaveRequest<T> save = new SaveRequest<>(
                                cache, networkManager, writePolicy, obj, syncManager);
                        ft = new FutureTask<T>(new CallableAsyncSaveRequestHelper(save));
                        tasks.add(ft);
                        executor.execute(ft);
                    } catch (AccessControlException | KinveyException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        throw e;
                    }
                }
                for (FutureTask<T> task : tasks) {
                    try {
                        ret.add(task.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                executor.shutdown();
                Logger.INFO("Finish saving entities");
                break;
        }
        return ret;
    }

    private class CallableAsyncSaveRequestHelper implements Callable {

        SaveRequest<T> save;

        CallableAsyncSaveRequestHelper(SaveRequest<T> save) {
            this.save = save;
        }

        @Override
        public T call() throws Exception {
            return save.execute();
        }
    }

    @Override
    public void cancel() {
        //TODO: put async and track cancel
    }
}

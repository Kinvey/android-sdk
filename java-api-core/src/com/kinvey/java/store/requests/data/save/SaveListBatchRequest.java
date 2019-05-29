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
import com.kinvey.java.Logger;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.KinveyError;
import com.kinvey.java.model.KinveySaveBatchResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.store.requests.data.PushRequest;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kinvey.java.network.NetworkManager.ID_FIELD_NAME;

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
        List<T> retList = new ArrayList<>();
        List<T> items = (List<T>) objects;
        List<T> newObjects = filterNewObjects(items);
        switch (writePolicy) {
            case FORCE_LOCAL:
                retList = cache.save(objects);
                //syncManager.enqueueSaveRequests(networkManager.getCollectionName(), networkManager, ret);
                break;
            case LOCAL_THEN_NETWORK:
//                PushRequest<T> pushRequest = new PushRequest<>(networkManager.getCollectionName(), cache, networkManager,
//                        networkManager.getClient());
//                try {
//                    pushRequest.execute();
//                } catch (Throwable t) {
//                    t.printStackTrace();
//                    // silent fall, will be synced next time
//                }
                retList = cache.save(objects);
                KinveySaveBatchResponse<T> response = networkManager.saveBatchBlocking(newObjects).execute();
                if (response != null) {
                    if (response.getErrors() == null || response.getErrors().isEmpty()) {
                        retList = response.getEntities();
                        cache.save(retList);
                    } else if (response.getErrors() != null && !response.getErrors().isEmpty()) {
                        throw new IOException(((KinveyError)response.getErrors().get(0)).getErrmsg());
                    }
                }
                break;
            case FORCE_NETWORK:
                Logger.INFO("Start saving entities");
                response  = networkManager.saveBatchBlocking(newObjects).execute();
                if (response != null) {
                    retList = response.getEntities();
                }
                Logger.INFO("Finish saving entities");
                break;
        }
        return retList;
    }

    private List<T> filterNewObjects(List<T> list) {
        String sourceId;
        List<T> resultList = new ArrayList<T>();
        for (T object : list) {
             sourceId = (String) object.get(ID_FIELD_NAME);
             if (sourceId == null || !networkManager.isTempId(object)) {
                 resultList.add(object);
             }
        }
        return resultList;
    }

    @Override
    public void cancel() {
        //TODO: put async and track cancel
    }
}

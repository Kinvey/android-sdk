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
import com.kinvey.java.KinveySaveBunchException;
import com.kinvey.java.Logger;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.KinveySaveBatchResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.store.requests.data.PushBatchRequest;
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

    public SaveListBatchRequest(ICache<T> cache, NetworkManager<T> networkManager,
                                WritePolicy writePolicy, Iterable<T> objects, SyncManager syncManager) {

        this.cache = cache;
        this.networkManager = networkManager;
        this.objects = objects;
        this.writePolicy = writePolicy;
        this.syncManager = syncManager;
    }

    @Override
    public List<T> execute() throws IOException {
        List<T> retList = new ArrayList<>();
        List<List<T>> listObjects = filterObjects((List<T>) objects);
        List<T> updateList = listObjects.get(0);
        List<T> saveList   = listObjects.get(1);
        KinveySaveBunchException exception = null;
        switch (writePolicy) {
            case FORCE_LOCAL:
                retList = cache.save(objects);
                syncManager.enqueueSaveRequests(networkManager.getCollectionName(), networkManager, retList);
                break;
            case LOCAL_THEN_NETWORK:
                doPushRequest();
                cache.save(objects);
                KinveySaveBatchResponse<T> response = networkManager.saveBatchBlocking(saveList).execute();
                if (response != null) {
                    if (response.getErrors() == null || response.getErrors().isEmpty()) {
                        List<T> respResultList = response.getEntities();
                        if (respResultList != null) {
                            retList = respResultList;
                        }
                    } else if (response.getErrors() != null && !response.getErrors().isEmpty()) {
                        exception = new KinveySaveBunchException(response.getErrors(), response.getEntities());
                    }
                }
                List<T> updateResultList = updateObjects(updateList);
                retList.addAll(updateResultList);
                cache.save(retList);
                if (exception != null) {
                    throw exception;
                }
                break;
            case FORCE_NETWORK:
                Logger.INFO("Start saving entities");
                response = networkManager.saveBatchBlocking(saveList).execute();
                updateResultList = updateObjects(updateList);
                if (response != null) {
                    retList = response.getEntities();
                    if (response.getErrors() == null || response.getErrors().isEmpty()) {
                        List<T> respResultList = response.getEntities();
                        if (respResultList != null) {
                            retList = respResultList;
                        }
                    } else if (response.getErrors() != null && !response.getErrors().isEmpty()) {
                        exception = new KinveySaveBunchException(response.getErrors(), response.getEntities());
                    }
                }
                retList.addAll(updateResultList);
                if (exception != null) {
                    throw exception;
                }
                Logger.INFO("Finish saving entities");
                break;
        }
        return retList;
    }

    private void doPushRequest() {
        PushBatchRequest<T> pushRequest = new PushBatchRequest<>(networkManager.getCollectionName(),
                cache, networkManager, networkManager.getClient());
        try {
            pushRequest.execute();
        } catch (Throwable t) {
            Logger.ERROR(t.getMessage());
        }
    }

    private List<List<T>> filterObjects(List<T> list) {
        String sourceId;
        List<T> resultListOld = new ArrayList<>();
        List<T> resultListNew = new ArrayList<>();
        List<List<T>> objects = new ArrayList<>();
        objects.add(resultListOld);
        objects.add(resultListNew);
        for (T object : list) {
            sourceId = (String) object.get(ID_FIELD_NAME);
            if (sourceId == null || !networkManager.isTempId(object)) {
                resultListNew.add(object);
            } else {
                resultListOld.add(object);
            }
        }
        return objects;
    }

    private List<T> updateObjects(List<T> items) throws IOException {
        List<T> ret = new ArrayList<>();
        for (T object : items) {
            try {
                ret.add(networkManager.saveBlocking(object).execute());
            } catch (IOException e) {
                syncManager.enqueueRequest(networkManager.getCollectionName(),
                        networkManager, networkManager.isTempId(object) ? SyncRequest.HttpVerb.POST : SyncRequest.HttpVerb.PUT, (String)object.get(Constants._ID));
                //throw e;
            }
        }
        return ret;
    }

    @Override
    public void cancel() {
        //TODO: put async and track cancel
    }
}

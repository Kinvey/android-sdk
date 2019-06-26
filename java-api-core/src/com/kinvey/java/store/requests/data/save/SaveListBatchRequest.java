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

package com.kinvey.java.store.requests.data.save;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.Constants;
import com.kinvey.java.KinveySaveBatchException;
import com.kinvey.java.Logger;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.KinveyBatchInsertError;
import com.kinvey.java.model.KinveyUpdateSingleItemError;
import com.kinvey.java.model.KinveySaveBatchResponse;
import com.kinvey.java.model.KinveyUpdateObjectsResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.store.requests.data.PushBatchRequest;
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
    private KinveySaveBatchException exception = null;

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
        switch (writePolicy) {
            case FORCE_LOCAL:
                retList = cache.save(objects);
                syncManager.enqueueSaveRequests(networkManager.getCollectionName(), networkManager, retList);
                break;
            case LOCAL_THEN_NETWORK:
                doPushRequest();
                cache.save(objects);
                retList = runSaveItemsRequest(objects);
                cache.save(retList);
                if (exception != null) {
                    throw exception;
                }
                break;
            case FORCE_NETWORK:
                retList = runSaveItemsRequest(objects);
                if (exception != null) {
                    throw exception;
                }
                break;
        }
        return retList;
    }

    private List<T> runSaveItemsRequest(Iterable<T> objects) throws IOException {
        Logger.INFO("Start saving entities");
        List<List<T>> listObjects = filterObjects((List<T>) objects);
        List<T> updateList = listObjects.get(0);
        List<T> saveList   = listObjects.get(1);
        List<T> retList = new ArrayList<>();
        KinveySaveBatchResponse<T> response = networkManager.saveBatchBlocking(saveList).execute();
        KinveyUpdateObjectsResponse updateResponse = updateObjects(updateList);
        if (response != null) {
            List<T> respResultList = response.getEntities();
            if (!response.haveErrors()) {
                if (respResultList != null) {
                    retList.addAll(respResultList);
                }
            } else {
                enqueueBatchErrorsRequests(response);
                exception = new KinveySaveBatchException(response.getErrors(), updateResponse.getErrors(), respResultList);
            }
        }
        retList.addAll(updateResponse.getEntities());
        Logger.INFO("Finish saving entities");
        return retList;
    }

    private void enqueueBatchErrorsRequests(KinveySaveBatchResponse<T> response) throws IOException {
        List<T> respResultList = response.getEntities();
        List<T> errorItems = new ArrayList<>();
        if (respResultList != null) {
            for (KinveyBatchInsertError err : response.getErrors()) {
                T item = respResultList.get(err.getIndex());
                errorItems.add(item);
            }
        }
        syncManager.enqueueSaveRequests(networkManager.getCollectionName(), networkManager, errorItems);
        //syncManager.enqueueSaveBatchRequest(networkManager.getCollectionName(), networkManager, errorItems);
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
        boolean isTempId;
        List<T> resultListOld = new ArrayList<>();
        List<T> resultListNew = new ArrayList<>();
        List<List<T>> objects = new ArrayList<>();
        objects.add(resultListOld);
        objects.add(resultListNew);
        for (T object : list) {
            sourceId = (String) object.get(ID_FIELD_NAME);
            isTempId = networkManager.isTempId(object);
            if (sourceId == null || !isTempId) {
                resultListNew.add(object);
            } else {
                resultListOld.add(object);
            }
        }
        return objects;
    }

    private KinveyUpdateObjectsResponse<T> updateObjects(List<T> items) throws IOException {
        List<T> ret = new ArrayList<>();
        List<KinveyUpdateSingleItemError> errors = new ArrayList<>();
        KinveyUpdateObjectsResponse result = new KinveyUpdateObjectsResponse();
        for (T object : items) {
            try {
                ret.add(networkManager.saveBlocking(object).execute());
            } catch (IOException e) {
                errors.add(new KinveyUpdateSingleItemError(e, object));
                syncManager.enqueueRequest(networkManager.getCollectionName(),
                        networkManager, SyncRequest.HttpVerb.PUT, (String) object.get(Constants._ID));
            }
        }
        result.setEntities(ret);
        result.setErrors(errors);
        return result;
    }

    @Override
    public void cancel() {
        //TODO: put async and track cancel
    }
}

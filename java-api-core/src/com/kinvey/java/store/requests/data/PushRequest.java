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
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.util.List;

/**
 * Created by Prots on 2/8/16.
 */
public class PushRequest<T extends GenericJson> extends AbstractKinveyExecuteRequest<T> {

    private final String collectionName;
    private final SyncManager syncManager;
    private AbstractClient client;

    public PushRequest(String collectionName, AbstractClient client){

        this.collectionName = collectionName;
        this.syncManager = client.getSycManager();
        this.client = client;
    }

    @Override
    public Void execute() throws IOException {
        List<SyncRequest> requestList = syncManager.popSingleQueue(collectionName);
        for (SyncRequest syncRequest: requestList) {
            syncManager.executeRequest(client, syncRequest);
        }
        return null;
    }

    @Override
    public void cancel() {

    }
}

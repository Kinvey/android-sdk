package com.kinvey.java.store.requests.data;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

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
    public Void execute() {
        SyncRequest syncRequest = null;
        while ((syncRequest = syncManager.popSingleQueue(collectionName)) != null){
            syncManager.executeRequest(client, syncRequest);
        }
        return null;
    }

    @Override
    public void cancel() {

    }
}

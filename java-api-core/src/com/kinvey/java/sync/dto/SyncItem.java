package com.kinvey.java.sync.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.io.IOException;

/**
 * Created by yuliya on 10/04/17.
 */

public class SyncItem extends SyncRequest{

    @Key("requestMethod")
    private String requestMethod;

    public SyncItem() {
    }

    public SyncItem(SyncItem.HttpVerb httpMethod, SyncMetaData entityID, String collectionName) {
        this.requestMethod = httpMethod.name();
        this.id = entityID;
        this.collectionName = collectionName;
    }

    public SyncItem.HttpVerb getRequestMethod() {
        return SyncItem.HttpVerb.fromString(requestMethod);
    }

    public void setRequestMethod(SyncItem.HttpVerb requestMethod) {
        this.requestMethod = requestMethod.name();
    }

    public GenericJson getEntity() {
        GenericJson entity = null;
        SyncMetaData entityId = getEntityID();
        if (entityId != null) {
            entity = entityId.getEntity();
        }
        return entity;
    }
}

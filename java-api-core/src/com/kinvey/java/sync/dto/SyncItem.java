package com.kinvey.java.sync.dto;

import com.google.api.client.util.Key;
import com.kinvey.java.sync.RequestMethod;

/**
 * Created by yuliya on 10/04/17.
 */

public class SyncItem extends SyncRequest{

    @Key("requestMethod")
    private String requestMethod;

    public SyncItem() {
    }

    public SyncItem(RequestMethod requestMethod, SyncMetaData entityID, String collectionName) {
        this.requestMethod = requestMethod.name();
        this.id = entityID;
        this.collectionName = collectionName;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
}

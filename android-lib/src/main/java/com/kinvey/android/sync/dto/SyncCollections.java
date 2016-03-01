package com.kinvey.android.sync.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.sync.SyncManager;

/**
 * Created by Prots on 2/25/16.
 */
public class SyncCollections extends GenericJson {
    @Key("_id")
    private String collectionName;

    public SyncCollections(String collectionName){

        this.collectionName = collectionName;
    }

    public String getCollectionName(){
        return collectionName;
    }

}

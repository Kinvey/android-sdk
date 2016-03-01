package com.kinvey.android.sync;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.cache.RealmCacheManager;
import com.kinvey.android.sync.dto.SyncCollections;
import com.kinvey.android.sync.dto.SyncRequest;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.query.MongoQueryFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prots on 2/24/16.
 */
public class SyncManager {

    private RealmCacheManager cacheManager;

    public SyncManager(RealmCacheManager cacheManager){
        this.cacheManager = cacheManager;

    }


    public GenericJson getEntity(Client client, SyncRequest.SyncMetaData entityID) {
        ICache<SyncRequest> collectionsCache  = cacheManager.getCache("sync", SyncRequest.class, Long.MAX_VALUE);
        return collectionsCache.get(entityID.id);
    }

    public void removeEntity(String collectionName, String curEntityID) {
        Query query = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        query.equals("collectionName", collectionName)
                .equals("meta._id", curEntityID);
        cacheManager.getCache("sync", SyncRequest.class, Long.MAX_VALUE).delete(query);
    }

    public void insertEntity(String collectionName,  Client client, GenericJson result) {

    }

    public void storeCompletedRequestInfo(String collectionName, boolean b, SyncRequest cur, Throwable error) {

    }

    public List<String> getCollectionTables() {
        ICache<SyncCollections> collectionsCache  = cacheManager.getCache("syncCollections", SyncCollections.class, Long.MAX_VALUE);
        List<SyncCollections> collections = collectionsCache.get();
        List<String> ret = new ArrayList<String>();
        for (SyncCollections collection : collections){
            ret.add(collection.getCollectionName());
        }
        return ret;
    }

    public SyncRequest popSingleQueue(String s, SyncManager dbHelper) {
        return null;
    }

    public void storeQueryResults(String collectionName,  String id, List<String> resultIds) {

    }

    public void enqueueRequest(String collectionName, SyncRequest.HttpVerb httpVerb, SyncRequest.SyncMetaData entityID) {
        SyncRequest request = new SyncRequest(httpVerb, entityID);

    }
}

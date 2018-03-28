package com.kinvey.java.store;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by yuliya on 03/06/18.
 */

public class QueryCacheItem extends GenericJson {

    @Key
    private String collectionName;

    @Key
    private String query;

    @Key
    private String lastRequest;

    public QueryCacheItem() {
    }

    public QueryCacheItem(String collectionName, String query, String lastRequest) {
        this.collectionName = collectionName;
        this.query = query;
        this.lastRequest = lastRequest;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getQuery() {
        return query;
    }

    public String getLastRequest() {
        return lastRequest;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setLastRequest(String lastRequest) {
        this.lastRequest = lastRequest;
    }
}

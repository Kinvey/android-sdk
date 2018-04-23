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
    private String lastRequestTime;

    public QueryCacheItem() {
    }

    public QueryCacheItem(String collectionName, String query, String lastRequestTime) {
        this.collectionName = collectionName;
        this.query = query;
        this.lastRequestTime = lastRequestTime;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getQuery() {
        return query;
    }

    public String getLastRequestTime() {
        return lastRequestTime;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setLastRequestTime(String lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }
}

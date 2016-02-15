package com.kinvey.java.store;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.store.requests.data.PushRequest;
import com.kinvey.java.store.requests.data.ReadRequest;
import com.kinvey.java.store.requests.data.delete.DeleteIdsRequest;
import com.kinvey.java.store.requests.data.delete.DeleteQueryRequest;
import com.kinvey.java.store.requests.data.delete.DeleteSingleRequest;
import com.kinvey.java.store.requests.data.read.ReadSingleRequest;
import com.kinvey.java.store.requests.data.save.SaveListRequest;
import com.kinvey.java.store.requests.data.save.SaveRequest;
import com.kinvey.java.store.requests.data.read.ReadAllRequest;
import com.kinvey.java.store.requests.data.read.ReadIdsRequest;
import com.kinvey.java.store.requests.data.read.ReadQueryRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prots on 2/4/16.
 */
public class DataStore<T extends GenericJson> {

    protected final AbstractClient client;
    private final String collection;
    private StoreType storeType;
    private Class<T> storeItemType;
    private ICache<T> cache;
    private String clientAppVersion;
    private GenericJson customRequestProperties;


    public DataStore(AbstractClient client, String collection, Class<T> itemType, StoreType storeType){

        this.storeType = storeType;
        this.client = client;
        this.collection = collection;
        this.storeItemType = itemType;
        cache = client.getCacheManager().getCache(collection, itemType, 0L);
    }


    public T find (String id){
        T ret = null;
        try {
            ret = new ReadSingleRequest<T>(client, collection, storeItemType,
                    cache, id, this.storeType.readPolicy).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public List<T> find (Iterable<String> ids){
        List<T> ret = null;
        try {
            ret = new ReadIdsRequest<T>(client, collection, storeItemType,
                    cache, this.storeType.readPolicy, ids).execute();
        } catch (IOException e){

        }
        return ret;
    }

    public List<T> find (Query query) {
        // perform request based on policy
        List<T> ret = null;
        try {
            ret = new ReadQueryRequest<T>(client, collection, storeItemType,
                    cache, this.storeType.readPolicy, query).execute();
        } catch (IOException e){

        }
        return ret;
    }

    public List<T> find () {
        // perform request based on policy
        List<T> ret = null;
        try {
            ret = new ReadAllRequest<T>(client, collection, storeItemType,
                    cache, this.storeType.readPolicy).execute();
        } catch (IOException e){

        }
        return ret;
    }

    public List<T> save (Iterable<T> objects) {
        return new SaveListRequest<T>(client, collection, storeItemType, cache, this.storeType.writePolicy, objects).execute();
    }

    public T save (T object) {
        return new SaveRequest<T>(client, collection, storeItemType, cache, this.storeType.writePolicy, object).execute();
    }

    public void delete (String id){
        try {
            new DeleteSingleRequest<T>(client, collection, storeItemType, cache, this.storeType.writePolicy, id).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete (Query query){
        try {
            new DeleteQueryRequest<T>(client, collection, storeItemType, cache, this.storeType.writePolicy, query).execute();
        } catch (IOException e) {


        }
    }

    public void delete (Iterable<String> ids){
        try {
            new DeleteIdsRequest<T>(client, collection, storeItemType, cache, this.storeType.writePolicy, ids).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Push will commit local data for this collection
    public void push() {
        new PushRequest<T>().execute();
    }

    public void pull(Query query) {
        try {
            new ReadRequest<T>(client, collection, storeItemType, cache, query, ReadPolicy.FORCE_NETWORK, Long.MAX_VALUE).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sync(Query query) {
        push();
        pull(query);
    }


    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public AbstractClient getClient() {
        return client;
    }

    public Object getCurrentClass() {
        return storeItemType;
    }

    public void setClientAppVersion(String clientAppVersion) {
        this.clientAppVersion = clientAppVersion;
    }

    public void setCustomRequestProperties(GenericJson customRequestProperties) {
        this.customRequestProperties = customRequestProperties;
    }
}

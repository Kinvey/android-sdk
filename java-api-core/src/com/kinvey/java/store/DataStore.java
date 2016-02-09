package com.kinvey.java.store;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.store.requests.ReadRequest;
import com.kinvey.java.store.requests.SaveRequest;

import java.io.IOException;
import java.util.List;
import com.kinvey.java.store.requests.*;

/**
 * Created by Prots on 2/4/16.
 */
public class DataStore<T extends GenericJson> {

    private final AbstractClient client;
    private final String collection;
    private StoreType storeType;
    private Class<T> storeItemType;
    private ICache<T> cache;


    private DataStore(AbstractClient client, String collection, Class<T> itemType, StoreType storeType){

        this.storeType = storeType;
        this.client = client;
        this.collection = collection;
        this.storeItemType = itemType;
        cache = client.getCacheManager().getCache(collection, itemType, 0L);
    }


    public T findById (String id){
        T ret = null;
        try {
            ret = new ReadSingleRequest<T>(client, collection, storeItemType,
                    cache, id, this.storeType.readPolicy).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public T findById (Iterable<String> ids){
        //find single entity
        return null;
    }

    public List<T> find (Query query) {
        // perform request based on policy
        return new ListReadRequest<T>(cache, query, this.storeType.readPolicy, Long.MAX_VALUE).execute();
    }

    public void save (Iterable<T> objects) {
        new SaveRequest<T>(client, collection, storeItemType, cache, objects, this.storeType.writePolicy).execute();
    }

    public void removeById (String id){
        new DeleteRequest<T>(client, collection, storeItemType, cache, id, this.storeType.writePolicy).execute();
    }

    public void remove (Query query){
        new DeleteRequest<T>(client, collection, storeItemType, cache, query, this.storeType.writePolicy).execute();
        //...
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


}

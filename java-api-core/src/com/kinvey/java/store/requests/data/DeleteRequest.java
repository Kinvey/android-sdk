package com.kinvey.java.store.requests.data;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkStore;
import com.kinvey.java.store.WritePolicy;

import java.io.IOException;

/**
 * Created by Prots on 2/8/16.
 */
public class DeleteRequest<T extends GenericJson> extends AbstractKinveyDataRequest<T> {
    private final ICache<T> cache;
    private final Query query;
    private final String id;
    private final WritePolicy writePolicy;
    private final AbstractClient client;
    private final Class<T> clazz;
    private String collectionName;

    public DeleteRequest(AbstractClient client, String collectionName, Class<T> clazz,
                         ICache<T> cache, String id, WritePolicy writePolicy) {
        this.client = client;
        this.clazz = clazz;
        this.collectionName = collectionName;
        query = null;
        this.cache = cache;
        this.id = id;
        this.writePolicy = writePolicy;
    }

    public DeleteRequest(AbstractClient client, String collectionName, Class<T> clazz,
                         ICache<T> cache, Query query, WritePolicy writePolicy) {
        this.client = client;
        this.collectionName = collectionName;
        this.clazz = clazz;
        id = null;
        this.cache = cache;
        this.query = query;
        this.writePolicy = writePolicy;
    }

    @Override
    public T execute() {
        NetworkStore<T> appData = client.networkStore(collectionName, clazz);
        switch (writePolicy){
            case FORCE_LOCAL:
                cache.delete(query);
                //TODO: write to sync
                break;
            case FORCE_NETWORK:

                try {
                    appData.deleteBlocking(query);
                } catch (IOException e) {
                    //TODO: add to sync
                    e.printStackTrace();
                }

                //write to network, fallback to sync
                break;
            case LOCAL_THEN_NETWORK:
                //write to local and network, push to sync if network fails
                cache.delete(query);
                try {
                    appData.deleteBlocking(query);
                } catch (IOException e) {
                    //TODO: add to sync
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    @Override
    public void cancel() {

    }
}

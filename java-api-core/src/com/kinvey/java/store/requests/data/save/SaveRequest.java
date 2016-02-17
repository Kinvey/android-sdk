package com.kinvey.java.store.requests.data.save;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;

import java.io.IOException;

/**
 * Created by Prots on 2/5/16.
 */
public class SaveRequest<T extends GenericJson> implements IRequest<T> {
    private AbstractClient client;
    private final String collectionName;
    private final Class<T> clazz;
    private final ICache<T> cache;
    private final T object;
    private final WritePolicy writePolicy;

    public SaveRequest(AbstractClient client, String collectionName, Class<T> clazz,
                       ICache<T> cache, WritePolicy writePolicy, T object) {
        this.client = client;
        this.collectionName = collectionName;
        this.clazz = clazz;

        this.cache = cache;
        this.object = object;
        this.writePolicy = writePolicy;
    }

    @Override
    public T execute() {
        NetworkManager<T> appData = client.networkStore(collectionName, clazz);
        T ret = null;
        switch (writePolicy){
            case FORCE_LOCAL:
                ret = cache.save(object);
                //TODO: write to sync
                break;
            case FORCE_NETWORK:


                try {
                    NetworkManager<T>.Save save = appData.saveBlocking(object);
                    ret = save.execute();
                } catch (IOException e){
                    //TODO: put to sync on error
                }

                //write to network, fallback to sync
                break;
            case LOCAL_THEN_NETWORK:
                //write to local and network, push to sync if network fails
                ret = cache.save(object);
                try {
                    NetworkManager<T>.Save save = appData.saveBlocking(object);
                    save.execute();
                } catch (IOException e){
                    //TODO: put to sync on error
                }
                break;
        }
        return ret;
    }

    @Override
    public void cancel() {
        //TODO: put async and track cancel
    }
}

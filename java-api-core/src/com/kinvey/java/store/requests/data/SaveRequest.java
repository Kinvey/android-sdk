package com.kinvey.java.store.requests.data;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.AppData;
import com.kinvey.java.store.WritePolicy;

import java.io.IOException;

/**
 * Created by Prots on 2/5/16.
 */
public class SaveRequest<T extends GenericJson> extends AbstractKinveyExecuteRequest<T> {
    private AbstractClient client;
    private final String collectionName;
    private final Class<T> clazz;
    private final ICache<T> cache;
    private final Iterable<T> objects;
    private final WritePolicy writePolicy;

    public SaveRequest(AbstractClient client, String collectionName, Class<T> clazz,
                       ICache<T> cache, Iterable<T> objects, WritePolicy writePolicy) {
        this.client = client;
        this.collectionName = collectionName;
        this.clazz = clazz;

        this.cache = cache;
        this.objects = objects;
        this.writePolicy = writePolicy;
    }

    @Override
    public Void execute() {
        AppData<T> appData = client.appData(collectionName, clazz);
        switch (writePolicy){
            case FORCE_LOCAL:
                cache.save(objects);
                //TODO: write to sync
                break;
            case FORCE_NETWORK:

                for (T object : objects) {
                    try {
                        AppData<T>.Save save = appData.saveBlocking(object);
                        save.execute();
                    } catch (IOException e){
                        //TODO: put to sync on error
                    }
                }

                //write to network, fallback to sync
                break;
            case LOCAL_THEN_NETWORK:
                //write to local and network, push to sync if network fails
                cache.save(objects);
                for (T object : objects) {
                    try {
                        AppData<T>.Save save = appData.saveBlocking(object);
                        save.execute();
                    } catch (IOException e){
                        //TODO: put to sync on error
                    }
                }
                break;
        }
        return null;
    }

    @Override
    public void cancel() {
        //TODO: put async and track cancel
    }
}

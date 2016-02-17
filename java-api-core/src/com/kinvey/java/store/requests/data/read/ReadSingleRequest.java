package com.kinvey.java.store.requests.data.read;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.requests.data.AbstractKinveyDataRequest;

import java.io.IOException;

/**
 * Created by Prots on 2/8/16.
 */
public class ReadSingleRequest<T extends GenericJson> extends AbstractKinveyDataRequest<T> {
    private AbstractClient client;
    private final String collectionName;
    private final Class<T> clazz;
    private final ICache<T> cache;
    private String id;
    private final ReadPolicy readPolicy;

    public ReadSingleRequest(AbstractClient client, String collectionName, Class<T> clazz,
                             ICache<T> cache, String id, ReadPolicy readPolicy) {
        this.client = client;
        this.collectionName = collectionName;
        this.clazz = clazz;

        this.cache = cache;
        this.id = id;
        this.readPolicy = readPolicy;
    }

    @Override
    public T execute() throws IOException {
        NetworkManager<T> appData = client.networkStore(collectionName, clazz);
        T ret = null;
        switch (readPolicy){
            case FORCE_LOCAL:
                ret = cache.get(id);
                break;
            case FORCE_NETWORK:
                ret = appData.getEntityBlocking(id).execute();
                break;
            case PREFER_LOCAL:
                ret = cache.get(id);
                if (ret == null || ret.size() == 0){
                    ret = appData.getEntityBlocking(id).execute();
                }
                break;
            case PREFER_NETWORK:
                try {
                    ret = appData.getEntityBlocking(id).execute();
                } catch (IOException e){
                    ret = cache.get(id);
                }

        }
        return ret;
    }

    @Override
    public void cancel() {

    }
}

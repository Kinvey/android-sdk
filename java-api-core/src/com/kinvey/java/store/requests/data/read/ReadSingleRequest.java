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

    private final ICache<T> cache;
    private String id;
    private final ReadPolicy readPolicy;
    private NetworkManager<T> networkManager;

    public ReadSingleRequest(ICache<T> cache, String id, ReadPolicy readPolicy, NetworkManager<T> networkManager) {

        this.cache = cache;
        this.id = id;
        this.readPolicy = readPolicy;
        this.networkManager = networkManager;
    }

    @Override
    public T execute() throws IOException {
        T ret = null;
        switch (readPolicy){
            case FORCE_LOCAL:
                ret = cache.get(id);
                break;
            case FORCE_NETWORK:
                ret = networkManager.getEntityBlocking(id).execute();
                break;
            case PREFER_LOCAL:
                ret = cache.get(id);
                if (ret == null || ret.size() == 0){
                    ret = networkManager.getEntityBlocking(id).execute();
                }
                break;
            case PREFER_NETWORK:
                try {
                    ret = networkManager.getEntityBlocking(id).execute();
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

package com.kinvey.java.store.requests.data.save;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.sync.SyncManager;

import java.io.IOException;

/**
 * Created by Prots on 2/5/16.
 */
public class SaveRequest<T extends GenericJson> implements IRequest<T> {
    private final ICache<T> cache;
    private final T object;
    private final WritePolicy writePolicy;
    private SyncManager syncManager;
    private NetworkManager<T> networkManager;

    public SaveRequest(ICache<T> cache, NetworkManager<T> networkManager,
                       WritePolicy writePolicy, T object,
                       SyncManager syncManager) {
        this.networkManager = networkManager;
        this.cache = cache;
        this.object = object;
        this.writePolicy = writePolicy;
        this.syncManager = syncManager;
    }

    @Override
    public T execute() throws IOException {
        T ret = null;
        switch (writePolicy){
            case FORCE_LOCAL:
                ret = cache.save(object);
                break;
            case FORCE_NETWORK:
                NetworkManager<T>.Save save = networkManager.saveBlocking(object);
                ret = save.execute();
                break;
            case LOCAL_THEN_NETWORK:
                //write to local and push to sync
                ret = cache.save(object);
                syncManager.enqueueRequest(networkManager.getCollectionName(),
                        networkManager.saveBlocking(object));

                break;
        }
        return ret;
    }

    @Override
    public void cancel() {
    }
}

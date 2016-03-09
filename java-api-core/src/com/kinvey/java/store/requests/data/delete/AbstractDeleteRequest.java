package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.sync.SyncManager;

import java.io.IOException;

/**
 * Created by Prots on 2/8/16.
 */
public abstract class AbstractDeleteRequest<T extends GenericJson> implements IRequest<Integer> {
    protected final ICache<T> cache;
    private final WritePolicy writePolicy;
    protected NetworkManager<T> networkManager;
    private SyncManager syncManager;

    public AbstractDeleteRequest(ICache<T> cache, WritePolicy writePolicy, NetworkManager<T> networkManager,
                                 SyncManager syncManager) {

        this.cache = cache;
        this.writePolicy = writePolicy;
        this.networkManager = networkManager;
        this.syncManager = syncManager;
    }

    @Override
    public Integer execute() throws IOException {
        Integer ret = 0;
        AbstractKinveyJsonClientRequest<KinveyDeleteResponse> request = null;
        try {
            request = deleteNetwork();
        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (writePolicy){
            case FORCE_LOCAL:
                ret = deleteCached();
                break;
            case FORCE_NETWORK:
                KinveyDeleteResponse response = request.execute();
                ret = response.getCount();
                break;
            case LOCAL_THEN_NETWORK:
                //write to local, and push to sync network request
                ret = deleteCached();
                syncManager.enqueueRequest(networkManager.getCollectionName(), request);
                break;
        }
        return ret;
    }

    @Override
    public void cancel() {

    }

    abstract protected Integer deleteCached();
    abstract protected AbstractKinveyJsonClientRequest<KinveyDeleteResponse> deleteNetwork() throws IOException;

}

package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.sync.SyncManager;

import java.io.IOException;

/**
 * Created by Prots on 2/15/16.
 */
public class DeleteQueryRequest<T extends GenericJson> extends AbstractDeleteRequest<T> {

    private final Query query;

    public DeleteQueryRequest(ICache<T> cache, NetworkManager<T> networkManager, WritePolicy writePolicy,
                              Query query, SyncManager syncManager) {
        super(cache, writePolicy, networkManager, syncManager);
        this.query = query;
    }

    @Override
    protected Integer deleteCached() {
        return cache.delete(query);
    }

    @Override
    protected NetworkManager.Delete deleteNetwork() throws IOException {
        return networkManager.deleteBlocking(query);
    }
}

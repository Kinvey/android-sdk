package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.google.common.collect.Iterables;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.sync.SyncManager;

import java.io.IOException;
import java.util.List;

/**
 * Created by Prots on 2/15/16.
 */
public class DeleteSingleRequest<T extends GenericJson> extends AbstractDeleteRequest<T> {
    private String id;

    public DeleteSingleRequest(ICache<T> cache, NetworkManager<T> networkManager, WritePolicy writePolicy,
                               String id, SyncManager syncManager) {
        super(cache, writePolicy, networkManager, syncManager);
        this.id = id;
    }

    @Override
    protected Integer deleteCached() {
        return cache.delete(id);
    }

    @Override
    protected NetworkManager.Delete deleteNetwork() throws IOException {
        return networkManager.deleteBlocking(id);
    }
}

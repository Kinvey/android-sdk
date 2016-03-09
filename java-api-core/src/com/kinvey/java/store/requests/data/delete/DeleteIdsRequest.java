package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.google.common.collect.Iterables;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.sync.SyncManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Prots on 2/15/16.
 */
public class DeleteIdsRequest<T extends GenericJson> extends AbstractDeleteRequest<T> {
    private Iterable<String> ids;

    public DeleteIdsRequest(ICache<T> cache, NetworkManager<T> networkManager, WritePolicy writePolicy,
                            Iterable<String> ids, SyncManager syncManager) {
        super(cache, writePolicy, networkManager, syncManager);
        this.ids = ids;
    }

    @Override
    protected Integer deleteCached() {
        return cache.delete(ids);
    }

    @Override
    protected NetworkManager.Delete deleteNetwork() throws IOException {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.in("_id", Iterables.toArray(ids, String.class));
        return networkManager.deleteBlocking(q);
    }
}

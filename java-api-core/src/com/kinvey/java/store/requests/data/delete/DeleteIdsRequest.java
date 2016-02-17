package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.google.common.collect.Iterables;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.WritePolicy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Prots on 2/15/16.
 */
public class DeleteIdsRequest<T extends GenericJson> extends AbstractDeleteRequest<T> {
    private Iterable<String> ids;

    public DeleteIdsRequest(AbstractClient client, String collectionName, Class<T> clazz, ICache<T> cache, WritePolicy writePolicy,
                            Iterable<String> ids) {
        super(client, collectionName, clazz, cache, writePolicy);
        this.ids = ids;
    }

    @Override
    protected Integer deleteCached() {
        return cache.delete(ids);
    }

    @Override
    protected Integer deleteNetwork() throws IOException {
        Query q = new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
        q.in("_id", Iterables.toArray(ids, String.class));
        KinveyDeleteResponse response = getNetworkData().deleteBlocking(q).execute();
        return response.getCount();
    }
}

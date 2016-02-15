package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.google.common.collect.Iterables;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.WritePolicy;

import java.io.IOException;
import java.util.List;

/**
 * Created by Prots on 2/15/16.
 */
public class DeleteSingleRequest<T extends GenericJson> extends AbstractDeleteRequest<T> {
    private String id;

    public DeleteSingleRequest(AbstractClient client, String collectionName, Class<T> clazz, ICache<T> cache, WritePolicy writePolicy,
                               String id) {
        super(client, collectionName, clazz, cache, writePolicy);
        this.id = id;
    }

    @Override
    protected List<T> deleteCached() {
        cache.delete(id);
        return null;
    }

    @Override
    protected List<T> deleteNetwork() throws IOException {
        getNetworkData().deleteBlocking(id);
        return null;
    }
}

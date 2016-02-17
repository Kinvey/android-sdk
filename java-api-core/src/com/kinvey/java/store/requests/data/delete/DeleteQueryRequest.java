package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.WritePolicy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Prots on 2/15/16.
 */
public class DeleteQueryRequest<T extends GenericJson> extends AbstractDeleteRequest<T> {

    private final Query query;

    public DeleteQueryRequest(AbstractClient client, String collectionName, Class<T> clazz, ICache<T> cache, WritePolicy writePolicy,
                              Query query) {
        super(client, collectionName, clazz, cache, writePolicy);
        this.query = query;
    }

    @Override
    protected Integer deleteCached() {
        return cache.delete(query);
    }

    @Override
    protected Integer deleteNetwork() throws IOException {
        return getNetworkData().deleteBlocking(query).execute().getCount();
    }
}

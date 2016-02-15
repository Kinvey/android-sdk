package com.kinvey.java.store.requests.data.read;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.store.ReadPolicy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Prots on 2/15/16.
 */
public class ReadAllRequest<T extends GenericJson> extends AbstractReadRequest<T> {
    public ReadAllRequest(AbstractClient client, String collectionName, Class<T> clazz, ICache<T> cache, ReadPolicy readPolicy) {
        super(client, collectionName, clazz, cache, readPolicy);
    }

    @Override
    protected List<T> getCached() {
        return cache.get();
    }

    @Override
    protected List<T> getNetwork() throws IOException {
        return Arrays.asList(
                getNetworkData().getBlocking().execute()
            );
    }
}

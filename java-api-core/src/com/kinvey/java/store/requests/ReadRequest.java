package com.kinvey.java.store.requests;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.AbstractKinveyClient;
import com.kinvey.java.network.AppData;
import com.kinvey.java.store.ReadPolicy;
import java.util.List;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Prots on 2/8/16.
 */
public class ReadRequest<T extends GenericJson> extends AbstractKinveyDataListRequest<T> {
    private AbstractClient client;
    private final String collectionName;
    private final Class<T> clazz;
    private final ICache<T> cache;
    private final Query query;
    private final ReadPolicy readPolicy;
    private final long maxValue;

    public ReadRequest(AbstractClient client, String collectionName, Class<T> clazz,
                       ICache<T> cache, Query query, ReadPolicy readPolicy, long maxValue) {
        this.client = client;
        this.collectionName = collectionName;
        this.clazz = clazz;

        this.cache = cache;
        this.query = query;
        this.readPolicy = readPolicy;
        this.maxValue = maxValue;
    }

    @Override
    public List<T> execute() throws IOException {
        AppData<T> appData = client.appData(collectionName, clazz);
        query.setLimit((int) maxValue);
        List<T> ret = null;
        switch (readPolicy){
            case FORCE_LOCAL:
                ret = cache.get(query);
                break;
            case FORCE_NETWORK:
                ret = Arrays.asList(appData.getBlocking(query).execute());
                break;
            case PREFER_LOCAL:
                ret = cache.get(query);
                if (ret == null || ret.size() == 0){
                    ret = Arrays.asList(appData.getBlocking(query).execute());
                }
                break;
            case PREFER_NETWORK:
                try {
                    ret = Arrays.asList(appData.getBlocking(query).execute());
                } catch (IOException e){
                    ret = cache.get(query);
                }

        }
        return ret;
    }

    @Override
    public void cancel() {

    }
}

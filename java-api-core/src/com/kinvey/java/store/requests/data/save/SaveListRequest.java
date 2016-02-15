package com.kinvey.java.store.requests.data.save;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.AppData;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import java.util.List;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Prots on 2/5/16.
 */
public class SaveListRequest<T extends GenericJson> implements IRequest<List<T>> {
    private AbstractClient client;
    private final String collectionName;
    private final Class<T> clazz;
    private final ICache<T> cache;
    private final Iterable<T> objects;
    private final WritePolicy writePolicy;

    public SaveListRequest(AbstractClient client, String collectionName, Class<T> clazz,
                           ICache<T> cache, WritePolicy writePolicy, Iterable<T> objects) {
        this.client = client;
        this.collectionName = collectionName;
        this.clazz = clazz;

        this.cache = cache;
        this.objects = objects;
        this.writePolicy = writePolicy;
    }

    @Override
    public List<T> execute() {
        List<T> ret = new ArrayList<T>();
        for (T obj : objects){
            SaveRequest<T> save = new SaveRequest<T>(client, collectionName, clazz,
                    cache, writePolicy, obj);
            ret.add(save.execute());
        }
        return ret;
    }

    @Override
    public void cancel() {
        //TODO: put async and track cancel
    }
}

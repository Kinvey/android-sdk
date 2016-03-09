package com.kinvey.java.store.requests.data.save;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;
import com.kinvey.java.sync.SyncManager;

import java.io.IOException;
import java.util.List;

import java.util.ArrayList;

/**
 * Created by Prots on 2/5/16.
 */
public class SaveListRequest<T extends GenericJson> implements IRequest<List<T>> {
    private final ICache<T> cache;
    private NetworkManager<T> networkManager;
    private final Iterable<T> objects;
    private final WritePolicy writePolicy;
    private SyncManager syncManager;

    public SaveListRequest(ICache<T> cache, NetworkManager<T> networkManager, WritePolicy writePolicy, Iterable<T> objects,
                           SyncManager syncManager) {

        this.cache = cache;
        this.networkManager = networkManager;
        this.objects = objects;
        this.writePolicy = writePolicy;
        this.syncManager = syncManager;
    }

    @Override
    public List<T> execute() throws IOException {
        List<T> ret = new ArrayList<T>();
        for (T obj : objects){
            SaveRequest<T> save = new SaveRequest<T>(
                    cache, networkManager , writePolicy, obj, syncManager);
            ret.add(save.execute());
        }
        return ret;
    }

    @Override
    public void cancel() {
        //TODO: put async and track cancel
    }
}

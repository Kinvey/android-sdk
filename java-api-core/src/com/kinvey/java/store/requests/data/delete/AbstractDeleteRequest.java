package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkStore;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.AbstractKinveyExecuteRequest;
import com.kinvey.java.store.requests.data.IRequest;

import java.io.IOException;
import java.util.List;

/**
 * Created by Prots on 2/8/16.
 */
public abstract class AbstractDeleteRequest<T extends GenericJson> implements IRequest<Integer> {
    private AbstractClient client;
    private final String collectionName;
    private final Class<T> clazz;
    protected final ICache<T> cache;
    private final WritePolicy writePolicy;

    public AbstractDeleteRequest(AbstractClient client, String collectionName, Class<T> clazz,
                                 ICache<T> cache, WritePolicy writePolicy) {
        this.client = client;
        this.collectionName = collectionName;
        this.clazz = clazz;

        this.cache = cache;
        this.writePolicy = writePolicy;
    }

    @Override
    public Integer execute() {
        Integer ret = 0;
        NetworkStore<T> networkStore = client.networkStore(collectionName, clazz);
        switch (writePolicy){
            case FORCE_LOCAL:
                ret = deleteCached();
                //TODO: write to sync
                break;
            case FORCE_NETWORK:

                try {
                    ret = deleteNetwork();
                } catch (IOException e) {
                    //TODO: add to sync
                    e.printStackTrace();
                }

                //write to network, fallback to sync
                break;
            case LOCAL_THEN_NETWORK:
                //write to local and network, push to sync if network fails
                ret = deleteCached();
                try {
                    ret = deleteNetwork();
                } catch (IOException e) {
                    //TODO: add to sync
                    e.printStackTrace();
                }
                break;
        }
        return ret;
    }

    protected NetworkStore<T> getNetworkData(){
        return client.networkStore(collectionName, clazz);
    }

    @Override
    public void cancel() {

    }

    abstract protected Integer deleteCached();
    abstract protected Integer deleteNetwork() throws IOException;

}

package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.IRequest;

import java.io.IOException;

/**
 * Created by Prots on 2/8/16.
 */
public abstract class AbstractDeleteRequest<T extends GenericJson> implements IRequest<Integer> {
    protected final ICache<T> cache;
    private final WritePolicy writePolicy;
    protected NetworkManager<T> networkManager;

    public AbstractDeleteRequest(ICache<T> cache, WritePolicy writePolicy, NetworkManager<T> networkManager) {

        this.cache = cache;
        this.writePolicy = writePolicy;
        this.networkManager = networkManager;
    }

    @Override
    public Integer execute() {
        Integer ret = 0;
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

    @Override
    public void cancel() {

    }

    abstract protected Integer deleteCached();
    abstract protected Integer deleteNetwork() throws IOException;

}

package com.kinvey.java.store.requests.data.read;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.requests.data.AbstractKinveyDataListRequest;

import java.util.List;

import java.io.IOException;

/**
 * Created by Prots on 2/8/16.
 */
public abstract class AbstractReadRequest<T extends GenericJson> extends AbstractKinveyDataListRequest<T> {
    protected final ICache<T> cache;
    private final ReadPolicy readPolicy;
    private NetworkManager<T> networkManager;

    public AbstractReadRequest(ICache<T> cache, ReadPolicy readPolicy, NetworkManager<T> networkManager) {
        this.cache = cache;
        this.readPolicy = readPolicy;
        this.networkManager = networkManager;
    }

    @Override
    public List<T> execute() throws IOException {
        List<T> ret = null;
        switch (readPolicy){
            case FORCE_LOCAL:
                ret = getCached();
                break;
            case FORCE_NETWORK:
                ret = getNetwork();
                break;
            case PREFER_LOCAL:
                ret = getCached();
                if (ret == null || ret.size() == 0){
                    ret = getNetwork();
                }
                break;
            case PREFER_NETWORK:
                try {
                    ret = getNetwork();
                } catch (IOException e){
                    ret = getCached();
                }

        }
        return ret;
    }

    protected NetworkManager<T> getNetworkData(){
        return networkManager;
    }

    @Override
    public void cancel() {

    }

    abstract protected List<T> getCached();
    abstract protected List<T> getNetwork() throws IOException;

}

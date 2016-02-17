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
    private AbstractClient client;
    private final String collectionName;
    private final Class<T> clazz;
    protected final ICache<T> cache;
    private final ReadPolicy readPolicy;

    public AbstractReadRequest(AbstractClient client, String collectionName, Class<T> clazz,
                               ICache<T> cache, ReadPolicy readPolicy) {
        this.client = client;
        this.collectionName = collectionName;
        this.clazz = clazz;

        this.cache = cache;
        this.readPolicy = readPolicy;
    }

    @Override
    public List<T> execute() throws IOException {
        NetworkManager<T> appData = client.networkStore(collectionName, clazz);
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
        return client.networkStore(collectionName, clazz);
    }

    @Override
    public void cancel() {

    }

    abstract protected List<T> getCached();
    abstract protected List<T> getNetwork() throws IOException;

}

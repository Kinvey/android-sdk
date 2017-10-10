package com.kinvey.java.store.requests.data;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;

import java.io.IOException;
import java.util.List;

/**
 * Created by yuliya on 10/06/17.
 */

public abstract class AbstractCalculationRequest<T extends GenericJson> implements IRequest<T[]> {
    protected final ICache<T> cache;
    private final ReadPolicy readPolicy;
    protected NetworkManager<T> networkManager;

    public AbstractCalculationRequest(ICache<T> cache, ReadPolicy readPolicy, NetworkManager<T> networkManager) {

        this.cache = cache;
        this.readPolicy = readPolicy;
        this.networkManager = networkManager;
    }

    @Override
    public T[] execute() throws IOException {
        T[] ret = null;
        switch (readPolicy) {
            case FORCE_LOCAL:
                ret = getCached();
                break;
            case FORCE_NETWORK:
            case BOTH:
                ret = getNetwork();
                break;
        }
        return ret;
    }

    @Override
    public void cancel() {

    }

    abstract protected T[] getCached();

    abstract protected T[] getNetwork() throws IOException;

}

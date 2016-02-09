package com.kinvey.java.store.requests;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;

/**
 * Created by Prots on 2/8/16.
 */
public abstract class AbstractKinveyExecuteRequest<T extends GenericJson> implements IRequest<Void> {
    public static class RequestConfig{

    }

    //configuration options for the request.
    //In strong typed languages, this can be a RequestConfig class that allows the developer to specify timeout, custom headers etc.
    protected RequestConfig requestConfig;

    //What collection we are operating on
    protected String collection;

    //The cache that backs the collection
    protected ICache<T> cache;

    //Network manager
    protected NetworkManager networkMgr;


}

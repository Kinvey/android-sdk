/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.cache;

import com.google.api.client.http.UriTemplate;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.core.AsyncExecutor;
import com.kinvey.java.offline.AbstractKinveyOfflineClientRequest;

import java.io.IOException;

/**
 * Implementation of a Client Request, which can either pull a response from a Cache instance or from online.
 * <p>
 * Behavior is determined by a {@link CachePolicy}, which must be set along with an instance of a {@link Cache}.
 * </p>
 * <p>
 * This class provides all available functionality through public methods, but most of the implementation is handled by
 * the specified cache policy.
 * </p>
 *
 * @author edwardf
 * @since 2.0
 *
 */
public abstract class AbstractKinveyCachedClientRequest<T> extends AbstractKinveyOfflineClientRequest<T> {


    private CachePolicy policy = CachePolicy.NOCACHE;
    private Cache<String, T> cache = new Cache<String, T>() {
        @Override
        public void put(String key, T value) {
            //Do nothing by default!
        }

        @Override
        public T get(String key) {
            return null;
        }
    };

    private Object lock = new Object();
    private AsyncExecutor executor;


    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod HTTP Method
     * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
     *        the base path from the base URL will be stripped out. The URI template can also be a
     *        full URL. URI template expansion is done using
     *        {@link com.google.api.client.http.UriTemplate#expand(String, String, Object, boolean)}
     * @param jsonContent POJO that can be serialized into JSON content or {@code null} for none
     * @param responseClass response class to parse into
     * @param collectionName the collection this request is associated with
     */
    protected AbstractKinveyCachedClientRequest(AbstractKinveyJsonClient abstractKinveyJsonClient,
                                              String requestMethod, String uriTemplate, Object jsonContent, Class<T> responseClass, String collectionName) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent,
                responseClass, collectionName);
    }


    /**
     * use this method to set a cache and a caching policy for this specific client request.
     *
     * @param cache - an implementation of a cache to use
     * @param policy - the caching policy to use for this individual request.
     */
    protected void setCache(Cache cache, CachePolicy policy){
        this.policy = policy;
        this.cache = cache;
    }

    /**
     * This method retrieves an entity from the cache.  The complete URL of the request is used as a key in the cache.
     * <P/>
     * This method is synchronized on an object lock, providing threadsafe accesss to the cache.
     * <P/>
     * @return an entity or null, from the cache
     * @throws IOException
     */
    public T fromCache() throws IOException{
        synchronized (lock) {
            return this.cache.get(UriTemplate.expand(super.getAbstractKinveyClient().getBaseUrl(), super.getUriTemplate(), this, true));
        }
    }


    /**
     * This method retrieves an entity from the service.  The request is executed as normal, and, if persisted, the
     * response can be added to the cache.
     *
     * @param persist - true if the response should be added to the cache, false if the cache shouldn't be updated.
     * @return an entity from the online collection..
     * @throws IOException
     */
    public T fromService(boolean persist) throws IOException{
        ///if this throws an IO exception, then there is probably no return object and thus nothing to persist...
        T ret = super.execute();
        if (persist && ret != null){
            synchronized (lock){
                this.cache.put(UriTemplate.expand(super.getAbstractKinveyClient().getBaseUrl(), super.getUriTemplate(), this, true), ret);
            }
        }
        return ret;
    }

    @Override
    public T execute() throws IOException{
        T ret =  policy.execute(this);
        return ret;
    }

    public AsyncExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(AsyncExecutor executor) {
        this.executor = executor;
    }
}

/*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License
* is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing permissions and limitations under
* the License.
*/
package com.kinvey.java.cache;

import com.google.api.client.http.UriTemplate;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.KinveyClientCallback;

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
public abstract class AbstractKinveyCachedClientRequest<T> extends AbstractKinveyJsonClientRequest<T>{


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

    private KinveyClientCallback<T>  callback;
    private Object lock = new Object();


    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod HTTP Method
     * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
     *        the base path from the base URL will be stripped out. The URI template can also be a
     *        full URL. URI template expansion is done using
     *        {@link com.google.api.client.http.UriTemplate#expand(String, String, Object, boolean)}
     * @param jsonContent POJO that can be serialized into JSON content or {@code null} for none
     * @param responseClass response class to parse into
     */
    protected AbstractKinveyCachedClientRequest(AbstractKinveyJsonClient abstractKinveyJsonClient,
                                              String requestMethod, String uriTemplate, Object jsonContent, Class<T> responseClass) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent,
                responseClass);
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
        return policy.execute(this);
    }

    public KinveyClientCallback<T> getCallback(){
        return callback;
    }


    public void setCallback(KinveyClientCallback<T> callback) {
        this.callback = callback;
    }
}

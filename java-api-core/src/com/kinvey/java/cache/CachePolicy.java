/*
 * Copyright (c) 2013 Kinvey Inc.
 *
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

import java.io.IOException;



/**
 * Set the caching policy on AppData to getBlocking different desired behavior for you app.
 *
 * @author edwardf
 * @since 2.0
 * @see {@link com.kinvey.java.AppData#setCache(Cache, CachePolicy)} for more details.
 */
public enum CachePolicy{
    /**
     * This policy will not use any caching, and will execute every request online.
     */
    NOCACHE {
        public <T> T execute(AbstractKinveyCachedClientRequest<T> cachedRequest) throws IOException {
            return cachedRequest.fromService(false);
        }
    },

    /**
     * This policy will only retrieveBlocking data from the cache, and will not use any network connection.
     */
    CACHEONLY{
        public <T> T execute(AbstractKinveyCachedClientRequest<T> cachedRequest) throws IOException{
            return cachedRequest.fromCache();
        }
    },

    /**
     * This policy will first attempt to retrieveBlocking data from the cache.  If the data has been cached, it will be returned.
     * If the data does not exist in the cache, the data will be retrieved from Kinvey's Backend and the cache will be updated.
     */
    CACHEFIRST{
        public <T> T execute(AbstractKinveyCachedClientRequest<T> cachedRequest) throws IOException{

            T ret = cachedRequest.fromCache();

            if (ret == null){
                ret = cachedRequest.fromService(true);
            }
            return ret;
        }
    },

    /**
     * This policy will first attempt to retrieveBlocking data from the cache.  If the data has been cached, it will be returned.
     * If the data does not exist in the cache, the data will be retrieved from Kinvey's Backend but the cache will not
     * be updated with the new results.
     */
    CACHEFIRST_NOREFRESH{
        public <T> T execute(AbstractKinveyCachedClientRequest<T> cachedRequest) throws IOException{
            T ret = cachedRequest.fromCache();

            if (ret == null){
                ret = cachedRequest.fromService(false);
            }

            return ret;
        }
    },


    /**
     * This policy will execute the request on the network, and will store the result in the cache.  If the online
     * execution fails, the results will be pulled from the cache.
     */
    NETWORKFIRST{
        public <T> T execute(AbstractKinveyCachedClientRequest< T> cachedRequest) throws IOException{


            T ret =  cachedRequest.fromService(true);
            if (ret == null){
                ret = cachedRequest.fromCache();
            }
            return ret;

        }
    },

    /**
     * This policy will first retrieveBlocking an element from the cache, and then it will attempt to execute the request on line.
     * This caching policy will make two calls to the KinveyClientCallback, either onSuccess or onFailure for both
     * executing on the cache as well as executing online.
     */
    BOTH{
        public <T> T execute(AbstractKinveyCachedClientRequest< T> cachedRequest) throws IOException{
            if (cachedRequest.getCallback() == null){
                return cachedRequest.fromService(false);
            }

            T ret = cachedRequest.fromCache();
            if (ret != null){
                cachedRequest.getCallback().onSuccess(ret);
            }
            ret = cachedRequest.fromService(true);
            cachedRequest.getCallback().onSuccess(ret);

            return ret;
        }
    };

    public abstract <T> T execute(AbstractKinveyCachedClientRequest<T> cachedRequest) throws IOException;

}
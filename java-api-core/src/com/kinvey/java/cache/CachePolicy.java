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

import com.kinvey.java.network.AppData;

import java.io.IOException;

/**
 * Set the caching policy on AppData to getdifferent desired behavior for you app.
 *
 * @author edwardf
 * @since 2.0
 * @see {@link AppData#setCache(Cache, CachePolicy)} for more details.
 */
public enum CachePolicy{
    /**
     * This policy will not use any caching, and will execute every request online.
     * <p>
     * Use this policy if your application is dependant on data that is shared between multiple users and always needs to be up to date.
     * </p>
     * */
    NOCACHE {
        public <T> T execute(AbstractKinveyCachedClientRequest<T> cachedRequest) throws IOException {
            return cachedRequest.fromService(false);
        }
    },

    /**
     * This policy will only retrieve data from the cache, and will not use any network connection.
     * <p>
     * Use this policy in combination with another policy, to allow for quick response times without requiring a network connection for specific operations.
     * </p>
     */
    CACHEONLY{
        public <T> T execute(AbstractKinveyCachedClientRequest<T> cachedRequest) throws IOException{
            return cachedRequest.fromCache();
        }
    },

    /**
     * This policy will first attempt to retrieve data from the cache.  If the data has been cached, it will be returned.  If the data does not exist in the cache, the data will be retrieved from Kinvey's Backend and the cache will be updated.
     * <p>
     * Use this policy if your application can display data that doesn't change very often but you still want local updates.
     * </p>
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
     * This policy will first attempt to retrieve data from the cache.  If the data has been cached, it will be returned.  If the data does not exist in the cache, the data will be retrieved from Kinvey's Backend but the cache will not be updated with the new results.
     * <p>
     * Use this policy if you want to set default results, however if a request is made that cannot return these defaults a live request will be made (without modifying those default values)
     * </p>
     */
    @Deprecated
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
     * This policy will execute the request on the network, and will store the result in the cache.  If the online execution fails, the results will be pulled from the cache.
     * <p>
     * Use this policy if you application wants the latest data but you still want responsiveness if a connection is lost
     * </p>
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
     * This policy will first retrieve an element from the cache, and then it will attempt to execute the request on line.  This caching policy will make two calls to the KinveyClientCallback, either onSuccess or onFailure for both executing on the cache as well as executing online.
     * <p>
     * Use this policy if you want more responsiveness without sacrificing the consistency of data with your backend.
     * </p>
     */
    BOTH{
        public <T> T execute(AbstractKinveyCachedClientRequest<T> cachedRequest) throws IOException{
            if (cachedRequest.getExecutor() == null){
                return cachedRequest.fromService(false);
            }
            T ret = cachedRequest.fromCache();

            if (ret != null){
                cachedRequest.getExecutor().notify(ret);
            }
            ret = cachedRequest.fromService(true);
            return ret;
        }
    };

    public abstract <T> T execute(AbstractKinveyCachedClientRequest<T> cachedRequest) throws IOException;

}
/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
package com.kinvey.java.offline;


import java.io.IOException;

/**
 * This enum set determines behaivor of an Offline Request
 *
 *
 * @author edwardf
 */
public enum OfflinePolicy {

    /**
     * This policy will not use any local storage or queueing, and will execute every request online.
     * <p>
     * If no network connection is available, errors will be returned through the onFailure callback.
     * </p>
     * <p>
     * Use this policy if your application is fully dependant on data in the backend, and data cannot be stored locally.
     * </p>
     */
    ALWAYS_ONLINE{
        @Override
        public <T> T execute(AbstractKinveyOfflineClientRequest<T> offlineRequest) throws IOException {
            return offlineRequest.offlineFromService(true);
        }
    },
    /**
     * This policy will attempt to execute the request online first, and if that is successful will update the local store with the results.
     * <p>
     * If the request fails due to connectivity issues, then the request will be executed against the local store.  If it fails for any other reason such as an Authorization Error, the onFailure callback will be called.
     * </p>
     * <p>
     * Use this policy if your application's data is constantly changing on the backend, but you want to support offline mode.
     * </p>
     */
    ONLINE_FIRST{
        @Override
        public <T> T execute(AbstractKinveyOfflineClientRequest<T> offlineRequest) throws IOException {
            T ret = offlineRequest.offlineFromService(false);
            if (ret == null){
                ret = offlineRequest.offlineFromStore();
            }
            return ret;
        }
    },
    /**
     * This policy will attempt to execute the request against the local store first.
     * <p>
     * If the request is a Get, and the data cannot be found in the local store, then an online request will be attempted.  If that suceeds, the store will be updated, and onSuccess will be called.  If that fials, then onFailure will be called.  For save requests, the local store will be updated and the entity will be returned through the onSuccess callback.
     * </p>
     * <p>
     * Use this policy if each user has their own data, and updates are not constantly from your backend.
     * </p>
     *
     */
    LOCAL_FIRST{
        @Override
        public <T> T execute(AbstractKinveyOfflineClientRequest<T> offlineRequest) throws IOException {
            T ret =  offlineRequest.offlineFromStore();
            if (ret == null){
                ret = offlineRequest.offlineFromService(false);
            }
            return ret;
        }
    };

    public abstract <T> T execute(AbstractKinveyOfflineClientRequest<T> offlineRequest) throws IOException;



    }

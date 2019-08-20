/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
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

package com.kinvey.java.store

/**
 * Created by Prots on 2/4/16.
 */
enum class StoreType (var readPolicy: ReadPolicy, var writePolicy: WritePolicy, var ttl: Long) {
    /**
     * This StoreType means that all changes will be done locally, and sending the local changes to the server is done via [BaseDataStore.pushBlocking]
     * updating local storage could be done via [BaseDataStore.pullBlocking]
     * to perform both operation in batch use [BaseDataStore.syncBlocking]
     */
    SYNC(ReadPolicy.FORCE_LOCAL, WritePolicy.FORCE_LOCAL, java.lang.Long.MAX_VALUE),
    /**
     * This StoreType means that all fetch request may have 2 callbacks:
     * - Callback when cached data will be retreived
     * - Callbach when network data will be fetched
     * that store type is used if you need your app works even if network is down
     * all the changes will be stored both locally and remotely and sync in case of network failtures
     */
    @Deprecated("use {@link StoreType#AUTO}")
    CACHE(ReadPolicy.BOTH, WritePolicy.LOCAL_THEN_NETWORK, java.lang.Long.MAX_VALUE),
    /**
     * This StoreType that will always try to retrieve `find` results from the network.
     * If successful, it will store those results in the local database.If a subsequent call to `find`
     * fails to retrieve results from the network because of connectivity
     * issues, then results will be returned from the local database
     */
    AUTO(ReadPolicy.NETWORK_OTHERWISE_LOCAL, WritePolicy.LOCAL_THEN_NETWORK, java.lang.Long.MAX_VALUE),
    /**
     * This store type means that all the changes goes dirrectly to the server
     * no caching will be used
     */
    NETWORK(ReadPolicy.FORCE_NETWORK, WritePolicy.FORCE_NETWORK, 0L)
}

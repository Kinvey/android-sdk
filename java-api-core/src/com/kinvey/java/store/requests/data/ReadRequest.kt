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

package com.kinvey.java.store.requests.data

import com.google.api.client.json.GenericJson
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.query.MongoQueryFilter
import com.kinvey.java.store.ReadPolicy

import java.io.IOException

/**
 * Created by Prots on 2/8/16.
 */
open class ReadRequest<T : GenericJson>(cache: ICache<T>?, query: Query?, private val readPolicy: ReadPolicy?, private val maxValue: Long,
                                   networkManager: NetworkManager<T>?) : AbstractKinveyReadRequest<T>() {
    private val query: Query

    init {
        this.cache = cache
        this.query = query ?: Query(MongoQueryFilter.MongoQueryFilterBuilder())
        this.networkManager = networkManager
    }

    @Throws(IOException::class)
    override fun execute(): KinveyReadResponse<T>? {
        query.setLimit(maxValue.toInt())
        var ret: KinveyReadResponse<T>? = null
        when (readPolicy) {
            ReadPolicy.FORCE_LOCAL -> {
                ret = runLocal()
            }
            ReadPolicy.FORCE_NETWORK, ReadPolicy.BOTH -> ret = readItem(query)
            ReadPolicy.NETWORK_OTHERWISE_LOCAL -> {
                ret = runOverNetwork()
            }
        }
        return ret
    }

    protected open fun runLocal(): KinveyReadResponse<T>? {
        val response = KinveyReadResponse<T>()
        response.result = cache?.get(query)
        return response
    }

    protected open fun runOverNetwork(): KinveyReadResponse<T>? {
        var networkException: IOException? = null
        var ret: KinveyReadResponse<T>? = null
        try {
            ret = readItem(query)
        } catch (e: IOException) {
            if (NetworkManager.checkNetworkRuntimeExceptions(e)) {
                throw e
            }
            networkException = e
        }
        // if the network request fails, fetch data from local cache
        if (networkException != null) {
            val res = KinveyReadResponse<T>()
            res.result = cache?.get(query)
            ret = res
        }
        return ret
    }

    @Throws(IOException::class)
    protected open fun readItem(query: Query): KinveyReadResponse<T>? {
        return networkManager?.getBlocking(query)?.execute()
    }

    override fun cancel() {}
}

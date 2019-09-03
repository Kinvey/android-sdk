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
class ReadRequest<T : GenericJson>(override val cache: ICache<T>?, query: Query?, private val readPolicy: ReadPolicy, private val maxValue: Long,
                                   override val networkManager: NetworkManager<T>) : AbstractKinveyReadRequest<T>() {
    private val query: Query

    init {
        this.query = query ?: Query(MongoQueryFilter.MongoQueryFilterBuilder())
    }

    @Throws(IOException::class)
    override fun execute(): KinveyReadResponse<T>? {
        query.limit = maxValue.toInt()
        var ret: KinveyReadResponse<T>? = null
        when (readPolicy) {
            ReadPolicy.FORCE_LOCAL -> {
                val response = KinveyReadResponse<T>()
                response.result = cache?.let { it[query] }
                ret = response
            }
            ReadPolicy.FORCE_NETWORK, ReadPolicy.BOTH -> ret = networkManager.getBlocking(query).execute()
            ReadPolicy.NETWORK_OTHERWISE_LOCAL -> {
                var networkException: IOException? = null
                try {
                    ret = networkManager.getBlocking(query).execute()
                } catch (e: IOException) {
                    if (NetworkManager.checkNetworkRuntimeExceptions(e)) {
                        throw e
                    }
                    networkException = e
                }

                // if the network request fails, fetch data from local cache
                if (networkException != null) {
                    val res = KinveyReadResponse<T>()
                    res.result = cache?.let { it[query] }
                    ret = res
                }
            }
        }
        return ret
    }

    override fun cancel() {

    }
}

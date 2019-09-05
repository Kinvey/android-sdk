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

package com.kinvey.java.store.requests.data.read

import com.google.api.client.json.GenericJson
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.requests.data.AbstractKinveyDataRequest

import java.io.IOException

/**
 * Created by Prots on 2/8/16.
 */
class ReadSingleRequest<T : GenericJson>(cache: ICache<T>?, private val id: String, private val readPolicy: ReadPolicy?,
                                         private val networkManager: NetworkManager<T>?) : AbstractKinveyDataRequest<T>() {

    init {
        this.cache = cache
    }

    @Throws(IOException::class)
    override fun execute(): T? {
        var ret: T? = null
        when (readPolicy) {
            ReadPolicy.FORCE_LOCAL -> ret = cache?.get(id)
            ReadPolicy.FORCE_NETWORK ->  // Logic for getting cached data implemented before running Request
                ret = networkManager?.getEntityBlocking(id)?.execute()
            ReadPolicy.BOTH -> {
                ret = networkManager?.getEntityBlocking(id)?.execute()
                ret?.let { cache?.save(it) }
            }
            ReadPolicy.NETWORK_OTHERWISE_LOCAL -> {
                var networkException: IOException? = null
                try {
                    ret = networkManager?.getEntityBlocking(id)?.execute()
                    ret?.let { cache?.save(it) }
                } catch (e: IOException) {
                    if (NetworkManager.checkNetworkRuntimeExceptions(e)) {
                        throw e
                    }
                    networkException = e
                }
                // if the network request fails, fetch data from local cache
                if (networkException != null) {
                    ret = cache?.get(id)
                }
            }
        }
        return ret
    }

    override fun cancel() {}
}

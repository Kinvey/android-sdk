/*
 *  Copyright (c) 2017, Kinvey, Inc. All rights reserved.
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
import com.kinvey.java.core.AbstractKinveyReadHeaderRequest
import com.kinvey.java.model.KinveyCountResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.requests.data.IRequest
import com.kinvey.java.store.requests.data.PushRequest
import com.kinvey.java.sync.SyncManager

import java.io.IOException

abstract class AbstractReadCountRequest<T : GenericJson>(protected val cache: ICache<T>?, private val readPolicy: ReadPolicy,
                                                         protected var networkManager: NetworkManager<T>?,
                                                         private val syncManager: SyncManager?) : IRequest<KinveyCountResponse> {

    @Throws(IOException::class)
    override fun execute(): KinveyCountResponse? {
        var ret: KinveyCountResponse? = KinveyCountResponse()
        when (readPolicy) {
            ReadPolicy.FORCE_LOCAL -> ret?.count = countCached()
            ReadPolicy.FORCE_NETWORK -> {
                try { ret = countNetwork() } catch (e: IOException) { e.printStackTrace() }
            }
            ReadPolicy.BOTH -> {
                val pushRequest = PushRequest(networkManager?.collectionName,
                        cache, networkManager, networkManager?.client)
                try {
                    pushRequest.execute()
                } catch (t: Throwable) {
                    // silent fall, will be synced next time
                }
                try { ret = countNetwork() } catch (e: IOException) { e.printStackTrace() }
            }
            ReadPolicy.NETWORK_OTHERWISE_LOCAL -> {
                val pushAutoRequest = PushRequest(networkManager?.collectionName,
                        cache, networkManager, networkManager?.client)
                try {
                    pushAutoRequest.execute()
                } catch (t: Throwable) {
                    // silent fall, will be synced next time
                }
                var networkException: IOException? = null
                try {
                    ret = countNetwork()
                } catch (e: IOException) {
                    if (NetworkManager.checkNetworkRuntimeExceptions(e)) {
                        throw e
                    }
                    networkException = e
                }
                // if the network request fails, fetch data from local cache
                if (networkException != null) {
                    ret?.count = countCached()
                }
            }
        }
        return ret
    }

    override fun cancel() {}

    protected abstract fun countCached(): Int

    @Throws(IOException::class)
    protected abstract fun countNetwork(): KinveyCountResponse?
}

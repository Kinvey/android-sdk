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

package com.kinvey.java.store.requests.data.delete

import com.google.api.client.json.GenericJson
import com.kinvey.java.cache.ICache
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.model.KinveyDeleteResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.requests.data.IRequest
import com.kinvey.java.store.requests.data.PushRequest
import com.kinvey.java.sync.SyncManager

import java.io.IOException

/**
 * Created by Prots on 2/8/16.
 */
abstract class AbstractDeleteRequest<T : GenericJson>(protected val cache: ICache<T>?, private val writePolicy: WritePolicy,
                                                      protected var networkManager: NetworkManager<T>?,
                                                      protected var syncManager: SyncManager?) : IRequest<Int> {

    @Throws(IOException::class)
    override fun execute(): Int? {
        var ret: Int? = 0
        val request: AbstractKinveyJsonClientRequest<KinveyDeleteResponse>?

        when (writePolicy) {
            WritePolicy.FORCE_LOCAL -> {
                enqueueRequest(networkManager?.collectionName, networkManager)
                ret = deleteCached()
            }
            WritePolicy.LOCAL_THEN_NETWORK -> {
                val pushRequest = PushRequest(networkManager?.collectionName,
                        cache, networkManager, networkManager?.client)
                try {
                    pushRequest.execute()
                } catch (t: Throwable) {
                    // silent fall, will be synced next time
                }

                try {
                    request = deleteNetwork()
                    ret = request?.execute()?.count
                } catch (e: IOException) {
                    enqueueRequest(networkManager?.collectionName, networkManager)
                    throw e
                }
                deleteCached()
            }
            WritePolicy.FORCE_NETWORK -> {
                request = deleteNetwork()
                val response = request?.execute()
                ret = response?.count
            }
        }
        return ret
    }

    override fun cancel() {}

    abstract fun deleteCached(): Int?
    @Throws(IOException::class)
    abstract fun enqueueRequest(collectionName: String?, networkManager: NetworkManager<T>?)

    @Throws(IOException::class)
    abstract fun deleteNetwork(): AbstractKinveyJsonClientRequest<KinveyDeleteResponse>?
}

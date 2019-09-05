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

package com.kinvey.java.store.requests.data.save

import com.google.api.client.json.GenericJson
import com.kinvey.java.Constants
import com.kinvey.java.Logger
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.requests.data.IRequest
import com.kinvey.java.store.requests.data.PushRequest
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest

import java.io.IOException

/**
 * Created by Prots on 2/5/16.
 */
class SaveRequest<T : GenericJson>(private val cache: ICache<T>?, private val networkManager: NetworkManager<T>?,
                                   private val writePolicy: WritePolicy?, private val item: T,
                                   private val syncManager: SyncManager?) : IRequest<T> {
    @Throws(IOException::class)
    override fun execute(): T? {
        var ret: T? = null
        when (writePolicy) {
            WritePolicy.FORCE_LOCAL -> {
                ret = cache?.save(item)
                val requestType = if (networkManager?.isTempId(ret) == true) SyncRequest.HttpVerb.POST
                                  else SyncRequest.HttpVerb.PUT
                val itemId = item[Constants._ID] as String?
                syncManager?.enqueueRequest(networkManager?.collectionName, networkManager, requestType, itemId)
            }
            WritePolicy.LOCAL_THEN_NETWORK -> {
                val pushRequest = PushRequest(networkManager?.collectionName, cache, networkManager, networkManager?.client)
                try {
                    pushRequest.execute()
                } catch (t: Throwable) {
                    // silent fall, will be synced next time
                }
                // If object does not have an _id, then it is being created locally. The cache may
                // provide an _id in this case, but before it is saved to the network, this temporary
                // _id should be removed prior to saving to the backend. This way, the backend
                // will generate a permanent _id that will be used by the cache. Once we get the
                // result from the backend with the permanent _id, the record in the cache with the
                // temporary _id should be removed, and the new record should be saved.
                var id = ""
                ret = cache?.save(item)
                ret?.let { r -> id = r[Constants._ID].toString() }
                val bRealmGeneratedId = networkManager?.isTempId(ret)
                if (bRealmGeneratedId == true) { ret?.set(Constants._ID, null) }
                try {
                    ret = networkManager?.saveBlocking(item)?.execute()
                    if (bRealmGeneratedId == true) {
                        // The result from the network has the entity with its permanent ID. Need
                        // to remove the entity from the local cache with the temporary ID.
                        cache?.delete(id)
                    }
                } catch (e: IOException) {
                    syncManager?.enqueueRequest(networkManager?.collectionName,
                    networkManager, if (bRealmGeneratedId == true) SyncRequest.HttpVerb.POST else SyncRequest.HttpVerb.PUT, id)
                    throw e
                }
                ret?.let { cache?.save(it) }
            }
            WritePolicy.FORCE_NETWORK -> {
                Logger.INFO("Start saving entity")
                ret = networkManager?.saveBlocking(item)?.execute()
                Logger.INFO("Finish saving entity")
            }
        }
        return ret
    }

    override fun cancel() {}
}

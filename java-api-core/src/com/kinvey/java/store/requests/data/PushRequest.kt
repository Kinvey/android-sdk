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
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.cache.ICache
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest

import java.io.IOException


/**
 * Created by Prots on 2/8/16.
 */
class PushRequest<T : GenericJson>(collectionName: String?, cache: ICache<T>?,
                                   private val networkManager: NetworkManager<T>?,
                                   private val client: AbstractClient<*>?) : AbstractKinveyExecuteRequest<T>() {
    private val syncManager: SyncManager?

    init {
        this.cache = cache
        this.collection = collectionName
        this.syncManager = client?.syncManager
    }

    @Throws(IOException::class)
    override fun execute(): Void? {
        val requests = syncManager?.popSingleQueue(collection)
        requests?.forEach { syncRequest -> syncManager?.executeRequest(client, syncRequest) }
        val syncItems = syncManager?.popSingleItemQueue(collection)
        var syncRequest: SyncRequest? = null

        syncItems?.forEach { syncItem ->
            val httpVerb = syncItem.requestMethod ?: ""
            val itemId = syncItem.entityID?.id ?: ""
            when (httpVerb) {
                SyncRequest.HttpVerb.SAVE, //the SAVE case need for backward compatibility
                SyncRequest.HttpVerb.POST,
                SyncRequest.HttpVerb.PUT -> {
                    val item = cache?.get(itemId)
                    if (item == null) {
                        // check that item wasn't deleted before
                        syncManager?.deleteCachedItems(client?.query()?.equals("meta.id", itemId)?.notEqual(Constants.REQUEST_METHOD, Constants.DELETE))
                    } else {
                        syncRequest = syncManager?.createSyncRequest(collection, networkManager?.saveBlocking(item))
                    }
                }
                SyncRequest.HttpVerb.DELETE -> syncRequest = syncManager?.createSyncRequest(collection, networkManager?.deleteBlocking(itemId))
            }
            try {
                if (httpVerb == SyncRequest.HttpVerb.POST) {
                    val tempID = syncRequest?.entityID?.id ?: ""
                    val result = syncManager?.executeRequest(client, syncRequest)
                    val temp = cache?.get(tempID)
                    var resultId = ""
                    result?.let {res -> resultId = res[Constants._ID] as String }
                    temp?.set(Constants._ID, resultId)
                    cache?.delete(tempID)
                    temp?.let { t -> cache?.save(t) }
                } else {
                    syncManager?.executeRequest(client, syncRequest)
                }
            } catch (e: KinveyJsonResponseException) {
                if (e.statusCode != IGNORED_EXCEPTION_CODE && e.message?.contains(IGNORED_EXCEPTION_MESSAGE) == false) {
                    throw e
                }
            }
            syncManager?.deleteCachedItem(syncItem[Constants._ID] as String?)
        }
        return null
    }

    override fun cancel() {}

    companion object {
        private const val IGNORED_EXCEPTION_MESSAGE = "EntityNotFound"
        private const val IGNORED_EXCEPTION_CODE = 404
    }
}

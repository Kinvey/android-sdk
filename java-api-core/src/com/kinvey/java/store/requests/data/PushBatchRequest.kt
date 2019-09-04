/*
 *  Copyright (c) 2019, Kinvey, Inc. All rights reserved.
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
import com.kinvey.java.Constants.META_ID
import com.kinvey.java.cache.ICache
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncItem
import com.kinvey.java.sync.dto.SyncRequest

import java.io.IOException
import java.util.ArrayList

class PushBatchRequest<T : GenericJson>(
    collectionName: String,
    private val itemsCache: ICache<T>,
    private val networkManager: NetworkManager<T>,
    private val client: AbstractClient<*>) : AbstractKinveyExecuteRequest<T>() {

    private val syncManager: SyncManager

    init {
        this.collection = collectionName
        this.syncManager = client.syncManager
    }

    @Throws(IOException::class)
    override fun execute(): Void? {
        val requests = syncManager.popSingleQueue(collection)
        requests.onEach { syncRequest -> syncManager.executeRequest(client, syncRequest) }
        val syncItems = syncManager.popSingleItemQueue(collection)
        var syncRequest: SyncRequest? = null
        val batchSyncItems = ArrayList<SyncItem>()
        syncItems?.let { sItems ->
            for (syncItem in sItems) {
                val httpVerb = syncItem.getRequestMethod()
                val itemId = syncItem.entityID?.id ?: ""
                when (httpVerb) {
                    SyncRequest.HttpVerb.SAVE, //the SAVE case need for backward compatibility
                    SyncRequest.HttpVerb.POST,
                    SyncRequest.HttpVerb.PUT -> {
                        val item = itemsCache.get(itemId)
                        if (item == null) {
                            // check that item wasn't deleted before
                            syncManager.deleteCachedItems(client.query().equals(META_ID, itemId).notEqual(Constants.REQUEST_METHOD, Constants.DELETE))
                        } else if (httpVerb != SyncRequest.HttpVerb.POST) {
                            syncRequest = syncManager.createSyncRequest(collection, networkManager.saveBlocking(item))
                        }
                    }
                    SyncRequest.HttpVerb.DELETE -> syncRequest = syncManager.createSyncRequest(collection, networkManager.deleteBlocking(itemId))
                    else -> {}
                }
                try {
                    if (SyncRequest.HttpVerb.POST == httpVerb) {
                        batchSyncItems.add(syncItem)
                    } else {
                        syncManager.executeRequest(client, syncRequest)
                    }
                } catch (e: KinveyJsonResponseException) {
                    if (e.statusCode != IGNORED_EXCEPTION_CODE
                    && e.message?.contains(IGNORED_EXCEPTION_MESSAGE) == false) throw e
                }
                syncManager.deleteCachedItem(syncItem[Constants._ID] as String?)
            }
            if (batchSyncItems.isNotEmpty()) {
                val saveItems = getSaveItems(batchSyncItems)
                executeSaveRequest(saveItems)
                removeBatchTempItems(batchSyncItems)
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun executeSaveRequest(saveItems: List<T>) {
        val syncRequest = syncManager.createSaveBatchSyncRequest(collection, networkManager, saveItems)
        val response = syncManager.executeBatchRequest(client, networkManager, syncRequest)
        val resultItems = response?.entityList
        resultItems?.let { list -> itemsCache.save(list.mapNotNull { it }) }
    }

    private fun removeBatchTempItems(batchSyncItems: List<SyncItem>) {
        batchSyncItems.mapNotNull { item -> item.entityID?.id }
            .onEach { id -> itemsCache.delete(id) }
    }

    @Throws(IOException::class)
    private fun getSaveItems(batchSyncItems: List<SyncItem>): List<T> {
        return batchSyncItems.mapNotNull { s ->
            val id = s.entityID?.id
            if (id?.isNotEmpty() == true) { itemsCache.get(id) } else null
        }
    }

    override fun cancel() {}

    companion object {
        private const val IGNORED_EXCEPTION_MESSAGE = "EntityNotFound"
        private const val IGNORED_EXCEPTION_CODE = 404
    }
}

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

package com.kinvey.android.async

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Preconditions.checkArgument
import com.kinvey.android.AsyncClientRequest
import com.kinvey.android.sync.KinveyPushBatchResponse
import com.kinvey.android.sync.KinveyPushCallback
import com.kinvey.android.sync.KinveyPushResponse
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.Constants.META_ID
import com.kinvey.java.Constants._ID
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger
import com.kinvey.java.cache.ICache
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.model.KinveySyncSaveBatchResponse
import com.kinvey.java.model.KinveyUpdateSingleItemError
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncItem
import com.kinvey.java.sync.dto.SyncRequest

import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.security.AccessControlException

/**
 * Class represents internal implementation of Async push request that is used to create push
 */
open class AsyncBatchPushRequest<T : GenericJson>(
    private val collection: String,
    private val manager: SyncManager?,
    private val client: AbstractClient<*>?,
    private val storeType: StoreType,
    private val networkManager: NetworkManager<T>,
    storeItemType: Class<T>?,
    val pushCallback: KinveyPushCallback) : AsyncClientRequest<KinveyPushResponse>(pushCallback) {

    private var progress = 0
    private var fullCount = 0
    private val cache: ICache<T>? = client?.cacheManager?.getCache(collection, storeItemType, java.lang.Long.MAX_VALUE)

    private val errors = mutableListOf<Exception>()
    private val batchSyncItems = mutableListOf<SyncItem>()

    @Throws(IOException::class, InvocationTargetException::class)
    override fun executeAsync(): KinveyPushBatchResponse? {

        checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType")

        val pushResponse = KinveyPushBatchResponse()
        val requests = manager?.popSingleQueue(collection)
        val syncItems = manager?.popSingleItemQueue(collection)
        val resultAllItems = mutableListOf<GenericJson>()

        processQueuedSyncRequests(requests, pushResponse)

        fullCount = requests?.size ?: 0
        fullCount += syncItems?.size ?: 0

        val resultSingleItems = processSingleSyncItems(pushResponse, syncItems)
        resultAllItems.addAll(resultSingleItems)
        pushResponse.listOfExceptions = errors

        var batchResponse: KinveySyncSaveBatchResponse<*>? = null
        if (batchSyncItems.isNotEmpty()) {
            val saveItems = getSaveItems(batchSyncItems)
            batchResponse = processBatchSyncRequest(batchSyncItems, saveItems)
        }
        batchResponse?.entityList?.let { entities ->
            entities.mapNotNull { item -> if (item is GenericJson) { item } else { null } }
                .onEach { i -> resultAllItems.add(i) }
            progress += entities.count()
            pushResponse.successCount = progress
        }
        batchResponse?.errors?.let { errors -> pushResponse.errors = errors }
        pushResponse.entities = resultAllItems
        return pushResponse
    }

    @Throws(IOException::class)
    protected open fun processSingleSyncItems(pushResponse: KinveyPushBatchResponse, syncItems: List<SyncItem>?): List<GenericJson> {
        var item: T? = null
        var syncRequest: SyncRequest? = null
        val resultSingleItems = mutableListOf<GenericJson>()
        syncItems?.let { sItems ->
            for (syncItem in sItems) {
                val itemId = syncItem.entityID?.id ?: ""
                val requestMethod = syncItem.requestMethod
                when (requestMethod) {
                    SyncRequest.HttpVerb.SAVE, // the SAVE case need for backward compatibility
                    SyncRequest.HttpVerb.POST,
                    SyncRequest.HttpVerb.PUT -> {
                        item = cache?.get(itemId)
                        if (item == null) {
                            // check that item wasn't deleted before
                            // if item wasn't found, it means that the item was deleted from the Cache by Delete request and the item will be deleted in case:DELETE
                            manager?.deleteCachedItems(client?.query()?.equals(META_ID, itemId)?.notEqual(Constants.REQUEST_METHOD, Constants.DELETE))
                        } else if (requestMethod != SyncRequest.HttpVerb.POST) {
                            syncRequest = manager?.createSyncRequest(collection, networkManager.saveBlocking(item))
                        }
                    }
                    SyncRequest.HttpVerb.DELETE -> syncRequest = manager?.createSyncRequest(collection, networkManager.deleteBlocking(itemId))
                    else -> {}
                }
                try {
                    if (SyncRequest.HttpVerb.POST == requestMethod) {
                        batchSyncItems.add(syncItem)
                    } else {
                        val resultItem = runSingleSyncRequest(syncRequest)
                        resultItem?.let { item -> resultSingleItems.add(item) }
                        pushResponse.successCount = ++progress
                        manager?.deleteCachedItem(syncItem[_ID] as String?)
                    }
                } catch (e: IOException) {
                    val err = KinveyUpdateSingleItemError(e, item)
                    errors.add(err)
                } catch (e: Exception) {
                    callback?.onFailure(e)
                }
                pushCallback?.onProgress(pushResponse.successCount.toLong(), fullCount.toLong())
            }
        }
        return resultSingleItems
    }

    @Throws(IOException::class)
    protected open fun processBatchSyncRequest(syncItems: List<SyncItem>, saveItems: List<T>): KinveySyncSaveBatchResponse<*>? {
        val syncRequest = manager?.createSaveBatchSyncRequest(collection, networkManager, saveItems)
        var resultList: KinveySyncSaveBatchResponse<T>? = null
        try {
            resultList = manager?.executeBatchRequest(client, networkManager, syncRequest)
            removeBatchTempItems(syncItems, resultList)
        } catch (e: IOException) {
            Logger.ERROR(e.message)
            throw e
        }
        return resultList
    }

    @Throws(IOException::class)
    protected open fun runSingleSyncRequest(syncRequest: SyncRequest?): GenericJson? {
        var resultItem: GenericJson? = null
        try {
            if (syncRequest?.httpVerb == SyncRequest.HttpVerb.POST) {
                val tempID = syncRequest.entityID?.id ?: ""
                resultItem = manager?.executeRequest(client, syncRequest)
                val temp = cache?.get(tempID)
                val resultValue = if (resultItem != null) resultItem[_ID] else ""
                temp?.let { item ->
                    if (resultItem != null) { item.set(_ID, resultValue) }
                    cache?.delete(tempID)
                    cache?.save(item)
                }
            } else {
                resultItem = manager?.executeRequest(client, syncRequest)
            }
        } catch (e: KinveyJsonResponseException) {
            if (e.statusCode != IGNORED_EXCEPTION_CODE && !e.message.contains(IGNORED_EXCEPTION_MESSAGE)) throw e
        }
        return resultItem
    }

    protected open fun removeBatchTempItems(batchSyncItems: List<SyncItem>, result: KinveySyncSaveBatchResponse<T>?) {
        batchSyncItems.onEach { item ->
            val tempID = item[_ID] as String?
            val entityID = item.entityID?.id
            if (tempID != null) { manager?.deleteCachedItem(tempID) }
            if (entityID != null) { cache?.delete(entityID) }
            result?.entityList?.filterNotNull()?.onEach { resultItem -> cache?.save(resultItem) }
        }
    }

    @Throws(IOException::class)
    protected open fun getSaveItems(syncItems: List<SyncItem>): List<T> {
        return syncItems.mapNotNull { s ->
            val id = s.entityID?.id
            if (id?.isNotEmpty() == true) { cache?.get(id) } else null
        }
    }

    protected open fun processQueuedSyncRequests(requests: List<SyncRequest>?, pushResponse: KinveyPushResponse) {
        if (requests != null) {
            for (syncRequest in requests) {
                try {
                    manager?.executeRequest(client, syncRequest)
                    pushResponse.successCount = ++progress
                } catch (e: AccessControlException) { //TODO check Exception
                    errors.add(e)
                } catch (e: KinveyException) {
                    errors.add(e)
                } catch (e: Exception) {
                    callback?.onFailure(e)
                }
                pushCallback?.onProgress(pushResponse.successCount.toLong(), fullCount.toLong())
            }
        }
    }

    companion object {
        private const val IGNORED_EXCEPTION_MESSAGE = "EntityNotFound"
        private const val IGNORED_EXCEPTION_CODE = 404
    }
}


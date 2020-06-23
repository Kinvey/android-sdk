/*
 *  Copyright (c) 2020, Kinvey, Inc. All rights reserved.
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

import com.google.api.client.http.HttpResponseException
import com.google.api.client.json.GenericJson
import com.kinvey.java.KinveySaveBatchException
import com.kinvey.java.Logger
import com.kinvey.java.cache.ICache
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.model.KinveyBatchInsertError
import com.kinvey.java.model.KinveySaveBatchResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.requests.data.IRequest
import com.kinvey.java.store.requests.data.PushBatchRequest
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest

import java.io.IOException

import com.kinvey.java.Constants._ID

class CreateRequest<T : GenericJson>(
        private val cache: ICache<T>?,
        private val networkManager: NetworkManager<T>,
        private val writePolicy: WritePolicy,
        private val entity: T,
        private val syncManager: SyncManager?) : IRequest<KinveySaveBatchResponse<T>> {

    private var exception: IOException? = null
    private var wasException = false

    @Throws(IOException::class)
    override fun execute(): KinveySaveBatchResponse<T>  {
        var res = KinveySaveBatchResponse<T>()
        when (writePolicy) {
            WritePolicy.FORCE_LOCAL -> {
                val ret: T? = cache?.save(entity)
                ret?.let {
                    syncManager?.enqueueSaveRequests(networkManager.collectionName
                            ?: "", networkManager, listOf(ret))
                    res.entities = mutableListOf(ret)
                }
            }
            WritePolicy.LOCAL_THEN_NETWORK -> {
                doPushRequest()
                val itemsToSend = cache?.save(entity)
                res = runSaveItemsRequest(itemsToSend)
                if (exception is IOException) {
                    throw IOException(exception)
                }
            }
            WritePolicy.FORCE_NETWORK -> {
                res = runSaveItemsRequest(entity, false)
                if (exception is IOException) {
                    throw IOException(exception)
                }
            }
        }
        return res
    }

    @Throws(IOException::class)
    private fun runSaveItemsRequest(entity: T?, useCache: Boolean = true): KinveySaveBatchResponse<T> {
        Logger.INFO("Start saving entities")
        val res: KinveySaveBatchResponse<T> = KinveySaveBatchResponse()
        if (entity != null) {
            postCreateRequest(entity, res, useCache)
        }
        if (wasException && exception == null) {
            exception = KinveySaveBatchException(null, null, null)
        }
        Logger.INFO("Finish saving entities")
        return res
    }

    @Throws(IOException::class)
    private fun postCreateRequest(entity: T,
                                     result: KinveySaveBatchResponse<T>, useCache: Boolean = true): KinveySaveBatchResponse<T>? {
        var response: KinveySaveBatchResponse<T>? = null
        var tempIds: String? = null
        if (useCache) tempIds = if (networkManager.isTempId(entity)) entity[_ID] as String  else null
        try {
            response = if (useCache && !tempIds.isNullOrEmpty()) {
                val entityWithoutId = if (networkManager.isTempId(entity)) {
                    entity.set(_ID, null) as T
                } else {
                    entity
                }
                networkManager.createBlocking(entityWithoutId)?.execute()
            } else {
                networkManager.createBlocking(entity)?.execute()
            }
        } catch (e: KinveyJsonResponseException) {
            throw e
        } catch (e: IOException) {
            if (!useCache || (e is HttpResponseException && e.statusCode == 401)) {
                wasException = true
                exception = e
            }
            if (useCache) { enqueueSaveRequest(entity, SyncRequest.HttpVerb.POST) }
        }
        if (response != null) {
            if (response.entities != null) {
                if (result.entities == null) {
                    result.entities = mutableListOf()
                }
                result.entities!!.addAll(response.entities!!)
                // If object does not have an _id, then it is being created locally. The cache may
                // provide an _id in this case, but before it is saved to the network, this temporary
                // _id should be removed prior to saving to the backend. This way, the backend
                // will generate a permanent _id that will be used by the cache. Once we get the
                // result from the backend with the permanent _id, the record in the cache with the
                // temporary _id should be removed, and the new record should be saved.
                if (useCache && !tempIds.isNullOrEmpty()) {
                    // The result from the network has the entity with its permanent ID. Need
                    // to remove the entity from the local cache with the temporary ID.
                    cache?.delete(tempIds)
                    cache?.save(response.entities)
                }
            }
            if (response.errors != null) {
                if (result.errors == null) {
                    result.errors = mutableListOf()
                }
                result.errors!!.addAll(response.errors!!)
            }
            if (response.haveErrors && useCache) {
               enqueueErrorRequest(entity, response)
            }
            if (response.haveErrors) {
                removeSuccessItemFromCache(entity, response.errors!![0])
            }

        }
        return response
    }

    @Throws(IOException::class)
    private fun enqueueErrorRequest(saveItem: T, response: KinveySaveBatchResponse<*>) {
        if (response.haveErrors) {
            enqueueSaveRequest(saveItem, SyncRequest.HttpVerb.POST)
        }
    }

    @Throws(IOException::class)
    private fun enqueueSaveRequest(errorItem: T, requestType: SyncRequest.HttpVerb) {
        syncManager?.enqueueRequest(networkManager.collectionName, networkManager, requestType, errorItem[_ID] as String?)
    }

    private fun doPushRequest() {
        val pushRequest = PushBatchRequest(networkManager.collectionName ?: "",
                cache as ICache<T>, networkManager, networkManager.client)
        try {
            pushRequest.execute()
        } catch (t: Throwable) {
            Logger.ERROR(t.message)
        }
    }

    private fun removeSuccessItemFromCache(saveItem: T, error: KinveyBatchInsertError?) {
        if (cache != null && error != null) {
            removeFromCache(saveItem)
        }
    }

    private fun removeFromCache(item: T): String {
        var itemId = ""
        cache?.let {
            itemId = item[_ID]?.toString() ?: ""
            it.delete(itemId)
        }
        return itemId
    }

    //TODO: put async and track cancel
    override fun cancel() {}
}

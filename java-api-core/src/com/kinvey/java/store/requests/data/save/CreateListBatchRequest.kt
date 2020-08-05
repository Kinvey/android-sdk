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
import java.util.ArrayList

import com.kinvey.java.Constants._ID
import com.kinvey.java.KinveyException


class CreateListBatchRequest<T : GenericJson>(
        private val cache: ICache<T>?,
        private val networkManager: NetworkManager<T>,
        private val writePolicy: WritePolicy,
        private val objects: Iterable<T>,
        private val syncManager: SyncManager?) : IRequest<KinveySaveBatchResponse<T>> {

    private var saveList: MutableList<T>? = null
    private var exception: IOException? = null
    private var wasException = false
    private var multipleRequests = false

    private val MAX_POST_ITEMS = 100

    @Throws(IOException::class)
    override fun execute(): KinveySaveBatchResponse<T> {

        var res = KinveySaveBatchResponse<T>()
        when (writePolicy) {
            WritePolicy.FORCE_LOCAL -> {
                res = saveLocally(true)
            }
            WritePolicy.LOCAL_THEN_NETWORK -> {
                doPushRequest()
                val cacheRes = saveLocally()
                res = runSaveItemsRequest(cacheRes.entities)
                cacheRes.errors?.let {
                    if (res.errors == null && !it.isNullOrEmpty()) {
                        res.errors = mutableListOf()
                    }
                    res.errors?.addAll(it)
                }
                if (exception is IOException) {
                    throw IOException(exception)
                }
            }
            WritePolicy.FORCE_NETWORK -> {
                res = runSaveItemsRequest(objects, false)
                if (exception is IOException) {
                    throw IOException(exception)
                }
            }
        }
        return res
    }

    private fun saveLocally(addToSyncManager: Boolean = false): KinveySaveBatchResponse<T> {
        Logger.INFO("Start saving entities locally")
        val res = KinveySaveBatchResponse<T>()
        val ids = objects.filter { it[_ID] != null }.map { it[_ID] as String }

        // has the same ids
        val theSameIds = ids.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (theSameIds.isNotEmpty()) {
            val kinveyException = KinveyException("The array contains more than one entity with _id ${theSameIds.keys}")
            throw kinveyException
        }
        //items already in the cache

        val itemsInCache = cache?.get(ids) ?: mutableListOf()
        if (itemsInCache.isNotEmpty()) {
            val idsInCache = itemsInCache.map { it[_ID] as String }
            val kinveyException = KinveyException("An entity with _id $idsInCache already exists.")
            throw kinveyException
        }
        val retList: List<T> = cache?.save(objects) ?: ArrayList()

        if (addToSyncManager) {
            syncManager?.enqueueSaveRequests(networkManager.collectionName
                    ?: "", networkManager, retList)
        }
        res.entities = mutableListOf()
        res.entities!!.addAll(retList)
        res.errors = mutableListOf()
        Logger.INFO("Finish saving entities locally")
        return res
    }

    @Throws(IOException::class)
    protected fun runSaveItemsRequest(objects: Iterable<T>?, useCache: Boolean = true): KinveySaveBatchResponse<T> {
        Logger.INFO("Start saving entities")
        filterObjects(objects as List<T>)
        val res: KinveySaveBatchResponse<T> = KinveySaveBatchResponse()
        val count = saveList?.count() ?: 0
        multipleRequests = count > MAX_POST_ITEMS
        if (saveList?.isNotEmpty() == true && saveList is List<T>) {
            postBatchItems(saveList as List<T>, res, useCache)
        }
        if (wasException && exception == null) {
            exception = KinveySaveBatchException(null, null, null)
        }
        Logger.INFO("Finish saving entities")
        return res
    }

    private fun postBatchItems(entities: List<T>, result: KinveySaveBatchResponse<T>, useCache: Boolean = true) {
        entities.chunked(MAX_POST_ITEMS).onEach { items ->
            postSaveBatchRequest(items, result, useCache)
        }
    }

    @Throws(IOException::class)
    protected fun postSaveBatchRequest(entities: List<T>,
                                       result: KinveySaveBatchResponse<T>, useCache: Boolean = true): KinveySaveBatchResponse<T>? {
        var response: KinveySaveBatchResponse<T>? = null
        val batchSaveErrors = ArrayList<KinveyBatchInsertError>()
        var tempIds: List<String> = mutableListOf()
        if (useCache) tempIds = entities.filter { networkManager.isTempId(it) }.map { it[_ID] as String }
        try {
            response = if (useCache && tempIds.isNotEmpty()) {
                val entitiesWithoutIds =
                        entities.map {
                            if (networkManager.isTempId(it)) {
                                it.set(_ID, null) as T
                            } else {
                                it
                            }
                        }
                networkManager.saveBatchBlocking(entitiesWithoutIds)?.execute()
            } else {
                networkManager.saveBatchBlocking(entities)?.execute()
            }
        } catch (e: KinveyJsonResponseException) {
            if (!multipleRequests) throw e
        } catch (e: IOException) {
            if (!useCache || (e is HttpResponseException && e.statusCode == 401)) {
                wasException = true
                exception = e
            }
            if (useCache) {
                enqueueSaveRequests(entities, SyncRequest.HttpVerb.POST)
            }
        }
        if (response != null) {
            if (response.entities != null) {
                if (result.entities == null) {
                    result.entities = mutableListOf()
                }

                result.entities!!.addAll(response.entities!!.filter { it[_ID] != null })
                // If object does not have an _id, then it is being created locally. The cache may
                // provide an _id in this case, but before it is saved to the network, this temporary
                // _id should be removed prior to saving to the backend. This way, the backend
                // will generate a permanent _id that will be used by the cache. Once we get the
                // result from the backend with the permanent _id, the record in the cache with the
                // temporary _id should be removed, and the new record should be saved.
                if (useCache && tempIds.isNotEmpty()) {
                    // The result from the network has the entity with its permanent ID. Need
                    // to remove the entity from the local cache with the temporary ID.
                    cache?.delete(tempIds)
                    cache?.save(response.entities)
                }
            }
            result.errors = mutableListOf()
            if (response.errors != null) {
                result.errors!!.addAll(response.errors!!)
            }
            if (response.haveErrors && useCache) {
                enqueueBatchErrorsRequests(entities, response)
            }
            response.errors?.let { errors -> batchSaveErrors.addAll(errors) }
            removeSuccessBatchItemsFromCache(entities, batchSaveErrors)
        }
        return response
    }

    @Throws(IOException::class)
    private fun enqueueBatchErrorsRequests(saveList: List<T>?, response: KinveySaveBatchResponse<*>) {
        val errIndexes = getErrIndexes(response.errors)
        var errorItems: List<T>? = null
        saveList?.let { sList -> errorItems = errIndexes.mapNotNull { idx -> sList.getOrNull(idx) } }
        errorItems?.let { errItems -> enqueueSaveRequests(errItems, SyncRequest.HttpVerb.POST) }
    }

    @Throws(IOException::class)
    private fun enqueueSaveRequests(errorItems: List<T>, requestType: SyncRequest.HttpVerb) {
        errorItems.forEach { itm ->
            syncManager?.enqueueRequest(networkManager.collectionName, networkManager, requestType, itm[_ID] as String?)
        }
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

    private fun filterObjects(list: List<T>) {
        saveList = mutableListOf()
        list.onEach { itm ->
            saveList?.add(itm)
        }
    }

    private fun getErrIndexes(errList: List<KinveyBatchInsertError>?): List<Int> {
        return errList?.map { err -> err.index }.orEmpty()
    }

    private fun removeSuccessBatchItemsFromCache(saveList: List<T>, errList: List<KinveyBatchInsertError>) {
        if (cache == null) {
            return
        }
        val errIndexes = getErrIndexes(errList)
        errIndexes.mapNotNull { idx -> saveList.getOrNull(idx) }
                .onEach { item -> removeFromCache(item) }
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

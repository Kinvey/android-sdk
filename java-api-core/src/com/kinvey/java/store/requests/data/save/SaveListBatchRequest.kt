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

import com.google.api.client.json.GenericJson
import com.kinvey.java.KinveySaveBatchException
import com.kinvey.java.Logger
import com.kinvey.java.cache.ICache
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.model.KinveyBatchInsertError
import com.kinvey.java.model.KinveyUpdateSingleItemError
import com.kinvey.java.model.KinveySaveBatchResponse
import com.kinvey.java.model.KinveyUpdateObjectsResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.requests.data.IRequest
import com.kinvey.java.store.requests.data.PushBatchRequest
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest

import java.io.IOException
import java.util.ArrayList

import com.kinvey.java.Constants._ID
import com.kinvey.java.network.NetworkManager.ID_FIELD_NAME

class SaveListBatchRequest<T : GenericJson>(
    private val cache: ICache<T>?,
    private val networkManager: NetworkManager<T>,
    private val writePolicy: WritePolicy,
    private val objects: Iterable<T>,
    private val syncManager: SyncManager) : IRequest<List<T>> {

    private var updateList: MutableList<T>? = null
    private var saveList: MutableList<T>? = null
    private var exception: KinveySaveBatchException? = null
    private var wasException = false
    private var multipleRequests = false

    private val MAX_POST_ITEMS = 100

    @Throws(IOException::class)
    override fun execute(): List<T> {
        var retList: List<T> = ArrayList()
        when (writePolicy) {
            WritePolicy.FORCE_LOCAL -> {
                retList = cache?.save(objects) ?: ArrayList()
                syncManager.enqueueSaveRequests(networkManager.collectionName, networkManager, retList)
            }
            WritePolicy.LOCAL_THEN_NETWORK -> {
                doPushRequest()
                val itemsToSend = cache?.save(objects)
                retList = runSaveItemsRequest(itemsToSend)
                cache?.save(retList)
                if (exception is IOException) {
                    throw exception as IOException
                }
            }
            WritePolicy.FORCE_NETWORK -> {
                retList = runSaveItemsRequest(objects, false)
                if (exception is IOException) {
                    throw exception as IOException
                }
            }
        }
        return retList
    }

    @Throws(IOException::class)
    private fun runSaveItemsRequest(objects: Iterable<T>?, useCache: Boolean = true): List<T> {
        Logger.INFO("Start saving entities")
        filterObjects(objects as List<T>)
        val postEntities = ArrayList<T>()
        val batchSaveErrors = ArrayList<KinveyBatchInsertError>()
        val updateResponse = updateObjects(updateList)
        //updateResponse.entities?.let { list -> saveEntities.addAll(list) }
        val count = saveList?.count() ?: 0
        multipleRequests = count > MAX_POST_ITEMS

        if (saveList?.isNotEmpty() == true && saveList is List<T>) {
            postBatchItems(saveList as List<T>, postEntities, batchSaveErrors, useCache)
        }
        if (wasException) {
            exception = KinveySaveBatchException(batchSaveErrors, updateResponse.errors, postEntities)
        }
        Logger.INFO("Finish saving entities")
        val putEntities = updateResponse.entities ?: mutableListOf()
        val resultItems = recoverItemsOrder(objects, postEntities, putEntities)
        return resultItems
    }

    private fun postBatchItems(entities: List<T>, batchSaveEntities: MutableList<T>, batchSaveErrors: MutableList<KinveyBatchInsertError>, useCache: Boolean = true) {
        entities.chunked(MAX_POST_ITEMS).onEach { items ->
            postSaveBatchRequest(items, batchSaveEntities, batchSaveErrors, useCache)
        }
    }

    @Throws(IOException::class)
    private fun postSaveBatchRequest(entities: List<T>,
        batchSaveEntities: MutableList<T>, batchSaveErrors: MutableList<KinveyBatchInsertError>, useCache: Boolean = true): KinveySaveBatchResponse<*>? {
        var response: KinveySaveBatchResponse<*>? = null
        try {
            val batchList = prepareSaveItems(SyncRequest.HttpVerb.POST, entities)
            response = networkManager.saveBatchBlocking(batchList).execute()
        }
        catch (e: KinveyJsonResponseException) {
            if (!multipleRequests) throw e
        }
        catch (e: IOException) {
            //wasException = true
            if (useCache) { enqueueSaveRequests(entities, SyncRequest.HttpVerb.POST) }
        }
        if (response != null) {
            if (!response.haveErrors) {
                response.entities?.let { list -> batchSaveEntities.addAll(list as List<T>) }
            } else {
                //wasException = true
                response.errors?.let{ errors -> batchSaveErrors.addAll(errors) }
                enqueueBatchErrorsRequests(entities, response)
            }
            removeSuccessBatchItemsFromCache(entities, batchSaveErrors)
        } else if (multipleRequests) {
            val emptyList = listOf(*Array<Any>(entities.count()) { GenericJson() })
            batchSaveEntities.addAll(emptyList as List<T>)
        }
        return response
    }

    private fun recoverItemsOrder(srcItems: List<T>, postItems: List<T?>, putItems: List<T?>): List<T> {
        var postIdx = 0
        var putIdx = 0
        if (srcItems.count() != postItems.count() + putItems.count()) {
            return postItems.filterNotNull() + putItems.filterNotNull()
        }
        return srcItems.mapNotNull { item ->
            if (item[_ID] != null
             && item[_ID] == putItems.getOrNull(putIdx)?.getValue(_ID)) {
                putItems.getOrNull(putIdx++)
            } else {
                postItems.getOrNull(postIdx++)
            }
        }
    }

    private fun prepareSaveItems(requestType: SyncRequest.HttpVerb, itemsList: List<T>?): List<T> {
        return itemsList?.map { itm -> checkEntityId(requestType, itm) } ?: ArrayList()
    }

    private fun checkEntityId(requestType: SyncRequest.HttpVerb, entity: T): T {
        if (SyncRequest.HttpVerb.POST == requestType) {
            // Remvove the _id, since this is a create operation
            entity.takeIf { item -> item[_ID] != null }?.let { item ->
                return item.clone().set(_ID, null) as T
            }
        }
        return entity
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
            syncManager.enqueueRequest(networkManager.collectionName, networkManager, requestType, itm[_ID] as String?)
        }
    }

    private fun doPushRequest() {
        val pushRequest = PushBatchRequest(networkManager.collectionName,
        cache as ICache<T>, networkManager, networkManager.client)
        try {
            pushRequest.execute()
        } catch (t: Throwable) {
            Logger.ERROR(t.message)
        }
    }

    private fun filterObjects(list: List<T>) {
        saveList = mutableListOf()
        updateList = mutableListOf()
        list.onEach { itm ->
            val sourceId = itm[ID_FIELD_NAME] as String?
            val isTempId = networkManager.isTempId(itm)
            if (sourceId == null || isTempId) {
                saveList?.add(itm)
            } else {
                updateList?.add(itm)
            }
        }
    }

    @Throws(IOException::class)
    private fun updateObjects(items: List<T>?): KinveyUpdateObjectsResponse<T> {
        val errors = ArrayList<KinveyUpdateSingleItemError>()
        val result = KinveyUpdateObjectsResponse<T>()
        val ret = items?.mapNotNull { itm ->
            var res: T? = null
            try {
                res = networkManager.saveBlocking(itm).execute()
                removeFromCache(itm)
            } catch (e: IOException) {
                wasException = true
                errors.add(KinveyUpdateSingleItemError(e, itm))
                syncManager.enqueueRequest(networkManager.collectionName,
                        networkManager, SyncRequest.HttpVerb.PUT, itm[_ID] as String?)
            }
            res
        }
        result.entities = ret
        result.errors = errors
        return result
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

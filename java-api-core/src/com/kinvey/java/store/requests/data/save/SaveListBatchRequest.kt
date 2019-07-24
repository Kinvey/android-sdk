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
                cache?.save(objects)
                retList = runSaveItemsRequest(objects)
                cache?.save(retList)
                if (exception is IOException) {
                    throw exception as IOException
                }
            }
            WritePolicy.FORCE_NETWORK -> {
                retList = runSaveItemsRequest(objects)
                if (exception is IOException) {
                    throw exception as IOException
                }
            }
        }
        return retList
    }

    @Throws(IOException::class)
    private fun runSaveItemsRequest(objects: Iterable<T>): List<T> {
        Logger.INFO("Start saving entities")
        filterObjects(objects as List<T>)
        val saveEntities = ArrayList<T>()
        val batchSaveErrors = ArrayList<KinveyBatchInsertError>()

        val updateResponse = updateObjects(updateList)
        saveEntities.addAll(updateResponse.entities)

        if (saveList?.isNotEmpty() == true) {
            val batchList = prepareSaveItems(SyncRequest.HttpVerb.POST, saveList)
            postSaveBatchRequest(batchList, saveEntities, batchSaveErrors)
        }
        if (wasException) {
            exception = KinveySaveBatchException(batchSaveErrors, updateResponse.errors, saveEntities)
        }
        Logger.INFO("Finish saving entities")
        return saveEntities
    }

    @Throws(IOException::class)
    private fun postSaveBatchRequest(entities: List<T>,
        batchSaveEntities: MutableList<T>, batchSaveErrors: MutableList<KinveyBatchInsertError>): KinveySaveBatchResponse<*>? {
        var response: KinveySaveBatchResponse<*>? = null
        try {
            response = networkManager.saveBatchBlocking(entities).execute()
        } catch (e: KinveyJsonResponseException) {
            throw e
        } catch (e: IOException) {
            wasException = true
            enqueueSaveRequests(entities, SyncRequest.HttpVerb.POST)
        }
        if (response != null) {
            if (!response.haveErrors) {
                response.entities?.let { list -> batchSaveEntities.addAll(list as List<T>) }
            } else {
                wasException = true
                response.errors?.let{ errors -> batchSaveErrors.addAll(errors) }
                enqueueBatchErrorsRequests(entities, response)
            }
            removeSuccessBatchItemsFromCache(entities, batchSaveErrors)
        }
        return response
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

//        if (SyncRequest.HttpVerb.POST == requestType) {
//            if (entity[_ID] != null) {
//                // Remvove the _id, since this is a create operation
//                return entity.clone().set(_ID, null) as T
//            }
//        }
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
                cache, networkManager, networkManager.client)
        try {
            pushRequest.execute()
        } catch (t: Throwable) {
            Logger.ERROR(t.message)
        }
    }

    private fun filterObjects(list: List<T>) {
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

    override fun cancel() {
        //TODO: put async and track cancel
    }
}

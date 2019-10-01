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

package com.kinvey.java.sync

import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonParser
import com.google.gson.Gson
import com.kinvey.java.AbstractClient
import com.kinvey.java.AbstractClient.Companion.sharedInstance
import com.kinvey.java.KinveySaveBatchException
import com.kinvey.java.Logger.Companion.ERROR
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.cache.ICacheManager
import com.kinvey.java.core.AbstractKinveyClientRequest
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.model.KinveySyncSaveBatchResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.query.MongoQueryFilter.MongoQueryFilterBuilder
import com.kinvey.java.store.BaseDataStore
import com.kinvey.java.store.BaseDataStore.Companion.collection
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.dto.SyncCollections
import com.kinvey.java.sync.dto.SyncItem
import com.kinvey.java.sync.dto.SyncRequest
import com.kinvey.java.sync.dto.SyncRequest.HttpVerb
import com.kinvey.java.sync.dto.SyncRequest.SyncMetaData
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Created by Prots on 2/24/16.
 */
open class SyncManager(val cacheManager: ICacheManager?) {

    fun removeEntity(collectionName: String?, curEntityID: String?) {
        val query = Query(MongoQueryFilterBuilder())
        query.equals(COLLECTION_NAME, collectionName).equals(META_ID, curEntityID)
        cacheManager?.getCache(SYNC, SyncRequest::class.java, Long.MAX_VALUE)?.delete(query)
    }

    val collectionTables: List<String>
        get() {
            val collectionsCache = cacheManager?.getCache(SYNC_COLLECTIONS, SyncCollections::class.java, Long.MAX_VALUE)
            val collections = collectionsCache?.get()
            return collections?.mapNotNull { it.collectionName }?.toMutableList() as List<String>
        }

    @Deprecated("use [.popSingleItemQueue]")
    open fun popSingleQueue(collectionName: String?): List<SyncRequest>? {
        val requestCache = cacheManager?.getCache(SYNC, SyncRequest::class.java, Long.MAX_VALUE)
        val q = Query(MongoQueryFilterBuilder()).equals(COLLECTION, collectionName)
        var requests: List<SyncRequest>? = null
        requestCache?.let {
            requests = it[q]
        }
        //delete request from the queue
        requests?.let { it ->
            val ids = it.map { request -> request[ID].toString() }.toMutableList()
            requestCache?.delete(ids)
        }
        return requests
    }

    fun popSingleItemQueue(collectionName: String?): List<SyncItem>? {
        val requestCache = cacheManager?.getCache(SYNC_ITEM_TABLE_NAME, SyncItem::class.java, Long.MAX_VALUE)
        val q = Query(MongoQueryFilterBuilder()).equals(COLLECTION, collectionName)
        return requestCache?.run { if (this.get().isNotEmpty()) this[q] else null }
    }

    @Deprecated("use [.deleteCachedItem]")
    fun deleteCachedRequest(id: String?): Int {
        val requestCache = cacheManager?.getCache(SYNC, SyncRequest::class.java, Long.MAX_VALUE)
        return requestCache?.delete(id ?: "") ?: 0
    }

    fun deleteCachedItem(id: String?): Int {
        val requestCache = cacheManager?.getCache(SYNC_ITEM_TABLE_NAME, SyncItem::class.java, Long.MAX_VALUE)
        return requestCache?.delete(id ?: "") ?: 0
    }

    fun deleteCachedItems(q: Query?): Int {
        val requestCache = cacheManager?.getCache(SYNC_ITEM_TABLE_NAME, SyncItem::class.java, Long.MAX_VALUE)
        return requestCache?.delete(q!!) ?: 0
    }

    @Deprecated("use [.enqueueRequest]")
    @Throws(IOException::class)
    fun enqueueRequest(collectionName: String, clientRequest: AbstractKinveyJsonClientRequest<*>) {
        val requestCache = cacheManager?.getCache(SYNC, SyncRequest::class.java, Long.MAX_VALUE)
        requestCache?.save(createSyncRequest(collectionName, clientRequest))
    }

    @Deprecated("use [.enqueueRequests]")
    @Throws(IOException::class)
    fun <T : GenericJson> enqueueRequests(collectionName: String, networkManager: NetworkManager<T>, ret: List<T>) {
        val requestCache = cacheManager?.getCache(SYNC, SyncRequest::class.java, Long.MAX_VALUE)
        val syncRequests = ret.map { item -> createSyncRequest(collectionName, networkManager.saveBlocking(item)) }
        requestCache?.save(syncRequests)
    }

    /**
     * Used to save sync requests that were not completed
     * @param request Sync request to be saved
     */
    fun enqueueRequest(request: SyncRequest) {
        val requestCache = cacheManager?.getCache(SYNC, SyncRequest::class.java, Long.MAX_VALUE)
        requestCache?.save(request)
    }

    @Throws(IOException::class)
    fun <T : GenericJson> enqueueRequest(collectionName: String?, networkManager: NetworkManager<T>?, httpMethod: HttpVerb?, id: String?) {
        val requestCache = cacheManager?.getCache(SYNC_ITEM_TABLE_NAME, SyncItem::class.java, Long.MAX_VALUE)
        val syncItem = prepareSyncItemRequest(requestCache, collectionName, networkManager, httpMethod, id)
        syncItem?.let { requestCache?.save(it) }
    }

    @Throws(IOException::class)
    fun <T : GenericJson> enqueueDeleteRequests(collectionName: String?, networkManager: NetworkManager<T>?, ret: List<T>?) {
        val requestCache = cacheManager?.getCache(SYNC_ITEM_TABLE_NAME, SyncItem::class.java, Long.MAX_VALUE)
        val syncRequests = ret?.mapNotNull { item ->
            val syncItemId = item[ID] as String?
            prepareSyncItemRequest(requestCache, collectionName, networkManager, HttpVerb.DELETE, syncItemId)
        }
        syncRequests?.let { requests -> requestCache?.save(requests) }
    }

    @Throws(IOException::class)
    fun <T : GenericJson> enqueueSaveRequests(collectionName: String, networkManager: NetworkManager<T>, ret: List<T>?) {
        val requestCache = cacheManager?.getCache(SYNC_ITEM_TABLE_NAME, SyncItem::class.java, Long.MAX_VALUE)
        val syncRequests = ret?.mapNotNull { item ->
            val syncItemId = item[ID] as String?
            prepareSyncItemRequest(requestCache, collectionName, networkManager, if (networkManager.isTempId(item)) HttpVerb.POST else HttpVerb.PUT, syncItemId)
        }
        syncRequests?.let { requests -> requestCache?.save(requests) }
    }

    @Throws(IOException::class)
    fun <T : GenericJson> enqueueDeleteRequests(collectionName: String?, networkManager: NetworkManager<T>?, ids: Iterable<String>?) {
        val requestCache = cacheManager?.getCache(SYNC_ITEM_TABLE_NAME, SyncItem::class.java, Long.MAX_VALUE)
        val syncRequests = ids?.map { id -> prepareSyncItemRequest(requestCache, collectionName, networkManager, HttpVerb.DELETE, id) }
        syncRequests?.let { requests -> requestCache?.save(requests) }
    }

    @Throws(IOException::class)
    private fun <T : GenericJson> prepareSyncItemRequest(requestCache: ICache<SyncItem>?,
                                                          collectionName: String?,
                                                          networkManager: NetworkManager<T>?,
                                                          httpMethod: HttpVerb?,
                                                          syncItemId: String?): SyncItem? {
        val entityQuery = sharedInstance?.query()
        entityQuery?.equals(META_DOT_ID, syncItemId)
        val cache = requestCache ?: return null
        return entityQuery?.run {
            val itemsList = cache[this]
            when {
                itemsList.isEmpty() -> createSyncItem(collectionName, httpMethod, networkManager, syncItemId)
                httpMethod == HttpVerb.DELETE -> {
                    cache.delete(this)
                    createSyncItem(collectionName, httpMethod, networkManager, syncItemId)
                }
                else -> null
            }
        }
    }

    @Throws(IOException::class)
    private fun createSyncItem(collectionName: String?, httpMethod: HttpVerb?, networkManager: NetworkManager<*>?, id: String?): SyncItem {
        val entityID = SyncMetaData()
        if (id != null) {
            entityID.id = id
        }
        entityID.customerVersion = networkManager?.clientAppVersion
        entityID.customheader = networkManager?.customRequestProperties?.get("X-Kinvey-Custom-Request-Properties") as String?
        return SyncItem(httpMethod, entityID, collectionName)
    }

    @Throws(IOException::class)
    fun createSyncRequest(collectionName: String?, clientRequest: AbstractKinveyJsonClientRequest<*>?): SyncRequest {
        val httpRequest = clientRequest?.buildHttpRequest()
        val entityID = SyncMetaData()
        val os = ByteArrayOutputStream()
        httpRequest?.content?.writeTo(os)
        entityID.data = os.toString(UTF_8)
        if (clientRequest?.jsonContent != null) {
            entityID.id = clientRequest.jsonContent[ID].toString()
        }
        if (clientRequest?.containsKey(ENTITY_ID) == true) {
            entityID.id = clientRequest[ENTITY_ID].toString()
        }
        entityID.customerVersion = clientRequest?.customerAppVersion
        entityID.customheader = clientRequest?.customRequestProperties
        return SyncRequest(
                HttpVerb.valueOf(clientRequest?.requestMethod?.toUpperCase() ?: ""),
                entityID, httpRequest?.url, collectionName
        )
    }

    /**
     * This methods gets the count of sync operation to be performed
     * @param collectionName the name of the collection we want to get the info
     * @return the count of sync objects for given collection
     */
    fun getCount(collectionName: String?): Long {
        val requestCache = cacheManager?.getCache(SYNC, SyncRequest::class.java, Long.MAX_VALUE)
        val requestItemCache = cacheManager?.getCache(SYNC_ITEM_TABLE_NAME, SyncItem::class.java, Long.MAX_VALUE)
        val q = Query(MongoQueryFilterBuilder()).equals(COLLECTION, collectionName)
        return (requestCache?.count(q) ?: 0) + (requestItemCache?.count(q) ?: 0)
    }

    /**
     * This method uses the AnsycAppData API to execute requests.  It contains an if/else block for the verb of the request
     * and then calls the appropriate appdata method.  As it uses the Async API, every request has a callback.
     *
     *
     * Dependant on the verb and the result, this method will also update the local database.
     *
     * @param client kinvey client to execute request with
     * @param request Sync request to be executed
     */
    @Throws(IOException::class)
    open fun executeRequest(client: AbstractClient<*>?, request: SyncRequest?): GenericJson? {
        client?.clientAppVersion = request?.entityID?.customerVersion
        client?.setCustomRequestProperties(Gson().fromJson(request?.entityID?.customheader, GenericJson::class.java))
        var entity: GenericJson? = null
        var result: GenericJson? = null
        try {
            if (!request?.entityID?.data.isNullOrEmpty()) {
                entity = client?.jsonFactory?.createJsonParser(request?.entityID?.data)?.parse<GenericJson>(GenericJson::class.java)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var networkDataStore: BaseDataStore<GenericJson>? = null
        if (client != null) {
            networkDataStore = collection(request?.collectionName ?: "", GenericJson::class.java, StoreType.NETWORK, client)
        }
        if (request?.httpVerb == HttpVerb.PUT || request?.httpVerb == HttpVerb.POST) {
            entity?.let {
                try {
                    if (request.httpVerb == HttpVerb.POST) {
                        // Remvove the _id, since this is a create operation
                        entity.set("_id", null)
                    }
                    result = networkDataStore?.save(it)
                } catch (e: Exception) {
                    enqueueRequest(request)
                    throw e
                }
            }
        } else if (request?.httpVerb == HttpVerb.DELETE) {
            val curID: String? = request.entityID?.id
            if (curID != null && curID.startsWith("{") && curID.endsWith("}")) {
                //it's a query
                val q = Query().setQueryString(curID)
                try {
                    networkDataStore?.delete(q)
                } catch (e: Exception) {
                    enqueueRequest(request)
                    throw e
                }
            } else if (curID == null && request.url?.contains("?query=") == true) {
                val url = request.url ?: ""
                val index = url.indexOf("?query=") + 7
                val qString = url.substring(index)
                var decodedQuery: String? = null
                try {
                    decodedQuery = URLDecoder.decode(qString, UTF_8)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                if (decodedQuery == null) {
                    return result
                }
                val q = Query().setQueryString(decodedQuery)
                try {
                    networkDataStore?.delete(q)
                } catch (e: Exception) {
                    enqueueRequest(request)
                    throw e
                }
            } else {
                //it's a single ID
                try {
                    networkDataStore?.delete(request.entityID?.id?: "")
                } catch (e: Exception) {
                    //TODO: need to check the errors
                    //enqueueRequest(request);
                    throw e
                }
            }
        }
        return result
    }

    /**
     * This method uses the AnsycAppData API to execute requests.  It contains an if/else block for the verb of the request
     * and then calls the appropriate appdata method.  As it uses the Async API, every request has a callback.
     *
     *
     * Dependant on the verb and the result, this method will also update the local database.
     *
     * @param client kinvey client to execute request with
     * @param request Sync request to be executed
     */
    @Throws(IOException::class)
    fun <T : GenericJson> executeBatchRequest(client: AbstractClient<*>, networkManager: NetworkManager<T>, request: SyncRequest): KinveySyncSaveBatchResponse<T>? {
        client.clientAppVersion = request.entityID?.customerVersion
        client.setCustomRequestProperties(Gson().fromJson(request.entityID?.customheader, GenericJson::class.java))
        var entityList: Collection<T>? = null
        val bunchData = request.entityID?.bunchData ?: false
        val dataJson: String? = request.entityID?.data
        try {
            if (dataJson != null) {
                val parser: JsonParser = client.jsonFactory.createJsonParser(dataJson)
                if (bunchData) {
                    entityList = parser.parseArray(List::class.java, networkManager.currentClass)
                } else return null
            }
        } catch (e: IOException) {
            ERROR(e.message)
        }
        val dataStoreNet: BaseDataStore<T> = collection(request.collectionName ?: "", networkManager.currentClass, StoreType.NETWORK, client)
        return runSaveBatchSyncRequest(request, entityList, dataStoreNet)
    }

    @Throws(IOException::class)
    private fun <T : GenericJson> runSaveBatchSyncRequest(request: SyncRequest,
                                                           entityList: Collection<T>?, dataStore: BaseDataStore<T>): KinveySyncSaveBatchResponse<T>? {
        var result: KinveySyncSaveBatchResponse<T>? = null
        if (entityList != null) {
            result = try {
                val resultItems: List<T> = dataStore.saveBatch(entityList)
                KinveySyncSaveBatchResponse(resultItems, null)
            } catch (e: KinveySaveBatchException) {
                KinveySyncSaveBatchResponse<T>(e.entities as List<T>, e.errors)//throw e;
            } catch (e: Exception) {
                enqueueRequest(request)
                throw e
            }
        }
        return result
    }

    @Throws(IOException::class)
    fun <T : GenericJson> createSaveBatchSyncRequest(collectionName: String?, networkManager: NetworkManager<T>, ret: List<T>?): SyncRequest {
        val request = createSaveBatchSyncRequest(collectionName, networkManager.saveBatchBlocking(ret) as AbstractKinveyClientRequest<*>)
        request.entityID?.bunchData = true
        return request
    }

    @Throws(IOException::class)
    fun createSaveBatchSyncRequest(collectionName: String?, clientRequest: AbstractKinveyClientRequest<*>): SyncRequest {
        val httpRequest = clientRequest.buildHttpRequest()
        val entityID = SyncMetaData()
        if (httpRequest.content != null) {
            val os = ByteArrayOutputStream()
            httpRequest.content.writeTo(os)
            entityID.data = os.toString(UTF_8)
        }
        if (clientRequest is AbstractKinveyJsonClientRequest<*>) {
            val content = clientRequest.jsonContent
            if (content != null) {
                entityID.id = content[ID].toString()
            }
        }
        if (clientRequest.containsKey(ENTITY_ID)) {
            entityID.id = clientRequest[ENTITY_ID].toString()
        }
        entityID.customerVersion = clientRequest.customerAppVersion
        entityID.customheader = clientRequest.customRequestProperties
        val requestMethod = clientRequest.requestMethod.toUpperCase()
        return SyncRequest(
                HttpVerb.fromString(requestMethod),
                entityID, httpRequest.url,
                collectionName
        )
    }

    fun clear(collectionName: String?): Int {
        val requestCache = cacheManager?.getCache(SYNC, SyncRequest::class.java, Long.MAX_VALUE)
        val requestItemCache = cacheManager?.getCache(SYNC_ITEM_TABLE_NAME, SyncItem::class.java, Long.MAX_VALUE)
        val q = Query(MongoQueryFilterBuilder()).equals(COLLECTION, collectionName)
        return (requestCache?.delete(q) ?: 0) + (requestItemCache?.delete(q) ?: 0)
    }

    fun clear(collectionName: String?, query: Query): Int {
        var query = query
        val requestCache = cacheManager?.getCache(SYNC, SyncRequest::class.java, Long.MAX_VALUE)
        val requestItemCache = cacheManager?.getCache(SYNC_ITEM_TABLE_NAME, SyncItem::class.java, Long.MAX_VALUE)
        query = query.equals(COLLECTION, collectionName)
        return (requestCache?.delete(query) ?: 0) + (requestItemCache?.delete(query) ?: 0)
    }

    companion object {
        private const val SYNC_ITEM_TABLE_NAME = "syncitems"
        private const val COLLECTION = "collection"
        private const val COLLECTION_NAME = "collectionName"
        private const val SYNC = "sync"
        private const val META_DOT_ID = "meta.id"
        private const val META_ID = "meta._id"
        private const val ID = "_id"
        private const val UTF_8 = "UTF-8"
        private const val ENTITY_ID = "entityID"
        private const val SYNC_COLLECTIONS = "syncCollections"
    }

}
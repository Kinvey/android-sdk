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

package com.kinvey.java.network

import com.google.api.client.json.GenericJson
import com.google.api.client.util.GenericData
import com.google.api.client.util.Key
import com.google.common.base.Joiner
import com.google.common.base.Preconditions
import com.google.gson.Gson
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.Logger.Companion.ERROR
import com.kinvey.java.Logger.Companion.INFO
import com.kinvey.java.Query
import com.kinvey.java.annotations.ReferenceHelper
import com.kinvey.java.annotations.ReferenceHelper.ReferenceListener
import com.kinvey.java.cache.ICache
import com.kinvey.java.core.*
import com.kinvey.java.deltaset.DeltaSetItem
import com.kinvey.java.deltaset.DeltaSetMerge
import com.kinvey.java.dto.BatchList
import com.kinvey.java.dto.DeviceId
import com.kinvey.java.model.*
import com.kinvey.java.query.MongoQueryFilter.MongoQueryFilterBuilder
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.collections.ArrayList

/**
 * Class for managing appData access to the Kinvey backend.
 *
 * @author mjsalinger
 * @author m0rganic
 * @author edwardf
 * @since 2.0.2
 */
open class NetworkManager<T : GenericJson>(

    /**
     * Gets/Sets the collectionName
     * @param collectionName Name of the appData collection.
     */
    var collectionName: String?,
    /**
     * Gets current class that this NetworkManager instance references.
     * @return Current appData class for marshalling data
     */
    val currentClass: Class<T>?,
    /**
     * Gets current client for this NetworkManager
     * @return current client instance
     */
    val client: AbstractClient<*>?) {

    var clientAppVersion: String? = null
    var customRequestProperties: GenericData? = GenericData()
        private set

    fun setClientAppVersion(major: Int, minor: Int, patch: Int) {
        clientAppVersion = "$major.$minor.$patch"
    }

    fun setCustomRequestProperties(customheaders: GenericJson?) {
        customRequestProperties = customheaders
    }

    fun setCustomRequestProperty(key: String?, value: Any?) {
        if (customRequestProperties == null) {
            customRequestProperties = GenericJson()
        }
        customRequestProperties?.run { this[key] = value }
    }

    fun clearCustomRequestProperties() {
        customRequestProperties = GenericJson()
    }

    /**
     * Creates a new instance of [Query]
     *
     * @return New instance of Query object.
     */
    fun query(): Query? {
        return Query(MongoQueryFilterBuilder())
    }

    /**
     * Method to get an entity or entities.  Pass null to entityID to return all entities
     * in a collection.
     *
     * @param entityID entityID to get
     * @return Get object
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    fun getEntityBlocking(entityID: String): GetEntity? {
        val getEntity = GetEntity(entityID, currentClass)
        client?.initializeRequest(getEntity)
        return getEntity
    }

    /**
     * Method to get an entity or entities.  Pass null to entityID to return all entities
     * in a collection.
     *
     * @param entityID entityID to get
     * @param resolves list of KinveyReference fields to resolve
     * @param resolve_depth the depth of KinveyReferences fields to resolve
     * @param retain should resolved KinveyReferences be retained
     * @return Get object
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    fun getEntityBlocking(entityID: String?, resolves: Array<String>?, resolve_depth: Int, retain: Boolean): GetEntity? {
        val getEntity = GetEntity(entityID, currentClass, resolves, resolve_depth, retain)
        client?.initializeRequest(getEntity)
        return getEntity
    }

    /**
     * Method to get a query of entities.  Pass an empty query to return all entities
     * in a collection.
     *
     * @param query Query to get
     * @return Get object
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    fun getBlocking(query: Query?): Get? {
        Preconditions.checkNotNull(query)
        val get = Get(query, currentClass)
        client?.initializeRequest(get)
        return get
    }

    /**
     * Method to resolve a raw query string
     *
     * @param queryString Query to get
     * @return Get object
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    fun getBlocking(queryString: String?): Get? {
        Preconditions.checkNotNull(queryString)
        val get = Get(queryString, currentClass)
        client?.initializeRequest(get)
        return get
    }

    /**
     * Method to get a query of entities.  Pass an array of entity _ids to return the entites.
     *
     * @param ids array of _ids to query for
     * @return Get object
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    fun getBlocking(ids: Array<String>?): Get? {
        Preconditions.checkNotNull(ids, "ids cannot be null.")
        val q = Query()
        q.`in`("_id", ids)
        return this.getBlocking(q)
    }

    /**
     * Method to execute a query and resolve KinveyReferences in the entities
     *
     * @param query Query to get
     * @param resolves list of KinveyReference fields to resolve
     * @param resolve_depth the depth of KinveyReferences fields to resolve
     * @param retain should resolved KinveyReferences be retained
     * @return Get object
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    fun getBlocking(query: Query?, resolves: Array<String>?, resolve_depth: Int, retain: Boolean): Get? {
        val getEntity = Get(query, currentClass, resolves, resolve_depth, retain)
        client?.initializeRequest(getEntity)
        return getEntity
    }

    /**
     * Convenience wrapper method to execute a query and resolve KinveyReferences in the entities
     *
     *
     * @param query - Query to get
     * @param resolves list of KinveyReference fields to resolve
     * @return Get Request object ready for execution
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getBlocking(query: Query?, resolves: Array<String>?): Get? {
        return getBlocking(query, resolves, 1, true)
    }

    /**
     * Convenience wrapper method to get an entity and resolve KinveyReferences
     *
     * @param id the id of the entity to query for
     * @param resolves list of KinveyReference fields to resolve
     * @return Get Request object ready for execution
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getEntityBlocking(id: String?, resolves: Array<String>?): GetEntity? {
        return getEntityBlocking(id, resolves, 1, true)
    }

    /**
     * Method to get all entities in a collection.
     *
     * @return Get Object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getBlocking(): Get? = getBlocking(client?.query())

    @Deprecated("use {@link #getBlocking(Query)} instead.")
    @Throws(IOException::class)
    fun pullBlocking(query: Query?, cachedItems: List<T>?, deltaSetCachingEnabled: Boolean): Get? {
        Preconditions.checkNotNull(query)
        val pull = if (deltaSetCachingEnabled) {
            DeltaGet(query, currentClass, cachedItems)
        } else {
            Get(query, currentClass)
        }
        client?.initializeRequest(pull)
        return pull
    }

    /**
     * Method to create a Pull request
     *
     * @param query Query to get
     * @param cache Cache
     * @param deltaSetCachingEnabled Flag to show if Delta Set Caching is enable
     * @return Pull request
     * @throws IOException
     *
     */
    @Deprecated("use {@link #getBlocking(Query)} instead.")
    @Throws(IOException::class)
    fun pullBlocking(query: Query?, cache: ICache<T>?, deltaSetCachingEnabled: Boolean): Get? {
        Preconditions.checkNotNull(query)
        val pull = if (deltaSetCachingEnabled) {
            if (cache != null) {
                DeltaGet(query, currentClass, query?.run { cache[this] })
            } else null
        } else {
            Get(query, currentClass)
        }
        client?.initializeRequest(pull as AbstractKinveyClientRequest<*>)
        return pull
    }

    /**
     * Method to create a Pull request
     *
     * @param query Query to get
     * @return Pull request
     * @throws IOException
     */
    @Deprecated("use {@link #getBlocking(Query)} instead.")
    @Throws(IOException::class)
    fun pullBlocking(query: Query): Get? {
        Preconditions.checkNotNull(query)
        val pull = Get(query, currentClass)
        client?.initializeRequest(pull)
        return pull
    }

    /**
     * Method to get a query of entities that are changed on server side agains given ones.  Pass
     * an empty query and empty items to compare with to return all entities in a collection.
     *
     * @param query Query to get
     * @return Get object
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    fun getBlocking(query: Query, cachedItems: List<T>?, deltaSetCachingEnabled: Boolean): Get? {
        Preconditions.checkNotNull(query)
        val get = if (deltaSetCachingEnabled) {
            DeltaGet(query, currentClass, cachedItems)
        } else {
            Get(query, currentClass)
        }
        client?.initializeRequest(get)
        return get
    }

    /**
     * Get blocking with Delta Sync implementation
     * @param query Query to get
     * @param lastRequestTime Last request time
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    open fun queryCacheGetBlocking(query: Query?, lastRequestTime: String?): QueryCacheGet? {
        Preconditions.checkNotNull(query)
        val queryCacheGet = QueryCacheGet(query, currentClass, lastRequestTime)
        client?.initializeRequest(queryCacheGet)
        return queryCacheGet
    }

    /**
     * Get items count in the collection
     * @return GetCount request
     * @throws IOException
     */
    @get:Throws(IOException::class)
    val countBlocking: GetCount?
        get() {
            val getCount = GetCount()
            client?.initializeRequest(getCount)
            return getCount
        }

    /**
     * Get count of queried items in the collection
     * @return GetCount request
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getCountBlocking(query: Query?): GetCount? {
        val getCount = GetCount(query)
        client?.initializeRequest(getCount)
        return getCount
    }

    /**
     * Save (create or update) an entity to a collection.
     *
     * @param entity Entity to Save
     * @return Save object
     * @throws IOException
     */
    @Throws(IOException::class)
    open fun saveBlocking(entity: T?): Save? {
        INFO("Start saveBlocking for object")
        val save: Save
        val sourceID: String?
        val jsonEntity = entity as GenericJson?
        sourceID = jsonEntity?.run { this[ID_FIELD_NAME] as String? }
//prepare entity relation data saving
        entityRelationDataSavingCheck(entity)
        val bRealmGeneratedId = isTempId(entity)
        INFO("Start choosing PUT or POST request")
        if (sourceID != null && !bRealmGeneratedId) {
            INFO("Start for preparing new Save(entity, myClass, sourceID, SaveMode.PUT)")
            save = Save(entity, currentClass, sourceID, SaveMode.PUT)
            INFO("Finish for preparing new Save(entity, myClass, sourceID, SaveMode.PUT)")
        } else {
            INFO("Start for preparing new Save(entity, myClass, sourceID, SaveMode.POST)")
            save = Save(entity, currentClass, SaveMode.POST)
            INFO("Finish for preparing new Save(entity, myClass, SaveMode.POST)")
        }
        client?.initializeRequest(save)
        INFO("Finish for initializing request with save object")
        INFO("Return save object")
        return save
    }

    private fun entityRelationDataSavingCheck(entity: T?) {
        try {
            INFO("Start prepare entity relation data saving")
            ReferenceHelper.processReferences(entity, object : ReferenceListener {
                override fun onUnsavedReferenceFound(collection: String, item: GenericJson?): String {
                    INFO("Calling onUnsavedReferenceFound(String, GenericJson)")
                    if (item?.containsKey("_id") == true) {
                        INFO("return object.get(_id).toString() for onUnsavedReferenceFound(String, GenericJson)")
                        return item["_id"].toString()
                    }
                    val manager = NetworkManager(collection, GenericJson::class.java, client)
                    try {
                        INFO("Start recursive call for manager.saveBlocking(object).execute() inside saveBlocking")
                        val saved = manager.saveBlocking(item)?.execute()
                        INFO("return saved.get(ID_FIELD_NAME).toString() for onUnsavedReferenceFound(String, GenericJson)")
                        return saved?.run { this[ID_FIELD_NAME]?.toString() } ?: ""
                    } catch (e: IOException) {
                        ERROR("Catch exception for recursive call for manager.saveBlocking(object).execute()")
                        e.printStackTrace()
                    }
                    INFO("return null for onUnsavedReferenceFound(String, GenericJson)")
                    return ""
                }
            })
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    open fun saveBatchBlocking(list: List<T>?): SaveBatch? {
        val responseClassType = KinveySaveBatchResponse::class.java
        val batch = SaveBatch(list, responseClassType as Class<KinveySaveBatchResponse<T>>, currentClass, SaveMode.POST)
        client?.initializeRequest(batch)
        return batch
    }

    fun isTempId(item: T?): Boolean {
        INFO("Start checking for isTempId(entity)")
        var isTempId = false
        val itemId = item?.run { this[Constants._ID] }
        if (itemId != null) {
            try {
                isTempId = itemId.toString().startsWith("temp_")
            } catch (npe: NullPointerException) {
                // issue with the regex, so do nothing because we default to false
            }
        }
        INFO("Finish checking for isTempId(entity)")
        return isTempId
    }

    /**
     * Delete an entity from a collection by ID.
     *
     * @param entityID entityID to delete
     * @return Delete object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deleteBlocking(entityID: String?): Delete? {
        Preconditions.checkNotNull(entityID)
        val delete = Delete(entityID)
        client?.initializeRequest(delete)
        return delete
    }

    /**
     * Delete an entity from a collection by Query.
     *
     * @param query query for entities to delete
     * @return Delete object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deleteBlocking(query: Query?): Delete? {
        Preconditions.checkNotNull(query)
        val delete = Delete(query)
        client?.initializeRequest(delete)
        return delete
    }

    /**
     * Retrieve a group by COUNT on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param query  optional query to filter by (null for all records in a collection)
     * @return Aggregate object
     * @throws IOException
     */
    @Throws(IOException::class)
    fun countBlocking(fields: List<String>?, myClass: Class<Array<T>>?, query: Query?): Aggregate? {
        Preconditions.checkNotNull(fields)
        return aggregate(fields, AggregateType.COUNT, null, myClass, query)
    }

    /**
     * Retrieve a group by SUM on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param sumField field to sum
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun sumBlocking(fields: List<String>?, sumField: String?, myClass: Class<Array<T>>?, query: Query?): Aggregate? {
        Preconditions.checkNotNull(fields)
        Preconditions.checkNotNull(sumField)
        return aggregate(fields, AggregateType.SUM, sumField, myClass, query)
    }

    /**
     * Retrieve a group by MAX on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param maxField field to obtain max value from
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun maxBlocking(fields: List<String>?, maxField: String?, myClass: Class<Array<T>>?, query: Query?): Aggregate? {
        Preconditions.checkNotNull(fields)
        Preconditions.checkNotNull(maxField)
        return aggregate(fields, AggregateType.MAX, maxField, myClass, query)
    }

    /**
     * Retrieve a group by MIN on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param minField field to obtain MIN value from
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun minBlocking(fields: List<String>?, minField: String?, myClass: Class<Array<T>>?, query: Query?): Aggregate? {
        Preconditions.checkNotNull(fields)
        Preconditions.checkNotNull(minField)
        return aggregate(fields, AggregateType.MIN, minField, myClass, query)
    }

    /**
     * Retrieve a group by AVERAGE on a collection or filtered collection
     *
     * @param fields fields to group by
     * @param averageField field to average
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun averageBlocking(fields: List<String>?, averageField: String?, myClass: Class<Array<T>>?, query: Query?): Aggregate? {
        Preconditions.checkNotNull(fields)
        Preconditions.checkNotNull(averageField)
        return aggregate(fields, AggregateType.AVERAGE, averageField, myClass, query)
    }

    /**
     * public helper method to create AggregateEntity and return an initialize Aggregate Request Object
     * @param fields fields to group by
     * @param type Type of aggregation
     * @param aggregateField Field to aggregate on
     * @param query optional query to filter by (null for all records in a collection)
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun aggregate(fields: List<String>?, type: AggregateType?, aggregateField: String?,
                  myClass: Class<Array<T>>?, query: Query?): Aggregate? {
        val entity = AggregateEntity(fields, type, aggregateField, query, client)
        val aggregate = Aggregate(entity, myClass)
        client?.initializeRequest(aggregate)
        return aggregate
    }

    /**
     *
     * @param deviceId Device id
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun subscribe(deviceId: String?): Subscribe? {
        Preconditions.checkNotNull(deviceId)
        val deviceID = DeviceId()
        deviceID.deviceId = deviceId
        val subscribe = Subscribe(deviceID)
        client?.initializeRequest(subscribe)
        return subscribe
    }

    private inner class MetadataGet internal constructor(getRequest: DeltaGet?)
        : AbstractKinveyJsonClientRequest<Array<DeltaSetItem>>(client, HttpVerb.GET.verb, METADATA_GET_REST_PATH, null, Array<DeltaSetItem>::class.java) {
        @Key
        private var collectionName: String? = this@NetworkManager.collectionName
        @Key("query")
        private val queryFilter: String?
        @Key("sort")
        private val sortFilter: String?
        @Key("limit")
        protected var limit: String?
        @Key("skip")
        protected var skip: String?
        @Key
        private val fields: String? = "_id,_kmd"
        @Key
        private val tls = true

        @Throws(IOException::class)
        override fun execute(): Array<DeltaSetItem>? {
            return super.execute()
        }

        init {
            queryFilter = getRequest?.queryFilter
            skip = getRequest?.skip
            limit = getRequest?.limit
            collectionName = getRequest?.collectionName
            sortFilter = getRequest?.sortFilter
            //prevent caching and offline store for that request
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = client?.clientAppVersion
            if (!client?.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(client?.customRequestProperties)
            }
        }
    }

    /**
     * Generic DeltaGet class.  Constructs the HTTP request object for Get
     * requests with Delta set cache functionality.
     *
     */
    inner class DeltaGet constructor(private val query: Query?, var myClass: Class<T>?, private val currentItems: List<T>?) : Get(query, myClass) {

        @Throws(IOException::class)
        override fun execute(): KinveyReadResponse<T>? {
            var ret: KinveyReadResponse<T>? = null
            if (currentItems != null && currentItems.isNotEmpty()) {
                ret = KinveyReadResponse()
                val deltaRequest = MetadataGet(DeltaGet(query, myClass, currentItems))
                client?.initializeRequest(deltaRequest)
                val itemsArray = deltaRequest.execute()
                //init empty array in case if there is no ids to update
                var updatedOnline: List<T>? = ArrayList()
                val items = itemsArray?.asList()
                val ids: List<String>? = DeltaSetMerge.getIdsForUpdate(currentItems, items)
                if (!ids.isNullOrEmpty()) {
                    updatedOnline = fetchIdsWithPaging(ids)
                }
                ret.result = listOf(*DeltaSetMerge.merge(items, currentItems, updatedOnline))
            }
            if (ret == null) {
                ret = super.execute()
            }
            return ret
        }

        @Throws(IOException::class)
        private fun fetchIdsWithPaging(ids: List<String>?): List<T>? {
            var ids = ids
            val ret: MutableList<T> = mutableListOf()
            while (!ids.isNullOrEmpty()) {
                val chunkSize = if (ids.size < IDS_PER_PAGE) ids.size else IDS_PER_PAGE
                val chunkItems = ids.subList(0, chunkSize)
                ids = ids.subList(chunkSize, ids.size)
                val arrayItems = chunkItems.toTypedArray()
                val query = query()?.`in`("_id", arrayItems)
                val pageGet = Get(query,
                        myClass,
                        if (resolve != null) resolve?.split(",")?.toTypedArray() else arrayOf(),
                        if (resolveDepth != null) Integer.parseInt(resolveDepth) else 0,
                        retainReferences?.toBoolean() == true)
                client?.initializeRequest(pageGet)
                val pageGetResult: KinveyReadResponse<T>? = pageGet.execute()
                pageGetResult?.result?.run { ret.addAll(this) }
            }
            return ret
        }
    }

    /**
     * Generic Get class.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    open inner class Get: AbstractKinveyReadRequest<T> {
        @Key
        var collectionName: String?
        @Key("query")
        var queryFilter: String?
        @Key("sort")
        var sortFilter: String? = null
        @Key("limit")
        var limit: String? = null
        @Key("skip")
        var skip: String? = null
        @Key("resolve")
        protected var resolve: String? = null
        @Key("resolve_depth")
        protected var resolveDepth: String? = null
        @Key("retainReferences")
        protected var retainReferences: String? = null

        constructor(query: Query?, myClass: Class<T>?) : super(client, HttpVerb.GET.verb, GET_REST_PATH, null, myClass) {
            this.collectionName = this@NetworkManager.collectionName
            queryFilter = query?.getQueryFilterJson(client?.jsonFactory)
            val queryLimit = query?.limit ?: 0
            val querySkip = query?.skip ?: 0
            limit = if (queryLimit > 0) queryLimit.toString() else null
            skip = if (querySkip > 0) querySkip.toString() else null
            val sortString = query?.sortString
            sortFilter = if (sortString != "") sortString else null
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }

        constructor(query: Query?, myClass: Class<T>?, resolves: Array<String>?, resolveDepth: Int, retain: Boolean)
            : super(client, HttpVerb.GET.verb, GET_REST_PATH, null, myClass) {
            this.collectionName = this@NetworkManager.collectionName
            queryFilter = query?.getQueryFilterJson(client?.jsonFactory)
            val queryLimit = query?.limit ?: 0
            val querySkip = query?.skip ?: 0
            limit = if (queryLimit > 0) queryLimit.toString() else null
            skip = if (querySkip > 0) querySkip.toString() else null
            val sortString = query?.sortString ?: ""
            sortFilter = if (sortString.isNotEmpty()) sortString else null
            this.resolve = Joiner.on(",").join(resolves)
            this.resolveDepth = if (resolveDepth > 0) resolveDepth.toString() else null
            retainReferences = retain.toString()
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = client?.clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }

        constructor(queryString: String?, myClass: Class<T>?) : super(client, HttpVerb.GET.verb, GET_REST_PATH, null, myClass) {
            this.collectionName = this@NetworkManager.collectionName
            queryFilter = if (queryString != "{}") queryString else null
            setTemplateExpand(false)
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }

        @Throws(IOException::class)
        override fun execute(): KinveyReadResponse<T>? {
            return super.execute()
        }
    }

    open inner class QueryCacheGet internal constructor(query: Query?, myClass: Class<T>?, @Key("since") protected var since: String?)
        : AbstractKinveyQueryCacheReadRequest<T>(client, HttpVerb.GET.verb, QUERY_CACHE_GET_REST_PATH, null, myClass) {
        @Key
        protected var collectionName: String?
        @Key("query")
        protected var queryFilter: String?
        @Key("sort")
        protected var sortFilter: String?
        @Key("limit")
        protected var limit: String?
        @Key("skip")
        protected var skip: String?
        @Key("resolve")
        protected var resolve: String? = null
        @Key("resolve_depth")
        protected var resolveDepth: String? = null

        @Throws(IOException::class)
        override fun execute(): KinveyQueryCacheResponse<T>? {
            return super.execute()
        }

        init {
            this.collectionName = this@NetworkManager.collectionName
            queryFilter = query?.getQueryFilterJson(client?.jsonFactory)
            val queryLimit = query?.limit ?: 0
            val querySkip = query?.skip ?: 0
            limit = if (queryLimit > 0) queryLimit.toString() else null
            skip = if (querySkip > 0) querySkip.toString() else null
            val sortString = query?.sortString ?: ""
            sortFilter = if (sortString.isNotEmpty()) sortString else null
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }
    }

    /**
     * Generic Get class.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    inner class GetEntity : AbstractKinveyJsonClientRequest<T> {
        @Key
        private var entityID: String?
        @Key
        private var collectionName: String?
        @Key("resolve")
        private var resolve: String? = null
        @Key("resolve_depth")
        private var resolveDepth: String? = null
        @Key("retainReferences")
        private var retainReferences: String? = null

        internal constructor(entityID: String?, myClass: Class<T>?) : super(client, HttpVerb.GET.verb, GET_ENTITY_REST_PATH, null, myClass) {
            this.collectionName = this@NetworkManager.collectionName
            this.entityID = entityID
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }

        internal constructor(entityID: String?, myClass: Class<T>?, resolves: Array<String>?, resolveDepth: Int, retain: Boolean)
                : super(client, HttpVerb.GET.verb, GET_ENTITY_REST_PATH, null, myClass) {
            this.collectionName = this@NetworkManager.collectionName
            this.entityID = entityID
            this.resolve = Joiner.on(",").join(resolves)
            this.resolveDepth = if (resolveDepth > 0) resolveDepth.toString() else null
            retainReferences = retain.toString()
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }

        @Throws(IOException::class)
        override fun execute(): T? {
            return super.execute()
        }
    }

    /**
     * Generic Get class.  Constructs the HTTP request object for Get
     * requests.
     *
     */
    inner class GetCount : AbstractKinveyReadHeaderRequest<KinveyCountResponse> {
        @Key
        private var collectionName: String?
        @Key("query")
        private var queryFilter: String? = null
        @Key("sort")
        private var sortFilter: String? = null
        @Key("limit")
        private var limit: String? = null
        @Key("skip")
        private var skip: String? = null

        internal constructor() : super(client, HttpVerb.GET.verb, GET_COUNT_REST_PATH, null, KinveyCountResponse::class.java) {
            this.collectionName = this@NetworkManager.collectionName
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }

        internal constructor(query: Query?) : super(client, HttpVerb.GET.verb, GET_COUNT_REST_PATH, null, KinveyCountResponse::class.java) {
            this.collectionName = this@NetworkManager.collectionName
            queryFilter = query?.getQueryFilterJson(client?.jsonFactory)
            val queryLimit = query?.limit ?: 0
            val querySkip = query?.skip ?: 0
            limit = if (queryLimit > 0) queryLimit.toString() else null
            skip = if (querySkip > 0) querySkip.toString() else null
            val sortString = query?.sortString
            sortFilter = if (sortString != "") sortString else null
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }
    }

    /**
     * Generic Save<T> class.  Constructs the HTTP request object for
     * Create / Update requests.
     *
    </T> */
    inner class Save internal constructor(entity: T?, myClass: Class<T>?, entityID: String?, update: SaveMode?)
        : AbstractKinveyJsonClientRequest<T>(client, update.toString(), SAVE_REST_PATH, entity, myClass) {
        @Key
        private val collectionName: String? = this@NetworkManager.collectionName
        @Key
        private var entityID: String? = null

        internal constructor(entity: T?, myClass: Class<T>?, update: SaveMode?) : this(entity, myClass, null, update) {}

        init {
            if (update == SaveMode.PUT) {
                this.entityID = entityID
            }
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }
    }

    /**
     * SaveBatch class. Constructs the HTTP request object to
     * create multi-insert requests.
     *
     */
    inner class SaveBatch internal constructor(itemsList: List<T>?, responseClassType: Class<KinveySaveBatchResponse<T>>, parClassType: Class<T>?, update: SaveMode?)
        : KinveyJsonStringClientRequest<KinveySaveBatchResponse<T>>(client, update.toString(), SAVE_BATCH_REST_PATH, BatchList<Any?>(itemsList).toString(), responseClassType, parClassType) {
        @Key
        private var collectionName: String? = this@NetworkManager.collectionName

        init {
            val customRequestProperties = this@NetworkManager.customRequestProperties
            val clientAppVersion = clientAppVersion
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(customRequestProperties)
            }
        }
    }

    /**
     * Generic Delete class.  Constructs the HTTP request object
     * for Delete requests.
     *
     */
    inner class Delete : AbstractKinveyJsonClientRequest<KinveyDeleteResponse> {
        @Key
        private var entityID: String? = null
        @Key
        private var collectionName: String?
        @Key("query")
        private var queryFilter: String? = null
        @Key("sort")
        private var sortFilter: String? = null
        @Key("limit")
        private var limit: String? = null
        @Key("skip")
        private var skip: String? = null

        internal constructor(entityID: String?) : super(client, HttpVerb.DELETE.verb, DELETE_REST_PATH, null, KinveyDeleteResponse::class.java) {
            this.entityID = entityID
            this.collectionName = this@NetworkManager.collectionName
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }

        internal constructor(query: Query?) : super(client, HttpVerb.DELETE.verb, DELETE_REST_PATH, null, KinveyDeleteResponse::class.java) {
            this.collectionName = this@NetworkManager.collectionName
            queryFilter = query?.getQueryFilterJson(client?.jsonFactory)
            val queryLimit = query?.limit ?: 0
            val querySkip = query?.skip ?: 0
            limit = if (queryLimit > 0) queryLimit.toString() else null
            skip = if (querySkip > 0) querySkip.toString() else null
            val sortString = query?.sortString
            sortFilter = if (!sortString.isNullOrEmpty()) sortString else null
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }
    }

    /**
     * Generic Aggregate<T> class, constructs the HTTP request object for
     * Aggregate requests.
     *
     */
    inner class Aggregate internal constructor(entity: AggregateEntity?, myClass: Class<Array<T>>?)
        : AbstractKinveyJsonClientRequest<Array<T>>(client, HttpVerb.POST.verb, AGGREGATE_REST_PATH, entity, myClass) {
        @Key
        private var collectionName: String? = this@NetworkManager.collectionName

        init {
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }
    }

    /**
     * Generic Aggregate<T> class, constructs the HTTP request object for
     * Aggregate requests.
     *
     */
    inner class Subscribe internal constructor(deviceId: DeviceId?)
        : AbstractKinveyJsonClientRequest<GenericJson>(client, HttpVerb.POST.verb, SUBSCRIBE_REST_PATH, deviceId, GenericJson::class.java) {
        @Key
        private var collectionName: String? = this@NetworkManager.collectionName

        init {
            getRequestHeaders()["X-Kinvey-Client-App-Version"] = clientAppVersion
            if (!this@NetworkManager.customRequestProperties.isNullOrEmpty()) {
                getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(this@NetworkManager.customRequestProperties)
            }
        }
    }

    open val isOnline: Boolean
        get() = true

    companion object {
        /**
         * static final String representing universal "_id" value, used to uniquely identify entites
         */
        val ID_FIELD_NAME: String? = "_id"
        /**
         * static final String representing universal "_geoloc" value, used for geoqueries
         */
        val GEOLOC_FIELD_NAME: String? = "_geoloc"

        fun checkNetworkRuntimeExceptions(e: Exception?): Boolean =
                e !is UnknownHostException && e !is SocketTimeoutException

        const val IDS_PER_PAGE = 100

        const val SAVE_REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}"

        const val SAVE_BATCH_REST_PATH = "appdata/{appKey}/{collectionName}"

        const val GET_REST_PATH = "appdata/{appKey}/{collectionName}" +
                "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}"

        const val GET_COUNT_REST_PATH = "appdata/{appKey}/{collectionName}/_count" +
                    "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}"

        const val GET_ENTITY_REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}" +
               "{resolve,resolve_depth,retainReference}"

        const val QUERY_CACHE_GET_REST_PATH = "appdata/{appKey}/{collectionName}" +
                    "/_deltaset{?since,query,sort,limit,skip,resolve,resolve_depth,retainReference}"

        const val METADATA_GET_REST_PATH = "appdata/{appKey}/{collectionName}" +
                "{?query,fields,tls,sort,limit,skip,resolve,resolve_depth,retainReference}"

        const val DELETE_REST_PATH = "appdata/{appKey}/{collectionName}/{entityID}" +
                    "{?query,sort,limit,skip,resolve,resolve_depth,retainReference}"

        const val AGGREGATE_REST_PATH = "appdata/{appKey}/{collectionName}/_group"

        const val  SUBSCRIBE_REST_PATH = "appdata/{appKey}/{collectionName}/_subscribe"
    }

    /**
     * Constructor to instantiate the NetworkManager class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass Class Type to marshall data between.
     */

    init {
        Preconditions.checkNotNull(collectionName, "collectionName must not be null.")
        Preconditions.checkNotNull(client, "client must not be null.")
        clientAppVersion = client?.clientAppVersion
        customRequestProperties = client?.customRequestProperties
    }
}
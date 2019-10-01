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

package com.kinvey.java.store

import com.google.api.client.json.GenericJson
import com.google.common.base.Preconditions
import com.google.common.collect.Iterables
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.cache.KinveyCachedClientCallback
import com.kinvey.java.core.KinveyCachedAggregateCallback
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.model.AggregateType
import com.kinvey.java.model.Aggregation
import com.kinvey.java.model.KinveyCountResponse
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.model.KinveyPullResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.query.AbstractQuery
import com.kinvey.java.store.requests.data.AggregationRequest
import com.kinvey.java.store.requests.data.PushRequest
import com.kinvey.java.store.requests.data.delete.DeleteIdsRequest
import com.kinvey.java.store.requests.data.delete.DeleteQueryRequest
import com.kinvey.java.store.requests.data.delete.DeleteSingleRequest
import com.kinvey.java.store.requests.data.read.ReadAllRequest
import com.kinvey.java.store.requests.data.read.ReadCountRequest
import com.kinvey.java.store.requests.data.read.ReadIdsRequest
import com.kinvey.java.store.requests.data.read.ReadQueryRequest
import com.kinvey.java.store.requests.data.read.ReadSingleRequest
import com.kinvey.java.store.requests.data.save.SaveListBatchRequest
import com.kinvey.java.store.requests.data.save.SaveListRequest
import com.kinvey.java.store.requests.data.save.SaveRequest

import java.io.IOException
import java.security.AccessControlException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

open class BaseDataStore<T : GenericJson> @JvmOverloads protected constructor(
        /**
         * Getter for client
         * @return Client instance for given BaseDataStore
         */
        val client: AbstractClient<*>,
        val collectionName: String,
        val currentClass: Class<T>,
        var storeType: StoreType,
        protected var networkManager: NetworkManager<T> = NetworkManager(collectionName, currentClass, client)
) {
    private var cache: ICache<T>? = null
    private var queryCache: ICache<QueryCacheItem>? = null

    /**
     * It is a parameter to enable mechanism to optimize the amount of data retrieved from the backend.
     * When you use a Sync or Cache datastore, data requests to the backend
     * only fetch data that changed since the previous update.
     * Default value is false.
     */
    /**
     * Getter to check if delta set cache is enabled
     * @return delta set get flag
     */
    /**
     * Setter for delta set get cache flag
     * @param deltaSetCachingEnabled boolean representing if we should use delta set caching
     */
    var isDeltaSetCachingEnabled = false

    internal var liveServiceCallback: KinveyDataStoreLiveServiceCallback<T>? = null

    init {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        if (storeType != StoreType.NETWORK) {
            cache = client.cacheManager?.getCache(collectionName, currentClass, storeType.ttl)
        }
        this.isDeltaSetCachingEnabled = client.isUseDeltaCache
    }

    /**
     * Look up for data with given id
     * @param id the id of object we need to find
     * @param cachedCallback callback to be executed in case of [StoreType.CACHE] is used to get cached data before network
     * @return null or object that matched given id
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun find(id: String, cachedCallback: KinveyCachedClientCallback<T>? = null): T? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkNotNull(id, "id must not be null.")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        var ret: T?
        if (storeType == StoreType.CACHE && cachedCallback != null) {
            ret = ReadSingleRequest(cache, id, ReadPolicy.FORCE_LOCAL, networkManager).execute()
            cachedCallback.onSuccess(ret)
        }
        ret = ReadSingleRequest(cache, id, this.storeType.readPolicy, networkManager).execute()
        return ret
    }

    /**
     * Look up for object that have id in given collection of ids
     * @param ids collection of strings that identify a set of ids we have to look for
     * @param cachedCallback callback to be executed in case of [StoreType.CACHE] is used to get cached data before network
     * @return List of object found for given ids
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun find(ids: Iterable<String>, cachedCallback: KinveyCachedClientCallback<KinveyReadResponse<T>>? = null): KinveyReadResponse<T>? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkNotNull(ids, "ids must not be null.")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        if (storeType == StoreType.CACHE) {
            cachedCallback?.onSuccess(ReadIdsRequest(cache, networkManager, ReadPolicy.FORCE_LOCAL, ids).execute())
            return if (isDeltaSetCachingEnabled) {
                val query = client.query().`in`("_id", Iterables.toArray(ids, String::class.java) as Array<Any>)
                findBlockingDeltaSync(query)
            } else {
                ReadIdsRequest(cache, networkManager, this.storeType.readPolicy, ids).execute()
            }
        } else {
            return if (storeType == StoreType.AUTO && isDeltaSetCachingEnabled) {
                val query = client.query().`in`("_id", Iterables.toArray(ids, String::class.java) as Array<Any>)
                findBlockingDeltaSync(query)
            } else {
                ReadIdsRequest(cache, networkManager, this.storeType.readPolicy, ids).execute()
            }
        }
    }


    /**
     * Lookup objects in given collection by given query
     * @param query prepared query we have to look with
     * @param cachedCallback callback to be executed in case of [StoreType.CACHE] is used to get cached data before network
     * @return list of objects that are found
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun find(query: Query, cachedCallback: KinveyCachedClientCallback<KinveyReadResponse<T>>? = null): KinveyReadResponse<T>? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkNotNull(query, "query must not be null.")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        // perform request based on policy
        if (storeType == StoreType.CACHE) {
            cachedCallback?.onSuccess(ReadQueryRequest(cache, networkManager, ReadPolicy.FORCE_LOCAL, query).execute())
            return if (isDeltaSetCachingEnabled && !isQueryContainSkipLimit(query)) {
                findBlockingDeltaSync(query)
            } else {
                ReadQueryRequest(cache, networkManager, this.storeType.readPolicy, query).execute()
            }
        } else {
            return if (storeType == StoreType.AUTO && isDeltaSetCachingEnabled && !isQueryContainSkipLimit(query)) {
                findBlockingDeltaSync(query)
            } else {
                ReadQueryRequest(cache, networkManager, this.storeType.readPolicy, query).execute()
            }
        }
    }

    /**
     * Get all objects for given collections
     * @param cachedCallback callback to be executed in case of [StoreType.CACHE] is used to get cached data before network
     * @return all objects in given collection
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun find(cachedCallback: KinveyCachedClientCallback<KinveyReadResponse<T>>? = null): KinveyReadResponse<T>? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        // perform request based on policy
        if (storeType == StoreType.CACHE) {
            cachedCallback?.onSuccess(ReadAllRequest(cache, ReadPolicy.FORCE_LOCAL, networkManager).execute())
            return if (isDeltaSetCachingEnabled) {
                findBlockingDeltaSync(client.query())
            } else {
                ReadAllRequest(cache, this.storeType.readPolicy, networkManager).execute()
            }
        } else {
            return if (storeType == StoreType.AUTO && isDeltaSetCachingEnabled) {
                findBlockingDeltaSync(client.query())
            } else {
                ReadAllRequest(cache, this.storeType.readPolicy, networkManager).execute()
            }
        }
    }

    /**
     * Get items count in collection
     * @return items count in collection
     */
    @Throws(IOException::class)
    fun count(): Int? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        return count(null)
    }

    /**
     * Get items count in collection
     * @param cachedCallback is using with StoreType.CACHE to get items count in collection
     * @return items count in collection
     */
    @Throws(IOException::class)
    fun count(cachedCallback: KinveyCachedClientCallback<Int>?): Int? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        if (storeType == StoreType.CACHE && cachedCallback != null) {
            val ret = ReadCountRequest(cache, networkManager, ReadPolicy.FORCE_LOCAL, null, client.syncManager).execute()?.count
            cachedCallback.onSuccess(ret)
        }
        return ReadCountRequest(cache, networkManager, this.storeType.readPolicy, null, client.syncManager).execute()?.count ?: 0
    }

    /**
     * Get count of queried items in collection
     * @param cachedCallback is using with StoreType.CACHE to get items count in collection
     * @return count of queried items in collection
     */
    @Throws(IOException::class)
    fun count(cachedCallback: KinveyCachedClientCallback<Int>?, query: Query): Int? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        if (storeType == StoreType.CACHE && cachedCallback != null) {
            val ret = ReadCountRequest(cache, networkManager, ReadPolicy.FORCE_LOCAL, query, client.syncManager).execute()?.count
            cachedCallback.onSuccess(ret)
        }
        return ReadCountRequest(cache, networkManager, this.storeType.readPolicy, query, client.syncManager).execute()?.count ?: 0
    }

    /**
     * Get items count in collection on the server
     * @return items count in collection on the server
     */
    @Throws(IOException::class)
    fun countNetwork(): Int? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        return ReadCountRequest(cache, networkManager, ReadPolicy.FORCE_NETWORK, null, client.syncManager).execute()?.count ?: 0
    }

    /**
     * Get items count in collection on the server with last request time info from the response headers.
     * Is used for Delta Set auto-pagination requests.
     * @return items count in collection on the server
     */
    @Throws(IOException::class)
    private fun internalCountNetwork(): KinveyCountResponse? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        return ReadCountRequest(cache, networkManager, ReadPolicy.FORCE_NETWORK, null, client.syncManager).execute()
    }

    /**
     * Save multiple objects for collections
     * @param objects list of objects to be saved
     * @return updated list of object that will contain ids if they was not present in moment of saving
     * @throws IOException
     */
    @Throws(IOException::class)
    fun save(objects: Iterable<T>): List<T>? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkNotNull(objects, "objects must not be null.")
        Logger.INFO("Calling BaseDataStore#save(listObjects)")
        return SaveListRequest(cache, networkManager, this.storeType.writePolicy, objects, client.syncManager).execute()
    }

    /**
     * Save multiple objects for collections
     * @param objects list of objects to be saved
     * @return updated list of object that will contain ids if they was not present in moment of saving
     * @throws IOException
     */
    @Throws(IOException::class)
    fun saveBatch(objects: Iterable<T>): List<T> {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkNotNull(objects, "objects must not be null.")
        Logger.INFO("Calling BaseDataStore#save(listObjects)")
        return SaveListBatchRequest(cache, networkManager, this.storeType.writePolicy, objects, client.syncManager).execute()
    }

    /**
     * Save single object into collection
     * @param object Object to be saved in given collection
     * @return updated object with filled some required fields
     * @throws IOException
     */
    @Throws(IOException::class)
    fun save(`object`: T): T? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkNotNull(`object`, "object must not be null.")
        Logger.INFO("Calling BaseDataStore#save(object)")
        return SaveRequest(cache, networkManager, this.storeType.writePolicy, `object`, client.syncManager).execute()
    }

    /**
     * Clear the local cache storage
     */
    fun clear() {
        Preconditions.checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType")
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        client.cacheManager?.clearCollection(collectionName, currentClass, java.lang.Long.MAX_VALUE)
        if (isDeltaSetCachingEnabled && queryCache != null) {
            queryCache?.clear()
        }
        purge()
    }

    /**
     * Clear the local cache storage
     */
    fun clear(query: Query) {
        Preconditions.checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType")
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        purge(query)
        client.cacheManager?.getCache(collectionName, currentClass, java.lang.Long.MAX_VALUE)?.delete(query)
    }

    /**
     * Remove object from from given collection with given id
     * @param id id of object to be deleted
     * @return count of object that was deleted
     * @throws IOException
     */
    @Throws(IOException::class)
    fun delete(id: String): Int? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkNotNull(id, "id must not be null.")
        return DeleteSingleRequest(cache, networkManager, this.storeType.writePolicy, id, client.syncManager).execute()
    }

    /**
     * Remove objects from given query that matches given query
     * @param query query to lookup objects for given collection
     * @return count of objects that was removed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun delete(query: Query): Int? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkNotNull(query, "query must not be null.")
        return DeleteQueryRequest(cache, networkManager, this.storeType.writePolicy, query, client.syncManager).execute() ?: 0
    }

    /**
     * Remove objects from given collections with list of ids
     * @param ids identifiers of objects to be deleted
     * @return count of objects that was deleted bu given call
     * @throws IOException
     */
    @Throws(IOException::class)
    fun delete(ids: Iterable<String>): Int? {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkNotNull(ids, "ids must not be null.")
        return DeleteIdsRequest(cache, networkManager, this.storeType.writePolicy, ids, client.syncManager).execute() ?: 0
    }

    /**
     * Push local changes to network
     * should be used with [StoreType.SYNC]
     */
    @Throws(IOException::class)
    fun pushBlocking() {
        Preconditions.checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType")
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        PushRequest(collectionName, cache, networkManager, client).execute()
    }

    /**
     * Pull network data with given query into local storage
     * should be used with [StoreType.SYNC]
     * @param query query to pull the objects
     */
    @Throws(IOException::class)
    fun pullBlocking(query: Query?): KinveyPullResponse {
        var query = query
        Preconditions.checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType")
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkArgument(client.syncManager.getCount(collectionName) == 0L, "InvalidOperation. You must push all pending sync items before new data is pulled. Call push() on the data store instance to push pending items, or purge() to remove them.")
        query = query ?: client.query()
        val response: KinveyPullResponse
        if (isDeltaSetCachingEnabled && !isQueryContainSkipLimit(query)) {
            response = pullBlockingDeltaSync(query)
        } else {
            response = KinveyPullResponse()
            val readResponse = networkManager.getBlocking(query)?.execute()
            cache?.delete(query)
            readResponse?.result?.let { list -> response.count = cache?.save(list)?.size ?: 0 }
            response.listOfExceptions = if (readResponse?.listOfExceptions != null) readResponse.listOfExceptions else ArrayList()
        }
        return response
    }

    /**
     * Pull network data with given query into local storage
     * should be used with [StoreType.SYNC]
     * @param isAutoPagination true if auto-pagination is used
     * @param query query to pull the objects
     */
    @Throws(IOException::class)
    fun pullBlocking(query: Query?, isAutoPagination: Boolean): KinveyPullResponse {
        Preconditions.checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType")
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkArgument(client.syncManager.getCount(collectionName) == 0L, "InvalidOperation. You must push all pending sync items before new data is pulled. Call push() on the data store instance to push pending items, or purge() to remove them.")
        return if (isAutoPagination) pullBlocking(query, DEFAULT_PAGE_SIZE) else pullBlocking(query)
    }

    /**
     * Pull network data with given query into local storage page by page
     * getting pages works concurrently
     * should be used with [StoreType.SYNC]
     * @param query query to pull the objects
     * @param pageSize page size for auto-pagination
     */
    @Throws(IOException::class)
    fun pullBlocking(query: Query?, pageSize: Int): KinveyPullResponse {
        var query = query
        Preconditions.checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType")
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkArgument(client.syncManager.getCount(collectionName) == 0L, "InvalidOperation. You must push all pending sync items before new data is pulled. Call push() on the data store instance to push pending items, or purge() to remove them.")
        query = query ?: client.query()
        var cacheItem: QueryCacheItem? = null
        if (isDeltaSetCachingEnabled && !isQueryContainSkipLimit(query)) {
            cacheItem = getQueryCacheItem(query)
        }
        return if (cacheItem != null) pullBlockingDeltaSync(cacheItem, query, pageSize) else pullBlockingPaged(query, pageSize)
    }

    /**
     * Delta Set isn't used in the method
     * @param query query to filter results
     * @param pageSize page size for auto-pagination
     * @return KinveyPullResponse object
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun pullBlockingPaged(query: Query, pageSize: Int): KinveyPullResponse {
        val response = KinveyPullResponse()
        val stringQuery = query.queryFilterMap.toString()
        if (query.sortString.isNullOrEmpty()) {
            query.addSort(Constants._ID, AbstractQuery.SortOrder.ASC)
        }
        val exceptions = ArrayList<Exception>()
        var skipCount = 0

        // First, get the count of all the items to pull
        val countResponse = internalCountNetwork()
        val totalItemNumber = countResponse?.count ?: 0
        val lastRequestTime = countResponse?.lastRequestTime
        var pulledItemCount = 0
        val totalPagesNumber = Math.abs(totalItemNumber / pageSize) + 1
        val batchSize = BATCH_SIZE // batch size for concurrent push requests
        var executor: ExecutorService
        var tasks: MutableList<FutureTask<PullTaskResponse<T>>>
        var pullRequest: NetworkManager<T>.Get?
        var ft: FutureTask<PullTaskResponse<T>>
        cache?.delete(query.setSkip(0).setLimit(0))// To be sure that skip and limit are 0,
        // because in next lines custom skip and limit are set anyway
        var i = 0
        while (i < totalPagesNumber) {
            executor = Executors.newFixedThreadPool(batchSize)
            tasks = ArrayList()
            do {
                query.setSkip(skipCount).setLimit(pageSize)
                pullRequest = networkManager.getBlocking(query)
                skipCount += pageSize
                try {
                    ft = FutureTask(CallableAsyncPullRequestHelper(pullRequest, query))
                    tasks.add(ft)
                    executor.execute(ft)
                } catch (e: AccessControlException) {
                    e.printStackTrace()
                    exceptions.add(e)
                } catch (e: KinveyException) {
                    e.printStackTrace()
                    exceptions.add(e)
                } catch (e: Exception) {
                    throw e
                }

            } while (skipCount < totalItemNumber)

            for (task in tasks) {
                try {
                    val tempResponse = task.get()
                    pulledItemCount += cache?.save(tempResponse.kinveyReadResponse?.result ?: listOf())?.size ?: 0
                    exceptions.addAll(tempResponse.kinveyReadResponse?.listOfExceptions ?: listOf())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }

            }
            executor.shutdown()
            i += batchSize
        }
        query.setSkip(0).setLimit(0) // To set back default value of skip and limit
        response.listOfExceptions = exceptions
        response.count = pulledItemCount
        if (isDeltaSetCachingEnabled && lastRequestTime != null) {
            saveQueryCacheItem(stringQuery, lastRequestTime)
        }
        return response
    }

    private fun saveQueryCacheItem(stringQuery: String, lastRequestTime: String) {
        val queryCacheItem = getQueryCacheItem(stringQuery)
        if (queryCacheItem != null) {
            queryCacheItem.lastRequestTime = lastRequestTime
            queryCache?.save(queryCacheItem)
        } else {
            queryCache?.save(QueryCacheItem(collectionName, stringQuery, lastRequestTime))
        }
    }

    /**
     * Get network data with given query into local storage using Delta Sync
     * @param query [Query]
     * @return KinveyReadResponse object
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun findBlockingDeltaSync(query: Query): KinveyReadResponse<T>? {
        val cacheItem = getQueryCacheItem(query) //one is correct number of query cache item count for any request.
        return if (cacheItem != null) findBlockingDeltaSync(cacheItem, query) else getBlocking(query)
    }

    /**
     * Get network data with given query into local storage using Delta Sync
     * @param query [Query]
     * @return KinveyReadResponse object
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun pullBlockingDeltaSync(query: Query): KinveyPullResponse {
        val cacheItem = getQueryCacheItem(query) //one is correct number of query cache item count for any request.
        return if (cacheItem != null) pullBlockingDeltaSync(cacheItem, query, 0) else pullBlockingRegular(query)
    }

    /**
     * Regular Pull. Delta Set and auto-pagination aren't used
     * @param query query to filter results
     * @return KinveyPullResponse object
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun pullBlockingRegular(query: Query): KinveyPullResponse {
        val readResponse = getBlocking(query)
        val pullResponse = KinveyPullResponse()
        pullResponse.count = readResponse?.result?.size ?: 0
        pullResponse.listOfExceptions = if (readResponse?.listOfExceptions != null) readResponse.listOfExceptions else ArrayList()
        return pullResponse
    }

    /**
     * Delta Set isn't used
     * @param query query to filter results
     * @return KinveyReadResponse object
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getBlocking(query: Query): KinveyReadResponse<T>? {
        val response = networkManager.getBlocking(query)?.execute()
        cache?.delete(query)
        response?.result?.let { list -> cache?.save(list) }
        response?.lastRequestTime?.let { timeStr ->
            saveQueryCacheItem(query.queryFilterMap.toString(), timeStr)
        }
        return response
    }

    /**
     * PullBlocking with Delta Set
     * @param cacheItem cached query from QueryCacheTable
     * @param query query to filter results
     * @return KinveyReadResponse object
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun findBlockingDeltaSync(cacheItem: QueryCacheItem, query: Query): KinveyReadResponse<T>? {
        try {
            val response = KinveyReadResponse<T>()
            val queryCacheResponse = networkManager.queryCacheGetBlocking(query, cacheItem.lastRequestTime)?.execute()
            cache?.let { cache ->
                queryCacheResponse?.deleted?.let { deleted ->
                    val ids = deleted.mapNotNull { it[Constants._ID] as String? }
                    cache.delete(ids)
                }
                queryCacheResponse?.changed?.let { changed ->
                    cache.save(changed)
                }
            }
            response.result = cache?.run { this[query] }
            response.listOfExceptions = queryCacheResponse?.listOfExceptions ?: ArrayList()
            response.lastRequestTime = queryCacheResponse?.lastRequestTime
            cacheItem.lastRequestTime = queryCacheResponse?.lastRequestTime
            queryCache?.save(cacheItem)
            return response
        } catch (responseException: KinveyJsonResponseException) {
            val statusCode = responseException.statusCode
            val jsonError = responseException.details
            return if (statusCode == 400 && jsonError?.error == RESULT_SIZE_ERROR ||
                    statusCode == 400 && jsonError?.error == PARAMETER_VALUE_OF_RANGE_ERROR ||
                    statusCode == 403 && jsonError?.error == MISSING_CONFIGURATION_ERROR) {
                getBlocking(query)
            } else {
                throw responseException
            }
        }
    }

    /**
     * PullBlocking with Delta Set
     * @param cacheItem cached query from QueryCacheTable
     * @param query query to filter results
     * @return KinveyReadResponse object
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun pullBlockingDeltaSync(cacheItem: QueryCacheItem, query: Query, pageSize: Int): KinveyPullResponse {
        try {
            val response = KinveyPullResponse()
            val queryCacheResponse = networkManager.queryCacheGetBlocking(query, cacheItem.lastRequestTime)?.execute()
            cache?.let { cache ->
                queryCacheResponse?.deleted?.let { deleted ->
                    val ids = deleted.mapNotNull { it[Constants._ID] as String? }
                    cache.delete(ids)
                }
                queryCacheResponse?.changed?.let { changed ->
                    response.count = cache.save(changed).size
                }
            }
            response.listOfExceptions = queryCacheResponse?.listOfExceptions ?: ArrayList()
            cacheItem.lastRequestTime = queryCacheResponse?.lastRequestTime
            queryCache?.save(cacheItem)
            return response
        } catch (responseException: KinveyJsonResponseException) {
            val statusCode = responseException.statusCode
            val jsonError = responseException.details
            return if (statusCode == 400 && jsonError?.error == RESULT_SIZE_ERROR ||
                    statusCode == 400 && jsonError?.error == PARAMETER_VALUE_OF_RANGE_ERROR ||
                    statusCode == 403 && jsonError?.error == MISSING_CONFIGURATION_ERROR) {
                if (pageSize > 0) pullBlockingPaged(query, pageSize) else pullBlockingRegular(query)
            } else {
                throw responseException
            }
        }
    }

    private fun getQueryCacheItem(query: Query): QueryCacheItem? {
        return getQueryCacheItem(query.queryFilterMap.toString())
    }

    private fun getQueryCacheItem(stringQuery: String): QueryCacheItem? {
        if (queryCache == null) {
            queryCache = client.cacheManager?.getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem::class.java, java.lang.Long.MAX_VALUE)
        }
        val queryCacheItems = queryCache?.run { this[client.query().equals(Constants.QUERY, stringQuery)] } ?: listOf()
        // In usual case, queryCacheItems always 1 or 0.
        if (queryCacheItems.size > 1) { // check that the queryCache has only 1 item for the query,
            var tempItem = queryCacheItems[0] // else remove all items(with the same query) except the latest
            val format = SimpleDateFormat(Constants.DATE_FORMAT, Locale.US)
            var tempItemDate: Date
            var cacheItemDate: Date
            for (cacheItem in queryCacheItems) {
                try {
                    tempItemDate = format.parse(tempItem.lastRequestTime)
                    cacheItemDate = format.parse(cacheItem.lastRequestTime)
                    if (tempItemDate < cacheItemDate) { // if result is < 0 than cacheItemDate is after tempItemDate
                        tempItem = cacheItem
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                    return tempItem
                }

            }
            queryCache?.clear()
            queryCache?.save(tempItem)
            return tempItem
        }
        return if (queryCacheItems.size == 1) queryCacheItems[0] else null
    }

    /**
     * Run sync operation to sync local and network storages
     * @param query query to pull the objects
     */
    @Throws(IOException::class)
    fun syncBlocking(query: Query?) {
        pushBlocking()
        pullBlocking(query)
    }

    /**
     * Run sync operation to sync local and network storages
     * @param query query to pull the objects
     * @param isAutoPagination true if auto-pagination is used
     */
    @Throws(IOException::class)
    fun syncBlocking(query: Query?, isAutoPagination: Boolean) {
        pushBlocking()
        pullBlocking(query, isAutoPagination)
    }

    /**
     * Run sync operation to sync local and network storages
     * @param query query to pull the objects
     * @param pageSize page size for auto-pagination
     */
    @Throws(IOException::class)
    fun syncBlocking(query: Query?, pageSize: Int) {
        pushBlocking()
        pullBlocking(query, pageSize)
    }

    fun purge() {
        Preconditions.checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType")
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        client.syncManager.clear(collectionName)
    }

    fun purge(query: Query) {
        Preconditions.checkArgument(storeType != StoreType.NETWORK, "InvalidDataStoreType")
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        var t: Any?
        for (item in cache!![query]) {
            t = item["_id"]
            if (t != null) {
                client.syncManager.deleteCachedItems(Query().equals("meta.id", item["_id"]))
            }
        }
    }

    /**
     * Collect all entities with the same value for fields,
     * and then apply a reduce function (such as count or average) on all those items.
     * @param aggregateType [AggregateType] (such as min, max, sum, count, average)
     * @param fields fields for group by
     * @param reduceField field for apply reduce function
     * @param query query to filter results
     * @return the array of groups containing the result of the reduce function
     */
    @Throws(IOException::class)
    fun group(aggregateType: AggregateType, fields: ArrayList<String>, reduceField: String?, query: Query,
              cachedCallback: KinveyCachedAggregateCallback?): Aggregation {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
        Preconditions.checkArgument(cachedCallback == null || storeType == StoreType.CACHE, "KinveyCachedClientCallback can only be used with StoreType.CACHE")
        return aggregation(aggregateType, fields, reduceField, query, cachedCallback)
    }

    /**
     * Used for aggregate fields
     */
    @Throws(IOException::class)
    private fun aggregation(type: AggregateType, fields: ArrayList<String>,
                            field: String?, query: Query, cachedCallback: KinveyCachedAggregateCallback?): Aggregation {
        var ret: Aggregation? = null
        if (storeType == StoreType.CACHE && cachedCallback != null) {
            try {
                ret = Aggregation(listOf(*AggregationRequest(type, cache as ICache<Aggregation.Result>,
                        ReadPolicy.FORCE_LOCAL, networkManager as NetworkManager<Aggregation.Result>, fields, field, query).execute()))
            } catch (e: IOException) {
                cachedCallback.onFailure(e)
            }
            ret?.let { cachedCallback.onSuccess(it) }
        }
        ret = Aggregation(listOf(*AggregationRequest(type, cache as ICache<Aggregation.Result>?,
                this.storeType.readPolicy, networkManager as NetworkManager<Aggregation.Result>,
                fields, field, query).execute()))
        return ret
    }

    @Throws(IOException::class)
    fun subscribe(storeLiveServiceCallback: KinveyDataStoreLiveServiceCallback<T>): Boolean {
        var success = false
        if (storeLiveServiceCallback != null) {
            liveServiceCallback = storeLiveServiceCallback
            networkManager.subscribe(client.deviceId)?.execute()
            val callback = object : KinveyLiveServiceCallback<String> {
                override fun onNext(next: String) {
                    try {
                        liveServiceCallback?.onNext(client.jsonFactory.createJsonParser(next).parse(currentClass))
                    } catch (e: IOException) {
                        e.printStackTrace()
                        liveServiceCallback?.onError(e)
                    }
                }
                override fun onError(e: Exception) {
                    liveServiceCallback?.onError(e)
                }
                override fun onStatus(status: KinveyLiveServiceStatus) {
                    liveServiceCallback?.onStatus(status)
                }
            }
            success = LiveServiceRouter.instance?.subscribeCallback(collectionName, callback) ?: false
        }
        return success
    }

    fun unsubscribe() {
        liveServiceCallback = null
        LiveServiceRouter.instance?.unsubscribeCallback(collectionName)
    }

    private fun isQueryContainSkipLimit(query: Query): Boolean {
        return query.skip != 0 || query.limit != 0
    }

    companion object {

        private val BATCH_SIZE = 5

        const val FIND = "find"

        const val DELETE = "delete"

        const val PURGE = "purge"

        const val GROUP = "group"

        const val COUNT = "count"

        private val MISSING_CONFIGURATION_ERROR = "MissingConfiguration"
        private val RESULT_SIZE_ERROR = "ResultSetSizeExceeded"
        private val PARAMETER_VALUE_OF_RANGE_ERROR = "ParameterValueOutOfRange"

        private val DEFAULT_PAGE_SIZE = 10000  // default page size set to backend record retrieval limit

        @JvmStatic
        open fun <T : GenericJson, C: AbstractClient<*>> collection(collectionName: String, myClass: Class<T>, storeType: StoreType, client: C): BaseDataStore<T> {
            Preconditions.checkNotNull(collectionName, "collectionName cannot be null.")
            Preconditions.checkNotNull(storeType, "storeType cannot be null.")
            Preconditions.checkArgument(client.isInitialize, "client must be initialized.")
            return BaseDataStore(client, collectionName, myClass, storeType)
        }
    }

}
/**
 * Constructor for creating BaseDataStore for given collection that will be mapped to itemType class
 * @param client Kinvey client instance to work with
 * @param collection collection name
 * @param itemType class that data should be mapped to
 * @param storeType type of storage that client want to use
 */
/**
 * Look up for data with given id
 * @param id the id of object we need to find
 * @return null or object that matched given id
 */
/**
 * Look up for object that have id in given collection of ids
 * @param ids collection of strings that identify a set of ids we have to look for
 * @return List of object found for given ids
 */
/**
 * Lookup objects in given collection by given query
 * @param query prepared query we have to look with
 * @return list of objects that are found
 */
/**
 * get all objects for given collections
 * @return all objects in given collection
 */

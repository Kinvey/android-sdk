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

package com.kinvey.android.async

import com.google.api.client.json.GenericJson
import com.google.common.base.Preconditions
import com.kinvey.android.AsyncClientRequest
import com.kinvey.android.sync.KinveyPushCallback
import com.kinvey.android.sync.KinveyPushResponse
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.KinveyException
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest
import com.kinvey.java.sync.dto.SyncRequest.HttpVerb
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.security.AccessControlException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

/**
 * Class represents internal implementation of Async push request that is used to create push
 */
class AsyncPushRequest<T : GenericJson>(private val collection: String,
                                        private val manager: SyncManager?,
                                        private val client: AbstractClient<*>?,
                                        private val storeType: StoreType,
                                        networkManager: NetworkManager<T>,
                                        storeItemType: Class<T>?,
                                        val pushCallback: KinveyPushCallback?) : AsyncClientRequest<KinveyPushResponse>(pushCallback) {
    private val networkManager: NetworkManager<T>
    private val storeItemType: Class<T>?
    @Throws(IOException::class, InvocationTargetException::class)
    override fun executeAsync(): KinveyPushResponse {
        Preconditions.checkArgument(storeType !== StoreType.NETWORK, "InvalidDataStoreType")
        val pushResponse = KinveyPushResponse()
        val errors: MutableList<Exception> = ArrayList()
        val requests = manager?.popSingleQueue(collection)
        val syncItems = manager?.popSingleItemQueue(collection)
        val batchSize = 10 // batch size for concurrent push requests

        var progress = 0
        var fullCount = requests?.size ?: 0
        fullCount += syncItems?.size ?: 0
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
                    pushCallback?.onFailure(e)
                }
                pushCallback?.onProgress(pushResponse.successCount.toLong(), fullCount.toLong())
            }
        }
        if (syncItems != null) {
            var id: String
            var t: T?
            var syncRequest: SyncRequest? = null
            var totalNumberOfPendingEntities = 0
            totalNumberOfPendingEntities = syncItems.size
            var i = 0
            while (i < totalNumberOfPendingEntities) {
                val executor: ExecutorService = Executors.newFixedThreadPool(batchSize)
                val tasks: MutableList<FutureTask<*>> = ArrayList()
                var j = 0
                while (j < batchSize && j + i < totalNumberOfPendingEntities) {
                    val syncItem = syncItems[j + i]
                    id = syncItem.entityID?.id ?: ""
                    when (syncItem.requestMethod) {
                        HttpVerb.SAVE, HttpVerb.POST, HttpVerb.PUT -> {
                            t = client?.cacheManager?.getCache(collection, storeItemType, Long.MAX_VALUE)?.get(id)
                            if (t == null) {
                                // check that item wasn't deleted before
                                // if item wasn't found, it means that the item was deleted from the Cache by Delete request and the item will be deleted in case:DELETE
                                manager?.deleteCachedItems(client?.query()?.equals("meta.id", id)?.notEqual(Constants.REQUEST_METHOD, Constants.DELETE))
                                j++
                            } else {
                                syncRequest = manager?.createSyncRequest(collection, networkManager.saveBlocking(t))
                            }
                        }
                        HttpVerb.DELETE -> syncRequest = manager?.createSyncRequest(collection, networkManager.deleteBlocking(id))
                    }
                    try {
                        val ft = FutureTask(CallableAsyncPushRequestHelper(client, manager, syncRequest, syncItem, storeItemType))
                        tasks.add(ft)
                        executor.execute(ft)
                        pushResponse.successCount = ++progress
                    } catch (e: AccessControlException) { //TODO check Exception
                        errors.add(e)
                    } catch (e: KinveyException) {
                        errors.add(e)
                    } catch (e: Exception) {
                        pushCallback?.onFailure(e)
                    }
                    pushCallback?.onProgress(pushResponse.successCount.toLong(), fullCount.toLong())
                    j++
                }
                for (task in tasks) {
                    try {
                        task.get()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                    }
                }
                executor.shutdown()
                i += batchSize
            }
        }
        pushResponse.listOfExceptions = errors
        return pushResponse
    }

    /**
     * Async push request constructor
     *
     * @param collection Collection name that we want to push
     * @param manager    sync manager that is used
     * @param client     Kinvey client instance to be used to execute network requests
     * @param callback   async callbacks to be invoked when job is done
     */

    init {
        this.networkManager = networkManager
        this.storeItemType = storeItemType
    }
}
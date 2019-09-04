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

package com.kinvey.java.store.requests.data.save

import com.google.api.client.json.GenericJson
import com.kinvey.java.Constants
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.requests.data.IRequest
import com.kinvey.java.store.requests.data.PushRequest
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest

import java.io.IOException
import java.security.AccessControlException

import java.util.ArrayList
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

/**
 * Created by Prots on 2/5/16.
 */
class SaveListRequest<T : GenericJson>(private val cache: ICache<T>?, private val networkManager: NetworkManager<T>,
                                       private val writePolicy: WritePolicy, private val objects: Iterable<T>,
                                       private val syncManager: SyncManager) : IRequest<List<T>> {
    var exception: IOException? = null

    @Throws(IOException::class)
    override fun execute(): List<T>? {
        var ret: List<T>? = ArrayList()
        when (writePolicy) {
            WritePolicy.FORCE_LOCAL -> {
                ret = cache?.save(objects)
                syncManager.enqueueSaveRequests(networkManager.collectionName, networkManager, ret)
            }
            WritePolicy.LOCAL_THEN_NETWORK -> {
                val pushRequest = PushRequest(networkManager.collectionName, cache, networkManager,
                        networkManager.client)
                try {
                    pushRequest.execute()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    // silent fall, will be synced next time
                }
                cache?.save(objects)
                ret = objects.mapNotNull { item ->
                      var result: T? = null
                      try {
                          result = networkManager.saveBlocking(item).execute()
                      } catch (e: IOException) {
                          val requestType = if (networkManager.isTempId(item)) SyncRequest.HttpVerb.POST
                                            else SyncRequest.HttpVerb.PUT
                          val itemId = item[Constants._ID] as String?
                          syncManager.enqueueRequest(networkManager.collectionName, networkManager, requestType , itemId)
                          exception = e
                      }
                      result
                }.toMutableList()
                cache?.save(ret)
                exception?.let { throw it }
            }
            WritePolicy.FORCE_NETWORK -> {
                Logger.INFO("Start saving entities")
                val executor: ExecutorService = Executors.newFixedThreadPool(networkManager.client.numberThreadsForDataStoreSaveList)
                val tasks: MutableList<FutureTask<T>>
                var ft: FutureTask<T>
                val items = objects as List<T>
                tasks = ArrayList()

                for (obj in objects) {
                    try {
                        val save = SaveRequest(cache, networkManager, writePolicy, obj, syncManager)
                        ft = FutureTask(CallableAsyncSaveRequestHelper(save))
                        tasks.add(ft)
                        executor.execute(ft)
                    } catch (e: AccessControlException) {
                        e.printStackTrace()
                    } catch (e: KinveyException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        throw e
                    }
                }
                for (task in tasks) {
                    try {
                        (ret as ArrayList).add(task.get())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                    }
                }
                executor.shutdown()
                Logger.INFO("Finish saving entities")
            }
        }
        return ret
    }

    class CallableAsyncSaveRequestHelper<T : GenericJson> (var save: SaveRequest<T>) : Callable<T> {
        @Throws(Exception::class)
        override fun call(): T? {
            return save.execute()
        }
    }

    override fun cancel() {
        //TODO: put async and track cancel
    }
}

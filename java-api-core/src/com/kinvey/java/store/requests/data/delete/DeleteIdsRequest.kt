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

package com.kinvey.java.store.requests.data.delete

import com.google.api.client.json.GenericJson
import com.google.common.collect.Iterables
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.query.MongoQueryFilter
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest

import java.io.IOException

/**
 * Created by Prots on 2/15/16.
 */
class DeleteIdsRequest<T : GenericJson>(cache: ICache<T>?, networkManager: NetworkManager<T>?, writePolicy: WritePolicy,
                                        private val ids: Iterable<String>?, syncManager: SyncManager?)
    : AbstractDeleteRequest<T>(cache, writePolicy, networkManager, syncManager) {

    override fun deleteCached(): Int? {
        return if (ids != null) {
            cache?.delete(ids)
        } else 0
    }

    @Throws(IOException::class)
    override fun deleteNetwork(): NetworkManager.Delete<T>? {
        val q = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val idsArray = Iterables.toArray(ids, String::class.java)
        q.`in`("_id", idsArray as Array<Any>)
        return networkManager?.deleteBlocking(q)
    }

    @Throws(IOException::class)
    override fun enqueueRequest(collectionName: String?, networkManager: NetworkManager<T>?) {
        syncManager?.enqueueDeleteRequests(collectionName, networkManager, ids)
    }
}

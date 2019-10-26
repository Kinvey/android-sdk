package com.kinvey.android.async

import com.google.api.client.json.GenericJson
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncItem
import com.kinvey.java.sync.dto.SyncRequest
import com.kinvey.java.sync.dto.SyncRequest.HttpVerb
import java.util.concurrent.Callable

class CallableAsyncPushRequestHelper<T : GenericJson> internal constructor(private val client: AbstractClient<*>?,
                                                                            private val manager: SyncManager?,
                                                                            private val syncRequest: SyncRequest?,
                                                                            private val syncItem: SyncItem?,
                                                                            private val storeItemType: Class<T>?) : Callable<Long> {
    @Throws(Exception::class)
    override fun call(): Long {
        try {
            if (syncRequest?.httpVerb === HttpVerb.POST) {
                val tempID = syncRequest.entityID?.id ?: ""
                val result = manager?.executeRequest(client, syncRequest)
                val cache = client?.cacheManager?.getCache(syncRequest.collectionName, storeItemType, Long.MAX_VALUE)
                val temp = cache?.run { cache[tempID] }
                result?.let { temp?.set("_id", result["_id"]) }
                cache?.delete(tempID)
                cache?.save(temp)
            } else {
                manager?.executeRequest(client, syncRequest)
            }
        } catch (e: KinveyJsonResponseException) {
            if (e.statusCode != IGNORED_EXCEPTION_CODE && !e.message.contains(IGNORED_EXCEPTION_MESSAGE)) {
                throw e
            }
        }
        syncItem?.let { manager?.deleteCachedItem(it[Constants._ID] as String?) }
        return 1L
    }

    companion object {
        private const val IGNORED_EXCEPTION_MESSAGE = "EntityNotFound"
        private const val IGNORED_EXCEPTION_CODE = 404
    }
}

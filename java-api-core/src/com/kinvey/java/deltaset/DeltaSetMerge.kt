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

package com.kinvey.java.deltaset

import com.google.api.client.json.GenericJson
import com.kinvey.java.Constants._ID
import com.kinvey.java.Constants._KMD
import com.kinvey.java.Constants._LMT
import com.kinvey.java.deltaset.DeltaSetItem.KMD
import java.io.IOException

/**
 * Created by Prots on 12/11/15.
 */
object DeltaSetMerge {
    @Throws(IOException::class)
    fun <T> getIdsForUpdate(cache: List<T>?, items: List<DeltaSetItem>?): List<String> {
        val cachedMap = listToMap(cache)
        val onlineMap = listToMap(items)
        val idsToUpdate = HashSet<String>()

        //assume all data have to be updated
        idsToUpdate.addAll(onlineMap.keys)

        cachedMap.keys.onEach { cachedId ->
            if (onlineMap.containsKey(cachedId)) {
                val onlineItem = onlineMap[cachedId]
                val cachedItem = cachedMap[cachedId]
                var cachedKMD: KMD? = null
                if (cachedItem is GenericJson && (cachedItem as GenericJson).containsKey(_KMD)) {
                    val kmd = cachedItem[_KMD]
                    if (kmd is Map<*, *>) { cachedKMD = KMD(kmd[_LMT].toString()) }
                }
                if (cachedKMD == null || cachedKMD.lmt == onlineItem?.kmd?.lmt) {
                    idsToUpdate.remove(cachedId)
                }
            }
        }
        return idsToUpdate.toList()
    }

    @Throws(IOException::class)
    fun <T> listToMap(arr: List<T>?): Map<String, T> {
        return arr?.mapNotNull {
            if (it is GenericJson && (it as GenericJson).containsKey(_ID)) {
                it[_ID].toString() to it
            } else null
        }?.toMap() as Map<String, T>
    }

    @Throws(IOException::class)
    fun <T> merge(order: List<DeltaSetItem>?, cache: List<T>?, online: List<T>?): List<T> {
        val cacheMap = listToMap(cache)
        val onlineMap = listToMap(online)
        val orderedResult = order?.map { item ->
            val itemId = item.id
            when {
                //prefer online
                onlineMap.containsKey(itemId) -> onlineMap[itemId] as T
                cacheMap.containsKey(itemId) -> cacheMap[itemId] as T
                else -> null
            }
        }?.filterNot { item -> item == null }?.toMutableList() ?: mutableListOf()
        return orderedResult as List<T>
    }
}
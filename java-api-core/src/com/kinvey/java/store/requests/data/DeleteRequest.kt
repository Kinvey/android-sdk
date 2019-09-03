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

package com.kinvey.java.store.requests.data

import com.google.api.client.json.GenericJson
import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy

import java.io.IOException

/**
 * Created by Prots on 2/8/16.
 */
class DeleteRequest<T : GenericJson> : AbstractKinveyDataRequest<T> {

    private val query: Query?
    private val id: String?
    private val writePolicy: WritePolicy

    constructor(cache: ICache<T>, id: String, writePolicy: WritePolicy, networkManager: NetworkManager<T>) {
        this.networkManager = networkManager
        query = null
        this.cache = cache
        this.id = id
        this.writePolicy = writePolicy
    }

    constructor(client: AbstractClient<*>, collectionName: String, clazz: Class<T>,
                cache: ICache<T>, query: Query, writePolicy: WritePolicy) {
        id = null
        this.cache = cache
        this.query = query
        this.writePolicy = writePolicy
    }

    override fun execute(): T? {
        when (writePolicy) {
            WritePolicy.FORCE_LOCAL -> cache?.delete(query!!)
            WritePolicy.FORCE_NETWORK ->

                try {
                    networkManager?.deleteBlocking(query)
                } catch (e: IOException) {
                    //TODO: add to sync
                    e.printStackTrace()
                }

            WritePolicy.LOCAL_THEN_NETWORK -> {
                //write to local and network, push to sync if network fails
                cache?.delete(query!!)
                try {
                    networkManager?.deleteBlocking(query)
                } catch (e: IOException) {
                    //TODO: add to sync
                    e.printStackTrace()
                }

            }
        }//TODO: write to sync
        //write to network, fallback to sync
        return null
    }

    override fun cancel() {

    }
}

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

package com.kinvey.java.store.requests.user

import com.google.api.client.util.Key
import com.google.common.base.Joiner
import com.google.gson.Gson
import com.kinvey.java.Query
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.store.UserStoreRequestManager

/**
 * Retrieve Request Class, extends AbstractKinveyJsonClientRequest<T>.  Constructs the HTTP request object for
 * Retrieve BaseUser requests.
</T> */
class Retrieve<T : BaseUser> : AbstractKinveyJsonClientRequest<T> {

    private var userStoreRequestManager: UserStoreRequestManager<*>? = null
    @Key
    private var userID: String? = null
    @Key("query")
    private var queryFilter: String? = null
    @Key("sort")
    private var sortFilter: String? = null
    @Key
    private var limit: String? = null
    @Key
    private var skip: String? = null

    @Key("resolve")
    private var resolve: String? = null
    @Key("resolve_depth")
    private var resolve_depth: String? = null
    @Key("retainReferences")
    private var retainReferences: String? = null

    constructor(userStoreRequestManager: UserStoreRequestManager<T>, userID: String) : super(userStoreRequestManager.getClient(), "GET", REST_PATH, null, userStoreRequestManager.getClient().userClass as Class<T>) {
        this.userStoreRequestManager = userStoreRequestManager
        this.userID = userID
        this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = userStoreRequestManager.getClientAppVersion()
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()) {
            this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(userStoreRequestManager.getCustomRequestProperties())
        }
    }

    constructor(userStoreRequestManager: UserStoreRequestManager<T>, query: Query) : super(userStoreRequestManager.getClient(), "GET", REST_PATH, null, userStoreRequestManager.getClient().userClass as Class<T>) {
        this.userStoreRequestManager = userStoreRequestManager
        this.queryFilter = query.getQueryFilterJson(userStoreRequestManager.getClient().jsonFactory)
        val queryLimit = query.limit
        val querySkip = query.skip
        this.limit = if (queryLimit > 0) Integer.toString(queryLimit) else null
        this.skip = if (querySkip > 0) Integer.toString(querySkip) else null
        this.sortFilter = query.sortString
        this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = userStoreRequestManager.getClientAppVersion()
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()) {
            this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson()
                    .toJson(userStoreRequestManager.getCustomRequestProperties())
        }
    }

    constructor(userStoreRequestManager: UserStoreRequestManager<T>, userID: String, resolve: Array<String>, resolve_depth: Int, retain: Boolean) : super(userStoreRequestManager.getClient(), "GET", REST_PATH, null, userStoreRequestManager.getClient().userClass as Class<T>) {
        this.userStoreRequestManager = userStoreRequestManager
        this.userID = userID

        this.resolve = Joiner.on(",").join(resolve)
        this.resolve_depth = if (resolve_depth > 0) Integer.toString(resolve_depth) else null
        this.retainReferences = java.lang.Boolean.toString(retain)
        this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = userStoreRequestManager.getClientAppVersion()
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()) {
            this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson()
                    .toJson(userStoreRequestManager.getCustomRequestProperties())
        }
    }

    constructor(userStoreRequestManager: UserStoreRequestManager<T>, query: Query, resolve: Array<String>, resolve_depth: Int, retain: Boolean) : super(userStoreRequestManager.getClient(), "GET", REST_PATH, null, userStoreRequestManager.getClient().userClass as Class<T>) {
        this.userStoreRequestManager = userStoreRequestManager
        this.queryFilter = query.getQueryFilterJson(userStoreRequestManager.getClient().jsonFactory)
        val queryLimit = query.limit
        val querySkip = query.skip
        this.limit = if (queryLimit > 0) Integer.toString(queryLimit) else null
        this.skip = if (querySkip > 0) Integer.toString(querySkip) else null
        this.sortFilter = query.sortString

        this.resolve = Joiner.on(",").join(resolve)
        this.resolve_depth = if (resolve_depth > 0) Integer.toString(resolve_depth) else null
        this.retainReferences = java.lang.Boolean.toString(retain)
        this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = userStoreRequestManager.getClientAppVersion()
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()) {
            this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson()
                    .toJson(userStoreRequestManager.getCustomRequestProperties())
        }

    }

    companion object {
        private val REST_PATH = "user/{appKey}/{userID}{?query,sort,limit,skip,resolve,resolve_depth,retainReference}"
    }
}

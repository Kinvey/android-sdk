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
import com.google.gson.Gson
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.dto.PasswordRequest
import com.kinvey.java.store.UserStoreRequestManager

import java.io.IOException

/**
 * Update Request Class, extends AbstractKinveyJsonClientRequest<BaseUser>.  Constructs the HTTP request object for
 * Update BaseUser requests.
</BaseUser> */
class Update<T : BaseUser> : AbstractKinveyJsonClientRequest<T> {

    private var userStoreRequestManager: UserStoreRequestManager<T>? = null
    @Key
    private var userID: String? = null

    constructor(userStoreRequestManager: UserStoreRequestManager<T>, baseUser: BaseUser, userClass: Class<T>)
        : super(userStoreRequestManager.getClient(), "PUT", REST_PATH, baseUser, userClass) {
        this.userStoreRequestManager = userStoreRequestManager
        this.userID = baseUser.id
        this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = userStoreRequestManager.getClientAppVersion()
        if (userStoreRequestManager.getCustomRequestProperties()?.isEmpty() == false) {
            this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(userStoreRequestManager.getCustomRequestProperties())
        }
    }

    constructor(userStoreRequestManager: UserStoreRequestManager<T>, baseUser: BaseUser, passwordRequest: PasswordRequest, userClass: Class<T>)
        : super(userStoreRequestManager.getClient(), "PUT", REST_PATH, passwordRequest, userClass) {
        this.userStoreRequestManager = userStoreRequestManager
        this.userID = baseUser.id
        this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = userStoreRequestManager.getClientAppVersion()
        if (userStoreRequestManager.getCustomRequestProperties()?.isEmpty() == false) {
            this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(userStoreRequestManager.getCustomRequestProperties())
        }
    }

    constructor(userStoreRequestManager: UserStoreRequestManager<T>, userId: String, userClass: Class<T>)
        : super(userStoreRequestManager.getClient(), "PUT", REST_PATH, null, userClass) {
        this.userStoreRequestManager = userStoreRequestManager
        this.userID = userId
        this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = userStoreRequestManager.getClientAppVersion()
        if (userStoreRequestManager.getCustomRequestProperties()?.isEmpty() == false) {
            this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(userStoreRequestManager.getCustomRequestProperties())
        }
    }

    @Throws(IOException::class)
    override fun execute(): T? {
        val updatedUser = super.execute()
        if (updatedUser?.id == null) {
            return updatedUser
        }
        return if (updatedUser.id == userID) {
            userStoreRequestManager?.initUser(updatedUser)
        } else {
            updatedUser
        }
    }

    companion object {
        private val REST_PATH = "user/{appKey}/{userID}"
    }
}

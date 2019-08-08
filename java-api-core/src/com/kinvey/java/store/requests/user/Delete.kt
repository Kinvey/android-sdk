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
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.store.UserStoreRequestManager

import java.io.IOException

/**
 * Delete Request Class, extends AbstractKinveyJsonClientRequest<Void>.  Constructs the HTTP request object for
 * Delete BaseUser requests.
</Void> */
class Delete(private val userStoreRequestManager: UserStoreRequestManager<*>, @field:Key
private val userID: String, hard: Boolean) : AbstractKinveyJsonClientRequest<Void>(userStoreRequestManager.getClient(), "DELETE", REST_PATH, null, Void::class.java) {
    @Key
    private var hard = false

    init {
        this.hard = hard
        this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = userStoreRequestManager.getClientAppVersion()
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()) {
            this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(userStoreRequestManager.getCustomRequestProperties())
        }
    }

    @Throws(IOException::class)
    override fun execute(): Void? {
        super.execute()
        userStoreRequestManager.removeFromStore(userID)
        userStoreRequestManager.logoutSoft().execute()
        (abstractKinveyClient.getKinveyRequestInitializer() as KinveyClientRequestInitializer).setCredential(null)
        abstractKinveyClient.performLockDown()
        return null
    }

    companion object {
        private val REST_PATH = "user/{appKey}/{userID}?hard={hard}"
    }
}

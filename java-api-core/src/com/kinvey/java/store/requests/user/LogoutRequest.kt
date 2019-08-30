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

import com.kinvey.java.AbstractClient
import com.kinvey.java.auth.CredentialManager
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.store.LiveServiceRouter

import java.io.IOException

/**
 * Logout Request Class.  Constructs the HTTP request object for Logout requests.
 */
class LogoutRequest(private val client: AbstractClient<*>)
    : AbstractKinveyJsonClientRequest<Void>(client, "POST", REST_PATH, null, Void::class.java) {

    @Throws(IOException::class)
    override fun execute(): Void? {
        try {
            client.initializeRequest(this)
            super.execute()
        } catch (error: Exception) {
            error.printStackTrace()
        } finally {
            client.performLockDown()
            if (LiveServiceRouter.instance?.isInitialized == true) {
                LiveServiceRouter.instance?.uninitialize()
            }
            val manager = CredentialManager(client.store)
            manager.removeCredential(client.activeUser?.id)
            client.activeUser = null
            (client.kinveyRequestInitializer as KinveyClientRequestInitializer).setCredential(null)
        }
        return null
    }

    companion object {
        private const val REST_PATH = "/user/{appKey}/_logout"
    }
}

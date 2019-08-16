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

import com.google.api.client.http.HttpContent
import com.google.api.client.json.GenericJson
import com.kinvey.java.AbstractClient
import com.kinvey.java.KinveyException
import com.kinvey.java.core.AbstractKinveyClientRequest
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.store.UserStoreRequestManager

import java.io.IOException

/**
 * Created by Prots on 2/12/16.
 */
class LoginToTempURL(private val userStoreRequestManager: UserStoreRequestManager<*>, private val clientId: String, tempURL: String, content: HttpContent) : AbstractKinveyClientRequest<GenericJson>(userStoreRequestManager.getClient() as AbstractClient<BaseUser>, tempURL, "POST", "", content, GenericJson::class.java) {

    init {
        this.overrideRedirect = true
    }

    @Throws(IOException::class)
    override fun onRedirect(newLocation: String): GenericJson? {
        val codeIndex = newLocation.indexOf("code=")
        if (codeIndex == -1) {
            throw KinveyException("Redirect does not contain `code=`, was: $newLocation")
        }

        val accesstoken = newLocation.substring(codeIndex + 5, newLocation.length)

        return userStoreRequestManager.getMICToken(accesstoken, clientId).execute()


    }
}

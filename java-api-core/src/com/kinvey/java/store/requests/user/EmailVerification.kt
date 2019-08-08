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
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.store.UserStoreRequestManager

/**
 * EmailVerification Request Class, extends AbstractKinveyJsonClientRequest<BaseUser>.  Constructs the HTTP request
 * object for EmailVerification requests.
</BaseUser> */
class EmailVerification(private val userStoreRequestManager: UserStoreRequestManager<*>, @field:Key
private val userID: String) : AbstractKinveyJsonClientRequest<Void>(userStoreRequestManager.getClient(), "POST", REST_PATH, null, Void::class.java) {

    init {
        this.isRequireAppCredentials = true
    }

    companion object {
        private val REST_PATH = "rpc/{appKey}/{userID}/user-email-verification-initiate"
    }
}

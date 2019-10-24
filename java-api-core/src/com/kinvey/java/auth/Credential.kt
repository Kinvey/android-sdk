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

package com.kinvey.java.auth

import com.kinvey.java.Constants.AUTH_TOKEN
import com.kinvey.java.core.AbstractKinveyClientRequest
import com.kinvey.java.core.KinveyRequestInitializer
import com.kinvey.java.dto.BaseUser
import java.io.Serializable

/**
 * @author m0rganic
 * @since 2.0
 */
class Credential : KinveyRequestInitializer, Serializable {

    var userId: String? = null
    var clientId: String? = null
    var authToken: String? = null
    var refreshToken: String? = null

    /** package  */
    internal constructor() {}

    constructor(userId: String?, authToken: String?, refresh: String?) {
        this.userId = userId
        this.authToken = authToken
        refreshToken = refresh
    }

    override fun initialize(request: AbstractKinveyClientRequest<*>) {
        // execute the original intent for this interceptor

        if (authToken != null) {
            request.getRequestHeaders().authorization = String.format(AUTH_N_HEADER_FORMAT, authToken)
        }
    }

    companion object {
        private const val serialVersionUID = 1L
        private const val AUTH_N_HEADER_FORMAT = "Kinvey %s"
        private const val KMD = "_kmd"
        /**
         * Convenience method intended to shield calling code from having to deal with KinveyAuthResponse directly
         *
         * @param response a valid response from the Kinvey authentication
         * @return a newly constructed Credential object
         */
        fun from(response: KinveyAuthResponse?): Credential {
            return from(response?.userId, response?.authToken)
        }

        /**
         * @param baseUser authorised user for saving auth token
         * @return a newly constructed Credential object
         */
        fun from(baseUser: BaseUser?): Credential? {
            return baseUser?.run {
                val kmd = baseUser[KMD]
                if (kmd != null) {
                    from(baseUser.id, (kmd as Map<String, String>)[AUTH_TOKEN])
                } else null
            }
        }

        /**
         * @param userId user id for saving
         * @param authToken authToken for saving
         * @return a newly constructed Credential object
         */
        fun from(userId: String?, authToken: String?): Credential {
            return Credential(userId, authToken, null)
        }
    }
}
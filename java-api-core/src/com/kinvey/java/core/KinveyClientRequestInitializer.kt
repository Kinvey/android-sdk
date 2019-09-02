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

package com.kinvey.java.core

import com.google.common.base.Preconditions
import com.kinvey.java.auth.Credential

/**
 * @author m0rganic
 */
open class KinveyClientRequestInitializer
/**
 *
 * @param appKey the key to set on the request
 * @param appSecret application secret, used for user management methods
 * @param credential the authorization context for the request
 * @param kinveyHeaders
 */
(
        /** the app key for the request  */
        /**
         * @return the appKey
         */
        val appKey: String,
        /** the app secret for the request  */
        /**
         * @return the appSecret
         */
        val appSecret: String,
        /** authorization context for the request  */
        private var credential: Credential?,
        /** standard headers used across all of the kinvey api  */
        /**
         * @return Kinvey Headers configured for this request initializer
         */
        val kinveyHeaders: KinveyHeaders) : KinveyRequestInitializer {

    /** the clientId for the request in format:  AppKey.ServiceId  */
    private var clientId: String? = null

    /**
     * @param appKey application key, will be set on the request
     * @param appSecret application secret, used for user management methods
     * @param kinveyHeaders
     */
    constructor(appKey: String, appSecret: String, kinveyHeaders: KinveyHeaders) : this(appKey, appSecret, null, kinveyHeaders) {}

    fun setClientId(clientId: String) {
        this.clientId = clientId
    }

    /**
     *
     * @param credential valid authorization context obtained from [com.kinvey.java.auth.KinveyAuthRequest]
     * @return client request initializer
     */
    fun setCredential(credential: Credential?): KinveyClientRequestInitializer {
        this.credential = credential
        return this
    }

    /**
     * Sets the authentication header using credential, appkey is set and kinvey standard
     * headers are added to the request.
     *
     * @param request the request to initialize
     */
    override fun initialize(request: AbstractKinveyClientRequest<*>) {
        if (!request.isRequireAppCredentials && !request.isRequiredClientIdAuth) {
            Preconditions.checkNotNull<Credential>(credential, "No Active User - please login a user by calling UserStore.login( ... ) before retrying this request.")
            Preconditions.checkNotNull(credential?.userId, "No Active User - please login a user by calling UserStore.login( ... ) before retrying this request.")
            Preconditions.checkNotNull(credential?.authToken, "No Active User - please login a user by calling UserStore.login( ... ) before retrying this request.")
        }
        if (!request.isRequireAppCredentials && !request.isRequiredClientIdAuth) {
            credential?.initialize(request)
        }
        if (request.isRequireAppCredentials) {
            request.getRequestHeaders().setBasicAuthentication(appKey, appSecret)
        } else if (request.isRequiredClientIdAuth && clientId != null) {
            request.getRequestHeaders().setBasicAuthentication(clientId!!, appSecret)
        }
        if (!request.isRequiredClientIdAuth) {
            request.setAppKey(appKey)
        }
        request.getRequestHeaders().putAll(kinveyHeaders)
    }
}

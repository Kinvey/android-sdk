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

import com.kinvey.java.dto.BaseUser
import java.io.IOException

/**
 * @author m0rganic
 * @since 2.0
 */
class CredentialManager {

    private var credentialStore: CredentialStore? = null

    private constructor() {}

    constructor(credentialStore: CredentialStore?) {
        if (credentialStore == null) {
            this.credentialStore = InMemoryCredentialStore()
        } else {
            this.credentialStore = credentialStore
        }
    }

    /**
     *
     * @param userId a valid userId to retrieve the credential from storage
     * @return a credential object if one is found otherwise `null` is returned
     * @throws IOException error in access the low-level storage mechanism
     */
    @Throws(IOException::class)
    fun loadCredential(userId: String?): Credential? {
        return if (credentialStore == null) {
            null
        } else credentialStore?.load(userId)
    }

    /**
     *
     * @param userId unique string to hash the credential with
     * @param credential a credential object to store
     * @throws IOException error accessing the low-level storage mechanism
     */
    @Throws(IOException::class)
    fun makePersistent(userId: String?, credential: Credential?) {
        if (credentialStore == null) {
            return
        }
        credentialStore?.store(userId ?: "", credential)
    }

    /**
     * @param response response received from a new token request
     * @param userId username or `null` if no persistent store is being used
     * @return new credential object
     */
    @Throws(IOException::class)
    fun createAndStoreCredential(response: KinveyAuthResponse?, userId: String?): Credential {
        val newCredential: Credential = Credential.from(response)
        if (userId != null && credentialStore != null) {
            credentialStore?.store(userId, newCredential)
        }
        return newCredential
    }

    /**
     * @param baseUser user
     * @return new credential object
     */
    @Throws(IOException::class)
    fun createAndStoreCredential(baseUser: BaseUser?): Credential? {
        val userId = baseUser?.id
        val newCredential = Credential.from(baseUser)
        if (userId != null && credentialStore != null) {
            credentialStore?.store(userId, newCredential)
        }
        return newCredential
    }

    fun removeCredential(userId: String?) {
        if (credentialStore != null) {
            credentialStore?.delete(userId)
        }
    }
}
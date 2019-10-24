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

import com.google.common.base.Preconditions
import java.util.*

/**
 * @author m0rganic
 * @since 2.0
 */
class InMemoryCredentialStore : CredentialStore {

    private val store: MutableMap<String, Credential> = HashMap()

    override fun load(userId: String?): Credential? {
        return store[userId]
    }

    override fun store(userId: String?, credential: Credential?) {
        Preconditions.checkNotNull(credential, "credential must not be null")
        val cred = Credential(userId, credential?.authToken, credential?.refreshToken)
        if (userId != null) {
            store[userId] = cred
        }
    }

    override fun delete(userId: String?) {
        if (userId != null) {
            store.remove(userId)
        }
    }
}
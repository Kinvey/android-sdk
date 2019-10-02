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

import java.io.IOException

/**
 * A mechanism to store, retrieve and purge credentials from memory and disk
 */
interface CredentialStore {
    /**
     * @param userId a unique identifier for the stored credential
     * @return a credential object retrieved from storage otherwise `null` is returned
     * @throws IOException error in retrieving from low-level storage mechanism
     */
    @Throws(IOException::class)
    fun load(userId: String?): Credential?

    /**
     * @param userId a unique identifier to index the credential in storage
     * @param credential non-null credential to store
     * @throws IOException error in storing to low-level storage mechanism
     */
    @Throws(IOException::class)
    fun store(userId: String?, credential: Credential?)

    /**
     * @param userId the unique identifier to the credential to purge
     * @throws IOException error in purging the credential from low-level storage
     */
    fun delete(userId: String?)
}
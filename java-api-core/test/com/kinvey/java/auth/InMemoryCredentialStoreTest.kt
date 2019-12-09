/**
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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

import junit.framework.TestCase

/**
 * @author m0rganic
 * @since 2.0
 */
class InMemoryCredentialStoreTest : TestCase() {

    fun testNullParams() {
        val store = InMemoryCredentialStore()
        // store should not throw exception on null key
        try {
            store.store(null, Credential("testuserid", "testtoken", "testrefresh"))
        } catch (e: NullPointerException) {
            fail("store should not throw exception")
        }
        // store should throw exception on null credential
        try {
            store.store("testkey", null)
            fail("store should throw exception on null credential")
        } catch (e: NullPointerException) {
            assertEquals("credential must not be null", e.message)
        }
    }

    fun testHappy() {
        val userId = "userId"
        val authotoken = "authotoken"
        val refresh = "refreshToken"
        val testCredential = Credential(userId, authotoken, refresh)
        val store = InMemoryCredentialStore()
        assertNull(store.load(userId))
        store.store(userId, testCredential)
        val emptyCred= store.load(userId)
        assertNotNull(emptyCred)
        assertEquals(emptyCred?.userId, userId)
        assertEquals(emptyCred?.authToken, authotoken)
        assertEquals(emptyCred?.refreshToken, refresh)
    }
}
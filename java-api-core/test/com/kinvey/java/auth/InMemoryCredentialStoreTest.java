/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.auth;

import junit.framework.TestCase;

/**
 * @author m0rganic
 * @since 2.0
 */
public class InMemoryCredentialStoreTest extends TestCase{

    public void testNullParams() {
        InMemoryCredentialStore store = new InMemoryCredentialStore();

        // store should not throw exception on null key
        try {
            store.store(null, new Credential("testuserid", "testtoken"));
        } catch (NullPointerException e) {
            fail("store should not throw exception");
        }

        // store should throw exception on null credential
        try {
            store.store("testkey", null);
            fail("store should throw exception on null credential");
        } catch (NullPointerException e) {
            assertEquals("credential must not be null", e.getMessage());
        }

    }

    public void testHappy () {
        String userId = "userId";
        String authotoken = "authotoken";
        Credential testCredential = new Credential(userId, authotoken);

        InMemoryCredentialStore store = new InMemoryCredentialStore();

        assertNull(store.load(userId));
        store.store(userId, testCredential);

        Credential emptyCred = new Credential();
        emptyCred = store.load(userId);
        assertNotNull(emptyCred);

        assertEquals(emptyCred.getUserId(), userId);
        assertEquals(emptyCred.getAuthToken(), authotoken);
    }

}

/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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

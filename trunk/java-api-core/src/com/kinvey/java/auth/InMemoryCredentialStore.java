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

import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.HashMap;

/**
 * @author m0rganic
 * @since 2.0
 */
public class InMemoryCredentialStore implements CredentialStore {

    private final Map<String, Credential> store = new HashMap<String, Credential>();

    @Override
    public Credential load(String userId) {
        return store.get(userId);
    }

    @Override
    public void store(String userId, Credential credential) {
        Preconditions.checkNotNull(credential, "credential must not be null");

        Credential cred = new Credential(userId, credential.getAuthToken());
        if (userId != null) {
            store.put(userId, cred);
        }
    }

    @Override
    public void delete(String userId) {
        if (userId != null) {
            store.remove(userId);
        }
    }
}

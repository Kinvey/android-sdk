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

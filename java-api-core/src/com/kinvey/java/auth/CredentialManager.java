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

package com.kinvey.java.auth;

import java.io.IOException;

/**
 * @author m0rganic
 * @since 2.0
 */
public class CredentialManager {

    private CredentialStore credentialStore;

    private CredentialManager() {

    }

    public CredentialManager(CredentialStore credentialStore)  {
        if (credentialStore == null) {
            this.credentialStore = new InMemoryCredentialStore();
        } else {
            this.credentialStore = credentialStore;
        }
    }

    /**
     *
     * @param userId a valid userId to retrieve the credential from storage
     * @return a credential object if one is found otherwise {@code null} is returned
     * @throws IOException error in access the low-level storage mechanism
     */
    public Credential loadCredential (String userId) throws IOException {
        if (credentialStore == null) {
            return null;
        }
        return credentialStore.load(userId);
    }

    /**
     *
     * @param userId unique string to hash the credential with
     * @param credential a credential object to store
     * @throws IOException error accessing the low-level storage mechanism
     */
    public void makePersistent (String userId, Credential credential) throws IOException {
        if (credentialStore == null) {
            return;
        }

        credentialStore.store(userId,  credential);
    }

    /**
     * @param response response received from a new token request
     * @param userId username or {@code null} if no persistent store is being used
     * @return new credential object
     */
    public Credential createAndStoreCredential (KinveyAuthResponse response, String userId) throws IOException {
        Credential newCredential = Credential.from(response);
        if (userId != null && credentialStore != null) {
            credentialStore.store(userId, newCredential);
        }

        return newCredential;
    }

    public void removeCredential (String userId)  {
        if (credentialStore != null) {
            credentialStore.delete(userId);
        }
    }
}

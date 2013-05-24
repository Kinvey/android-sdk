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

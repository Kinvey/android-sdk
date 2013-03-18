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
 * A mechanism to store, retrieve and purge credentials from memory and disk
 */
public interface CredentialStore {

    /**
     * @param userId a unique identifier for the stored credential
     * @return a credential object retrieved from storage otherwise {@code null} is returned
     * @throws IOException error in retrieving from low-level storage mechanism
     */
    Credential load (String userId) throws IOException;

    /**
     * @param userId a unique identifier to index the credential in storage
     * @param credential non-null credential to store
     * @throws IOException error in storing to low-level storage mechanism
     */
    void store (String userId, Credential credential) throws IOException;

    /**
     * @param userId the unique identifier to the credential to purge
     * @throws IOException error in purging the credential from low-level storage
     */
    void delete (String userId);

}

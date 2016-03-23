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

package com.kinvey.java.store.requests.user;

import com.kinvey.java.auth.CredentialManager;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.store.UserStore;

/**
 * Logout Request Class.  Constructs the HTTP request object for Logout requests.
 */
public final class LogoutRequest {

    private UserStore userStore;
    private CredentialStore store;

    public LogoutRequest(UserStore userStore, CredentialStore store){
        this.userStore = userStore;
        this.store = store;
    }

    public void execute() {
        CredentialManager manager = new CredentialManager(this.store);
        manager.removeCredential(userStore.getCurrentUser().getId());
        userStore.setCurrentUser(null);
        ((KinveyClientRequestInitializer) userStore.getClient().getKinveyRequestInitializer()).setCredential(null);
    }
}

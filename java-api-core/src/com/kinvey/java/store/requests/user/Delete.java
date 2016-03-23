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

import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.store.UserStore;

import java.io.IOException;

/**
 * Delete Request Class, extends AbstractKinveyJsonClientRequest<Void>.  Constructs the HTTP request object for
 * Delete User requests.
 */
public final class Delete extends AbstractKinveyJsonClientRequest<Void> {
    private static final String REST_PATH = "user/{appKey}/{userID}?hard={hard}";

    private UserStore userStore;
    @Key
    private boolean hard = false;

    @Key
    private String userID;

    public Delete(UserStore userStore, String userID, boolean hard) {
        super(userStore.getClient(), "DELETE", REST_PATH, null, Void.class);
        this.userStore = userStore;
        this.userID = userID;
        this.hard = hard;
        this.getRequestHeaders().put("X-Kinvey-Client-App-Version", userStore.getClientAppVersion());
        if (userStore.getCustomRequestProperties() != null && !userStore.getCustomRequestProperties().isEmpty()){
            this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(userStore.getCustomRequestProperties()) );
        }
    }

    @Override
    public Void execute() throws IOException {
        super.execute();
        userStore.removeFromStore(userID);
        userStore.logout();

        return null;
    }
}

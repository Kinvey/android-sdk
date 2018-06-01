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
import com.kinvey.java.dto.BaseUser;
import com.kinvey.java.dto.PasswordRequest;
import com.kinvey.java.store.UserStoreRequestManager;

import java.io.IOException;

/**
 * Update Request Class, extends AbstractKinveyJsonClientRequest<BaseUser>.  Constructs the HTTP request object for
 * Update BaseUser requests.
 */
public final class Update<T extends BaseUser> extends AbstractKinveyJsonClientRequest<T> {
    private static final String REST_PATH = "user/{appKey}/{userID}";

    private UserStoreRequestManager<T> userStoreRequestManager;
    @Key
    private String userID;

    public Update(UserStoreRequestManager<T> userStoreRequestManager, BaseUser baseUser, Class<T> userClass) {
        super(userStoreRequestManager.getClient(), "PUT", REST_PATH, baseUser, userClass);
        this.userStoreRequestManager = userStoreRequestManager;
        this.userID = baseUser.getId();
        this.getRequestHeaders().put("X-Kinvey-Client-App-Version", userStoreRequestManager.getClientAppVersion());
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()){
            this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(userStoreRequestManager.getCustomRequestProperties()) );
        }
    }

    public Update(UserStoreRequestManager<T> userStoreRequestManager, BaseUser baseUser, PasswordRequest passwordRequest, Class<T> userClass) {
        super(userStoreRequestManager.getClient(), "PUT", REST_PATH, passwordRequest, userClass);
        this.userStoreRequestManager = userStoreRequestManager;
        this.userID = baseUser.getId();
        this.getRequestHeaders().put("X-Kinvey-Client-App-Version", userStoreRequestManager.getClientAppVersion());
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()){
            this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(userStoreRequestManager.getCustomRequestProperties()) );
        }
    }

    public Update(UserStoreRequestManager<T> userStoreRequestManager, String userId, Class<T> userClass) {
        super(userStoreRequestManager.getClient(), "PUT", REST_PATH, null, userClass);
        this.userStoreRequestManager = userStoreRequestManager;
        this.userID = userId;
        this.getRequestHeaders().put("X-Kinvey-Client-App-Version", userStoreRequestManager.getClientAppVersion());
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()){
            this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(userStoreRequestManager.getCustomRequestProperties()) );
        }
    }

    @Override
    public T execute() throws IOException {

        T updatedUser = super.execute();

        if (updatedUser.getId() == null || updatedUser.getId() == null){
            return updatedUser;
        }

        if (updatedUser.getId().equals(userID)) {
            return userStoreRequestManager.initUser(updatedUser);
        } else {
            return updatedUser;
        }
    }
}

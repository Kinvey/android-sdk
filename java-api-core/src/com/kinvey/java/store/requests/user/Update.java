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

import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.kinvey.java.auth.KinveyAuthResponse;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStoreRequestManager;

import java.io.IOException;

/**
 * Update Request Class, extends AbstractKinveyJsonClientRequest<User>.  Constructs the HTTP request object for
 * Update User requests.
 */
public final class Update<T extends User> extends AbstractKinveyJsonClientRequest<User> {
    private static final String REST_PATH = "user/{appKey}/{userID}";

    private UserStoreRequestManager<T> userStoreRequestManager;
    @Key
    private String userID;

    public Update(UserStoreRequestManager<T> userStoreRequestManager, User user, Class<User> myClass) {
        super(userStoreRequestManager.getClient(), "PUT", REST_PATH, user, myClass);
        this.userStoreRequestManager = userStoreRequestManager;
        this.userID = user.getId();
        this.getRequestHeaders().put("X-Kinvey-Client-App-Version", userStoreRequestManager.getClientAppVersion());
        if (userStoreRequestManager.getCustomRequestProperties() != null && !userStoreRequestManager.getCustomRequestProperties().isEmpty()){
            this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(userStoreRequestManager.getCustomRequestProperties()) );
        }

    }

    public User execute() throws IOException {

        User u = super.execute();

        if (u.getId() == null || u.getId() == null){
            return u;
        }

        if (u.getId().equals(userID)){
            KinveyAuthResponse auth = new KinveyAuthResponse();
            auth.put("_id", u.get("_id"));
            KinveyAuthResponse.KinveyUserMetadata kmd = new KinveyAuthResponse.KinveyUserMetadata();
            kmd.put("lmt", u.get("_kmd.lmt")) ;
            kmd.put("authtoken", u.get("_kmd.authtoken"));
            kmd.putAll((ArrayMap) u.get("_kmd"));
            auth.put("_kmd", kmd);
            auth.put("username", u.get("username"));
            for (Object key : u.keySet()){
                if (!key.toString().equals("_kmd")){
                    auth.put(key.toString(), u.get(key));
                }
            }
            String userType = userStoreRequestManager.getClient().getClientUsers().getCurrentUserType();
            return userStoreRequestManager.initUser(auth, userType, u);
        }else{
            return u;
        }
    }
}

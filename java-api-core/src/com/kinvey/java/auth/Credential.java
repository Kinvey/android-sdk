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

import com.kinvey.java.User;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.KinveyRequestInitializer;

import java.io.IOException;

/**
 * @author m0rganic
 * @since 2.0
 */
public class Credential implements KinveyRequestInitializer, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final static String AUTH_N_HEADER_FORMAT = "Kinvey %s";

    private String userId;

    private String authToken;

    /** package **/ Credential() {}

    /** package **/ Credential(String userId, String authToken) {
        this.userId = userId;
        this.authToken = authToken;
    }

    @Override
    public void initialize(AbstractKinveyClientRequest<?> request) {
        // execute the original intent for this interceptor
        if (authToken != null) {
            request.getRequestHeaders().setAuthorization(String.format(AUTH_N_HEADER_FORMAT, authToken));
        }
    }

    /**
     * Convenience method intended to shield calling code from having to deal with KinveyAuthResponse directly
     *
     * @param response a valid response from the Kinvey authentication
     * @return a newly constructed Credential object
     */
    public static Credential from(KinveyAuthResponse response){
        return new Credential(response.getUserId(), response.getAuthToken());
    }

    public static Credential from(User user){
        return new Credential(user.getId(), user.get("authToken").toString());
    }

    public String getUserId() {
        return userId;
    }

    /** package **/ void setUserId (String userid) {
        this.userId = userid;
    }

    public String getAuthToken() {
        return authToken;
    }

    /** package **/ void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}

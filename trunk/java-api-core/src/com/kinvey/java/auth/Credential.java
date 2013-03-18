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

import com.kinvey.java.User;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.KinveyRequestInitializer;

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
        return new Credential(user.getId(), user.get("authKey").toString());
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

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

import com.google.api.client.json.GenericJson;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.KinveyRequestInitializer;
import com.kinvey.java.dto.BaseUser;

import java.util.Map;

/**
 * @author m0rganic
 * @since 2.0
 */
public class Credential implements KinveyRequestInitializer, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final static String AUTH_N_HEADER_FORMAT = "Kinvey %s";
    private final static String AUTH_TOKEN = "authtoken";
    private final static String KMD = "_kmd";

    private String userId;

    private String clientId;

    private String authToken;
    
    private String refreshToken;

    /** package **/ Credential() {}

    public Credential(String userId, String authToken, String refresh) {
        this.userId = userId;
        this.authToken = authToken;
        this.refreshToken = refresh;
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
        return from(response.getUserId(), response.getAuthToken());
    }

    /**
     * @param baseUser authorised user for saving auth token
     * @return a newly constructed Credential object
     */
    public static Credential from(BaseUser baseUser){
        Map<String, String> kmd = (Map<String, String>)baseUser.get(KMD);
        return from(baseUser.getId(), kmd != null ? kmd.get(AUTH_TOKEN) : null);
    }

    /**
     * @param userId user id for saving
     * @param authToken authToken for saving
     * @return a newly constructed Credential object
     */
    public static Credential from(String userId, String authToken){
        return new Credential(userId, authToken, null);
    }

    public String getUserId() {
        return userId;
    }

    protected void setUserId (String userid) {
        this.userId = userid;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAuthToken() {
        return authToken;
    }

    protected void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public String getRefreshToken(){
    	return this.refreshToken;
    }
    
    public void setRefreshToken(String newToken){
    	this.refreshToken = newToken;
    }
}

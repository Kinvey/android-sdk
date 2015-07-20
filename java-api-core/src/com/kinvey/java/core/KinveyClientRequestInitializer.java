/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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
package com.kinvey.java.core;


import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.HttpHeaders;
import com.google.common.base.Preconditions;
import com.kinvey.java.auth.Credential;

import java.util.logging.Logger;

/**
 * @author m0rganic
 */
public class KinveyClientRequestInitializer implements KinveyRequestInitializer {

    /** the app key for the request **/
    private final String appKey;

    /** the app secret for the request **/
    private final String appSecret;

    /** authorization context for the request **/
    private Credential credential;

    /** standard headers used across all of the kinvey api **/
    private final KinveyHeaders kinveyHeaders;

    /**
     * @param appKey application key, will be set on the request
     * @param appSecret application secret, used for user management methods
     * @param kinveyHeaders
     */
    public KinveyClientRequestInitializer(String appKey, String appSecret, KinveyHeaders kinveyHeaders) {
        this(appKey, appSecret, null, kinveyHeaders);
    }

    /**
     *
     * @param appKey the key to set on the request
     * @param appSecret application secret, used for user management methods
     * @param credential the authorization context for the request
     * @param kinveyHeaders
     */
    public KinveyClientRequestInitializer(String appKey, String appSecret, Credential credential, KinveyHeaders kinveyHeaders) {
        this.credential = credential;
        this.kinveyHeaders = kinveyHeaders;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    /**
     * @return the appKey
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * @return the appSecret
     */
    public String getAppSecret() {
        return appSecret;
    }

    /**
     * @return Kinvey Headers configured for this request initializer
     */
    public KinveyHeaders getKinveyHeaders(){return kinveyHeaders;}

    /**
     *
     * @param credential valid authorization context obtained from {@link com.kinvey.java.auth.KinveyAuthRequest}
     * @return client request initializer
     */
    public KinveyClientRequestInitializer setCredential(Credential credential) {
        this.credential = credential;
        return this;
    }

    /**
     * Sets the authentication header using credential, appkey is set and kinvey standard
     * headers are added to the request.
     *
     * @param request the request to initialize
     */
    public void initialize(AbstractKinveyClientRequest<?> request) {
        if (!request.isRequireAppCredentials()){
            Preconditions.checkNotNull(credential, "No Active User - please login a user by calling myClient.user().login( ... ) before retrying this request.");
            Preconditions.checkNotNull(credential.getUserId(), "No Active User - please login a user by calling myClient.user().login( ... ) before retrying this request.");
            Preconditions.checkNotNull(credential.getAuthToken(), "No Active User - please login a user by calling myClient.user().login( ... ) before retrying this request.");
        }

        if (credential != null && !request.isRequireAppCredentials()) {
            credential.initialize(request);
        }

        if (request.isRequireAppCredentials()){
            request.getRequestHeaders().setBasicAuthentication(getAppKey(), getAppSecret());
        }

        request.setAppKey(appKey);
        request.getRequestHeaders().putAll(kinveyHeaders);
    }

}

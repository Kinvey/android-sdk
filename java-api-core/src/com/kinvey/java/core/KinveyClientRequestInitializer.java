/*
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
package com.kinvey.java.core;


import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.HttpHeaders;
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

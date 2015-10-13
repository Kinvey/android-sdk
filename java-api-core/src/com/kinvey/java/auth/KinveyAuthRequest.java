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
package com.kinvey.java.auth;

import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.ExponentialBackOffPolicy;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

import java.io.EOFException;
import java.io.IOException;

import com.kinvey.java.Logger;
import com.kinvey.java.core.KinveyHeaders;
import com.kinvey.java.core.KinveyJsonResponseException;

/**
 * @author m0rganic
 * @since 2.0
 */
public class KinveyAuthRequest extends GenericJson {


    public enum LoginType {
        IMPLICIT,
        KINVEY,
        THIRDPARTY
    }
    /**
     * used to construct the request body
     */
    private static class AuthRequestPayload extends GenericJson {

        @Key
        private final String username;

        @Key
        private final String password;

        public AuthRequestPayload(String username, String passwd) {
            this.username = username;
            this.password = passwd;
        }
    }

    private boolean create;
    private LoginType type;
    private int eofRetryAttempts =3;
    //TODO:  Set eofRetryAttempts value as builder method

    /**
     * http transport to utilize for the request *
     */
    private final HttpTransport transport;

    /**
     * json factory from with the object parser is built *
     */
    private final JsonFactory jsonFactory;


    /** kcs base url **/
    private final  String baseUrl;

    /** backoff policy to use **/
    private BackOffPolicy policy;

    /**
     * appkey and secret RequestInitializer for the initial POST to the user endpoint *
     */
    private final BasicAuthentication appKeyAuthentication;

    /**
     * used to formulate the uri in {@link #buildHttpRequestUrl()} *
     */
    @Key
    private final String appKey;

    /**
     * payload containing the json object Kinvey expects *
     */
    private final GenericJson requestPayload;

    /**
     * the response returned from the Kinvey server on {@link #executeUnparsed()} *
     */
    private HttpResponse response;

    /**
     * standard headers included in all requests
     */
    private KinveyHeaders kinveyHeaders;

    public void setKinveyHeaders(KinveyHeaders headers){
        this.kinveyHeaders = headers;
    }

    /**
     * Keep protected for testing support.
     *
     * @param transport            http transport layer
     * @param jsonFactory          json object parser factory
     * @param baseUrl
     * @param appKeyAuthentication app key and secret used to initialize the auth request
     * @param username             user provided username or {@code null} if none is known
     * @param password             password for the user or {@code null} if none is known
     */
    protected KinveyAuthRequest(HttpTransport transport, JsonFactory jsonFactory,
                                String baseUrl, BasicAuthentication appKeyAuthentication, String username, String password,
                                GenericJson user, boolean create) {
        this.transport = transport;
        this.jsonFactory = jsonFactory;
        this.baseUrl = baseUrl;
        this.appKeyAuthentication = appKeyAuthentication;
        this.appKey = appKeyAuthentication.getUsername();
        this.requestPayload = (username == null || password == null) ? new GenericJson() : new AuthRequestPayload(username, password);
        if (user != null) {
            this.requestPayload.putAll(user);
        }
        this.create = create;
        this.type = requestPayload == null ? LoginType.IMPLICIT : LoginType.KINVEY;
        this.policy = new ExponentialBackOffPolicy(); // TODO:  No current access to the client, so should add to the Constructor at some point.  For now, set to Expontential Backoff Policy.
    }

    protected KinveyAuthRequest(HttpTransport transport, JsonFactory jsonFactory,
                                String baseUrl, BasicAuthentication appKeyAuthentication, ThirdPartyIdentity thirdPartyIdentity,
                                GenericJson user, boolean create) {
        this.transport = transport;
        this.jsonFactory = jsonFactory;
        this.appKeyAuthentication = appKeyAuthentication;
        this.baseUrl = baseUrl;
        this.appKey = appKeyAuthentication.getUsername();
        this.requestPayload = thirdPartyIdentity;
        if (user != null) {
        	for(String key : user.keySet()){        		
        		if (!key.equals("_kmd") && !key.equals("access_token") && !key.equals("_socialIdentity")){
        			this.requestPayload.put(key, user.get(key));		
        		}
        	}
        }
        this.create = create;
        this.type=LoginType.THIRDPARTY;
        this.policy = new ExponentialBackOffPolicy(); // TODO:  No current access to the client, so should add to the Constructor at some point.  For now, set to Expontential Backoff Policy.

    }

    /**
     * @return properly formed url for submitting requests to Kinvey authentication module
     */
    private GenericUrl buildHttpRequestUrl() {
        // we hit different end points depending on whether 3rd party auth is utilize
        //TODO: change this.create boolean to a parameterized string, representing "login" portion of the url
        return new GenericUrl(UriTemplate.expand(baseUrl
                , "/user/{appKey}/" + (this.create ? "" : "login")
                , this
                , false));
    }


    /**
     * @return low level http response
     */
    public HttpResponse executeUnparsed() throws IOException {

        HttpContent content = (this.requestPayload != null) ? new JsonHttpContent(jsonFactory, this.requestPayload) : null;
        HttpRequest request = transport.createRequestFactory(appKeyAuthentication)
                .buildPostRequest(buildHttpRequestUrl(), content)
                .setSuppressUserAgentSuffix(true)
                .setThrowExceptionOnExecuteError(false)
                .setParser(jsonFactory.createJsonObjectParser())
                .setBackOffPolicy(policy)
                .setRetryOnExecuteIOException(true);
        if (kinveyHeaders != null) {
            request.getHeaders().putAll(kinveyHeaders);
        }

        try {
            response = request.execute();
        }  catch (EOFException ex) {
            if (eofRetryAttempts > 0) {
                eofRetryAttempts--;
                return executeUnparsed();
            } else {
                throw ex;
            }
        }
        if (response.isSuccessStatusCode()) {
            return response;
        } else if (response.getStatusCode() == 404 && this.type == LoginType.THIRDPARTY && this.create == false) {
            this.create = true;
            return executeUnparsed();
        }
        throw KinveyJsonResponseException.from(jsonFactory, response);
    }

    public KinveyAuthResponse execute() throws IOException {
        return executeUnparsed().parseAs(KinveyAuthResponse.class);
    }



    /**
     * Used to construct a {@link KinveyAuthRequest}. The result will be an auth request that adjusts for the
     * authentication scenario.
     * <p>
     * There are three scenarios that this builder will support for Kinvey authentication.
     * </p>
     * <p>
     * The first is where the user is known
     * and the both the username and password have been provided.
     * </p>
     *
     * <pre>
     *      KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,baseUrl, appKey,appSecret)
     *          .setUserIdAndPassword(userid, password)
     *          .build()
     *          .execute();
     * </pre>
     * <p/>
     * <p>
     * The second is where the user has established their identity with one of the supported 3rd party authentication
     * systems like Facebook, LinkedInCredential, etc.
     * </p>
     * <pre>
     *      KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,baseUrl, appKey,appSecret)
     *              .setThirdPartyAuthToken(thirdpartytoken)
     *              .build()
     *              .execute();
     * </pre>
     * <p/>
     * <p>
     * The third and final way authentication can occur is with just the appKey and appSecret, in this case, an
     * implicit user is created when the {@link com.kinvey.java.auth.KinveyAuthRequest#execute()} is called.
     * </p>
     * <p/>
     * <pre>
     *      KinveyAuthResponse response = new KinveyAuthRequest.Builder(transport,jsonfactory,baseUrl, appKey,appSecret)
     *              .build()
     *              .execute();
     * </pre>
     */
    public static class Builder {

        private final HttpTransport transport;

        private final JsonFactory jsonFactory;

        private final BasicAuthentication appKeyAuthentication;

        private Boolean create = false;

        private String username;

        private GenericJson user;

        private String password;
        private boolean isThirdPartyAuthUsed = false;
        private ThirdPartyIdentity thirdPartyIdentity;
        private final String baseUrl;

        public Builder(HttpTransport transport, JsonFactory jsonFactory, String baseUrl, String appKey, String appSecret,
                       GenericJson user) {
            this.transport = Preconditions.checkNotNull(transport);
            this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
            this.baseUrl = baseUrl;
            this.appKeyAuthentication = new BasicAuthentication(Preconditions.checkNotNull(appKey), Preconditions.checkNotNull(appSecret));
            this.user = user;
        }

        public Builder(HttpTransport transport, JsonFactory jsonFactory, String baseUrl, String appKey, String appSecret,
                       String username, String password, GenericJson user) {
            this(transport, jsonFactory, baseUrl, appKey, appSecret, user);
            Preconditions.checkArgument(!"".equals(username), "username cannot be empty, use null if no username is known");
            Preconditions.checkArgument(!"".equals(password), "password cannot be empty, use null if no password is known");
            this.username = username;
            this.password = password;
        }

        public Builder(HttpTransport transport, JsonFactory jsonFactory, String baseUrl, String appKey, String appSecret,
                       ThirdPartyIdentity identity, GenericJson user) {
            this(transport, jsonFactory, baseUrl, appKey, appSecret, user);
            Preconditions.checkNotNull(identity, "identity must not be null");
            this.isThirdPartyAuthUsed = (identity != null);
            this.thirdPartyIdentity = identity;
        }

        public KinveyAuthRequest build() {
            if (!isThirdPartyAuthUsed) {
                return new KinveyAuthRequest(getTransport()
                        , getJsonFactory()
                        , getBaseUrl(), getAppKeyAuthentication()
                        , getUsername()
                        , getPassword(),user, this.create);
            }
            return new KinveyAuthRequest(getTransport()
                    , getJsonFactory()
                    , getBaseUrl(), getAppKeyAuthentication()
                    , getThirdPartyIdentity(),user, this.create);
        }

        public Builder setUsernameAndPassword(String username, String password) {
            this.username = username;
            this.password = password;
            isThirdPartyAuthUsed = false;
            return this;
        }

        public Builder setThirdPartyIdentity(ThirdPartyIdentity identity) {
            thirdPartyIdentity = identity;
            isThirdPartyAuthUsed = true;
            return this;
        }

        public Builder setCreate(boolean create) {
            this.create = create;
            return this;
        }

        public Builder setUser(GenericJson user) {
            this.user = user;
            return this;
        }

        protected GenericJson getUser() {
            return user;
        }

        public ThirdPartyIdentity getThirdPartyIdentity() {
            return thirdPartyIdentity;
        }

        /**
         * @return the http trasport
         */
        public HttpTransport getTransport() {
            return transport;
        }

        /**
         * @return json factory
         */
        public JsonFactory getJsonFactory() {
            return jsonFactory;
        }

        /**
         * @return appkey and appsecret RequestInitializer used to create the BasicAuthentication for the POST request
         */
        public BasicAuthentication getAppKeyAuthentication() {
            return appKeyAuthentication;
        }

        /**
         * @return username or {@code null} in none is set
         */
        public final String getUsername() {
            return username;
        }

        /**
         * @return password or {@code null} if none is set
         */
        public final String getPassword() {
            return password;
        }

        /**
         * @param username uniquely identifies the user
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * @param password user provided password for the authentication request
         */
        public void setPassword(String password) {
            this.password = password;
        }

        protected boolean getThirdPartyAuthStatus() {
            return isThirdPartyAuthUsed;
        }

        protected boolean getCreate() {
            return create;
        }

        public String getBaseUrl() {
            return baseUrl;
        }
    }
}

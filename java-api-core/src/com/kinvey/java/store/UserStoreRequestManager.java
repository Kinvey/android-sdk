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

package com.kinvey.java.store;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.GenericData;
import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.CredentialManager;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.auth.KinveyAuthResponse;
import com.kinvey.java.auth.ThirdPartyIdentity;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.Email;
import com.kinvey.java.dto.PasswordRequest;
import com.kinvey.java.dto.User;
import com.kinvey.java.dto.Username;
import com.kinvey.java.store.requests.user.Delete;
import com.kinvey.java.store.requests.user.EmailVerification;
import com.kinvey.java.store.requests.user.ForgotUsername;
import com.kinvey.java.store.requests.user.GetMICAccessToken;
import com.kinvey.java.store.requests.user.GetMICTempURL;
import com.kinvey.java.store.requests.user.LockDownUser;
import com.kinvey.java.store.requests.user.LoginToTempURL;
import com.kinvey.java.store.requests.user.LogoutRequest;
import com.kinvey.java.store.requests.user.ResetPassword;
import com.kinvey.java.store.requests.user.Retrieve;
import com.kinvey.java.store.requests.user.RetrieveUsers;
import com.kinvey.java.store.requests.user.Update;
import com.kinvey.java.store.requests.user.UserExists;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Prots on 2/12/16.
 */
public class UserStoreRequestManager {


    public static final String USER_COLLECTION_NAME = "user";

    public KinveyAuthRequest.Builder getBuilder() {
        return builder;
    }


    public enum LoginType {
        IMPLICIT,
        KINVEY,
        GOOGLE,
        TWITTER,
        FACEBOOK,
        LINKED_IN,
        AUTH_LINK,
        MOBILE_IDENTITY,
        CREDENTIALSTORE,
        SALESFORCE,
        THIRDPARTY
    }

    private final AbstractClient client;
    private final KinveyAuthRequest.Builder builder;
    private final Class<User> myClazz;
    private final String clientAppVersion;
    private final GenericData customRequestProperties;
    private String authToken;

    public String getMICRedirectURI() {
        return MICRedirectURI;
    }

    public void setMICRedirectURI(String MICRedirectURI) {
        this.MICRedirectURI = MICRedirectURI;
    }

    /**
     * the redirect URI for MIC
     */
    private String MICRedirectURI;


    public GenericData getCustomRequestProperties() {
        return client.getCustomRequestProperties();
    }

    public String getClientAppVersion() {
        return client.getClientAppVersion();
    }

    public AbstractClient getClient() {
        return client;
    }

    public UserStoreRequestManager(AbstractClient client, KinveyAuthRequest.Builder builder){
        Preconditions.checkNotNull(client, "client must not be null.");
        Preconditions.checkNotNull(builder, "KinveyAuthRequest.Builder should not be null");
        this.client = client;
        this.builder = builder;
        this.myClazz = User.class;
        this.builder.setUser(client.getUser());
        this.clientAppVersion = client.getClientAppVersion();
        this.customRequestProperties = client.getCustomRequestProperties();
    }


    /**
     * Method to initialize the User after login, create a credential,
     * and add it to the KinveyClientRequestInitializer
     *
     * @param response KinveyAuthResponse object containing the login response
     * @throws IOException
     */
    public User initUser(KinveyAuthResponse response, User userObject) throws IOException {

        userObject.setId(response.getUserId());
        userObject.put("_kmd", response.getMetadata());
        userObject.putAll(response.getUnknownKeys());
        User currentUser = new User();
        currentUser.setId(response.getUserId());
        currentUser.put("_kmd", response.getMetadata());
        currentUser.putAll(response.getUnknownKeys());
        if (response.containsKey("username")){
            currentUser.setUsername(response.get("username").toString());
        }
        currentUser.setAuthToken(response.getAuthToken());

        CredentialManager credentialManager = new CredentialManager(client.getStore());
        ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer())
                .setCredential(credentialManager.createAndStoreCredential(response, userObject.getId()));
        client.getClientUser().setUser(currentUser.getId());
        client.setUser(currentUser);
        return currentUser;
    }

    private User initUser(Credential credential, User userObject) {
        User currentUser = new User();
        currentUser.setId(credential.getUserId());
        currentUser.setAuthToken(credential.getAuthToken());
        userObject.setId(credential.getUserId());
        userObject.setAuthToken(credential.getAuthToken());
        client.setUser(currentUser);
        return currentUser;
    }

    public void removeFromStore(String userID) {
        CredentialManager credentialManager = new CredentialManager(client.getStore());
        credentialManager.removeCredential(userID);
    }


    /**
     * Login with the implicit user.  If implicit user does not exist, the user is created.  After calling this method,
     * the application should retrieve and store the userID using getId()
     *
     * @return LoginRequest object
     * @throws IOException
     */
    public LoginRequest loginBlocking() throws IOException {
        return new LoginRequest().buildAuthRequest();
    }

    /**
     * Login with Kinvey user and password.   If user does not exist, returns a error response.
     *
     * @param username userID of Kinvey User
     * @param password password of Kinvey user
     * @return LoginRequest object
     * @throws IOException
     */
    public LoginRequest loginBlocking(String username, String password) throws IOException {
        Preconditions.checkNotNull(username, "Username cannot be null.");
        Preconditions.checkNotNull(password, "Password cannot be null.");
        return new LoginRequest(username, password, false).buildAuthRequest();
    }

    /**
     * Method to login via third party OAuth credentials
     *
     * @param thirdPartyType ThirdPartyIdentity Type enum
     * @param args Associated Keys for OAuth login
     *             OAuth 2 providers (Google, Facebook) AccessToken
     *             OAuth 1a providers (LinkedIn, Twitter) Access Token, Access Secret, Consumer Key, Consumer Secret
     * @return LoginRequest object
     * @throws IOException
     */
    public LoginRequest login(ThirdPartyIdentity.Type thirdPartyType, String... args) throws IOException {
        Preconditions.checkNotNull((args));
        ThirdPartyIdentity identity = ThirdPartyIdentity.createThirdPartyIdentity(thirdPartyType, args);
        return new LoginRequest(identity).buildAuthRequest();
    }

    /**
     * Log in with existing credential
     *
     * @param credential
     * @return LoginRequest object
     * @throws IOException
     */
    public LoginRequest login(Credential credential) {
        return new LoginRequest(credential).buildAuthRequest();
    }

    /**
     * Convenience Method to retrieve Metadata.
     *
     * @return Current user object with refreshed metadata
     * @throws IOException
     */
    public User retrieveMetadataBlocking() throws IOException {
        User ret = this.retrieveBlocking().execute();
        User currentUser = null;
        if (client.getUser() == null){
            currentUser = new User();
        } else {
            currentUser = client.getUser();
        }
        currentUser.putAll(ret.getUnknownKeys());
        currentUser.setUsername(ret.getUsername());
        client.setUser(currentUser);
        return currentUser;
    }

    /**
     * Login to Kinvey services using Facebook access token obtained through OAuth2.  If the user does not exist in the
     * Kinvey service, the user will be created.
     *
     * @param accessToken Facebook-generated access token.
     * @return LoginRequest Object
     * @throws IOException
     */
    public LoginRequest loginFacebookBlocking(String accessToken) throws IOException {
        return login(ThirdPartyIdentity.Type.FACEBOOK, accessToken);
    }

    /**
     * Login to Kinvey services using Google access token obtained through OAuth2.  If the user does not exist in the
     * Kinvey service, the user will be created.
     *
     * @param accessToken Google-generated access token
     * @return LoginRequest Object
     * @throws IOException
     */
    public LoginRequest loginGoogleBlocking(String accessToken) throws IOException {
        return login(ThirdPartyIdentity.Type.GOOGLE, accessToken);
    }

    /**
     * Login to Kinvey services using SalesForce access token obtained through OAuth2.  If the user does not exist in the
     * Kinvey service, the user will be created.
     *
     * @param accessToken SalesForce-generated access token
     * @return LoginRequest Object
     * @throws IOException
     */
    public LoginRequest loginSalesForceBlocking(String accessToken, String Clientid, String refreshToken, String id) throws IOException {
        return login(ThirdPartyIdentity.Type.SALESFORCE, accessToken, Clientid, refreshToken, id);
    }

    /**
     * Login to Kinvey services using Twitter-generated access token, access secret, consumer key, and consumer secret
     * obtained through OAuth1a.  If the user does not exist in the Kinvey service, the user will be created.
     *
     * @param accessToken Twitter-generated access token
     * @param accessSecret Twitter-generated access secret
     * @param consumerKey Twitter-generated consumer key
     * @param consumerSecret Twitter-generated consumer secret
     * @return LoginRequest Object
     * @throws IOException
     */
    public LoginRequest loginTwitterBlocking(String accessToken, String accessSecret, String consumerKey, String consumerSecret)
            throws IOException {
        return login(ThirdPartyIdentity.Type.TWITTER,
                accessToken, accessSecret, consumerKey, consumerSecret);
    }


    /**
     * Login to Kinvey services using a Kinvey user's _id and their valid Kinvey Auth Token.  This method is provided
     * to allow for cross-platform login, by reusing a session provided with another client library (or the REST api).
     *
     * @param userId the _id field of the user to login
     * @param authToken a valid Kinvey Auth token
     * @return a LoginRequest ready to be executed
     * @throws IOException
     */
    public LoginRequest loginKinveyAuthTokenBlocking(String userId, String authToken) throws IOException{
        User currentUser = new User();
        currentUser.setAuthToken(authToken);
        currentUser.setId(userId);
        Credential c = Credential.from(currentUser);
        client.setUser(currentUser);
        return login(c);

    }

    /***
     * Login to Kinvey Services using Mobile Identity Connect
     *
     * @param authToken
     * @return
     * @throws IOException
     */
    public LoginRequest loginMobileIdentityBlocking(String authToken) throws IOException {
        return login(ThirdPartyIdentity.Type.MOBILE_IDENTITY, authToken);

    }

    /**
     * Login to Kinvey services using LinkedIn-generated access token, access secret, consumer key, and consumer secret
     * obtained through OAuth1a.  If the user does not exist in the Kinvey service, the user will be created.
     *
     * @param accessToken Linked In generated access token
     * @param accessSecret Linked In generated access secret
     * @param consumerKey Linked In generated consumer key
     * @param consumerSecret Linked In generated consumer secret
     * @return LoginRequest Object
     * @throws IOException
     */
    public LoginRequest loginLinkedInBlocking(String accessToken, String accessSecret, String consumerKey, String consumerSecret)
            throws IOException {
        return login(ThirdPartyIdentity.Type.LINKED_IN, accessToken, accessSecret, consumerKey, consumerSecret);
    }

    public LoginRequest loginAuthLinkBlocking (String accessToken, String refreshToken) throws IOException {
        return login(ThirdPartyIdentity.Type.AUTH_LINK, accessToken, refreshToken);
    }

    /**
     * Logs the user out of the current app
     *
     * @return LogoutRequest object
     * @throws IOException
     */
    public LogoutRequest logout() {
        return new LogoutRequest(client);
    }


    /**
     * Creates an explicit Kinvey User.
     *
     * @param username userName of Kinvey user
     * @param password password of Kinvey user
     * @return LoginRequest Object
     * @throws IOException
     */
    public LoginRequest createBlocking(String username, String password) throws IOException {
        return new LoginRequest(username, password, true).buildAuthRequest();
    }


    /**
     * Delete's the given user from the server.
     *
     * @param hardDelete if true, physically deletes the user. If false, marks user as inactive.
     * @return Delete Request
     * @throws IOException
     */
    public Delete deleteBlocking(boolean hardDelete) throws IOException {

        Preconditions.checkNotNull(client.getUser(), "currentUser must not be null");
        Preconditions.checkNotNull(client.getUser().getId(), "currentUser ID must not be null");
        Delete delete = new Delete(this, client.getUser().getId(), hardDelete);
        client.initializeRequest(delete);
        return delete;
    }

    /**
     * Retrieves current user's metadata.
     *
     * @return Retrieve Request
     * @throws IOException
     */
    public Retrieve retrieveBlocking() throws IOException{
        Preconditions.checkNotNull(client.getUser(), "currentUser must not be null");
        Preconditions.checkNotNull(client.getUser().getId(), "currentUser ID must not be null");
        Retrieve retrieve = new Retrieve(this, client.getUser().getId());
        client.initializeRequest(retrieve);
        return retrieve;
    }

    /**
     * Retrieves an array of User[] based on a Query.
     *
     * @return a Retrieve Request ready to be executed
     * @throws IOException
     */
    public RetrieveUsers retrieveBlocking(Query query) throws IOException{
        RetrieveUsers retrieve = new RetrieveUsers(this, query);
        client.initializeRequest(retrieve);
        return retrieve;
    }

    /**
     * Retrieve current user's metadata with support for resolving KinveyReferences
     *
     * @param resolves - List of {@link com.kinvey.java.model.KinveyReference} fields to resolve
     * @return a Retrieve Request ready to be executed
     * @throws IOException
     */
    public Retrieve retrieveBlocking(String[] resolves) throws IOException{
        Preconditions.checkNotNull(client.getUser(), "currentUser must not be null");
        Preconditions.checkNotNull(client.getUser(), "currentUser ID must not be null");
        Retrieve retrieve = new Retrieve(this, client.getUser().getId(), resolves, 1, true);
        client.initializeRequest(retrieve);
        return retrieve;
    }

    /**
     * Retrieves an array of User[] based on a Query with support for resolving KinveyReferences
     *
     * @param query the query to execute
     * @param resolves - List of {@link com.kinvey.java.model.KinveyReference} fields to resolve
     * @return a Retrieve Request ready to be executed
     * @throws IOException
     */
    public RetrieveUsers retrieveBlocking(Query query, String[] resolves) throws IOException{
        Preconditions.checkNotNull(query, "query must not be null");
        RetrieveUsers retrieve = new RetrieveUsers(this, query, resolves, 1, true);
        client.initializeRequest(retrieve);
        return retrieve;
    }

    /**
     * Updates the current user's profile
     *
     * @return Update request
     * @throws IOException
     */
    public Update updateBlocking() throws IOException{
        Preconditions.checkNotNull(client.getUser(), "currentUser must not be null");
        Preconditions.checkNotNull(client.getUser().getId(), "currentUser ID must not be null");
        Update update = new Update(this, client.getUser());
        client.initializeRequest(update);
        return update;
    }

    /**
     * Updates a provided user's profile
     *
     * @param user the user to update
     * @return an Update request ready to be executed
     * @throws IOException
     */
    public Update updateBlocking(User user) throws IOException{
        Preconditions.checkNotNull(user, "currentUser must not be null");
        Preconditions.checkNotNull(user.getId(), "currentUser ID must not be null");
        Update update = new Update(this, user);
        client.initializeRequest(update);
        return update;
    }

    public Update changePassword(String newPassword) throws IOException{
        Preconditions.checkNotNull(client.getUser(), "currentUser must not be null");
        Preconditions.checkNotNull(client.getUser().getId(), "currentUser ID must not be null");
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setPassword(newPassword);
        Update update = new Update(this, client.getUser(), passwordRequest);
        client.initializeRequest(update);
        return update;
    }

    public UserExists exists(String username) throws IOException {
        Preconditions.checkNotNull(username, "username must not be null");
        Username name = new Username();
        name.setUsername(username);
        UserExists userExists = new UserExists(client, name);
        client.initializeRequest(userExists);
        return userExists;
    }

    public Update getUser(String userId) throws IOException {
        Preconditions.checkNotNull(userId, "username must not be null");
        Update update = new Update(this, userId);
        client.initializeRequest(update);
        return update;
    }

    public Update save() throws IOException {
        return updateBlocking();
    }

    /**
     * Initiates a password reset request for a provided username
     *
     * @param usernameOrEmail the username to request a password reset for
     * @return ResetPassword request
     * @throws IOException
     */
    public ResetPassword resetPasswordBlocking(String usernameOrEmail) throws IOException {
        Preconditions.checkNotNull(usernameOrEmail, "username must not be null!");
        ResetPassword reset = new ResetPassword(this, usernameOrEmail);
        client.initializeRequest(reset);
        return reset;
    }

    /**
     * Initiates an EmailVerification request for the current user
     *
     * @return EMail Verification Request
     * @throws IOException
     */
    public EmailVerification sendEmailVerificationBlocking() throws IOException {
        Preconditions.checkNotNull(client.getUser(), "currentUser must not be null");
        Preconditions.checkNotNull(client.getUser().getId(), "currentUser ID must not be null");
        EmailVerification verify = new EmailVerification(this, client.getUser().getId());
        client.initializeRequest(verify);
        return verify;
    }


    /**
     * modify the locked down state of the provided user id.
     * <p/>
     * This operation must be performed with the master secret
     * <p/>
     * Locking down a user will prevent them from logging in and remove all locally stored content on their device
     *
     * @param userid  the id to lockdown
     * @param setLockdownStateTo true to lockdown, false to remove lockdown state
     * @return a LockDownUser request ready to execute
     * @throws IOException
     */
    public LockDownUser lockDownUserBlocking(String userid, boolean setLockdownStateTo) throws IOException{
        Preconditions.checkNotNull(userid, "userID must not be null");
        GenericJson lock = new GenericJson();
        lock.put("userId", userid);
        lock.put("setLockdownStateTo", setLockdownStateTo);
        LockDownUser lockdown = new LockDownUser(this, lock);
        client.initializeRequest(lockdown);
        return lockdown;
    }

    public ForgotUsername forgotUsername(String email) throws IOException {
        Preconditions.checkNotNull(email, "email must not be null");
        Email userEmail = new Email();
        userEmail.setEmail(email);
        ForgotUsername forgotUsername = new ForgotUsername(client, userEmail);
        client.initializeRequest(forgotUsername);
        return forgotUsername;
    }

    public GetMICAccessToken getMICToken(String code) throws IOException{

//        grant_type: "authorization_code" - this is always set to this value
//        code: use the 'code' returned in the callback
//        redirect_uri: The same redirect uri used when obtaining the auth grant.
//        client_id:  The appKey (kid) of the app

        Map<String, String> data = new HashMap<String, String>();
        data.put("grant_type", "authorization_code");
        data.put("code", code);
        data.put("redirect_uri", MICRedirectURI);
        data.put("client_id", ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey());

        HttpContent content = new UrlEncodedContent(data) ;
        GetMICAccessToken getToken = new GetMICAccessToken(this, content);
        getToken.setRequireAppCredentials(true);
        client.initializeRequest(getToken);
        return getToken;
    }

    public GetMICAccessToken useRefreshToken(String refreshToken) throws IOException{
//        grant_type: "refresh_token" - this is always set to this value  - note the difference
//        refresh_token: use the refresh token
//        redirect_uri: The same redirect uri used when obtaining the auth grant.
//        client_id:  The appKey (kid) of the app

        Map<String, String> data = new HashMap<String, String>();
        data.put("grant_type", "refresh_token");
        data.put("refresh_token", refreshToken);
        data.put("redirect_uri", MICRedirectURI);
        data.put("client_id", ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey());

        HttpContent content = new UrlEncodedContent(data) ;
        GetMICAccessToken getToken = new GetMICAccessToken(this, content);
        getToken.setRequireAppCredentials(true);
        client.initializeRequest(getToken);
        return getToken;
    }

    public GetMICTempURL getMICTempURL() throws IOException{

//    	client_id:  this is the app’s appKey (the KID)
//    	redirect_uri:  the uri that the grant will redirect to on authentication, as set in the console. Note, this much exactly match one of the redirect URIs configured in the console.
//    	response_type:  this is always set to “code”

        Map<String, String> data = new HashMap<String, String>();
        data.put("response_type", "code");
        data.put("redirect_uri", MICRedirectURI);
        data.put("client_id", ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey());

        HttpContent content = new UrlEncodedContent(data) ;
        GetMICTempURL getTemp = new GetMICTempURL(client, content);
        getTemp.setRequireAppCredentials(true);
        client.initializeRequest(getTemp);
        return getTemp;

    }


    public LoginToTempURL MICLoginToTempURL(String username, String password, String tempURL) throws IOException{

//    	client_id:  this is the app’s appKey (the KID)
//    	redirect_uri:  the uri that the grant will redirect to on authentication, as set in the console. Note, this much exactly match one of the redirect URIs configured in the console.
//    	response_type:  this is always set to “code”
//    	username
//    	password


        Map<String, String> data = new HashMap<String, String>();
        data.put("client_id", ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey());
        data.put("redirect_uri", MICRedirectURI);
        data.put("response_type", "code");
        data.put("username", username);
        data.put("password", password);

        HttpContent content = new UrlEncodedContent(data) ;
        LoginToTempURL loginTemp = new LoginToTempURL(this, tempURL, content);
        loginTemp.setRequireAppCredentials(true);
        client.initializeRequest(loginTemp);
        return loginTemp;

    }


    public class LoginRequest {
        Credential credential;
        UserStoreRequestManager.LoginType type;
        KinveyAuthRequest request;

        public LoginRequest() {
            builder.setCreate(true);
            this.type = UserStoreRequestManager.LoginType.IMPLICIT;
        }

        public LoginRequest(String username, String password, boolean setCreate) {
            builder.setUsernameAndPassword(username, password);
            builder.setCreate(setCreate);
            builder.setUser(client.getUser());
            this.type = UserStoreRequestManager.LoginType.KINVEY;

        }

        public LoginRequest(ThirdPartyIdentity identity) {
            builder.setThirdPartyIdentity(identity);
            builder.setUser(client.getUser());
            builder.setCreate(false);
            this.type = UserStoreRequestManager.LoginType.THIRDPARTY;
        }

        public LoginRequest(Credential credential) {
            this.credential = credential;
            this.type = UserStoreRequestManager.LoginType.CREDENTIALSTORE;
        }

        public LoginRequest buildAuthRequest() {
            this.request = builder.build();
            this.request.setKinveyHeaders(((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getKinveyHeaders());
            return this;
        }

        public User execute() throws IOException {
            if (client.isUserLoggedIn()) {
                throw new KinveyException("Attempting to login when a user is already logged in",
                        "call `myClient.user().logout().execute() first -or- check `myClient.user().isUserLoggedIn()` before attempting to login again",
                        "Only one user can be active at a time, and logging in a new user will replace the current user which might not be intended");
            }
            User ret;
            try {
                ret = myClazz.newInstance();
            } catch (Exception e) {
//                e.printStackTrace();
                throw new NullPointerException(e.getMessage());
            }
            if (this.type == UserStoreRequestManager.LoginType.CREDENTIALSTORE) {
                return initUser(credential, ret);
            }
            KinveyAuthResponse response = this.request.execute();
            //if (response.)
            return initUser(response, ret);
        }
    }


}

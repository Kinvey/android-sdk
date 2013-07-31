/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.lang.reflect.Array;

import com.kinvey.java.auth.*;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;

/**
 *
 * <p>This class is not thread-safe.</p>
 *
 * @author edwardf
 * @author mjsalinger
 * @since 2.0
 */
public class User extends GenericJson   {

    public static final String USER_COLLECTION_NAME = "user";

    protected enum LoginType {
        IMPLICIT,
        KINVEY,
        GOOGLE,
        TWITTER,
        FACEBOOK,
        LINKED_IN,
        AUTH_LINK,
        CREDENTIALSTORE,
        SALESFORCE,
        THIRDPARTY
    }

    @Key("_id")
    private String id;

    @Key("authToken")
    private String authToken;

    @Key("username")
    private String username;

    private Class thisClass = User.class;

    public String getId(){
        return this.id;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private AbstractClient client;

    private KinveyAuthRequest.Builder builder;

    /**
     *
     * Constructor to access Kinvey's UserGroup management.
     *
     * @param client - an instance of Kinvey AbstractClient, configured for the application
     */
    protected User(AbstractClient client, KinveyAuthRequest.Builder builder) {
        Preconditions.checkNotNull(client, "client must not be null.");
        Preconditions.checkNotNull(builder, "KinveyAuthRequest.Builder should not be null");
        this.client = client;
        this.builder = builder;
        builder.setUser(this);
    }

    public User() {}

    protected AbstractClient getClient() {
        return client;
    }

    /**
     * Method to determine if the current user instnace represents a logged-in user
     *
     * @return true if user is logged in, false if not
     */
    public boolean isUserLoggedIn() {
        return (this.id != null || this.authToken != null || this.username != null);
    }

    /**
     * Method to initialize the User after login, create a credential,
     * and add it to the KinveyClientRequestInitializer
     *
     * @param response KinveyAuthResponse object containing the login response
     * @throws IOException
     */
    private User initUser(KinveyAuthResponse response, String userType) throws IOException {

        this.setId(response.getUserId());
        this.put("_kmd",response.getMetadata());
        this.putAll(response.getUnknownKeys());
        this.setUsername(response.get("username").toString());
        this.setAuthToken(response.getAuthToken());
        CredentialManager credentialManager = new CredentialManager(client.getStore());
        ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer())
                .setCredential(credentialManager.createAndStoreCredential(response, this.getId()));
        getClient().getClientUsers().addUser(getId(),userType);
        getClient().getClientUsers().setCurrentUser(getId());
        return this;
    }

    private User initUser(Credential credential) {
        setId(credential.getUserId());
        setAuthToken(credential.getAuthToken());
        return this;
    }

    private void removeFromStore(String userID) {
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
     LoginRequest login(ThirdPartyIdentity.Type thirdPartyType, String ... args) throws IOException {
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
        return new LoginRequest(credential);
    }

    /**
     * Convenience Method to retrieve Metadata.
     *
     * @return Current user object with refreshed metadata
     * @throws IOException
     */
    protected User retrieveMetadataBlocking() throws IOException {
        User ret = this.retrieveBlocking().execute();
        String authToken = this.authToken;
        this.putAll(ret.getUnknownKeys());
        this.username = ret.username;
//        this.authToken = authToken;

        return this;
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

    public LoginRequest loginKinveyAuthTokenBlocking(String userId, String authToken) throws IOException{
        this.setAuthToken(authToken);
        this.setId(userId);
        Credential c = Credential.from(this);
        return login(c);

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
        return new LogoutRequest(this.client.getStore());
    }


    /**
     * Creates an explicit Kinvey User.
     *
     * @param userid userID of Kinvey user
     * @param password password of Kinvey user
     * @return LoginRequest Object
     * @throws IOException
     */
    public LoginRequest createBlocking(String userid, String password) throws IOException {
        return new LoginRequest(userid, password, true).buildAuthRequest();
    }

    /**
     * Delete's the given user from the server.
     *
     * @param hardDelete if true, physically deletes the user. If false, marks user as inactive.
     * @return Delete Request
     * @throws IOException
     */
    public Delete deleteBlocking(boolean hardDelete) throws IOException {
        Preconditions.checkNotNull(this.getId());
        Delete delete = new Delete(this.getId(), hardDelete);
        client.initializeRequest(delete);
        return delete;
    }

    /**
     * Retrieves current user profile metadata.
     *
     * @return Retrieve Request
     * @throws IOException
     */
    public Retrieve<User> retrieveBlocking() throws IOException{
        Preconditions.checkNotNull(this.getId(), "userID must not be null");
        Retrieve<User> retrieve = new Retrieve(this.getId(), thisClass);
        client.initializeRequest(retrieve);
        return retrieve;
    }

    /**
     * Retrieves an array of User[] based on a Query.
     *
     * @return Retrieve Request
     * @throws IOException
     */
    public Retrieve<User[]> retrieveBlocking(Query query) throws IOException{
        Preconditions.checkNotNull(query, "query must not be null");
        Retrieve<User[]> retrieve = new Retrieve(query, Array.newInstance(thisClass,0).getClass());
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
        Preconditions.checkNotNull(this.getId(), "user must not be null");
        Update update = new Update(this);
        client.initializeRequest(update);
        return update;
    }

    /**
     * Initiates a password reset request for a provided username
     *
     * @param username the username to request a password reset for
     * @return ResetPassword request
     * @throws IOException
     */
    public ResetPassword resetPasswordBlocking(String username) throws IOException {
        Preconditions.checkNotNull(username, "username must not be null!");
        ResetPassword reset = new ResetPassword(username);
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
        Preconditions.checkNotNull(this.getId(), "userID must not be null");
        EmailVerification verify = new EmailVerification(this.getId());
        client.initializeRequest(verify);
        return verify;
    }

    /**
     * Login Request Class.  Constructs the HTTP request object for Login requests.
     */
    public class LoginRequest {
        Credential credential;
        LoginType type;
        KinveyAuthRequest request;

        public LoginRequest() {
            builder.setCreate(true);
            this.type=LoginType.IMPLICIT;
        }

        public LoginRequest(String username, String password, boolean setCreate) {
            builder.setUsernameAndPassword(username, password);
            builder.setCreate(setCreate);
            builder.setUser(User.this);
            this.type=LoginType.KINVEY;
        }

        public LoginRequest(ThirdPartyIdentity identity) {
            builder.setThirdPartyIdentity(identity);
            builder.setUser(User.this);
            builder.setCreate(false);
            this.type=LoginType.THIRDPARTY;
        }

        public LoginRequest(Credential credential) {
            this.credential = credential;
            this.type = LoginType.CREDENTIALSTORE;
        }

        public LoginRequest buildAuthRequest() {
            this.request = builder.build();
            return this;
        }

        public User execute() throws IOException {
            String userType = "";
            if (this.type == LoginType.CREDENTIALSTORE) {
                return initUser(credential);
            } else {
               switch (this.type) {
                   case IMPLICIT:
                       userType="Implicit";
                       break;
                   case KINVEY:
                       userType="Kinvey";
                       break;
                   case THIRDPARTY:
                       userType="ThirdParty";
                       break;
                   default:
                       Preconditions.checkArgument(false,"Invalid LoginType operation");
               }

            }
            KinveyAuthResponse response = this.request.execute();
            //if (response.)
            return initUser(response, userType);
        }
    }

    /**
     * Logout Request Class.  Constructs the HTTP request object for Logout requests.
     */
    public class LogoutRequest {

        private CredentialStore store;

        public LogoutRequest(CredentialStore store){
            this.store = store;
        }

        public void execute() {
            CredentialManager manager = new CredentialManager(this.store);
            manager.removeCredential(getId());
            client.setCurrentUser(null);
            ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).setCredential(null);
        }
    }

    /**
     * Delete Request Class, extends AbstractKinveyJsonClientRequest<Void>.  Constructs the HTTP request object for
     * Delete User requests.
     */
    public class Delete extends AbstractKinveyJsonClientRequest<Void> {
        private static final String REST_PATH = "user/{appKey}/{userID}?hard={hard}";

        @Key
        private boolean hard = false;

        @Key
        private String userID;

        Delete(String userID, boolean hard) {
            super(client, "DELETE", REST_PATH, null, Void.class);
            this.userID = userID;
            this.hard = hard;
        }

        @Override
        public Void execute() throws IOException {
            super.execute();
            removeFromStore(userID);
            logout();

            return null;
        }
    }

    /**
     * Retrieve Request Class, extends AbstractKinveyJsonClientRequest<User>.  Constructs the HTTP request object for
     * Retrieve User requests.
     */
    public class Retrieve<T> extends AbstractKinveyJsonClientRequest<T> {
        private static final String REST_PATH = "user/{appKey}/{userID}{?query,sort,limit,skip}";

        @Key
        private String userID;
        @Key("query")
        private String queryFilter;
        @Key("sort")
        private String sortFilter;
        @Key
        private String limit;
        @Key
        private String skip;

        Retrieve(String userID, Class myClass) {
            super(client, "GET", REST_PATH, null, myClass);
            this.userID = userID;
        }

        Retrieve(Query query, Class myClass){
            super(client, "GET", REST_PATH, null, myClass);
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            this.sortFilter = query.getSortString();
        }
    }

    /**
     * Update Request Class, extends AbstractKinveyJsonClientRequest<User>.  Constructs the HTTP request object for
     * Update User requests.
     */
    public class Update extends AbstractKinveyJsonClientRequest<User> {
        private static final String REST_PATH = "user/{appKey}/{userID}";

        @Key
        private String userID;

        Update(User user) {
            super(client, "PUT", REST_PATH, user, User.class);
            this.userID = user.getId();

        }
    }

    /**
     * ResetPassword Request Class, extends AbstractKinveyJsonClientRequest<User>.  Constructs the HTTP request object
     * for ResetPassword User requests.
     */
    public class ResetPassword extends AbstractKinveyJsonClientRequest<Void> {
        private static final String REST_PATH = "/rpc/{appKey}/{userID}/user-password-reset-initiate";

        @Key
        private String userID;

        ResetPassword(String username) {
            super(client, "POST", REST_PATH, null,  Void.class);
            this.userID = username;
            this.setRequireAppCredentials(true);

        }
    }

    /**
     * EmailVerification Request Class, extends AbstractKinveyJsonClientRequest<User>.  Constructs the HTTP request
     * object for EmailVerification requests.
     */
    public class EmailVerification extends AbstractKinveyJsonClientRequest<Void> {
        private static final String REST_PATH = "/rpc/{appKey}/{userID}/user-email-verification-initiate";

        @Key
        private String userID;

        EmailVerification(String userID) {
            super(client, "POST", REST_PATH, null, Void.class);
            this.userID = userID;
            this.setRequireAppCredentials(true);
        }
    }
}

package com.kinvey.android.store;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.api.client.json.GenericJson;
import com.google.common.base.Preconditions;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.Client;
import com.kinvey.android.authentication.KinveyAuthenticator;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyMICCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserDeleteCallback;
import com.kinvey.android.callback.KinveyUserListCallback;
import com.kinvey.android.callback.KinveyUserManagementCallback;
import com.kinvey.android.ui.MICLoginActivity;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.BaseUserStore;
import com.kinvey.java.store.UserStoreRequestManager;
import com.kinvey.java.store.requests.user.GetMICTempURL;
import com.kinvey.java.store.requests.user.LoginToTempURL;
import com.kinvey.java.store.requests.user.LogoutRequest;

import java.io.IOException;

public class UserStore {

    private static boolean clearStorage = true;
    private static KinveyUserCallback MICCallback;
    private static String MICRedirectURI;

    public static void signUp(String username, String password, AbstractClient client, KinveyClientCallback<User> callback) {
        new Create(username, password, client, callback).execute();
    }

    public static void login(AbstractClient client, KinveyClientCallback<User> callback) throws IOException {
        new Login(client, callback).execute();
    }

    public static void login(String userId, String password, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(userId, password, client, callback).execute();
    }

    public static void loginFacebook(String accessToken, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, UserStoreRequestManager.LoginType.FACEBOOK, client, callback).execute();
    }

    public static void loginGoogle(String accessToken, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, UserStoreRequestManager.LoginType.FACEBOOK, client, callback).execute();
    }

    public static void loginTwitter(String accessToken, String accessSecret, String consumerKey, String consumerSecret, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, accessSecret, consumerKey, consumerSecret, client, UserStoreRequestManager.LoginType.TWITTER, callback).execute();
    }

    public static void loginLinkedIn(String accessToken, String accessSecret, String consumerKey, String consumerSecret, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, accessSecret, consumerKey, consumerSecret, client, UserStoreRequestManager.LoginType.LINKED_IN, callback).execute();
    }

    public static void loginAuthLink(String accessToken, String refreshToken, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, refreshToken,UserStoreRequestManager.LoginType.AUTH_LINK, client, callback).execute();
    }


    public static void loginSalesForce(String accessToken, String client_id, String refreshToken, String id, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, client_id, refreshToken, id, client, UserStoreRequestManager.LoginType.SALESFORCE, callback).execute();
    }

    public static void loginMobileIdentity(String accessToken, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(accessToken, UserStoreRequestManager.LoginType.MOBILE_IDENTITY, client, callback).execute();
    }

    public static void login(Credential credential, AbstractClient client, KinveyClientCallback callback) throws IOException {
        new Login(credential, client, callback).execute();
    }

    /**
     * Login to Kinvey services using a Kinvey user's _id and their valid Kinvey Auth Token.  This method is provided
     * to allow for cross-platform login, by reusing a session provided with another client library (or the REST api).
     *
     * @param userId the _id field of the user to login
     * @param authToken a valid Kinvey Auth token
     * @param callback {@link KinveyUserCallback} that contains a valid logged in user
     * @return a LoginRequest ready to be executed
     * @throws IOException
     */
    public void loginKinveyAuthToken(String userId, String authToken, AbstractClient client, KinveyClientCallback callback){
        new LoginKinveyAuth(userId, authToken, client, callback).execute();
    }

    public static void logout(AbstractClient client) {
        if (client.isUserLoggedIn()) {
            removeAccountFromAccountManager(client);

            new LogoutRequest(client).execute();

            if (clearStorage) {
                client.performLockDown();
            }
        }
    }

    public static void destroy(boolean isHard, AbstractClient client, KinveyUserDeleteCallback callback) {
        new Delete(isHard, client,callback).execute();
    }

    /**
     * Set a flag to allow local offline storage to persist after calls to logout.
     * <p/>
     * Only use this method if each device will have a guaranteed consistent user and there are no concerns about security
     */
    public void keepOfflineStorageOnLogout(){
        clearStorage = false;
    }

    public static void sendEmailConfirmation(AbstractClient client, KinveyUserManagementCallback callback) {
        new EmailVerification(client, callback).execute();
    }

    public static void forgotUsername(AbstractClient client, String email, KinveyUserManagementCallback callback) {
        new ForgotUsername(client, email, callback).execute();
    }

    public static void resetPassword(String usernameOrEmail, AbstractClient client, KinveyUserManagementCallback callback) {
        new ResetPassword(usernameOrEmail, client, callback).execute();
    }

    public static void exists(String username, AbstractClient client, KinveyUserManagementCallback callback) {
        new ExistsUser(username, client, callback).execute();
    }

    public static void changePassword(String password, AbstractClient client, KinveyUserManagementCallback callback) {
        new ChangePassword(password, client, callback).execute();
    }

    public static void get(String userId, AbstractClient client, KinveyUserManagementCallback callback) {
        new GetUser(userId, client, callback).execute();
    }

    public void save(AbstractClient client,KinveyClientCallback<User> callback) {
        new Update(client, callback).execute();
    }

    /**
     * Asynchronous Retrieve Metadata
     *
     * <p>
     * Convenience method for retrieving user metadata and updating the current user with the metadata.  Used
     * when initializing the client.
     * </p>
     *
     * @param callback KinveyUserCallback
     */
    public static void convenience(AbstractClient client,KinveyClientCallback<User> callback) {
        new RetrieveMetaData(client, callback).execute();
    }

    /**
     * Asynchronous Call to Save the current user
     * <p>
     * Constructs an asynchronous request to save the current Kinvey user.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     {@code
     User user = kinveyClient.user();
     user.update(new KinveyUserCallback() {
     public void onFailure(Throwable e) { ... }
     public void onSuccess(User result) { ... }
     });
     }
     * </pre>
     *
     * @param callback {@link KinveyUserCallback} containing an updated User instance.
     */
/*    public void update(AbstractClient client,KinveyClientCallback callback) {
        new Update(client, callback).execute();
    }*/

    /**
     * Asynchronous Call to Retrieve (refresh) the current user
     * <p>
     * Constructs an asynchronous request to refresh current user's data via the Kinvey back-end.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     {@code
     User user = kinveyClient.user();
     user.retrieve(new KinveyUserCallback() {
     public void onFailure(Throwable e) { ... }
     public void onSuccess(User result) { ... }
     });
     }
     * </pre>
     *
     * @param callback {@link KinveyUserCallback} containing a refreshed User instance.
     */
    public static void retrieve(AbstractClient client, KinveyClientCallback callback) {
        new Retrieve(client, callback).execute();
    }

    /**
     * Asynchronous call to retrive (refresh) the current user, and resolve KinveyReferences
     * <p>
     * Constructs an asynchronous request to refresh current user's data via the Kinvey back-end.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     {@code
     User user = kinveyClient.user();
     user.retrieve(new String[]{"myKinveyReferencedField"}, new KinveyUserCallback() {
     public void onFailure(Throwable e) { ... }
     public void onSuccess(User result) { ... }
     });
     }
     * </pre>
     *
     * @param resolves an array of json keys maintaining KinveyReferences to be resolved
     * @param callback {@link KinveyUserCallback} containing refreshed user instance
     */
    public static void retrieve(String[] resolves, AbstractClient client, KinveyClientCallback<User> callback){
        new Retrieve(resolves, client, callback).execute();
    }

    /**
     * Asynchronous call to retrive (refresh) the users by query, and resolve KinveyReferences
     * <p>
     * Constructs an asynchronous request to retrieve User objects via a Query.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     * {@code
    User user = kinveyClient.user();
    user.retrieve(Query query, new String[]{"myKinveyReferenceField"}, new KinveyUserListCallback() {
    public void onFailure(Throwable e) { ... }
    public void onSuccess(User[] result) { ... }
    });
    }
     * </pre>
     *
     *
     *
     * @param query the query to execute defining users to return
     * @param resolves an array of json keys maintaining KinveyReferences to be resolved
     * @param callback {@link com.kinvey.android.callback.KinveyUserListCallback} containing an array of queried users
     */
    public static void retrieve(Query query, String[] resolves, AbstractClient client, KinveyUserListCallback callback){
        new RetrieveUserList(query, resolves, client, callback).execute();
    }

    /**
     * Asynchronous Call to Retrieve users via a Query
     * <p>
     * Constructs an asynchronous request to retrieve User objects via a Query.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     * {@code
    User user = kinveyClient.user();
    user.retrieve(Query query, new KinveyUserListCallback() {
    public void onFailure(Throwable e) { ... }
    public void onSuccess(User[] result) { ... }
    });
    }
     * </pre>
     *
     * @param callback {@link com.kinvey.android.callback.KinveyUserListCallback} for retrieved users
     */
    public static void retrieve(Query q, AbstractClient client, KinveyListCallback callback) {
        new RetrieveUserList(q, client, callback).execute();
    }



    /***
     *
     * Login with the MIC service, using the oauth flow.  This method provides a URL to render containing a login page.
     *
     * @param redirectURI
     * @param callback
     */
    public static void loginWithAuthorizationCodeLoginPage(Client client, /*Class userClass, */String redirectURI, KinveyMICCallback callback){
        //return URL for login pagef
        //https://auth.kinvey.com/oauth/auth?client_id=<your_app_id>i&redirect_uri=<redirect_uri>&response_type=code
        String appkey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String host = client.getMICHostName();
        String apiVersion = client.getMICApiVersion();
        if (apiVersion != null && apiVersion.length() > 0){
            host = client.getMICHostName() + apiVersion + "/";
        }
        String myURLToRender = host + "oauth/auth?client_id=" + appkey + "&redirect_uri=" + redirectURI + "&response_type=code";
        //keep a reference to the callback and redirect uri for later

        MICCallback = callback;
        MICRedirectURI = redirectURI;

        if (callback != null){
            callback.onReadyToRender(myURLToRender);
        }

    }

    /**
     * Used by the MIC login flow, this method should be called after a successful login in the onNewItent Method of your activity.  See the MIC guide for more information.
     *
     * @param intent The intent provided to the application from the redirect
     */
    public static void onOAuthCallbackRecieved(Intent intent, AbstractClient client){
        if (intent == null || intent.getData() == null){
            return;
        }
        final Uri uri = intent.getData();
        String accessToken = uri.getQueryParameter("code");
        if (accessToken == null){
            return;
        }
        getMICAccessToken(accessToken, client);
    }

    /***
     *
     * Login with the MIC service, using the oauth flow.  This method provides direct login, without rending a login page.
     *
     * @param username
     * @param password
     * @param redirectURI
     * @param callback
     */
    public static void loginWithAuthorizationCodeAPI(AbstractClient client, String username, String password, String redirectURI, KinveyUserCallback<User> callback){
        MICCallback = callback;

        new PostForTempURL(client, redirectURI, username, password, callback).execute();
    }

    /**
     * Posts for a MIC login Access token
     *
     * @param token the access code returned from the MIC Auth service
     */
    public static void getMICAccessToken(String token, AbstractClient client){
        new PostForAccessToken(client, MICRedirectURI, token, (KinveyClientCallback) MICCallback).execute();
    }

    /***
     * Initiate the MIC login flow with an Activity containing a Webview
     *
     * @param redirectURI
     * @param callback
     */
    public static void presentMICLoginActivity(final Client client, String redirectURI, final KinveyUserCallback<User> callback){

        loginWithAuthorizationCodeLoginPage(client, redirectURI, new KinveyMICCallback() {
            @Override
            public void onReadyToRender(String myURLToRender) {
                Intent i = new Intent(client.getContext(), MICLoginActivity.class);
                i.putExtra(MICLoginActivity.KEY_LOGIN_URL, myURLToRender);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                client.getContext().startActivity(i);
            }

            @Override
            public void onSuccess(User result) {
                if(callback != null){
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onSuccess(Object result) {
//                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable error) {
                if(callback != null){
                    callback.onFailure(error);
                }
            }
        });
    }


    private static class Login extends AsyncClientRequest<User> {

        String username;
        String password;
        String accessToken;
        String refreshToken;
        String accessSecret;
        String consumerKey;
        String consumerSecret;
        Credential credential;
        UserStoreRequestManager.LoginType type;
        AbstractClient client;

        //Salesforce...
        String id;
        String client_id;

        private Login(AbstractClient client, KinveyClientCallback<User> callback) {
            super(callback);
            
            this.client = client;
            this.type = UserStoreRequestManager.LoginType.IMPLICIT;
        }

        private Login(String username, String password, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.username = username;
            this.password = password;
            
            this.client = client;
            this.type = UserStoreRequestManager.LoginType.KINVEY;
        }

        private Login(String accessToken, UserStoreRequestManager.LoginType type, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.type = type;
            
            this.client = client;
        }

        private Login(String accessToken, String refreshToken, UserStoreRequestManager.LoginType type, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.type = type;
            
            this.client = client;
        }

        private Login(String accessToken, String accessSecret, String consumerKey, String consumerSecret, AbstractClient client,
                      UserStoreRequestManager.LoginType type, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.accessSecret = accessSecret;
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.type=type;
        }

        //TODO edwardf method signature is ambiguous with above method if this one also took a login type, so hardcoded to salesforce.
        private Login(String accessToken, String clientID, String refresh, String id, AbstractClient client, KinveyClientCallback callback){
            super(callback);
            this.accessToken = accessToken;
            this.refreshToken = refresh;
            this.client_id = clientID;
            this.id = id;
            
            this.client = client;
            this.type = UserStoreRequestManager.LoginType.SALESFORCE;
        }

        private Login(Credential credential, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.credential = credential;
            
            this.client = client;
            this.type = UserStoreRequestManager.LoginType.CREDENTIALSTORE;
        }

        @Override
        protected User executeAsync() throws IOException {
            User user = null;
            switch(this.type) {
                case IMPLICIT:
                    user = BaseUserStore.login(client);
                    break;
                case KINVEY:
                    user = BaseUserStore.login(username, password, client);
                    break;
                case FACEBOOK:
                    user = BaseUserStore.loginFacebook(accessToken, client);
                    break;
                case GOOGLE:
                    user = BaseUserStore.loginGoogle(accessToken, client);
                    break;
                case TWITTER:
                    user = BaseUserStore.loginTwitter(accessToken, accessSecret, consumerKey, consumerSecret, client);
                    break;
                case LINKED_IN:
                    user = BaseUserStore.loginLinkedIn(accessToken, accessSecret, consumerKey, consumerSecret, client);
                    break;
                case AUTH_LINK:
                    user = BaseUserStore.loginAuthLink(accessToken, refreshToken, client);
                    break;
                case SALESFORCE:
                    user = BaseUserStore.loginSalesForce(accessToken, client_id, refreshToken, id, client);
                    break;
                case MOBILE_IDENTITY:
                    user = BaseUserStore.loginMobileIdentity(accessToken, client);
                    break;
                case CREDENTIALSTORE:
                    user = BaseUserStore.login(credential, client);
                    break;
            }
            saveAccountToAccountManager(user, this, client);
            return user;
        }
    }

    private static void saveAccountToAccountManager(User user, UserStore.Login login, AbstractClient client) {
        Preconditions.checkArgument(client instanceof  Client, "Client.class must be used for this method");
        String accountType = ((Client)client).getAccountType();
        Preconditions.checkNotNull(accountType, "Account Type must be initialized in Client");
        if (user != null) {
            AccountManager mAccountManager = AccountManager.get(Client.sharedInstance().getContext());
            String authToken = user.getAuthToken();
            boolean success = ((authToken != null) && (authToken.length() > 0));
            if (success) {
                final Account account = new Account(getApplicationName(((Client) client).getContext()), accountType);
                Bundle userData = new Bundle();
                userData.putString(KinveyAuthenticator.KINVEY_TOKEN, authToken);
                userData.putString(KinveyAuthenticator.KINVEY_USER_ID, user.getId());
                mAccountManager.addAccountExplicitly(account, login.password, userData);
            }
        }
    }

    private static void removeAccountFromAccountManager(AbstractClient client) {
        Preconditions.checkArgument(client instanceof  Client, "Client.class must be used for this method");
        String accountType = ((Client)client).getAccountType();
        Preconditions.checkNotNull(accountType, "Account Type must be initialized in Client");
        AccountManager mAccountManager = AccountManager.get(((Client)client).getContext());
        User user = client.activeUser();
        if (user != null) {
            final Account account = new Account(getApplicationName(((Client) client).getContext()), accountType);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                mAccountManager.removeAccountExplicitly(account);
            } else {
                mAccountManager.removeAccount(account, null, null);
            }
        }
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    private static class Create extends AsyncClientRequest<User> {
        String username;
        String password;
        private final AbstractClient client;
        

        private Create(String username, String password, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.username=username;
            this.password=password;
            this.client = client;
            
        }

        @Override
        protected User executeAsync() throws IOException {
            return BaseUserStore.signUp(username, password, client);
        }
    }

    private static class Delete extends AsyncClientRequest<Void> {
        boolean hardDelete;
        private final AbstractClient client;
        

        private Delete(boolean hardDelete,  AbstractClient client, KinveyUserDeleteCallback callback) {
            super(callback);
            this.hardDelete = hardDelete;
            this.client = client;
            
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.destroy(hardDelete, client);
            return null;
        }
    }

    private static class PostForAccessToken extends AsyncClientRequest<User>{

        private final AbstractClient client;
        private final String redirectURI;
        private String token;

        public PostForAccessToken(AbstractClient client, String redirectURI, String token, KinveyClientCallback<User> callback) {
            super(callback);
            this.client = client;
            this.redirectURI = redirectURI;

            this.token = token;
        }

        @Override
        protected User executeAsync() throws IOException {
            UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
            requestManager.setMICRedirectURI(redirectURI);
            GenericJson result = requestManager.getMICToken(token).execute();

            User ret =  BaseUserStore.loginMobileIdentity(result.get("access_token").toString(), client);

            Credential currentCred = client.getStore().load(client.activeUser().getId());
            currentCred.setRefreshToken(result.get("refresh_token").toString());
            client.getStore().store(client.activeUser().getId(), currentCred);

            return ret;
        }
    }

    private static class PostForTempURL extends AsyncClientRequest<User>{

        private final AbstractClient client;
        private final String redirectURI;
        String username;
        String password;

        public PostForTempURL(AbstractClient client, String redirectURI, String username, String password, KinveyUserCallback<User> callback) {
            super(callback);
            this.client = client;
            this.redirectURI = redirectURI;
            this.username=username;
            this.password=password;
        }

        @Override
        protected User executeAsync() throws IOException {

            UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
            requestManager.setMICRedirectURI(redirectURI);
            GetMICTempURL micTempURL = requestManager.getMICTempURL();
            GenericJson tempResult = micTempURL.execute();

            String tempURL = tempResult.get("temp_login_uri").toString();
            LoginToTempURL loginToTempURL = requestManager.MICLoginToTempURL(username, password, tempURL);
            GenericJson accessResult = loginToTempURL.execute();

            User user = BaseUserStore.loginMobileIdentity(accessResult.get("access_token").toString(), client);


            Credential currentCred = client.getStore().load(client.activeUser().getId());
            currentCred.setRefreshToken(accessResult.get("refresh_token").toString());
            client.getStore().store(client.activeUser().getId(), currentCred);

            return user;
        }
    }


    private static class Retrieve extends AsyncClientRequest<User> {

        private String[] resolves = null;
        private final AbstractClient client;

        private Retrieve(AbstractClient client,KinveyClientCallback<User> callback) {
            super(callback);
            this.client = client;
        }

        private Retrieve(String[] resolves, AbstractClient client, KinveyClientCallback<User> callback){
            super(callback);
            this.resolves = resolves;
            this.client = client;
        }

        @Override
        public User executeAsync() throws IOException {
            if (resolves == null){
                return BaseUserStore.retrieve(client);
            }else{
                return BaseUserStore.retrieve(resolves, client);
            }
        }
    }

    private static class RetrieveUserList extends AsyncClientRequest<User[]> {

        private Query query = null;
        private String[] resolves = null;
        private final AbstractClient client;
        

        private RetrieveUserList(Query query, AbstractClient client,KinveyClientCallback<User[]> callback){
            super(callback);
            this.query = query;
            this.client = client;
            
        }

        private RetrieveUserList(Query query, String[] resolves, AbstractClient client, KinveyClientCallback<User[]> callback){
            super(callback);
            this.query = query;
            this.resolves = resolves;
            this.client = client;
            
        }

        @Override
        public User[] executeAsync() throws IOException {
            if (resolves == null){
                return BaseUserStore.retrieve(query, client);
            }else{
                return BaseUserStore.retrieve(query, resolves, client);
            }
        }
    }

    private static class RetrieveMetaData extends AsyncClientRequest<User> {

        private final AbstractClient client;
        

        private RetrieveMetaData(AbstractClient client, KinveyClientCallback<User> callback) {
            super(callback);
            this.client = client;
            
        }

        @Override
        protected User executeAsync() throws IOException {
            return BaseUserStore.convenience(client);
        }
    }

    private static class Update extends AsyncClientRequest<User> {

        AbstractClient client = null;
        

        private Update(AbstractClient client, KinveyClientCallback<User> callback){
            super(callback);
            this.client = client;
            
        }

        @Override
        protected User executeAsync() throws IOException {
            return BaseUserStore.save(client);
        }
    }

    private static class ChangePassword extends AsyncClientRequest<Void> {

        private final String password;
        AbstractClient client = null;
        

        private ChangePassword(String password, AbstractClient client, KinveyClientCallback<Void> callback){
            super(callback);
            this.password = password;
            this.client = client;
            
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.changePassword(password, client);
            return null;
        }
    }


    private static class ResetPassword extends AsyncClientRequest<Void> {

        String usernameOrEmail;
        private final AbstractClient client;
        

        private ResetPassword(String usernameOrEmail, AbstractClient client, KinveyClientCallback<Void> callback) {
            super(callback);
            this.usernameOrEmail = usernameOrEmail;
            this.client = client;
            
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.resetPassword(usernameOrEmail, client);
            return null;
        }
    }

    private static class ExistsUser extends AsyncClientRequest<Void> {

        String username;
        private final AbstractClient client;
        

        private ExistsUser(String username, AbstractClient client, KinveyClientCallback<Void> callback) {
            super(callback);
            this.username = username;
            this.client = client;
            
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.exists(username, client);
            return null;
        }
    }

    private static class GetUser extends AsyncClientRequest {

        String userId;
        private final AbstractClient client;
        

        private GetUser(String userId, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.userId = userId;
            this.client = client;
            
        }

        @Override
        protected User executeAsync() throws IOException {
            BaseUserStore.get(userId, client);
            return null;
        }
    }

    private static class EmailVerification extends AsyncClientRequest<Void> {

        private final AbstractClient client;
        

        private EmailVerification(AbstractClient client, KinveyClientCallback<Void> callback) {
            super(callback);
            this.client = client;
            
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.sendEmailConfirmation(client);
            return null;
        }
    }

    private static class ForgotUsername extends AsyncClientRequest<Void> {

        private final AbstractClient client;
        
        private final String email;

        private ForgotUsername(AbstractClient client, String email, KinveyClientCallback<Void> callback) {
            super(callback);
            this.client = client;
            
            this.email = email;
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.forgotUsername(client, email);
            return null;
        }
    }

    private class LoginKinveyAuth extends AsyncClientRequest<User> {

        private String authToken;
        private final AbstractClient client;
        
        private String userID;

        private LoginKinveyAuth(String userId, String authToken, AbstractClient client, KinveyClientCallback callback){
            super(callback);
            this.userID = userId;
            this.authToken = authToken;
            this.client = client;
            
        }

        @Override
        protected User executeAsync() throws IOException {
            return BaseUserStore.loginKinveyAuthToken(userID, authToken, client);

        }
    }

    private static KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}

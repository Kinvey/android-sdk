package com.kinvey.android.store;


import android.content.Intent;
import android.net.Uri;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.Client;
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
import com.kinvey.java.store.UserStore;
import com.kinvey.java.store.UserStoreRequestManager;
import com.kinvey.java.store.requests.user.LogoutRequest;

import java.io.IOException;

public class AsyncUserStore extends UserStore{

    private static boolean clearStorage = true;
    private static KinveyUserCallback MICCallback;
    private static String MICRedirectURI;

    public static void signUp(String username, String password, Class<User> userClass, AbstractClient client, KinveyClientCallback<User> callback) {
        new Create(username, password, client, userClass, callback).execute();
    }

    public static void login(String userId, String password, Class<User> userClass, AbstractClient client, KinveyClientCallback<User> callback) throws IOException {
        new Login(userId, password, userClass, client, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
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
    public void loginKinveyAuthToken(String userId, String authToken, AbstractClient client, Class<User> userClass, KinveyClientCallback<T> callback){
        new LoginKinveyAuth(userId, authToken, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    private static void logout(AbstractClient client) {
        if(clearStorage) {
            client.performLockDown();
        }
        new LogoutRequest(client).execute();
    }

    public static void destroy(boolean isHard, AbstractClient client, Class<User> userClass, KinveyUserDeleteCallback callback) {
        new Delete(isHard, client, userClass,callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Set a flag to allow local offline storage to persist after calls to logout.
     * <p/>
     * Only use this method if each device will have a guaranteed consistent user and there are no concerns about security
     */
    public void keepOfflineStorageOnLogout(){
        clearStorage = false;
    }

    public static void sendEmailConfirmation(AbstractClient client, Class<User> userClass, KinveyUserManagementCallback callback) {
        new EmailVerification(client, userClass, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    public static void resetPassword(String username, AbstractClient client, Class<User> userClass, KinveyUserManagementCallback callback) {
        new ResetPassword(username, client, userClass, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

//    public static void changePassword(String password, AbstractClient client, Class<User> userClass, KinveyUserManagementCallback callback) {
//        new Update(password, client, userClass, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
//    }

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
    public static void convenience(AbstractClient client, Class<User> userClass,KinveyClientCallback<User> callback) {
        new RetrieveMetaData(client, userClass, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
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
    public void update(AbstractClient client, Class<User> userClass,KinveyClientCallback<User> callback) {
        new Update(client, userClass, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

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
     * @param <T>
     */
    public<T> void retrieve(AbstractClient client, Class<User> userClass, KinveyClientCallback<User> callback) {
        new Retrieve(client, userClass, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
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
    public void retrieve(String[] resolves, AbstractClient client, Class<User> userClass, KinveyClientCallback<User> callback){
        new Retrieve(resolves, client, userClass, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
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
    public void retrieve(Query query, String[] resolves, AbstractClient client, Class<User> userClass, KinveyUserListCallback callback){
        new Retrieve(query, resolves, client, userClass, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
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
    public void retrieve(Query q, AbstractClient client, Class<User> userClass, KinveyListCallback<User> callback) {
        new Retrieve(q, client, userClass, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }



    /***
     *
     * Login with the MIC service, using the oauth flow.  This method provides a URL to render containing a login page.
     *
     * @param redirectURI
     * @param callback
     */
    public static void loginWithAuthorizationCodeLoginPage(Client client, /*Class<User> userClass, */String redirectURI, KinveyMICCallback callback){
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
    public void onOAuthCallbackRecieved(Intent intent, AbstractClient client, Class<User> userClass){
        if (intent == null || intent.getData() == null){
            return;
        }
        final Uri uri = intent.getData();
        String accessToken = uri.getQueryParameter("code");
        if (accessToken == null){
            return;
        }
        getMICAccessToken(accessToken, client, userClass);
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
    public static void loginWithAuthorizationCodeAPI(AbstractClient client, Class<User> userClass, String username, String password, String redirectURI, KinveyUserCallback callback){
        MICCallback = callback;
        MICRedirectURI = redirectURI;

        new PostForTempURL(client, userClass, username, password, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Posts for a MIC login Access token
     *
     * @param token the access code returned from the MIC Auth service
     */
    public void getMICAccessToken(String token, AbstractClient client, Class<User> userClass){
        new PostForAccessToken(client, userClass, token, (KinveyClientCallback<User>) MICCallback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /***
     * Initiate the MIC login flow with an Activity containing a Webview
     *
     * @param redirectURI
     * @param callback
     */
    public void presentMICLoginActivity(final Client client, String redirectURI, final KinveyUserCallback callback){

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
        private Class<User> userClass;
        AbstractClient client;

        //Salesforce...
        String id;
        String client_id;

        private Login(KinveyClientCallback callback) {
            super(callback);
            this.type = UserStoreRequestManager.LoginType.IMPLICIT;
        }

        private Login(String username, String password, Class<User> userClass, AbstractClient client, KinveyClientCallback<User> callback) {
            super(callback);
            this.username = username;
            this.password = password;
            this.userClass = userClass;
            this.client = client;
            this.type = UserStoreRequestManager.LoginType.KINVEY;
        }

        private Login(String accessToken, UserStoreRequestManager.LoginType type, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.type = type;
        }

        private Login(String accessToken, String refreshToken, UserStoreRequestManager.LoginType type, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.type = type;
        }

        private Login(String accessToken, String accessSecret, String consumerKey, String consumerSecret,
                      UserStoreRequestManager.LoginType type, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.accessSecret = accessSecret;
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.type=type;
        }

        //TODO edwardf method signature is ambiguous with above method if this one also took a login type, so hardcoded to salesforce.
        private Login(String accessToken, String clientID, String refresh, String id, KinveyClientCallback<User> callback){
            super(callback);
            this.accessToken = accessToken;
            this.refreshToken = refresh;
            this.client_id = clientID;
            this.id = id;
            this.type = UserStoreRequestManager.LoginType.SALESFORCE;
        }

        private Login(Credential credential, KinveyClientCallback callback) {
            super(callback);
            this.credential = credential;
            this.type = UserStoreRequestManager.LoginType.CREDENTIALSTORE;
        }

        @Override
        protected User executeAsync() throws IOException {
            switch(this.type) {
                case IMPLICIT:
                    return UserStore.login(client, userClass);
                case KINVEY:
                    return UserStore.login(username, password, client, userClass);
                case FACEBOOK:
                    return UserStore.loginFacebook(accessToken, client, userClass);
                case GOOGLE:
                    return UserStore.loginGoogle(accessToken, client, userClass);
                case TWITTER:
                    return UserStore.loginTwitter(accessToken, accessSecret, consumerKey, consumerSecret, client, userClass);
                case LINKED_IN:
                    return UserStore.loginLinkedIn(accessToken, accessSecret, consumerKey, consumerSecret, client, userClass);
                case AUTH_LINK:
                    return UserStore.loginAuthLink(accessToken, refreshToken, client, userClass);
                case SALESFORCE:
                    return UserStore.loginSalesForce(accessToken, client_id, refreshToken, id, client, userClass);
                case MOBILE_IDENTITY:
                    return UserStore.loginMobileIdentity(accessToken, client, userClass);
                case CREDENTIALSTORE:
                    return UserStore.login(credential, client, userClass);
            }
            return null;
        }
    }

    private static class Create extends AsyncClientRequest<User> {
        String username;
        String password;
        private final AbstractClient client;
        private final Class<User> userClass;

        private Create(String username, String password, AbstractClient client, Class<User> userClass, KinveyClientCallback<User> callback) {
            super(callback);
            this.username=username;
            this.password=password;
            this.client = client;
            this.userClass = userClass;
        }

        @Override
        protected User executeAsync() throws IOException {
            return UserStore.signUp(username, password, client, userClass);
        }
    }

    private static class Delete extends AsyncClientRequest<Void> {
        boolean hardDelete;
        private final AbstractClient client;
        private final Class<User> userClass;

        private Delete(boolean hardDelete,  AbstractClient client, Class<User> userClass, KinveyUserDeleteCallback callback) {
            super(callback);
            this.hardDelete = hardDelete;
            this.client = client;
            this.userClass = userClass;
        }

        @Override
        protected Void executeAsync() throws IOException {
            UserStore.destroy(hardDelete, client, userClass);
            return null;
        }
    }

    private class PostForAccessToken extends AsyncClientRequest<User>{

        private final AbstractClient client;
        private final Class<User> userClass;
        private String token;

        public PostForAccessToken(AbstractClient client, Class<User> userClass, String token, KinveyClientCallback<User> callback) {
            super(callback);
            this.client = client;
            this.userClass = userClass;
            this.token = token;
        }

        @Override
        protected User executeAsync() throws IOException {
            UserStoreRequestManager requestManager = new UserStoreRequestManager<User>(client, userClass, createBuilder(client));
            GenericJson result = requestManager.getMICToken(token).execute();

            User ret =  UserStore.loginMobileIdentity(result.get("access_token").toString(), client, userClass);

            Credential currentCred = client.getStore().load(client.getUser().getId());
            currentCred.setRefreshToken(result.get("refresh_token").toString());
            client.getStore().store(client.getUser().getId(), currentCred);

            return ret;
        }
    }

    private static class PostForTempURL extends AsyncClientRequest<User>{

        private final AbstractClient client;
        private final Class<User> userClass;
        String username;
        String password;

        public PostForTempURL(AbstractClient client, Class<User> userClass, String username, String password, KinveyUserCallback callback) {
            super(callback);
            this.client = client;
            this.userClass = userClass;
            this.username=username;
            this.password=password;
        }

        @Override
        protected User executeAsync() throws IOException {

            UserStoreRequestManager requestManager = new UserStoreRequestManager<User>(client, userClass, createBuilder(client));
            GenericJson tempResult = requestManager.getMICTempURL().execute();
            String tempURL = tempResult.get("temp_login_uri").toString();
            GenericJson accessResult = requestManager.MICLoginToTempURL(username, password, tempURL).execute();

//			AbstractAsyncUser.this.loginMobileIdentity(accessResult.get("access_token").toString(), MICCallback);
            User user = UserStore.loginMobileIdentity(accessResult.get("access_token").toString(), client, userClass);


            Credential currentCred = client.getStore().load(client.getUser().getId());
            currentCred.setRefreshToken(accessResult.get("refresh_token").toString());
            client.getStore().store(client.getUser().getId(), currentCred);

            return user;
        }
    }


    private static class Retrieve<T extends User> extends AsyncClientRequest<T> {

        private Query query = null;
        private String[] resolves = null;
        private final AbstractClient client;
        private final Class<T> userClass;

        private Retrieve(AbstractClient client, Class<T> userClass,KinveyClientCallback callback) {
            super(callback);
            this.client = client;
            this.userClass = userClass;
        }

        private Retrieve(Query query, AbstractClient client, Class<T> userClass,KinveyClientCallback callback){
            super(callback);
            this.query = query;
            this.client = client;
            this.userClass = userClass;
        }

        private Retrieve(String[] resolves, AbstractClient client, Class<T> userClass, KinveyClientCallback callback){
            super(callback);
            this.resolves = resolves;
            this.client = client;
            this.userClass = userClass;
        }

        private Retrieve(Query query, String[] resolves, AbstractClient client, Class<T> userClass, KinveyClientCallback callback){
            super(callback);
            this.query = query;
            this.resolves = resolves;
            this.client = client;
            this.userClass = userClass;
        }

        @Override
        public T executeAsync() throws IOException {
            if (query == null){
                if (resolves == null){
                    return (T) UserStore.retrieve(client, userClass);
                }else{
                    return (T) UserStore.retrieve(resolves, client, userClass);
                }
            }else{
                if (resolves == null){
                    return (T) UserStore.retrieve(query, client, userClass);
                }else{
                    return (T) UserStore.retrieve(query, resolves, client, userClass);
                }
            }
        }
    }

    private static class RetrieveMetaData extends AsyncClientRequest<User> {

        private final AbstractClient client;
        private final Class<User> userClass;

        private RetrieveMetaData(AbstractClient client, Class<User> userClass, KinveyClientCallback<User> callback) {
            super(callback);
            this.client = client;
            this.userClass = userClass;
        }

        @Override
        protected User executeAsync() throws IOException {
            return UserStore.convenience(client, userClass);
        }
    }

    private static class Update extends AsyncClientRequest<User> {

        AbstractClient client = null;
        private final Class<User> userClass;

        private Update(AbstractClient client, Class<User> userClass, KinveyClientCallback callback){
            super(callback);
            this.client = client;
            this.userClass = userClass;
        }

        @Override
        protected User executeAsync() throws IOException {
            return UserStore.update(client, userClass);
        }
    }



    private static class ResetPassword extends AsyncClientRequest<Void> {

        String username;
        private final AbstractClient client;
        private final Class<User> userClass;

        private ResetPassword(String username, AbstractClient client, Class<User> userClass, KinveyClientCallback callback) {
            super(callback);
            this.username = username;
            this.client = client;
            this.userClass = userClass;
        }

        @Override
        protected Void executeAsync() throws IOException {
            UserStore.resetPassword(username, client, userClass);
            return null;
        }
    }

    private static class EmailVerification extends AsyncClientRequest<Void> {


        private final AbstractClient client;
        private final Class<User> userClass;

        private EmailVerification(AbstractClient client, Class<User> userClass, KinveyClientCallback callback) {
            super(callback);
            this.client = client;
            this.userClass = userClass;
        }

        @Override
        protected Void executeAsync() throws IOException {
            UserStore.sendEmailConfirmation(client, userClass);
            return null;
        }
    }

    private class LoginKinveyAuth extends AsyncClientRequest<User> {

        private String authToken;
        private final AbstractClient client;
        private final Class<User> userClass;
        private String userID;

        private LoginKinveyAuth(String userId, String authToken, AbstractClient client, Class<User> userClass, KinveyClientCallback<User> callback){
            super(callback);
            this.userID = userId;
            this.authToken = authToken;
            this.client = client;
            this.userClass = userClass;
        }

        @Override
        protected User executeAsync() throws IOException {
            return UserStore.loginKinveyAuthToken(userID, authToken, client, userClass);

        }
    }

    private static KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}

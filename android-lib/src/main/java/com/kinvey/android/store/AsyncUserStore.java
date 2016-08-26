package com.kinvey.android.store;


import com.google.api.client.json.GenericJson;
import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserDeleteCallback;
import com.kinvey.android.callback.KinveyUserManagementCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStore;
import com.kinvey.java.store.UserStoreRequestManager;
import com.kinvey.java.store.requests.user.LogoutRequest;

import java.io.IOException;

public class AsyncUserStore extends UserStore{

    private static boolean clearStorage = true;

    public static void signUp(String username, String password, Class<User> userClass, AbstractClient client, KinveyClientCallback<T> callback) {
        new Create(username, password, callback).execute();
    }

    public static void login(String userId, String password, Class<User> userClass, AbstractClient client, KinveyClientCallback<User> callback) throws IOException {
        new Login(userId, password, userClass, client, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    private static void logout(AbstractClient client) {
        if(clearStorage) {
            client.performLockDown();
        }
        new LogoutRequest(client).execute();
    }

    /**
     * Set a flag to allow local offline storage to persist after calls to logout.
     * <p/>
     * Only use this method if each device will have a guaranteed consistent user and there are no concerns about security
     */
    public void keepOfflineStorageOnLogout(){
        clearStorage = false;
    }

    public static void sendEmailConfirmation(KinveyUserManagementCallback callback) {

    }


    public static void resetPassword(String usernameOrEmail, KinveyUserManagementCallback callback) {}



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

    private class Delete extends AsyncClientRequest<Void> {
        boolean hardDelete;

        private Delete(boolean hardDelete, KinveyUserDeleteCallback callback) {
            super(callback);
            this.hardDelete = hardDelete;
        }

        @Override
        protected Void executeAsync() throws IOException {
            UserStore.deleteBlocking(hardDelete).execute();
            return null;
        }
    }

    private class PostForAccessToken<T> extends AsyncClientRequest<T>{

        private final AbstractClient client;
        private String token;

        public PostForAccessToken(AbstractClient client, String token, KinveyClientCallback<T> callback) {
            super(callback);
            this.client = client;
            this.token = token;
        }

        @Override
        protected T executeAsync() throws IOException {
            GenericJson result = UserStore.getMICToken(token).execute();

            T ret =  UserStore.loginMobileIdentityBlocking(result.get("access_token").toString()).execute();

            Credential currentCred = client.getStore().load(client.getUser().getId());
            currentCred.setRefreshToken(result.get("refresh_token").toString());
            client.getStore().store(client.getUser().getId(), currentCred);

            return ret;
        }
    }

    private class PostForTempURL<T> extends AsyncClientRequest<T>{

        private final AbstractClient client;
        String username;
        String password;

        public PostForTempURL(AbstractClient client, String username, String password, KinveyUserCallback callback) {
            super(callback);
            this.client = client;
            this.username=username;
            this.password=password;
        }

        @Override
        protected T executeAsync() throws IOException {

            GenericJson tempResult = getMICTempURL().execute();
            String tempURL = tempResult.get("temp_login_uri").toString();
            GenericJson accessResult = UserStore.MICLoginToTempURL(username, password, tempURL).execute();

//			AbstractAsyncUser.this.loginMobileIdentity(accessResult.get("access_token").toString(), MICCallback);
            User user = UserStore.loginMobileIdentityBlocking(accessResult.get("access_token").toString()).execute();


            Credential currentCred = client.getStore().load(client.getUser().getId());
            currentCred.setRefreshToken(accessResult.get("refresh_token").toString());
            client.getStore().store(client.getUser().getId(), currentCred);

            return (T) user;
        }
    }


    private class Retrieve<T> extends AsyncClientRequest<T> {

        private Query query = null;
        private String[] resolves = null;

        private Retrieve(KinveyClientCallback callback) {
            super(callback);
        }

        private Retrieve(Query query, KinveyClientCallback callback){
            super(callback);
            this.query = query;
        }

        private Retrieve(String[] resolves, KinveyClientCallback callback){
            super(callback);
            this.resolves = resolves;
        }

        private Retrieve(Query query, String[] resolves, KinveyClientCallback callback){
            super(callback);
            this.query = query;
            this.resolves = resolves;
        }

        @Override
        public T executeAsync() throws IOException {
            if (query == null){
                if (resolves == null){
                    return (T) UserStore.retrieveBlocking().execute();
                }else{
                    return (T) UserStore.retrieveBlocking(resolves).execute();
                }
            }else{
                if (resolves == null){
                    return (T) UserStore.retrieveBlocking(query).execute();
                }else{
                    return (T) UserStore.retrieveBlocking(query, resolves).execute();
                }

            }

        }
    }

}

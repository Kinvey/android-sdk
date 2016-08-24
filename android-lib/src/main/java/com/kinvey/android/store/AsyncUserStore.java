package com.kinvey.android.store;


import com.kinvey.android.AsyncClientRequest;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStore;

import java.io.IOException;

public class AsyncUserStore<T> extends UserStore{

    public AsyncUserStore(AbstractClient client, Class<T> userClass, KinveyAuthRequest.Builder builder) {
        super(client, userClass, builder);
    }

    public static<T> void login(String userId, String password, AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login(userId, password, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
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
        LoginType type;

        //Salesforce...
        String id;
        String client_id;

        private Login(KinveyClientCallback callback) {
            super(callback);
            this.type = LoginType.IMPLICIT;
        }

        private Login(String username, String password, KinveyClientCallback callback) {
            super(callback);
            this.username = username;
            this.password = password;
            this.type = LoginType.KINVEY;
        }

        private Login(String accessToken, LoginType type, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.type = type;
        }

        private Login(String accessToken, String refreshToken, LoginType type, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.type = type;
        }

        private Login(String accessToken, String accessSecret, String consumerKey, String consumerSecret,
                      LoginType type, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.accessSecret = accessSecret;
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.type=type;
        }


        //TODO edwardf method signature is ambiguous with above method if this one also took a login type, so hardcoded to salesforce.
        private Login(String accessToken, String clientID, String refresh, String id, KinveyClientCallback<T> callback){
            super(callback);
            this.accessToken = accessToken;
            this.refreshToken = refresh;
            this.client_id = clientID;
            this.id = id;
            this.type = LoginType.SALESFORCE;
        }

        private Login(Credential credential, KinveyClientCallback callback) {
            super(callback);
            this.credential = credential;
            this.type = LoginType.CREDENTIALSTORE;
        }

        @Override
        protected User executeAsync() throws IOException {
            switch(this.type) {
                case IMPLICIT:
                    return loginBlocking().execute();
                case KINVEY:
                    return loginBlocking(username, password).execute();
                case FACEBOOK:
                    return loginFacebookBlocking(accessToken).execute();
                case GOOGLE:
                    return loginGoogleBlocking(accessToken).execute();
                case TWITTER:
                    return loginTwitterBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
                case LINKED_IN:
                    return loginLinkedInBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
                case AUTH_LINK:
                    return loginAuthLinkBlocking(accessToken, refreshToken).execute();
                case SALESFORCE:
                    return loginSalesForceBlocking(accessToken, client_id, refreshToken, id).execute();
                case MOBILE_IDENTITY:
                    return loginMobileIdentityBlocking(accessToken).execute();
                case CREDENTIALSTORE:
                    return login(credential).execute();
            }
            return null;
        }
    }

}

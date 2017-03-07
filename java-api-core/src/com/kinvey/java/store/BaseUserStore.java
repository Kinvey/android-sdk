package com.kinvey.java.store;


import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.User;

import java.io.IOException;

public abstract class BaseUserStore {

    public static User signUp(String userId, String password, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).createBlocking(userId, password).execute();
    }

    /*Deletes a 'User'*/
    public static  void destroy(boolean isHard, AbstractClient client) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).deleteBlocking(isHard).execute();
    }

    public static  User login(String username, String password,
                              AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client))
                .loginBlocking(username, password).execute();
    }

    public static  User login(AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client))
                .loginBlocking().execute();
    }

    public static User loginFacebook(String accessToken, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client))
                .loginFacebookBlocking(accessToken).execute();
    }

    public static User loginGoogle(String accessToken, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client))
                .loginGoogleBlocking(accessToken).execute();
    }

    public static User loginTwitter(String accessToken, String accessSecret, String consumerKey,
                                    String consumerSecret, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).
                loginTwitterBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static User loginLinkedIn(String accessToken, String accessSecret, String consumerKey,
                                     String consumerSecret, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client))
                .loginLinkedInBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static User loginAuthLink(String accessToken, String refreshToken,
                                     AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client))
                .loginAuthLinkBlocking(accessToken, refreshToken).execute();
    }

    public static User loginSalesForce(String accessToken, String clientId,
                                       String refreshToken, String id,
                                       AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client))
                .loginSalesForceBlocking(accessToken, clientId, refreshToken, id).execute();
    }

    public static User loginMobileIdentity(String authToken, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client))
                .loginMobileIdentityBlocking(authToken).execute();
    }

    public static User login(Credential credential, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).login(credential).execute();
    }

    public static User loginKinveyAuthToken(String userId, String authToken, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).loginKinveyAuthTokenBlocking(userId, authToken).execute();
    }

    public static void logout(AbstractClient client, boolean clearStorage) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).logout(clearStorage).execute();
    }

    public static void logout(AbstractClient client) throws IOException {
        logout(client, true);
    }

    public static void sendEmailConfirmation(AbstractClient client) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).sendEmailVerificationBlocking().execute();
    }

    public static void resetPassword(String usernameOrEmail, AbstractClient client) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).resetPasswordBlocking(usernameOrEmail).execute();
    }

    public static void changePassword(String password, AbstractClient client) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).changePassword(password).execute();
    }

    public static User convenience(AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).retrieveMetadataBlocking();
    }

    public static User retrieve(AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking().execute();
    }

    public static User[] retrieve(Query query, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking(query).execute();
    }

    public static User retrieve(String[] resolves, AbstractClient client) throws IOException {
        return  new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking(resolves).execute();
    }

    public static User[] retrieve(Query query, String[] resolves, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking(query, resolves).execute();
    }

    public static void forgotUsername(AbstractClient client, String email) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).forgotUsername(email).execute();
    }

    public static boolean exists( String username, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).exists(username).execute();
    }

    public static User get(String userId, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).getUser(userId).execute();
    }

    public static User save(AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).save().execute();
    }

    private static KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}

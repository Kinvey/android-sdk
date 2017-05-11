package com.kinvey.java.store;


import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.User;

import java.io.IOException;

public abstract class BaseUserStore <T extends User> {

    public static <T extends User> T signUp(String userId, String password, T user, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).createBlocking(userId, password, user).execute();
    }

    public static <T extends User> T signUp(String userId, String password, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).createBlocking(userId, password).execute();
    }

    /*Deletes a 'User'*/
    public static  void destroy(boolean isHard, AbstractClient client) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).deleteBlocking(isHard).execute();
    }

    public static  <T extends User> T login(String username, String password,
                                            AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginBlocking(username, password).execute();
    }

    public static  <T extends User> T login(AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginBlocking().execute();
    }

    public static <T extends User> T loginFacebook(String accessToken, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginFacebookBlocking(accessToken).execute();
    }

    public static <T extends User> T loginGoogle(String accessToken, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginGoogleBlocking(accessToken).execute();
    }

    public static <T extends User> T loginTwitter(String accessToken, String accessSecret, String consumerKey,
                                    String consumerSecret, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).
                loginTwitterBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static <T extends User> T loginLinkedIn(String accessToken, String accessSecret, String consumerKey,
                                     String consumerSecret,AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginLinkedInBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static <T extends User> T loginAuthLink(String accessToken, String refreshToken, 
                                                   AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginAuthLinkBlocking(accessToken, refreshToken).execute();
    }

    public static <T extends User> T loginSalesForce(String accessToken, String clientId,
                                       String refreshToken, String id, 
                                                     AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginSalesForceBlocking(accessToken, clientId, refreshToken, id).execute();
    }

    public static <T extends User> T loginMobileIdentity(String authToken,  AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginMobileIdentityBlocking(authToken).execute();
    }

    public static <T extends User> T login(Credential credential, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).login(credential).execute();
    }

    public static <T extends User> T loginKinveyAuthToken(String userId, String authToken, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).loginKinveyAuthTokenBlocking(userId, authToken).execute();
    }

    public static void logout(AbstractClient client) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).logout().execute();
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

    public static <T extends User> T convenience(AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).retrieveMetadataBlocking();
    }

    public static <T extends User> T retrieve(AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking().execute();
    }

    public static <T extends User> T[] retrieve(Query query, AbstractClient client) throws IOException {
        return (T[]) new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking(query).execute();
    }

    public static <T extends User> T retrieve(String[] resolves, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking(resolves).execute();
    }

    public static <T extends User> T[] retrieve(Query query, String[] resolves, AbstractClient client) throws IOException {
        return (T[]) new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking(query, resolves).execute();
    }

    public static void forgotUsername(AbstractClient client, String email) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).forgotUsername(email).execute();
    }

    public static boolean exists( String username, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).exists(username).execute();
    }

    public static <T extends User> T get(String userId, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).getUser(userId).execute();
    }

    public static <T extends User> T save(AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).save().execute();
    }

    private static KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}

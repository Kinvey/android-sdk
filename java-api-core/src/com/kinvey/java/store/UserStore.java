package com.kinvey.java.store;


import com.kinvey.java.AbstractClient;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.User;

import java.io.IOException;

public class UserStore<T> {

    public static <T extends User> User signUp(String userId, String password, AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<>(client, userClass, createBuilder(client)).createBlocking(userId, password).execute();
    }

    /*Deletes a 'User' by the 'userId'*/
    public static void destroy(String userId, boolean isHard, AbstractClient client) {

    }

    /*Deletes the 'User'*/
    public static void destroy() {

    }

    public static <T extends User> User login(String username, String password,
                                              AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client))
                .loginBlocking(username, password).execute();
    }

    public static <T extends User> User login(AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client))
                .loginBlocking().execute();
    }

    public static <T extends User> User loginFacebook(String accessToken, AbstractClient client,
                                                      Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client))
                .loginFacebookBlocking(accessToken).execute();
    }

    public static <T extends User> User loginGoogle(String accessToken, AbstractClient client,
                                                    Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client))
                .loginGoogleBlocking(accessToken).execute();
    }

    public static <T extends User> User loginTwitter(String accessToken, String accessSecret,
                                                     String consumerKey, String consumerSecret,
                                                     AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client)).
                loginTwitterBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static <T extends User> User loginLinkedIn(String accessToken, String accessSecret,
                                                      String consumerKey, String consumerSecret,
                                                      AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client))
                .loginLinkedInBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static <T extends User> User loginAuthLink(String accessToken, String refreshToken,
                                                      AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client))
                .loginAuthLinkBlocking(accessToken, refreshToken).execute();
    }

    public static <T extends User> User loginSalesForce(String accessToken, String clientId,
                                                                String refreshToken, String id,
                                                                AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client))
                .loginSalesForceBlocking(accessToken, clientId, refreshToken, id).execute();
    }

    public static <T extends User> User loginMobileIdentity(String authToken, AbstractClient client,
                                                            Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client))
                .loginMobileIdentityBlocking(authToken).execute();
    }

    public static <T extends User> User login(Credential credential, AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client)).login(credential).execute();
    }


    public static <T extends User> void logout(AbstractClient client, Class<T> userClass) throws IOException {
        new UserStoreRequestManager(client, userClass, createBuilder(client)).logout().execute();
    }


    private static KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}

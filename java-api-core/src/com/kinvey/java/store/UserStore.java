package com.kinvey.java.store;


import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.User;

import java.io.IOException;

public class UserStore<T> {

    public static <T extends User> User signUp(String userId, String password, AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<>(client, userClass, createBuilder(client)).createBlocking(userId, password).execute();
    }

    /*Deletes a 'User'*/
    public static <T extends User> void destroy(boolean isHard, AbstractClient client, Class<T> userClass) throws IOException {
        new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).deleteBlocking(isHard).execute();
    }

    public static <T extends User> User login(String username, String password,
                                              AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client))
                .loginBlocking(username, password).execute();
    }

    public static <T extends User> User login(AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client))
                .loginBlocking().execute();
    }

    public static <T extends User> User loginFacebook(String accessToken, AbstractClient client,
                                                      Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client))
                .loginFacebookBlocking(accessToken).execute();
    }

    public static <T extends User> User loginGoogle(String accessToken, AbstractClient client,
                                                    Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client))
                .loginGoogleBlocking(accessToken).execute();
    }

    public static <T extends User> User loginTwitter(String accessToken, String accessSecret,
                                                     String consumerKey, String consumerSecret,
                                                     AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).
                loginTwitterBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static <T extends User> User loginLinkedIn(String accessToken, String accessSecret,
                                                      String consumerKey, String consumerSecret,
                                                      AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client))
                .loginLinkedInBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static <T extends User> User loginAuthLink(String accessToken, String refreshToken,
                                                      AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client))
                .loginAuthLinkBlocking(accessToken, refreshToken).execute();
    }

    public static <T extends User> User loginSalesForce(String accessToken, String clientId,
                                                                String refreshToken, String id,
                                                                AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client))
                .loginSalesForceBlocking(accessToken, clientId, refreshToken, id).execute();
    }

    public static <T extends User> User loginMobileIdentity(String authToken, AbstractClient client,
                                                            Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client))
                .loginMobileIdentityBlocking(authToken).execute();
    }

    public static <T extends User> User login(Credential credential, AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).login(credential).execute();
    }

    public static <T extends User> User loginKinveyAuthToken(String userId, String authToken, AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).loginKinveyAuthTokenBlocking(userId, authToken).execute();
    }

    public static <T extends User> void logout(AbstractClient client, Class<T> userClass) throws IOException {
        new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).logout().execute();
    }

    public static <T extends User> void sendEmailConfirmation(AbstractClient client, Class<T> userClass) throws IOException {
        new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).sendEmailVerificationBlocking().execute();
    }

    public static <T extends User> void resetPassword(String usernameOrEmail, AbstractClient client, Class<T> userClass) throws IOException {
        new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).resetPasswordBlocking(usernameOrEmail).execute();
    }

/*    public static <T extends User> User update(AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).updateBlocking().execute();
    }*/

    public static <T extends User> void changePassword(String password, AbstractClient client, Class<T> userClass) throws IOException {
        new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).changePassword(password).execute();
    }

    public static <T extends User> User convenience(AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).retrieveMetadataBlocking();
    }

    public static <T extends User> T retrieve(AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).retrieveBlocking().execute();
    }

    public static <T extends User> T[] retrieve(Query query, AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).retrieveBlocking(query).execute();
    }

    public static <T extends User> T retrieve(String[] resolves, AbstractClient client, Class<T> userClass) throws IOException {
        return  new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).retrieveBlocking(resolves).execute();
    }

    public static <T extends User> T[] retrieve(Query query, String[] resolves, AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).retrieveBlocking(query, resolves).execute();
    }

    public static <T extends User> void forgotUsername(AbstractClient client, Class<T> userClass, String email) throws IOException {
        new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).forgotUsername(email).execute();
    }

    public static <T extends User> boolean exists( String username, AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).exists(username).execute();
    }

    public static <T extends User> User get(String userId, AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).getUser(userId).execute();
    }

    public static <T extends User> User save(AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager<T>(client, userClass, createBuilder(client)).save().execute();
    }

    private static KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}

package com.kinvey.java.store;


import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.BaseUser;
import com.kinvey.java.dto.RealtimeRegisterResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseUserStore <T extends BaseUser> {

    public static <T extends BaseUser> T signUp(String userId, String password, T user, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).createBlocking(userId, password, user).execute();
    }

    public static <T extends BaseUser> T signUp(String userId, String password, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).createBlocking(userId, password).execute();
    }

    /*Deletes a 'BaseUser'*/
    public static  void destroy(boolean isHard, AbstractClient client) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).deleteBlocking(isHard).execute();
    }

    public static  <T extends BaseUser> T login(String username, String password,
                                                AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginBlocking(username, password).execute();
    }

    public static  <T extends BaseUser> T login(AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginBlocking().execute();
    }

    public static <T extends BaseUser> T loginFacebook(String accessToken, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginFacebookBlocking(accessToken).execute();
    }

    public static <T extends BaseUser> T loginGoogle(String accessToken, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginGoogleBlocking(accessToken).execute();
    }

    public static <T extends BaseUser> T loginTwitter(String accessToken, String accessSecret, String consumerKey,
                                                      String consumerSecret, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).
                loginTwitterBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static <T extends BaseUser> T loginLinkedIn(String accessToken, String accessSecret, String consumerKey,
                                                       String consumerSecret, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginLinkedInBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static <T extends BaseUser> T loginAuthLink(String accessToken, String refreshToken,
                                                       AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginAuthLinkBlocking(accessToken, refreshToken).execute();
    }

    public static <T extends BaseUser> T loginSalesForce(String accessToken, String clientId,
                                                         String refreshToken, String id,
                                                         AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginSalesForceBlocking(accessToken, clientId, refreshToken, id).execute();
    }

    public static <T extends BaseUser> T loginMobileIdentity(String authToken, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client))
                .loginMobileIdentityBlocking(authToken).execute();
    }

    public static <T extends BaseUser> T login(Credential credential, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).login(credential).execute();
    }

    public static <T extends BaseUser> T loginKinveyAuthToken(String userId, String authToken, AbstractClient client) throws IOException {
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

    public static <T extends BaseUser> T convenience(AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).retrieveMetadataBlocking();
    }

    public static <T extends BaseUser> T retrieve(AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking().execute();
    }

    public static <T extends BaseUser> List<T> retrieve(Query query, AbstractClient client) throws IOException {
        return new ArrayList<T>(Arrays.asList((T[]) new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking(query).execute()));
    }

    public static <T extends BaseUser> T retrieve(String[] resolves, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking(resolves).execute();
    }

    public static <T extends BaseUser> List<T> retrieve(Query query, String[] resolves, AbstractClient client) throws IOException {
        return new ArrayList<T>(Arrays.asList((T[]) new UserStoreRequestManager(client, createBuilder(client)).retrieveBlocking(query, resolves).execute()));
    }

    public static void forgotUsername(AbstractClient client, String email) throws IOException {
        new UserStoreRequestManager(client, createBuilder(client)).forgotUsername(email).execute();
    }

    public static boolean exists( String username, AbstractClient client) throws IOException {
        return new UserStoreRequestManager(client, createBuilder(client)).exists(username).execute().doesUsernameExist();
    }

    public static <T extends BaseUser> T get(String userId, AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).getUser(userId).execute();
    }

    public static <T extends BaseUser> T save(AbstractClient client) throws IOException {
        return (T) new UserStoreRequestManager(client, createBuilder(client)).save().execute();
    }

    public static <T extends BaseUser> T update() throws IOException {
        return (T) new UserStoreRequestManager(AbstractClient.sharedInstance(), createBuilder(AbstractClient.sharedInstance())).save().execute();
    }

    /**
     * Register the active user for realtime messaging.
     * @throws IOException
     */
    public static void registerRealtime() throws IOException {
        if (AbstractClient.sharedInstance().getActiveUser() == null) {
            throw new KinveyException("User object has to be the active user in order to register for realtime messages");
        }
        RealtimeRegisterResponse response = new UserStoreRequestManager(AbstractClient.sharedInstance(),
                createBuilder(AbstractClient.sharedInstance()))
                .realtimeRegister(AbstractClient.sharedInstance().getActiveUser().getId(),
                        AbstractClient.sharedInstance().getDeviceId()).execute();
        RealtimeRouter.getInstance().initialize(
                response.getUserChannelGroup(),
                response.getPublishKey(),
                response.getSubscribeKey(),
                AbstractClient.sharedInstance().getActiveUser().getAuthToken(),
                AbstractClient.sharedInstance());
    }

    /**
     * Unregister the active user from realtime messaging.
     * @throws IOException
     */
    public static void unRegisterRealtime() throws IOException {
        if (AbstractClient.sharedInstance().getActiveUser() == null) {
            throw new KinveyException("User object has to be the active user in order to register for realtime messages");
        }
        RealtimeRouter.getInstance().unInitialize();

        new UserStoreRequestManager(AbstractClient.sharedInstance(),
                createBuilder(AbstractClient.sharedInstance()))
                .realtimeUnregister(AbstractClient.sharedInstance().getActiveUser().getId(),
                        AbstractClient.sharedInstance().getDeviceId()).execute();
    }

    private static KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}

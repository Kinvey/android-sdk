package com.kinvey.java.store;


import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.BaseUser;
import com.kinvey.java.dto.LiveServiceRegisterResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseUserStore {

    public static <T extends BaseUser> T signUp(String userId, String password, T user, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).createBlocking(userId, password, user).execute();
    }

    public static <T extends BaseUser> T signUp(String userId, String password, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).createBlocking(userId, password).execute();
    }

    /*Deletes a 'BaseUser'*/
    public static <T extends BaseUser>  void destroy(boolean isHard, AbstractClient<T> client) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).deleteBlocking(isHard).execute();
    }

    public static  <T extends BaseUser> T login(String username, String password,
                                                AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginBlocking(username, password).execute();
    }

    public static  <T extends BaseUser> T login(AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginBlocking().execute();
    }

    public static <T extends BaseUser> T loginFacebook(String accessToken, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginFacebookBlocking(accessToken).execute();
    }

    public static <T extends BaseUser> T loginGoogle(String accessToken, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginGoogleBlocking(accessToken).execute();
    }

    public static <T extends BaseUser> T loginTwitter(String accessToken, String accessSecret, String consumerKey,
                                                      String consumerSecret, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).
                loginTwitterBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static <T extends BaseUser> T loginLinkedIn(String accessToken, String accessSecret, String consumerKey,
                                                       String consumerSecret, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginLinkedInBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    public static <T extends BaseUser> T loginAuthLink(String accessToken, String refreshToken,
                                                       AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginAuthLinkBlocking(accessToken, refreshToken).execute();
    }

    public static <T extends BaseUser> T loginSalesForce(String accessToken, String clientId,
                                                         String refreshToken, String id,
                                                         AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginSalesForceBlocking(accessToken, clientId, refreshToken, id).execute();
    }

    public static <T extends BaseUser> T loginMobileIdentity(String authToken, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginMobileIdentityBlocking(authToken).execute();
    }

    public static <T extends BaseUser> T login(Credential credential, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).login(credential).execute();
    }

    public static <T extends BaseUser> T loginKinveyAuthToken(String userId, String authToken, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).loginKinveyAuthTokenBlocking(userId, authToken).execute();
    }

    public static <T extends BaseUser> void logout(AbstractClient<T> client) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).logout().execute();
    }

    public static <T extends BaseUser> void sendEmailConfirmation(AbstractClient<T> client) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).sendEmailVerificationBlocking().execute();
    }

    public static<T extends BaseUser> void resetPassword(String usernameOrEmail, AbstractClient<T> client) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).resetPasswordBlocking(usernameOrEmail).execute();
    }

    public static <T extends BaseUser> void changePassword(String password, AbstractClient<T> client) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).changePassword(password).execute();
    }

    public static <T extends BaseUser> T convenience(AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).retrieveMetadataBlocking();
    }

    public static <T extends BaseUser> T retrieve(AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).retrieveBlocking().execute();
    }

    public static <T extends BaseUser> List<T> retrieve(Query query, AbstractClient<T> client) throws IOException {
        return new ArrayList<>(Arrays.asList((T[]) new UserStoreRequestManager<>(client, createBuilder(client)).retrieveBlocking(query).execute()));
    }

    public static <T extends BaseUser> T retrieve(String[] resolves, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).retrieveBlocking(resolves).execute();
    }

    public static <T extends BaseUser> List<T> retrieve(Query query, String[] resolves, AbstractClient<T> client) throws IOException {
        return new ArrayList<>(Arrays.asList((T[]) new UserStoreRequestManager<>(client, createBuilder(client)).retrieveBlocking(query, resolves).execute()));
    }

    public static <T extends BaseUser> void forgotUsername(AbstractClient<T> client, String email) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).forgotUsername(email).execute();
    }

    public static <T extends BaseUser> boolean exists( String username, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).exists(username).execute().doesUsernameExist();
    }

    public static <T extends BaseUser> T get(String userId, AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).getUser(userId).execute();
    }

    public static <T extends BaseUser> T save(AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).save().execute();
    }

    public static <T extends BaseUser> T update() throws IOException {
        return new UserStoreRequestManager<T>(AbstractClient.sharedInstance(), createBuilder(AbstractClient.sharedInstance())).save().execute();
    }

    /**
     * Register the active user for LiveService messaging.
     * @throws IOException
     */
    public static void registerLiveService() throws IOException {
        if (AbstractClient.sharedInstance().getActiveUser() == null) {
            throw new KinveyException("User object has to be the active user in order to register for LiveService messages");
        }
        if (!LiveServiceRouter.getInstance().isInitialized()) {
            LiveServiceRegisterResponse response = new UserStoreRequestManager(AbstractClient.sharedInstance(),
                    createBuilder(AbstractClient.sharedInstance()))
                    .liveServiceRegister(AbstractClient.sharedInstance().getActiveUser().getId(),
                            AbstractClient.sharedInstance().getDeviceId()).execute();
            LiveServiceRouter.getInstance().initialize(
                    response.getUserChannelGroup(),
                    response.getPublishKey(),
                    response.getSubscribeKey(),
                    AbstractClient.sharedInstance().getActiveUser().getAuthToken(),
                    AbstractClient.sharedInstance());
        }
    }

    /**
     * Unregister the active user from LiveService messaging.
     * @throws IOException
     */
    public static void unRegisterLiveService() throws IOException {
        if (AbstractClient.sharedInstance().getActiveUser() == null) {
            throw new KinveyException("User object has to be the active user in order to register for LiveService messages");
        }
        if (LiveServiceRouter.getInstance().isInitialized()) {
            LiveServiceRouter.getInstance().uninitialize();
            new UserStoreRequestManager<>(AbstractClient.sharedInstance(),
                    createBuilder(AbstractClient.sharedInstance()))
                    .liveServiceUnregister(AbstractClient.sharedInstance().getActiveUser().getId(),
                            AbstractClient.sharedInstance().getDeviceId()).execute();
        }
    }

    private static <T extends BaseUser> KinveyAuthRequest.Builder<T> createBuilder(AbstractClient<T> client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder<>(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}

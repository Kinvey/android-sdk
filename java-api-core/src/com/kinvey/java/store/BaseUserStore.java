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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseUserStore {

    @Nonnull
    public static <T extends BaseUser> T signUp(@Nonnull String userId, @Nonnull String password, @Nullable T user, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).createBlocking(userId, password, user).execute();
    }

    @Nonnull
    public static <T extends BaseUser> T signUp(@Nonnull String userId, @Nonnull String password, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).createBlocking(userId, password).execute();
    }

    /*Deletes a 'BaseUser'*/
    public static <T extends BaseUser>  void destroy(boolean isHard, @Nonnull AbstractClient<T> client) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).deleteBlocking(isHard).execute();
    }

    @Nonnull
    public static  <T extends BaseUser> T login(@Nonnull String username, @Nonnull String password,
                                                @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginBlocking(username, password).execute();
    }

    @Nonnull
    public static  <T extends BaseUser> T login(@Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginBlocking().execute();
    }

    @Nonnull
    public static <T extends BaseUser> T loginFacebook(@Nonnull String accessToken, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginFacebookBlocking(accessToken).execute();
    }

    @Nonnull
    public static <T extends BaseUser> T loginGoogle(@Nonnull String accessToken, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginGoogleBlocking(accessToken).execute();
    }

    @Nonnull
    public static <T extends BaseUser> T loginTwitter(@Nonnull String accessToken, @Nonnull String accessSecret, @Nonnull String consumerKey,
                                                      @Nonnull String consumerSecret, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).
                loginTwitterBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    @Nonnull
    public static <T extends BaseUser> T loginLinkedIn(@Nonnull String accessToken, @Nonnull String accessSecret, @Nonnull String consumerKey,
                                                       @Nonnull String consumerSecret, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginLinkedInBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
    }

    @Nonnull
    public static <T extends BaseUser> T loginAuthLink(@Nonnull String accessToken, @Nonnull String refreshToken,
                                                       @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginAuthLinkBlocking(accessToken, refreshToken).execute();
    }

    @Nonnull
    public static <T extends BaseUser> T loginSalesForce(@Nonnull String accessToken, @Nonnull String clientId,
                                                         @Nonnull String refreshToken, @Nonnull String id,
                                                         @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginSalesForceBlocking(accessToken, clientId, refreshToken, id).execute();
    }

    @Nonnull
    public static <T extends BaseUser> T loginMobileIdentity(@Nonnull String authToken, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client))
                .loginMobileIdentityBlocking(authToken).execute();
    }

    public static <T extends BaseUser> T login(@Nonnull Credential credential, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).login(credential).execute();
    }

    @Nonnull
    public static <T extends BaseUser> T loginKinveyAuthToken(@Nonnull String userId, @Nonnull String authToken, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).loginKinveyAuthTokenBlocking(userId, authToken).execute();
    }

    public static <T extends BaseUser> void logout(@Nonnull AbstractClient<T> client) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).logout().execute();
    }

    public static <T extends BaseUser> void sendEmailConfirmation(@Nonnull AbstractClient<T> client) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).sendEmailVerificationBlocking().execute();
    }

    public static<T extends BaseUser> void resetPassword(@Nonnull String usernameOrEmail, @Nonnull AbstractClient<T> client) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).resetPasswordBlocking(usernameOrEmail).execute();
    }

    public static <T extends BaseUser> void changePassword(@Nonnull String password, @Nonnull AbstractClient<T> client) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).changePassword(password).execute();
    }

    @Nonnull
    public static <T extends BaseUser> T convenience(@Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).retrieveMetadataBlocking();
    }

    @Nonnull
    public static <T extends BaseUser> T retrieve(@Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).retrieveBlocking().execute();
    }

    @Nonnull
    public static <T extends BaseUser> List<T> retrieve(@Nonnull Query query, @Nonnull AbstractClient<T> client) throws IOException {
        return new ArrayList<>(Arrays.asList((T[]) new UserStoreRequestManager<>(client, createBuilder(client)).retrieveBlocking(query).execute()));
    }

    @Nonnull
    public static <T extends BaseUser> T retrieve(@Nonnull String[] resolves, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).retrieveBlocking(resolves).execute();
    }

    @Nonnull
    public static <T extends BaseUser> List<T> retrieve(@Nonnull Query query, @Nonnull String[] resolves, @Nonnull AbstractClient<T> client) throws IOException {
        return new ArrayList<>(Arrays.asList((T[]) new UserStoreRequestManager<>(client, createBuilder(client)).retrieveBlocking(query, resolves).execute()));
    }

    public static <T extends BaseUser> void forgotUsername(@Nonnull AbstractClient<T> client, @Nonnull String email) throws IOException {
        new UserStoreRequestManager<>(client, createBuilder(client)).forgotUsername(email).execute();
    }

    public static <T extends BaseUser> boolean exists(@Nonnull String username, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).exists(username).execute().doesUsernameExist();
    }

    @Nonnull
    public static <T extends BaseUser> T get(@Nonnull String userId, @Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).getUser(userId).execute();
    }

    @Nonnull
    public static <T extends BaseUser> T save(@Nonnull AbstractClient<T> client) throws IOException {
        return new UserStoreRequestManager<>(client, createBuilder(client)).save().execute();
    }

    @Nonnull
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

    @Nonnull
    private static <T extends BaseUser> KinveyAuthRequest.Builder<T> createBuilder(@Nonnull AbstractClient<T> client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder<>(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}

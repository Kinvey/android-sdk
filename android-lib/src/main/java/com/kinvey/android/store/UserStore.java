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
import com.kinvey.android.model.User;
import com.kinvey.android.ui.MICLoginActivity;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.BaseUser;
import com.kinvey.java.store.BaseUserStore;
import com.kinvey.java.store.UserStoreRequestManager;
import com.kinvey.java.store.requests.user.GetMICTempURL;
import com.kinvey.java.store.requests.user.LoginToTempURL;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

public class UserStore {

    private static boolean clearStorage = true;
    private static KinveyUserCallback MICCallback;
    private static String MICRedirectURI;
    private static String MICClientId;

    /**
     * Asynchronous request to signUp.
     * <p>
     * Creates an asynchronous request to create new User at the kinvey backend.
     * If signUp was successful user will be login automatically.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.signUp("userName", "password", mClient, new KinveyClientCallback<User>() {
     *      void onSuccess(User user){...};
     *      void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param username {@link String} the userName of Kinvey user
     * @param password {@link String} the password of Kinvey user.
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     */
    public static void signUp(String username, String password, AbstractClient<User> client, KinveyClientCallback<User> callback) {
        signUp(username, password, null, client, callback);
    }

    public static <T extends User> void signUp(String username, String password, T user, AbstractClient<T> client, KinveyClientCallback<T> callback) {
        new Create<>(username, password, user, client, callback).execute();
    }

    /**
     * Asynchronous request to login the implicit user.
     * <p>
     * Creates an asynchronous request to login at the kinvey backend.
     * Login with the implicit user. If the implicit user does not exist, the user is created.
     * After calling this method, the application should retrieve and store the userID using getId().
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.login(mClient, new KinveyClientCallback<User>() {
     *      void onSuccess(User user){...};
     *      void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     * @throws IOException
     */
    public static <T extends User> void login(AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login<>(client, callback).execute();
    }

    /**
     * Asynchronous request to login with the existing user.
     * <p>
     * Creates an asynchronous request to login new User at kinvey backend.
     * Login with the with existing user.
     * If user does not exist, returns a error response.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.login("userID", "password", mClient, new KinveyClientCallback<User>() {
     *      void onSuccess(User user){...};
     *      void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param userId {@link String} the userId of Kinvey user
     * @param password {@link String} the password of Kinvey user.
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     * @throws IOException
     */
    public static <T extends User> void login(String userId, String password, AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login<>(userId, password, client, callback).execute();
    }

    /**
     * Asynchronous request to login with the Facebook application.
     * <p>
     * Creates an asynchronous request to login with the Facebook accessToken.
     * Before this request you must authorize in Facebook application and get Facebook access token.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.loginFacebook("accessToken", mClient, new KinveyClientCallback<User>() {
     *      void onSuccess(User user){...};
     *      void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param accessToken {@link String} the Facebook access token
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     * @throws IOException
     */
    public static  <T extends User> void loginFacebook(String accessToken, AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login<>(accessToken, UserStoreRequestManager.LoginType.FACEBOOK, client, callback).execute();
    }

    /**
     * Asynchronous request to login with the Google application.
     * <p>
     * Creates an asynchronous request to login with the Google accessToken.
     * Before this request you must authorize in Google application and get Google access token.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.loginGoogle("accessToken", mClient, new KinveyClientCallback<User>() {
     *      void onSuccess(User user){...};
     *      void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param accessToken {@link String} the Facebook access token
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     * @throws IOException
     */
    public static <T extends User> void loginGoogle(String accessToken, AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login<>(accessToken, UserStoreRequestManager.LoginType.GOOGLE, client, callback).execute();
    }

    /**
     * Asynchronous request to login with the Twitter application.
     * <p>
     * Creates an asynchronous request to login with the Twitter.
     * Before this request you must authorize in Twitter application and get Twitter accessToken,
     * accessSecret,consumerKey and consumerSecret.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.loginTwitter("accessToken", "accessSecret", "consumerKey", "consumerSecret", mClient,
     *      new KinveyClientCallback<User>() {
     *          void onSuccess(User user){...};
     *          void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param accessToken {@link String} the Twitter access token
     * @param accessSecret {@link String} the Twitter accessSecret token
     * @param consumerKey {@link String} the Twitter consumerKey token
     * @param consumerSecret {@link String} the Twitter consumerSecret token
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     * @throws IOException
     */
    public static <T extends User> void loginTwitter(String accessToken, String accessSecret, String consumerKey, String consumerSecret, AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login<>(accessToken, accessSecret, consumerKey, consumerSecret, client, UserStoreRequestManager.LoginType.TWITTER, callback).execute();
    }

    /**
     * Asynchronous request to login with the LinkedIn application.
     * <p>
     * Creates an asynchronous request to login with the LinkedIn.
     * Before this request you must authorize in LinkedIn application and get LinkedIn accessToken,
     * accessSecret,consumerKey and consumerSecret.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.loginLinkedIn("accessToken", "accessSecret", "consumerKey", "consumerSecret", mClient,
     *      new KinveyClientCallback<User>() {
     *          void onSuccess(User user){...};
     *          void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param accessToken {@link String} the LinkedIn access token
     * @param accessSecret {@link String} the LinkedIn accessSecret token
     * @param consumerKey {@link String} the LinkedIn consumerKey token
     * @param consumerSecret {@link String} the LinkedIn consumerSecret token
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     * @throws IOException
     */
    public static <T extends User> void loginLinkedIn(String accessToken, String accessSecret, String consumerKey, String consumerSecret, AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login<>(accessToken, accessSecret, consumerKey, consumerSecret, client, UserStoreRequestManager.LoginType.LINKED_IN, callback).execute();
    }

    /**
     * Asynchronous request to login with login link.
     * <p>
     * Creates an asynchronous request to login with login link.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.loginAuthLink("accessToken", "refreshToken", mClient, new KinveyClientCallback<User>() {
     *          void onSuccess(User user){...};
     *          void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param accessToken {@link String} the access token
     * @param refreshToken {@link String} the refresh token
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     * @throws IOException
     */
    public static <T extends User> void loginAuthLink(String accessToken, String refreshToken, AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login<>(accessToken, refreshToken,UserStoreRequestManager.LoginType.AUTH_LINK, client, callback).execute();
    }

    /**
     * Asynchronous request to login with the SalesForce application.
     * <p>
     * Creates an asynchronous request to login with the SalesForce.
     * Before this request you must authorize in LinkedIn application and get SalesForce accessToken,
     * client_id, refreshToken and id.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.loginSalesForce("accessToken", "client_id", "refreshToken", "id", mClient,
     *      new KinveyClientCallback<User>() {
     *          void onSuccess(User user){...};
     *          void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param accessToken {@link String} the SalesForce access token
     * @param client_id {@link String} the SalesForce client id
     * @param refreshToken {@link String} the SalesForce refresh token
     * @param id {@link String} the SalesForce id
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     * @throws IOException
     */
    public static <T extends User> void loginSalesForce(String accessToken, String client_id, String refreshToken, String id, AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login<>(accessToken, client_id, refreshToken, id, client, UserStoreRequestManager.LoginType.SALESFORCE, callback).execute();
    }

    /**
     * Asynchronous request to login with the MobileIdentity accessToken.
     * <p>
     * Creates an asynchronous request to login with the MobileIdentity accessToken.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.loginSalesForce("accessToken", mClient, new KinveyClientCallback<User>() {
     *          void onSuccess(User user){...};
     *          void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param accessToken {@link String} the MobileIdentity access token
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     * @throws IOException
     */
    public static <T extends User> void loginMobileIdentity(String accessToken, AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login<>(accessToken, UserStoreRequestManager.LoginType.MOBILE_IDENTITY, client, callback).execute();
    }

    /**
     * Asynchronous request to login with kinvey Credential object.
     * <p>
     * Creates an asynchronous request to login with kinvey Credential object.
     * You can get Credential object from CredentialStorage, if user was logged before.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.login(credential, mClient, new KinveyClientCallback<User>() {
     *          void onSuccess(User user){...};
     *          void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param credential {@link Credential} the credential of kinvey user
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.java.core.KinveyClientCallback<User>} the callback
     * @throws IOException
     */
    public static <T extends User> void login(Credential credential, AbstractClient client, KinveyClientCallback<T> callback) throws IOException {
        new Login<>(credential, client, callback).execute();
    }

    /**
     * Login to Kinvey services using a Kinvey user's _id and their valid Kinvey Auth Token.  This method is provided
     * to allow for cross-platform login, by reusing a session provided with another client library (or the REST api).
     *
     * @param userId the _id field of the user to login
     * @param authToken a valid Kinvey Auth token
     * @param callback {@link KinveyUserCallback} that contains a valid logged in user
     */
    public <T extends User> void loginKinveyAuthToken(String userId, String authToken, AbstractClient client, KinveyClientCallback<T> callback){
        new LoginKinveyAuth<>(userId, authToken, client, callback).execute();
    }

    /**
     * Synchronous request to logout.
     * <p>
     * Creates an Synchronous request to logout.
     * Storage will be cleared in this request. To keep data in storage need to call keepOfflineStorageOnLogout()
     * before this method.
     * Uses {@link com.kinvey.java.core.KinveyClientCallback<User>} to return a User.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.logout(mClient, new KinveyClientCallback<Vodid>() {
     *          void onSuccess(Void aVoid){...};
     *          void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param client {@link Client} an instance of the client
     */
    public static void logout(AbstractClient client, KinveyClientCallback<Void> callback) {
        new Logout(client, callback).execute();
    }

    /**
     * Asynchronous request to destroy user from kinvey backend.
     * <p>
     * Creates an Asynchronous request to destroy user from kinvey backend.
     * If isHard is true user will be deleted from kinvey backend.
     * If isHard is false user will be disabled from kinvey backend, but it can be enabled again.
     * Uses {@link KinveyUserDeleteCallback} to return a status of request execution.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.destroy("true", mClient, new KinveyUserDeleteCallback() {
     *          void onSuccess(Void aVoid){...};
     *          void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param isHard flag for detect hard/soft deleting user
     * @param client {@link Client} an instance of the client
     * @param callback {@link KinveyUserDeleteCallback} the callback
     */
    public static void destroy(boolean isHard, AbstractClient client, KinveyUserDeleteCallback callback) {
        new Delete(isHard, client,callback).execute();
    }


    /**
     * Asynchronous request to send email confirmation.
     * <p>
     * Creates an Asynchronous request to send email confirmation.
     * Uses {@link KinveyUserManagementCallback} to return a status of request execution.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.sendEmailConfirmation(mClient, new KinveyUserManagementCallback() {
     *          void onSuccess(Void aVoid){...};
     *          void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param client {@link Client} an instance of the client
     * @param callback {@link KinveyUserManagementCallback} the callback
     */
    public static void sendEmailConfirmation(AbstractClient client, KinveyUserManagementCallback callback) {
        new EmailVerification(client, callback).execute();
    }

    /**
     * Asynchronous request to forgot username.
     * <p>
     * Creates an Asynchronous request to forgot username.
     * Uses {@link KinveyUserManagementCallback} to return a status of request execution.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
     *  UserStore.forgotUsername(mClient, "email", new KinveyUserManagementCallback() {
     *          void onSuccess(Void aVoid){...};
     *          void onFailure(Throwable t){...};
     *  });
     * }
     * </pre>
     * </p>
     * @param client {@link Client} an instance of the client
     * @param email {@link String} a user's email
     * @param callback {@link KinveyUserManagementCallback} the callback
     */
    public static void forgotUsername(AbstractClient client, String email, KinveyUserManagementCallback callback) {
        new ForgotUsername(client, email, callback).execute();
    }

    public static void resetPassword(String usernameOrEmail, AbstractClient client, KinveyUserManagementCallback callback) {
        new ResetPassword(usernameOrEmail, client, callback).execute();
    }

    public static void exists(String username, AbstractClient client, KinveyClientCallback<Boolean> callback) {
        new ExistsUser(username, client, callback).execute();
    }

    public static void changePassword(String password, AbstractClient client, KinveyUserManagementCallback callback) {
        new ChangePassword(password, client, callback).execute();
    }

    public static <T extends User> void get(String userId, AbstractClient client, KinveyClientCallback<T> callback) {
        new GetUser<>(userId, client, callback).execute();
    }

    /**
     * Asynchronous Update current user info
     *
     * @deprecated use {@link User#update(KinveyClientCallback)} ()} instead.
     */
    @Deprecated
    public void save(AbstractClient client,KinveyClientCallback<User> callback) {
        new Update(client, callback).execute();
    }

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
    public static  <T extends User> void convenience(AbstractClient client,KinveyClientCallback<T> callback) {
        new RetrieveMetaData<>(client, callback).execute();
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
     User user = kinveyClient.getActiveUser();
     user.update(new KinveyUserCallback() {
     public void onFailure(Throwable e) { ... }
     public void onSuccess(User result) { ... }
     });
     }
     * </pre>
     *
     * @param callback {@link KinveyUserCallback} containing an updated User instance.
     */
/*    public void update(AbstractClient client,KinveyClientCallback callback) {
        new Update(client, callback).execute();
    }*/

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
     UserStore.retrieve(kinveyClient, new KinveyClientCallback<User> callback() {
     public void onFailure(Throwable e) { ... }
     public void onSuccess(User result) { ... }
     });
     }
     * </pre>
     *
     * @param callback {@link KinveyClientCallback<User>} containing a refreshed User instance.
     * @param client {@link Client} an instance of the client
     */
    public static void retrieve(AbstractClient client, KinveyClientCallback<User> callback) {
        new Retrieve(client, callback).execute();
    }

    /**
     * Asynchronous call to retrieve (refresh) the current user, and resolve KinveyReferences
     * <p>
     * Constructs an asynchronous request to refresh current user's data via the Kinvey back-end.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     {@code
     UserStore.retrieve(new String[]{"myKinveyReferencedField"}, kinveyClient, new KinveyClientCallback<User> callback() {
     public void onFailure(Throwable e) { ... }
     public void onSuccess(User result) { ... }
     });
     }
     * </pre>
     *
     * @param resolves an array of json keys maintaining KinveyReferences to be resolved
     * @param client {@link Client} an instance of the client
     * @param callback {@link KinveyUserCallback} containing refreshed user instance
     */
    public static <T extends User> void retrieve(String[] resolves, AbstractClient client, KinveyClientCallback<T> callback){
        new Retrieve<T>(resolves, client, callback).execute();
    }

    /**
     * Asynchronous call to retrieve (refresh) the users by query, and resolve KinveyReferences
     * <p>
     * Constructs an asynchronous request to retrieve User objects via a Query.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     * {@code
    UserStore.retrieve(query, new String[]{"myKinveyReferenceField"}, kinveyClient, new KinveyListCallback<User>() {
    public void onFailure(Throwable e) { ... }
    public void onSuccess(User[] result) { ... }
    });
    }
     * </pre>
     *
     *
     *
     * @param query {@link Query} the query to execute defining users to return
     * @param resolves an array of json keys maintaining KinveyReferences to be resolved
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.android.callback.KinveyListCallback<User>} containing an array of queried users
     */
    public static void retrieve(Query query, String[] resolves, AbstractClient client, KinveyListCallback<User> callback){
        new RetrieveUserList<User>(query, resolves, client, callback).execute();
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
    UserStore.retrieve(query, kinveyClient, new KinveyListCallback<User>() {
    public void onFailure(Throwable e) { ... }
    public void onSuccess(List<User> result) { ... }
    });
    }
     * </pre>
     * @param q {@link Query} the query to execute defining users to return
     *  @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.android.callback.KinveyListCallback<User>} for retrieved users
     */
    public static void retrieve(Query q, AbstractClient client, KinveyListCallback<User> callback) {
        new RetrieveUserList<User>(q, client, callback).execute();
    }

    /**
     * Asynchronous call to retrieve (refresh) the users by query, and resolve KinveyReferences
     * <p>
     * Constructs an asynchronous request to retrieve User objects via a Query.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
     * {@code
    UserStore.retrieve(query, new String[]{"myKinveyReferenceField"}, kinveyClient, new KinveyUserListCallback() {
    public void onFailure(Throwable e) { ... }
    public void onSuccess(User[] result) { ... }
    });
    }
     * </pre>
     *
     *
     *
     * @param query {@link Query} the query to execute defining users to return
     * @param resolves an array of json keys maintaining KinveyReferences to be resolved
     * @param client {@link Client} an instance of the client
     * @param callback {@link com.kinvey.android.callback.KinveyUserListCallback} containing an array of queried users
     *
     * @deprecated use {@link UserStore#retrieve(Query, String[], AbstractClient, KinveyListCallback)}
     */
    @Deprecated
    public static void retrieve(Query query, String[] resolves, AbstractClient client, KinveyUserListCallback callback){
        new RetrieveUserArray(query, resolves, client, callback).execute();
    }


    /***
     *
     * Login with the MIC service, using the oauth flow.  This method provides a URL to render containing a login page.
     *
     * @param client Client object
     * @param redirectURI redirectURI
     * @param callback KinveyMICCallback
     * @deprecated Use {@link #loginWithAuthorizationCodeLoginPage(Client, String, String, KinveyMICCallback)}
     */
    public static void loginWithAuthorizationCodeLoginPage(Client client, /*Class userClass, */String redirectURI, KinveyMICCallback callback){
        loginWithAuthorizationCodeLoginPage(client, null, redirectURI, callback);
    }

    /***
     *
     * Login with the MIC service, using the oauth flow.  This method provides a URL to render containing a login page.
     *
     * @param redirectURI redirectURI
     * @param callback KinveyMICCallback
     */
    public static void loginWithAuthorizationCodeLoginPage(Client client, String clientId, /*Class userClass, */String redirectURI, KinveyMICCallback callback){
        //return URL for login page
        //https://auth.kinvey.com/oauth/auth?client_id=<your_app_id>i&redirect_uri=<redirect_uri>&response_type=code
        String appkey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String host = client.getMICHostName();
        String apiVersion = client.getMICApiVersion();
        if (apiVersion != null && apiVersion.length() > 0){
            host = client.getMICHostName() + apiVersion + "/";
        }
        String myURLToRender = host + "oauth/auth?client_id=" + appkey;
        if (clientId != null) {
            myURLToRender = myURLToRender + "." + clientId;
        }
        myURLToRender  = myURLToRender  + "&redirect_uri=" + redirectURI + "&response_type=code" + "&scope=openid";
        //keep a reference to the callback and redirect uri for later

        MICCallback = callback;
        MICRedirectURI = redirectURI;

        if (callback != null){
            callback.onReadyToRender(myURLToRender);
        }

    }

    /**
     * Used by the MIC login flow, this method should be called after a successful login in the onNewIntent Method of your activity.  See the MIC guide for more information.
     *
     * @param intent The intent provided to the application from the redirect
     * @param clientId ClientId
     * @param client Client object
     */
    public static void onOAuthCallbackReceived(Intent intent, String clientId, AbstractClient client){
        if (intent == null || intent.getData() == null){
            return;
        }
        final Uri uri = intent.getData();
        String accessToken = uri.getQueryParameter("code");
        if (accessToken == null){
            return;
        }
        getMICAccessToken(accessToken, clientId, client);
    }

    /**
     * Used by the MIC login flow, this method should be called after a successful login in the onNewIntent Method of your activity.  See the MIC guide for more information.
     *
     * @param intent The intent provided to the application from the redirect
     * @deprecated Use {@link #onOAuthCallbackReceived(Intent, String, AbstractClient)}
     */
    @Deprecated
    public static void onOAuthCallbackRecieved(Intent intent, AbstractClient client){
        onOAuthCallbackReceived(intent, null, client);
    }

    /***
     *
     * Login with the MIC service, using the oauth flow.  This method provides direct login, without rending a login page.
     *
     * @param username {@link String} the userName of Kinvey user
     * @param password {@link String} the password of Kinvey user.
     * @param redirectURI redirectURI
     * @param callback {@link KinveyUserCallback}
     * @deprecated Use {@link #loginWithAuthorizationCodeAPI(AbstractClient, String, String, String, String, KinveyUserCallback)}
     */
    public static void loginWithAuthorizationCodeAPI(AbstractClient client, String username, String password, String redirectURI, KinveyUserCallback<User> callback){
        loginWithAuthorizationCodeAPI(client, username, password, null, redirectURI, callback);
    }

    /***
     *
     * Login with the MIC service, using the oauth flow.  This method provides direct login, without rending a login page.
     *
     * @param username {@link String} the userName of Kinvey user
     * @param password {@link String} the password of Kinvey user.
     * @param redirectURI redirectURI
     * @param callback {@link KinveyUserCallback}
     */
    public static void loginWithAuthorizationCodeAPI(AbstractClient client, String username, String password, String clientId, String redirectURI, KinveyUserCallback<User> callback){
        MICCallback = callback;

        new PostForTempURL(client, clientId, redirectURI, username, password, callback).execute();
    }

    /**
     * Posts for a MIC login Access token
     *
     * @param token the access code returned from the MIC Auth service
     * @param clientId clientId
     * @param client Client object
     */
    public static void getMICAccessToken(String token, String clientId, AbstractClient client){
        new PostForAccessToken(client, MICRedirectURI, token, clientId, (KinveyClientCallback) MICCallback).execute();
    }

    /**
     * Posts for a MIC login Access token
     *
     * @param token the access code returned from the MIC Auth service
     * @param client Client object
     * @deprecated use {@link #getMICAccessToken(String, String, AbstractClient)} ()} instead.
     */
    @Deprecated
    public static void getMICAccessToken(String token, AbstractClient client){
        getMICAccessToken(token, null, client);
    }

    /***
     * Initiate the MIC login flow with an Activity containing a Webview
     *
     * @param client Client object
     * @param redirectURI redirectURI
     * @param callback callback
     * @deprecated use {@link #presentMICLoginActivity(Client, String, String, KinveyUserCallback)} ()} instead.
     */
    public static void presentMICLoginActivity(final Client client, String redirectURI, final KinveyUserCallback<User> callback){
        presentMICLoginActivity(client, null, redirectURI, callback);
    }

    /***
     * Initiate the MIC login flow with an Activity containing a Webview
     *
     * @param client Client object
     * @param clientId clientId
     * @param redirectURI redirectURI
     * @param callback callback
     */
    public static void presentMICLoginActivity(final Client client, final String clientId, String redirectURI, final KinveyUserCallback<User> callback){

        loginWithAuthorizationCodeLoginPage(client, clientId, redirectURI, new KinveyMICCallback() {
            @Override
            public void onReadyToRender(String myURLToRender) {
                Intent i = new Intent(client.getContext(), MICLoginActivity.class);
                i.putExtra(MICLoginActivity.KEY_LOGIN_URL, myURLToRender);
                i.putExtra(MICLoginActivity.KEY_CLIENT_ID, clientId);
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
            public void onSuccess(Object result) {
//                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable error) {
                if(callback != null){
                    callback.onFailure(error);
                }
            }
        });
    }


    private static class Login<T extends BaseUser> extends AsyncClientRequest<T> {

        String username;
        String password;
        String accessToken;
        String refreshToken;
        String accessSecret;
        String consumerKey;
        String consumerSecret;
        Credential credential;
        UserStoreRequestManager.LoginType type;
        AbstractClient<T> client;

        //Salesforce...
        String id;
        String client_id;

        private Login(AbstractClient client, KinveyClientCallback<T> callback) {
            super(callback);

            this.client = client;
            this.type = UserStoreRequestManager.LoginType.IMPLICIT;
        }

        private Login(String username, String password, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.username = username;
            this.password = password;

            this.client = client;
            this.type = UserStoreRequestManager.LoginType.KINVEY;
        }

        private Login(String accessToken, UserStoreRequestManager.LoginType type, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.type = type;

            this.client = client;
        }

        private Login(String accessToken, String refreshToken, UserStoreRequestManager.LoginType type, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.type = type;

            this.client = client;
        }

        private Login(String accessToken, String accessSecret, String consumerKey, String consumerSecret, AbstractClient client,
                      UserStoreRequestManager.LoginType type, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.accessSecret = accessSecret;
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.type=type;
            this.client = client;
        }

        //TODO edwardf method signature is ambiguous with above method if this one also took a login type, so hardcoded to salesforce.
        private Login(String accessToken, String clientId, String refresh, String id, AbstractClient client, KinveyClientCallback callback){
            super(callback);
            this.accessToken = accessToken;
            this.refreshToken = refresh;
            this.client_id = clientId;
            this.id = id;

            this.client = client;
            this.type = UserStoreRequestManager.LoginType.SALESFORCE;
        }

        private Login(Credential credential, AbstractClient client, KinveyClientCallback callback) {
            super(callback);
            this.credential = credential;

            this.client = client;
            this.type = UserStoreRequestManager.LoginType.CREDENTIALSTORE;
        }

        @Override
        protected T executeAsync() throws IOException {
            switch(this.type) {
                case IMPLICIT:
                    return BaseUserStore.login(client);
                case KINVEY:
                    return BaseUserStore.login(username, password, client);
                case FACEBOOK:
                    return BaseUserStore.loginFacebook(accessToken, client);
                case GOOGLE:
                    return BaseUserStore.loginGoogle(accessToken, client);
                case TWITTER:
                    return BaseUserStore.loginTwitter(accessToken, accessSecret, consumerKey, consumerSecret, client);
                case LINKED_IN:
                    return BaseUserStore.loginLinkedIn(accessToken, accessSecret, consumerKey, consumerSecret, client);
                case AUTH_LINK:
                    return BaseUserStore.loginAuthLink(accessToken, refreshToken, client);
                case SALESFORCE:
                    return BaseUserStore.loginSalesForce(accessToken, client_id, refreshToken, id, client);
                case MOBILE_IDENTITY:
                    return BaseUserStore.loginMobileIdentity(accessToken, client);
                case CREDENTIALSTORE:
                    return BaseUserStore.login(credential, client);
            }
            return null;
        }
    }

    private static class Create<T extends User> extends AsyncClientRequest<T> {
        String username;
        String password;
        private T user;
        private final AbstractClient<T> client;


        private Create(String username, String password, T user, AbstractClient<T> client, KinveyClientCallback<T> callback) {
            super(callback);
            this.username=username;
            this.password=password;
            this.user = user;
            this.client = client;

        }

        @Override
        protected T executeAsync() throws IOException {
            if (user == null) {
                return BaseUserStore.signUp(username, password, client);
            } else {
                return BaseUserStore.signUp(username, password, user, client);
            }
        }
    }

    private static class Delete extends AsyncClientRequest<Void> {
        boolean hardDelete;
        private final AbstractClient client;


        private Delete(boolean hardDelete,  AbstractClient client, KinveyUserDeleteCallback callback) {
            super(callback);
            this.hardDelete = hardDelete;
            this.client = client;

        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.destroy(hardDelete, client);
            return null;
        }
    }

    private static class Logout extends AsyncClientRequest<Void> {
        private final AbstractClient client;

        private Logout(AbstractClient client, KinveyClientCallback<Void> callback) {
            super(callback);
            this.client = client;

        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.logout(client);
            return null;
        }
    }

    private static class PostForAccessToken<T extends User> extends AsyncClientRequest<T>{

        private final AbstractClient<T> client;
        private final String redirectURI;
        private String token;
        private String clientId;

        public PostForAccessToken(AbstractClient<T> client, String redirectURI, String token, String clientId, KinveyClientCallback<T> callback) {
            super(callback);
            this.client = client;
            this.redirectURI = redirectURI;

            this.token = token;
            this.clientId = clientId;
        }

        @Override
        protected T executeAsync() throws IOException {
            UserStoreRequestManager requestManager = new UserStoreRequestManager(client, createBuilder(client));
            requestManager.setMICRedirectURI(redirectURI);
            GenericJson result = requestManager.getMICToken(token, clientId).execute();

            T ret =  BaseUserStore.loginMobileIdentity(result.get("access_token").toString(), client);

            Credential currentCred = client.getStore().load(client.getActiveUser().getId());
            currentCred.setRefreshToken(result.get("refresh_token").toString());
            currentCred.setClientId(clientId);
            client.getStore().store(client.getActiveUser().getId(), currentCred);

            return ret;
        }
    }

    private static class PostForTempURL<T extends User> extends AsyncClientRequest<T>{

        private final AbstractClient<T> client;
        private String clientId;
        private final String redirectURI;
        String username;
        String password;

        public PostForTempURL(AbstractClient<T> client, String clientId, String redirectURI, String username, String password, KinveyUserCallback<T> callback) {
            super(callback);
            this.client = client;
            this.clientId = clientId;
            this.redirectURI = redirectURI;
            this.username=username;
            this.password=password;
        }

        @Override
        protected T executeAsync() throws IOException {

            UserStoreRequestManager<T> requestManager = new UserStoreRequestManager<>(client, createBuilder(client));
            requestManager.setMICRedirectURI(redirectURI);
            GetMICTempURL micTempURL = requestManager.getMICTempURL(clientId);
            GenericJson tempResult = micTempURL.execute();

            String tempURL = tempResult.get("temp_login_uri").toString();
            LoginToTempURL loginToTempURL = requestManager.MICLoginToTempURL(username, password, clientId, tempURL);
            GenericJson accessResult = loginToTempURL.execute();

            T user = BaseUserStore.loginMobileIdentity(accessResult.get("access_token").toString(), client);


            Credential currentCred = client.getStore().load(client.getActiveUser().getId());
            currentCred.setRefreshToken(accessResult.get("refresh_token").toString());
            currentCred.setClientId(clientId);
            client.getStore().store(client.getActiveUser().getId(), currentCred);

            return user;
        }
    }


    private static class Retrieve<T extends User> extends AsyncClientRequest<T> {

        private String[] resolves = null;
        private final AbstractClient<T> client;

        private Retrieve(AbstractClient<T> client,KinveyClientCallback<T> callback) {
            super(callback);
            this.client = client;
        }

        private Retrieve(String[] resolves, AbstractClient<T> client, KinveyClientCallback<T> callback){
            super(callback);
            this.resolves = resolves;
            this.client = client;
        }

        @Override
        public T executeAsync() throws IOException {
            if (resolves == null){
                return BaseUserStore.retrieve(client);
            }else{
                return BaseUserStore.retrieve(resolves, client);
            }
        }
    }

    private static class RetrieveUserList<T extends BaseUser> extends AsyncClientRequest<List<T>> {

        private Query query = null;
        private final AbstractClient<T> client;
        private String[] resolves = null;

        private RetrieveUserList(Query query, AbstractClient<T> client, KinveyListCallback<T> callback){
            super(callback);
            this.query = query;
            this.client = client;
        }

        private RetrieveUserList(Query query, String[] resolves, AbstractClient<T> client, KinveyListCallback<T> callback){
            super(callback);
            this.query = query;
            this.resolves = resolves;
            this.client = client;
        }

        @Override
        public List<T> executeAsync() throws IOException {
            if(resolves == null) {
                return BaseUserStore.retrieve(query, client);
            } else {
                return BaseUserStore.retrieve(query, resolves, client);
            }
        }
    }

    private static class RetrieveUserArray<T extends User> extends AsyncClientRequest<T[]> {

        private Query query = null;
        private String[] resolves = null;
        private final AbstractClient<T> client;

        private RetrieveUserArray(Query query, String[] resolves, AbstractClient<T> client, KinveyClientCallback<T[]> callback){
            super(callback);
            this.query = query;
            this.resolves = resolves;
            this.client = client;
        }

        @Override
        public T[] executeAsync() throws IOException {
            List<T> users = BaseUserStore.retrieve(query, resolves, client);
            return users.toArray((T[])Array.newInstance(client.getUserClass(), 1));
        }
    }

    private static class RetrieveMetaData<T extends User> extends AsyncClientRequest<T> {

        private final AbstractClient<T> client;


        private RetrieveMetaData(AbstractClient<T> client, KinveyClientCallback<T> callback) {
            super(callback);
            this.client = client;

        }

        @Override
        protected T executeAsync() throws IOException {
            return BaseUserStore.convenience(client);
        }
    }

    private static class Update<T extends User> extends AsyncClientRequest<T> {

        AbstractClient<T> client = null;


        private Update(AbstractClient<T> client, KinveyClientCallback<T> callback){
            super(callback);
            this.client = client;

        }

        @Override
        protected T executeAsync() throws IOException {
            return BaseUserStore.save(client);
        }
    }

    private static class ChangePassword extends AsyncClientRequest<Void> {

        private final String password;
        AbstractClient client = null;


        private ChangePassword(String password, AbstractClient client, KinveyClientCallback<Void> callback){
            super(callback);
            this.password = password;
            this.client = client;

        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.changePassword(password, client);
            return null;
        }
    }


    private static class ResetPassword extends AsyncClientRequest<Void> {

        String usernameOrEmail;
        private final AbstractClient client;


        private ResetPassword(String usernameOrEmail, AbstractClient client, KinveyClientCallback<Void> callback) {
            super(callback);
            this.usernameOrEmail = usernameOrEmail;
            this.client = client;

        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.resetPassword(usernameOrEmail, client);
            return null;
        }
    }

    private static class ExistsUser extends AsyncClientRequest<Boolean> {

        String username;
        private final AbstractClient client;


        private ExistsUser(String username, AbstractClient client, KinveyClientCallback<Boolean> callback) {
            super(callback);
            this.username = username;
            this.client = client;

        }

        @Override
        protected Boolean executeAsync() throws IOException {
            return BaseUserStore.exists(username, client);
        }
    }

    private static class GetUser<T extends User> extends AsyncClientRequest<T> {

        String userId;
        private final AbstractClient client;


        private GetUser(String userId, AbstractClient client, KinveyClientCallback<T> callback) {
            super(callback);
            this.userId = userId;
            this.client = client;

        }

        @Override
        protected T executeAsync() throws IOException {
            BaseUserStore.get(userId, client);
            return null;
        }
    }

    private static class EmailVerification extends AsyncClientRequest<Void> {

        private final AbstractClient client;


        private EmailVerification(AbstractClient client, KinveyClientCallback<Void> callback) {
            super(callback);
            this.client = client;

        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.sendEmailConfirmation(client);
            return null;
        }
    }

    private static class ForgotUsername extends AsyncClientRequest<Void> {

        private final AbstractClient client;

        private final String email;

        private ForgotUsername(AbstractClient client, String email, KinveyClientCallback<Void> callback) {
            super(callback);
            this.client = client;

            this.email = email;
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.forgotUsername(client, email);
            return null;
        }
    }

    private class LoginKinveyAuth<T extends User> extends AsyncClientRequest<T> {

        private String authToken;
        private final AbstractClient<T> client;

        private String userID;

        private LoginKinveyAuth(String userId, String authToken, AbstractClient<T> client, KinveyClientCallback<T> callback){
            super(callback);
            this.userID = userId;
            this.authToken = authToken;
            this.client = client;

        }

        @Override
        protected T executeAsync() throws IOException {
            return BaseUserStore.loginKinveyAuthToken(userID, authToken, client);

        }
    }

    private static <T extends BaseUser> KinveyAuthRequest.Builder<T> createBuilder(AbstractClient<T> client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder<>(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}

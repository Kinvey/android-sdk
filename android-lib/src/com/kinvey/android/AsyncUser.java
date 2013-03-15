/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.android;

import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.kinvey.android.callback.KinveyUserCallback;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientCallback;

/**
 * Wraps the {@link com.kinvey.java.User} public methods in asynchronous functionality using native Android AsyncTask.
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#user()} convenience method.
 * </p>
 *
 * <p>
 * Methods in this API use either {@link com.kinvey.android.callback.KinveyUserCallback} for authentication, login, and
 * user creation, or the general-purpose {@link com.kinvey.java.core.KinveyClientCallback} used for User data retrieval,
 * updating, and management.
 * </p>
 *
 * <p>
 * Login sample:
 * <pre>
    public void submit(View view) {
        kinveyClient.user().login(mEditUserName.getText().toString(), mEditPassword.getText().toString(),
                new KinveyUserCallback() {
            public void onFailure(Throwable t) {
                CharSequence text = "Wrong username or password.";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
            public void onSuccess(User u) {
                CharSequence text = "Welcome back," + u.getUserName() + ".";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                LoginActivity.this.startActivity(new Intent(LoginActivity.this,
                        SessionsActivity.class));
                LoginActivity.this.finish();
            }
        });
    }
 * </pre>
 *
 * </p>
 * <p>
 * Saving user data sample:
 * <pre>
    User user = kinveyClient.getActiveUser();
    user.put("fav_food", "bacon");
    user.update(new KinveyClientCallback<User.Update>() {

        public void onFailure(Throwable e) { ... }

        public void onSuccess(User u) { ... }
    });
 * </pre>
 * </p>
 *
 * <p>This class is not thread-safe.</p>
 *
 * @author mjsalinger
 * @since 2.0
 */
public class AsyncUser extends User {


    /**
     * Base constructor requires the client instance and a {@link KinveyAuthRequest.Builder} to be passed in.
     * <p>
     * {@link com.kinvey.java.core.AbstractKinveyClient#initializeRequest(com.kinvey.java.core.AbstractKinveyClientRequest)} is used to initialize all
     * requests constructed by this api.
     * </p>
     *
     * @param client required instance
     * @throws NullPointerException if the client parameter and KinveyAuthRequest.Builder is non-null
     */
    AsyncUser(AbstractClient client, KinveyAuthRequest.Builder builder) {
        super(client, builder);
    }

    /**
     * Asynchronous implicit user login.
     * <p>
     * Constructs an asynchronous request to log in an implicit (non-named) user and returns the associated User object
     * via a KinveyUserCallback.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
        kinveyClient.user().login(new KinveyUserCallback() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(User u) {
                CharSequence text = "Welcome back!";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
     * </pre>
     * </p>
     *
     * @param callback An implementation of the AbstractClient Callback that returns a valid user object
     */
    public void login(KinveyUserCallback callback) {
        new Login(callback).execute();
    }

    /**
     * Asynchronous Kinvey user login.
     * <p>
     * Constructs an asynchronous request to log in a Kinvey user with username and password, and returns the associated
     * User object via a KinveyUserCallback.
     * </p>
     *
     * <p>
       Sample Usage:
     * <pre>
            kinveyClient.user().login(mEditUserName.getText().toString(), mEditPassword.getText().toString(),
                    new KinveyUserCallback() {
            public void onFailure(Throwable t) {
                    CharSequence text = "Wrong username or password.";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
                public void onSuccess(User u) {
                    CharSequence text = "Welcome back," + u.getUserName() + ".";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                    LoginActivity.this.startActivity(new Intent(LoginActivity.this,
                            SessionsActivity.class));
                    LoginActivity.this.finish();
                }
            });
     * </pre>
     * </p>
     *
     * @param userid userID of the Kinvey User
     * @param password password of the Kinvey user
     * @param callback An implementation of the AbstractClient Callback that returns a valid user object
     */
    public void login(String userid, String password, KinveyUserCallback callback) {
        new Login(userid, password, callback).execute();
    }

    /**
     * Asynchronous Facebook login.
     * <p>Constructs an asynchronous request to log in a Facebook user and returns the associated User object via a
     * KinveyUserCallback.  A valid Facebook access token must be obtained via the Facebook OAuth API and passed to this
     * method.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
            kinveyClient.user().loginFacebook(accessToken, new KinveyUserCallback() {

                public void onFailure(Throwable e) {
                    error(progressDialog, "Kinvey: " + e.getMessage());
                    Log.e(TAG, "failed Kinvey facebook login", e);
                }

                public void onSuccess(User u) {
                    Log.d(TAG, "successfully logged in with facebook");
                }
            });
     * </pre>
     * </p>
     *
     * @param accessToken Facebook-generated access token.
     * @param callback An implementation of the AbstractClient Callback that returns a valid user object
     */
    public void loginFacebook(String accessToken, KinveyUserCallback callback) {
        new Login(accessToken, LoginType.FACEBOOK, callback).execute();
    }

    /**
     * Asynchronous Google login.
     *
     * <p>
     * Constructs an asynchronous request to log in a Google user and returns the associated User object via a
     * KinveyUserCallback.  A valid Google access token must be obtained via the Google OAuth API and passed to this
     * method.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
            kinveyClient.user().loginGoogle(accessToken, new KinveyUserCallback() {

                public void onFailure(Throwable e) {
                    error(progressDialog, "Kinvey: " + e.getMessage());
                    Log.e(TAG, "failed Kinvey facebook login", e);
                }

                public void onSuccess(User u) {
                    Log.d(TAG, "successfully logged in with facebook");
                }
            });
     * </pre>
     * </p>
     *
     * @param accessToken Google-generated access token.
     * @param callback KinveyUserCallback
     */
    public void loginGoogle(String accessToken, KinveyUserCallback callback)  {
        new Login(accessToken, LoginType.GOOGLE, callback).execute();
    }

    /**
     * Asynchronous Twitter login.
     *
     * <p>
     * Constructs an asynchronous request to log in a Twitter user and returns the associated User object via a
     * KinveyUserCallback.  A valid Twitter access token, access secret, consumer key, and consumer secret must be
     * obtained via the Twitter OAuth API and passed to this method.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
        kinveyClient.user().loginTwitter(accessToken, accessSecret, TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET,
                new KinveyUserCallback() {

            public void onFailure(Throwable e) {
                Log.e(TAG, "Failed Kinvey login", e)
            };

            public void onSuccess(User r) {
                Log.e(TAG, "Successfully logged in via Twitter");
            }
        });
     *
     * @param accessToken Twitter-generated access token
     * @param accessSecret Twitter-generated access secret
     * @param consumerKey Twitter supplied developer consumer key
     * @param consumerSecret Twitter supplied developer consumer secret
     * @param callback An implementation of the AbstractClient Callback that returns a valid user object
     */
    public void loginTwitter(String accessToken, String accessSecret, String consumerKey, String consumerSecret,
                             KinveyUserCallback callback)  {
        new Login(accessToken, accessSecret, consumerKey, consumerSecret, LoginType.TWITTER, callback).execute();
    }

    /**
     *  Asynchronous Linked In login.
     *
     * <p>
     * Constructs an asynchronous request to log in a Linked In user and returns the associated User object via a
     * KinveyUserCallback.  A valid Linked In access token, access secret, consumer key, and consumer secret must be
     * obtained via the Linked In OAuth API and passed to this method.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
        kinveyClient.user().loginLinkedIn(accessToken, accessSecret, LINKEDIN_CONSUMER_KEY, LINKEDIN_CONSUMER_SECRET,
                new KinveyUserCallback() {

        public void onFailure(Throwable e) {
            Log.e(TAG, "Failed Kinvey login", e)
        };

        public void onSuccess(User r) {
            Log.e(TAG, "Successfully logged in via Linked In");
        }
    });
     *
     * @param accessToken Linked In-generated access token
     * @param accessSecret Linked In-generated access secret
     * @param consumerKey Linked In supplied developer consumer key
     * @param consumerSecret Linked In supplied developer consumer secret
     * @param callback An implementation of the AbstractClient Callback that returns a valid user object
     */
    public void loginLinkedIn(String accessToken, String accessSecret, String consumerKey, String consumerSecret,
                              KinveyUserCallback callback) {
        new Login(accessToken, accessSecret, consumerKey, consumerSecret, LoginType.LINKED_IN, callback).execute();
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
    void retrieveMetadata(KinveyUserCallback callback) {
        new RetrieveMetaData(callback).execute();
    }

    /**
     * Asynchronous method to create a new Kinvey User.
     * <p>
     * Constructs an asynchronous request to create a Kinvey user with username and password, and returns the associated
     * User object via a KinveyUserCallback.  All metadata that is added to the user object prior to creating the user
     * will be persisted to the Kinvey backend.
     * </p>
     *
     * <p>
     * Sample Usage:
     * <pre>
        kinveyClient.user().put("State","MA");
        kinveyClient.user().put("Age", 25);
        kinveyClient.user().create(mEditUserName.getText().toString(), mEditPassword.getText().toString(),
                new KinveyUserCallback() {

            public void onFailure(Throwable t) {
                CharSequence text = "Unable to create account.";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }

            public void onSuccess(User u) {
                CharSequence text = "Welcome " + u.getUserName() + ".";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
     * </pre>
     * </p>
     *
     * @param username username of the Kinvey User
     * @param password password of the Kinvey user
     * @param callback An implementation of the AbstractClient Callback that returns a valid user object
     */
    public void create(String username, String password, KinveyUserCallback callback) {
        Log.v(Client.TAG, "creating");
        new Create(username, password, callback).execute();
    }

    /**
     * This method is primarily used for testing
     *
     * @return
     */
    @Override
    protected Client getClient() {
        return (Client) super.getClient();
    }

    /**
     * Asynchronous Call to Delete the current user from the Kinvey backend
     * <p>
     * Constructs an asynchronous request to delete the current Kinvey user.  The hardDelete flag determines whether
     * the user is simply marked as inactive or completely erased from the Kinvey backend.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
        User user = kinveyClient.getActiveUser();
        user.delete(new KinveyClientCallback<User.Delete>() {
            @Override
            public void onFailure(Throwable e) { ... }
            @Override
            public void onSuccess(Void v) { ... }
        });
     * </pre>
     *
     * @param hardDelete Erases user from Kinvey backend if true; inactivates the user if false.
     * @param callback KinveyClientCallback containing a deleted User instance.
     * @param <T>
     */
    public<T> void delete(Boolean hardDelete, KinveyClientCallback<T> callback) {
        // TODO:  Check callback
        new Delete(hardDelete, callback).execute();
    }

    /**
     * Asynchronous Call to Retrieve (refresh) the current user
     * <p>
     * Constructs an asynchronous request to refresh current user's data via the Kinvey back-end.
     * </p>
     * <p>
     * Sample Usage:
     * </p>
     * <pre>
        User user = kinveyClient.getActiveUser();
        user.retrieve(new KinveyClientCallback<User.Retrieve>() {
            @Override
            public void onFailure(Throwable e) { ... }
            @Override
            public void onSuccess(Void v) { ... }
        });
     * </pre>
     *
     * @param callback KinveyClientCallback containing a refreshed User instance.
     * @param <T>
     */
    public<T> void retrieve(KinveyClientCallback<T> callback) {
        new Retrieve(callback).execute();
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
         User user = kinveyClient.getActiveUser();
         user.retrieve(Query query, new KinveyClientCallback<User.Retrieve>() {
            @Override
            public void onFailure(Throwable e) { ... }
            @Override
            public void onSuccess(Void v) { ... }
        });
     * </pre>
     *
     * @param callback KinveyClientCallback for retrieved users.
     * @param <T>
     */
    public<T> void retrieve(Query q, KinveyClientCallback<T> callback) {
        // TODO:  Correct callback?
        new Retrieve(q, callback).execute();
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
        User user = kinveyClient.getActiveUser();
        user.update(new KinveyClientCallback<User.Update>() {
            @Override
            public void onFailure(Throwable e) { ... }
            @Override
            public void onSuccess(Void v) { ... }
        });
     * </pre>
     *
     * @param callback KinveyClientCallback containing an updated User instance.
     * @param <T>
     */
    public<T> void update(KinveyClientCallback<T> callback) {
        new Update(callback).execute();
    }

    /**
     * Asynchronous Call to initiate a Password Reset request
     * <p>
     * The reset password request initiates a server-side process to reset a user's password.  Once executed, a
     * success callback is initiated.  The user is then emailed by the server to receive the password reset.  The user's
     * email address must be stored in a property named 'email' in the User collection.
     * </p>
     * <p>Sample Usage:
     * <pre>
        kinveyClient.resetPassword(new KinveyClientCallback<User>() {
            @Override
            public void onFailure(Throwable e) { ... }
            @Override
            public void onSuccess() { ... }
        });
     * </pre></p>
     * @param callback KinveyClientCallback
     * @param <T>
     */
    public<T> void resetPassword(KinveyClientCallback<T> callback) {
        new ResetPassword(callback).execute();
    }

    /**
     * Asynchronous Call to initiate a Password Reset request
     * <p>
     * The reset password request initiates a server-side process to reset a user's password.  Once executed, a
     * success callback is initiated.  The user is then emailed by the server to receive the password reset.  The user's
     * email address must be stored in a property named 'email' in the User collection.
     * </p>
     * <p>Sample Usage:
     * <pre>
        kinveyClient.sendEmailVerification(new KinveyClientCallback<User>() {
            @Override
            public void onFailure(Throwable e) { ... }
            @Override
            public void onSuccess() { ... }
        });
     * </pre></p>
     * @param callback KinveyClientCallback
     * @param <T>
     */
    public<T> void sendEmailVerification(KinveyClientCallback<T> callback) {
        new EmailVerification(callback).execute();
    }

    /**
     * Register current user for push notifications
     * <p>
     * The registerPush method registers the current user for Push notifications.  Push must have already been activated
     * for the current application and a logged-in user context must exist.  This must be called when enabling
     * push for a user on a device.  Once called, it does not have to be called again unless push has been explicitly
     * disabled for a user, the app has been uninstalled / reinstalled, or app data has been cleared.
     * </p>
     * <p>Sample Usage:
     * <pre>
        kinveyClient.push().initialize(pushOptions,myApplication);
        kinveyClient.user().registerPush();
     * </pre></p>
     */
    public void registerPush() {
        // In the case where the app has no named users, while initializing push they'll
        // need a current user to set the apid field
        if (!isUserLoggedIn()) {
            return; //defers push
        }

        String pushId = getClient().push().getPushId();
        if (pushId != null) {
            // need to retrieve any existing properties to append apid
            final Object obj = this.get("_apids");
            final ArrayList<String> apidList = new ArrayList<String>();
            if (obj == null) {
                apidList.add(pushId);
            } else {
                if (obj instanceof ArrayList<?>) {
                    final ArrayList<?> objArr = (ArrayList<?>) obj;
                    boolean exists = false;
                    for (final Object itemObj : objArr) {
                        if (itemObj instanceof String) {
                            final String apid = (String) itemObj;
                            if (pushId.equals(apid)) {
                                exists = true;
                            }
                            apidList.add(apid);
                        }
                    }
                    if (exists == false) {
                        apidList.add(pushId);
                    }
                }
            }
            // TODO:  Set PushID attribute
            this.put("_apids", apidList);
            try {
                this.updateBlocking().execute();
            } catch (IOException ex) {}
            // TODO:  Implement callbacks for User Update on separate thread - still async for now
        }
    }

    /**
     * Asynchronous call to register current user for push notifications
     * <p>
     * Constructs an Asynchronous request to register the current user for Push notifications.  Push must have already been activated
     * for the current application and a logged-in user context must exist.  This must be called when enabling
     * push for a user on a device.  Once called, it does not have to be called again unless push has been explicitly
     * disabled for a user, the app has been uninstalled / reinstalled, or app data has been cleared.
     * </p>
     * <p>Sample Usage:
     * <pre>
        kinveyClient.push().initialize(pushOptions,myApplication);
        kinveyClient.user().registerPush(new KinveyClientCallback<Void> {
           onSuccess(Void v) { ... }
          onFailure(Throwable t) { ... }
        });
     * </pre></p>
     *
     * @param callback KinveyClientCallback
     */
    public void registerPush(KinveyClientCallback<Void> callback) {
        new RegisterPush(callback).execute();
    }

    /**
     * Unregisters current user from receiving push notifications
     * <p>
     * The unregisterPush method removes the registration for the current user from Push notifications.
     * Push must have already been activated for the current application and a logged-in user context must exist.
     * Once called, a user on a specific device will no longer receive push notifications for the app until explicitly
     * reenabled.
     * </p>
     * <p>Sample Usage:
     * <pre>
        kinveyClient.push().initialize(pushOptions,myApplication);
        kinveyClient.user().unregisterPush();
     * </pre></p>
     *
     * @param callback KinveyClientCallback
     */
    public void unregisterPush(KinveyClientCallback<Void> callback) {
        new UnregisterPush(callback).execute();
    }

    /**
     * Asynchronous call to unregiseter current user for push notifications
     * <p>
     * Constructs an Asynchronous request to remove the registration for the current user from Push notifications.
     * Push must have already been activated for the current application and a logged-in user context must exist.
     * Once called, a user on a specific device will no longer receive push notifications for the app until explicitly
     * reenabled.
     * </p>
     * <p>Sample Usage:
     * <pre>
        kinveyClient.push().initialize(pushOptions,myApplication);
        kinveyClient.user().unregisterPush(new KinveyClientCallback<Void> {
            onSuccess(Void v) { ... }
            onFailure(Throwable t) { ... }
        });
     * </pre></p>
     */
    public void unregisterPush() {
        // In the case where the app has no named users, while initializing push they'll
        // need a current user to set the apid field
        if (!isUserLoggedIn()) {
            return; //defers push
        }

        String pushId = getClient().push().getPushId();
        if (pushId != null) {
            // need to retrieve any existing properties to append apid
            final Object obj = this.get("_apids");
            final ArrayList<String> apidList = new ArrayList<String>();
            if (obj != null) {
                if (obj instanceof ArrayList<?>) {
                    final ArrayList<?> objArr = (ArrayList<?>) obj;
                    boolean exists = false;
                    for (final Object itemObj : objArr) {
                        if (itemObj instanceof String) {
                            final String apid = (String) itemObj;
                            if (pushId.equals(apid)) {
                                exists=true;
                            }
                        }
                    }
                    if (exists) {
                        apidList.remove(pushId);
                    }
                }
            }
            // TODO:  Set PushID attribute
            this.put("_apids", apidList);
            try {
                this.update().execute();
            } catch (IOException ex) {}
            // TODO:  Implement callbacks for User Update on separate thread - still async for now

        }
    }

    private class Login extends AsyncClientRequest<User> {

        String username;
        String password;
        String accessToken;
        String accessSecret;
        String consumerKey;
        String consumerSecret;
        Credential credential;
        LoginType type;

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

        private Login(String accessToken, String accessSecret, String consumerKey, String consumerSecret,
                      LoginType type, KinveyClientCallback callback) {
            super(callback);
            this.accessToken = accessToken;
            this.accessSecret = accessSecret;
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.type=type;
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
                    return AsyncUser.this.loginBlocking().execute();
                case KINVEY:
                    return AsyncUser.this.loginBlocking(username, password).execute();
                case FACEBOOK:
                    return AsyncUser.this.loginFacebookBlocking(accessToken).execute();
                case GOOGLE:
                    return AsyncUser.this.loginGoogleBlocking(accessToken).execute();
                case TWITTER:
                    return AsyncUser.this.loginTwitterBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
                case LINKED_IN:
                    return AsyncUser.this.loginLinkedInBlocking(accessToken, accessSecret, consumerKey, consumerSecret).execute();
                case CREDENTIALSTORE:
                    return AsyncUser.this.login(credential).execute();
            }
            return null;
        }
    }

    private class Create extends AsyncClientRequest<User> {
        String username;
        String password;

        private Create(String username, String password, KinveyUserCallback callback) {
            super(callback);
            this.username=username;
            this.password=password;
        }

        @Override
        protected User executeAsync() throws IOException {
            return AsyncUser.this.createBlocking(username, password).execute();
        }
    }

    private class Delete extends AsyncClientRequest<User.Delete> {
        boolean hardDelete;

        private Delete(boolean hardDelete, KinveyClientCallback callback) {
            super(callback);
            this.hardDelete = hardDelete;
        }

        @Override
        protected User.Delete executeAsync() throws IOException {
            return AsyncUser.this.deleteBlocking(hardDelete);
        }
    }

    private class Retrieve<T> extends AsyncClientRequest<T> {

        private Query query = null;

        private Retrieve(KinveyClientCallback callback) {
            super(callback);
        }

        private Retrieve(Query query, KinveyClientCallback callback){
            super(callback);
            this.query = query;
        }

        @Override
        public T executeAsync() throws IOException {
            if (query == null){
                return (T) AsyncUser.this.retrieveBlocking().execute();
            }else{
                return (T) AsyncUser.this.retrieveBlocking(query).execute();
            }

        }
    }

    private class RegisterPush extends AsyncClientRequest<Void> {

        private RegisterPush(KinveyClientCallback<Void> callback) {
            super(callback);
        }

        @Override
        protected Void executeAsync() throws IOException {
            AsyncUser.this.registerPush();
            return null;
        }
    }

    private class UnregisterPush extends AsyncClientRequest<Void> {

        private UnregisterPush(KinveyClientCallback<Void> callback) {
            super(callback);
        }

        @Override
        protected Void executeAsync() throws IOException {
            AsyncUser.this.unregisterPush();
            return null;
        }
    }

    private class RetrieveMetaData extends AsyncClientRequest<User> {

        private RetrieveMetaData(KinveyUserCallback callback) {
            super(callback);
        }

        @Override
        protected User executeAsync() throws IOException {
            return AsyncUser.this.retrieveMetadataBlocking();
        }
    }

    private class Update extends AsyncClientRequest<User.Update> {

        private Update(KinveyClientCallback callback) {
            super(callback);
        }

        @Override
        protected User.Update executeAsync() throws IOException {
            return AsyncUser.this.updateBlocking();
        }
    }

    private class ResetPassword extends AsyncClientRequest<User.ResetPassword> {

        private ResetPassword(KinveyClientCallback callback) {
            super(callback);
        }

        @Override
        protected User.ResetPassword executeAsync() throws IOException {
            return AsyncUser.this.resetPasswordBlocking();
        }
    }

    private class EmailVerification extends AsyncClientRequest<User.EmailVerification> {

        private EmailVerification(KinveyClientCallback callback) {
            super(callback);
        }

        @Override
        protected User.EmailVerification executeAsync() throws IOException {
            return AsyncUser.this.sendEmailVerificationBlocking();
        }
    }
}

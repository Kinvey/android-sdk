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


import android.content.Context;
import android.util.Log;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.offline.OfflineAppData;
import com.kinvey.android.push.AbstractPush;
import com.kinvey.android.push.UrbanAirshipPush;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.LinkedResources.LinkedGenericJson;
import com.kinvey.java.User;
import com.kinvey.java.auth.ClientUsers;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.CredentialManager;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;

/**
 * This class is an implementation of a {@link com.kinvey.java.AbstractClient} with default settings for the Android operating
 * system.
 *
 * <p>
 * Functionality is provided through a series of factory methods, which return the various API service wrappers.
 * These factory methods are all synchronized, and access is thread-safe.  Once a service API has been retrieved,
 * it can be used to instantiate and execute asynchronous service calls.  The {@link Client.Builder} is not thread-safe.
 * </p>
 *
 * <p>
 * The calling class can pass in a callback instance. Upon completion of the service call-- either success or failure will
 * be invoked. The Callback mechanism is null-safe, so {@code null} can be passed if no callbacks are required.
 * </p>
 *
 * <p>
 * All callback methods are <i>null-safe</i>, the callback will be ignored if {@code null} is passed in.
 * </p>
 *
 * @author edwardf
 * @author m0rganic
 * @author mjsalinger
 * @since 2.0
 * @version $Id: $
 */
public class Client extends AbstractClient {

    /** global TAG used in Android logging **/
    public final static String TAG = "Kinvey - Client";

    final static Logger LOGGER = Logger.getLogger(Client.class.getSimpleName());

    private Context context = null;

    private ConcurrentHashMap<String, AsyncAppData> appDataInstanceCache;
    private ConcurrentHashMap<String, AsyncLinkedData> linkedDataInstanceCache;
    private ConcurrentHashMap<String, OfflineAppData> offlineInstanceCache;
    private AbstractPush pushProvider;
    private AsyncUserDiscovery userDiscovery;
    private AsyncFile file;
    private AsyncUserGroup userGroup;
    private ClientUsers clientUsers;
    private AsyncUser currentUser;

    /**
     * Protected constructor.  Public AbstractClient.Builder class is used to construct the AbstractClient, so this method shouldn't be
     * called directly.
     *
     * @param transport the transport
     * @param httpRequestInitializer standard request initializer
     * @param rootUrl root url to base all requests
     * @param servicePath standard service path for all requests
     * @param objectParser object parse used in all requests
     * @param kinveyRequestInitializer a {@link com.kinvey.java.core.KinveyClientRequestInitializer} object.
     * @param store a {@link com.kinvey.java.auth.CredentialStore} object.
     */
    protected Client(HttpTransport transport, HttpRequestInitializer httpRequestInitializer, String rootUrl,
                     String servicePath, JsonObjectParser objectParser,
                     KinveyClientRequestInitializer kinveyRequestInitializer, CredentialStore store) {
        super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, store);
    }

    /**
     * <p>Setter for the field <code>context</code>.</p>
     *
     * @param context a {@link android.content.Context} object.
     */
    public void setContext(Context context) {
        if (context != null) {
            this.context = context.getApplicationContext();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Creates a new instance of the AppData class or returns the existing instance.
     */

    /**
     * AppData factory method
     * <p>
     * Returns an instance of {@link com.kinvey.java.AppData} for the supplied collection.  A new instance is created for each collection, but
     * only one instance of {@link AsyncAppData} is created per collection.  The method is Generic and takes an instance of a
     * {@link com.google.api.client.json.GenericJson} entity type that is used for fetching/saving of {@link com.kinvey.java.AppData}.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
        AppData<myEntity> myAppData = kinveyClient.appData("entityCollection", myEntity.class);
     }
     });
     * </pre>
     * </p>
     *
     * @param collectionName The name of the collection
     * @param myClass The class that defines the entity of type {@link com.google.api.client.json.GenericJson} used
     *                for saving and fetching of data
     * @param <T> Generic of type {@link com.google.api.client.json.GenericJson} of same type as myClass
     * @return Instance of {@link com.kinvey.java.AppData} for the defined collection
     */
    public <T> AsyncAppData<T> appData(String collectionName, Class<T> myClass) {
        synchronized (lock) {
            Preconditions.checkNotNull(collectionName, "collectionName must not be null");
            if (appDataInstanceCache == null) {
                appDataInstanceCache = new ConcurrentHashMap<String, AsyncAppData>();
            }
            if (!appDataInstanceCache.containsKey(collectionName)) {
                appDataInstanceCache.put(collectionName, new AsyncAppData(collectionName, myClass, this));
            }
            return appDataInstanceCache.get(collectionName);
        }
    }

    /**
     * LinkedData factory method
     * <p>
     * Returns an instance of {@link AsyncLinkedData} for the supplied collection.  A new instance is created for each collection, but
     * only one instance of LinkedData is created per collection.  The method is Generic and takes an instance of a
     * {@link LinkedGenericJson} entity type that is used for fetching/saving of {@link AsyncLinkedData}.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     LinkedData<myEntity> myAppData = kinveyClient.linkedData("entityCollection", myEntity.class);
     }
     });
     * </pre>
     * </p>
     *
     * @param collectionName The name of the collection
     * @param myClass The class that defines the entity of type {@link LinkedGenericJson} used for saving and fetching of data
     * @param <T> Generic of type {@link com.google.api.client.json.GenericJson} of same type as myClass
     * @return Instance of {@link AsyncLinkedData} for the defined collection
     */
    public <T extends LinkedGenericJson> AsyncLinkedData<T> linkedData(String collectionName, Class<T> myClass) {
        synchronized (lock) {
            Preconditions.checkNotNull(collectionName, "collectionName must not be null");
            if (linkedDataInstanceCache == null) {
                linkedDataInstanceCache = new ConcurrentHashMap<String, AsyncLinkedData>();
            }
            if (!linkedDataInstanceCache.containsKey(collectionName)) {
                linkedDataInstanceCache.put(collectionName, new AsyncLinkedData(collectionName, myClass, this));
            }
            return linkedDataInstanceCache.get(collectionName);
        }
    }

    /**
     * OfflineAppData factory method
     * <p>
     * Returns an instance of {@link OfflineAppData} for the supplied collection.  A new instance is created for each collection, but
     * only one instance of {@link OfflineAppData} is created per collection.  The method is Generic and takes an instance of a
     * {@link com.google.api.client.json.GenericJson} entity type that is used for fetching/saving of {@link OfflineAppData}.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     OfflineAppData<myEntity> myAppData = kinveyClient.offlineAppData("entityCollection", myEntity.class);
     }
     });
     * </pre>
     * </p>
     *
     *
     *
     * @param collectionName The name of the collection
     * @param myClass The class that defines the entity of type {@link com.google.api.client.json.GenericJson} used
     *                for saving and fetching of data
     * @return Instance of {@link OfflineAppData} for the defined collection
     */
    public <T> OfflineAppData<T> offlineAppData(String collectionName, Class<T> myClass) {
        synchronized (lock) {

            Preconditions.checkNotNull(collectionName, "collectionName must not be null");
            if (offlineInstanceCache == null) {
                offlineInstanceCache = new ConcurrentHashMap<String, OfflineAppData>();
            }
            if (!offlineInstanceCache.containsKey(collectionName)) {
                offlineInstanceCache.put(collectionName, new OfflineAppData(collectionName, myClass, this, context));
            }
            return offlineInstanceCache.get(collectionName);
        }
    }

    /**
     * File factory method
     * <p>
     * Returns an instance of {@link com.kinvey.java.File} for uploading and downloading of files.  Only one instance is created for each
     * instance of the Kinvey client.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     File myFile = kinveyClient.file();
     }
     });
     * </pre>
     * </p>
     *
     * @return Instance of {@link com.kinvey.java.File} for the defined collection
     */
    @Override
    public AsyncFile file() {
        synchronized (lock) {
            if (file == null) {
                file = new AsyncFile(this);
            }
            return file;
        }
    }

    /**
     * UserDiscovery factory method
     * <p>
     * Returns the instance of {@link com.kinvey.java.UserDiscovery} used for searching for users. Only one instance of
     * UserDiscovery is created for each instance of the Kinvey Client.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     UserDiscovery myUserDiscovery = kinveyClient.userDiscovery();
     }
     });
     * </pre>
     * </p>
     *
     * @return Instance of {@link com.kinvey.java.UserDiscovery} for the defined collection
     */
    @Override
    public AsyncUserDiscovery userDiscovery() {
        synchronized (lock) {
            if (userDiscovery == null) {
                userDiscovery = new AsyncUserDiscovery(this,
                        (KinveyClientRequestInitializer) this.getKinveyRequestInitializer());
            }
            return userDiscovery;
        }
    }

    /**
     * UserGroup factory method
     * <p>
     * Returns the instance of {@link com.kinvey.java.UserGroup} used for managing user groups. Only one instance of
     * UserGroup is created for each instance of the Kinvey Client.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     UserGroup myUserGroup = kinveyClient.userGroup();
     }
     });
     * </pre>
     * </p>
     *
     * @return Instance of {@link com.kinvey.java.UserGroup} for the defined collection
     */
    @Override
    public AsyncUserGroup userGroup() {
        synchronized (lock) {
            if (userGroup == null) {
                userGroup = new AsyncUserGroup(this,
                        (KinveyClientRequestInitializer) this.getKinveyRequestInitializer());
            }
            return userGroup;
        }

    }

    /** {@inheritDoc} */
    @Override
    protected ClientUsers getClientUsers() {
        synchronized (lock) {
            if (this.clientUsers == null) {
                this.clientUsers = AndroidClientUsers.getClientUsers(this.context);
            }
            return this.clientUsers;
        }

    }

    private boolean userExists() {
        String user = getClientUsers().getCurrentUser();
        return (user != null && !user.isEmpty());
    }

    /**
     * User factory method
     * <p>
     * Returns the instance of {@link com.kinvey.java.User} that contains the current active user.  If no active user context
     * has been established, the {@link com.kinvey.java.User} object returned will be instantiated and empty.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     User currentUser = kinveyClient.currentUser();
     }
     });
     * </pre>
     * </p>
     *
     * @return Instance of {@link com.kinvey.java.User} for the defined collection
     */
    @Override
    public AsyncUser user() {
        synchronized (lock) {
            if (getCurrentUser() == null) {
                String appKey = ((KinveyClientRequestInitializer) getKinveyRequestInitializer()).getAppKey();
                String appSecret = ((KinveyClientRequestInitializer) getKinveyRequestInitializer()).getAppSecret();
                setCurrentUser(new AsyncUser(this, new KinveyAuthRequest.Builder(getRequestFactory().getTransport(), getJsonFactory(),
                        appKey, appSecret, null)));
            }
            return (AsyncUser) getCurrentUser();
        }
    }


    /**
     * Push factory method
     * <p>
     * Returns the instance of {@link AbstractPush} used for configuring Push. Only one instance of
     * {@link AbstractPush} is created for each instance of the Kinvey Client.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     AbstractPush myPush = kinveyClient.push();
     }
     });
     * </pre>
     * </p>
     *
     * @return Instance of {@link AbstractPush} for the defined collection
     */
    public AbstractPush push() {
        synchronized (lock) {
            if (pushProvider == null) {
                pushProvider = new UrbanAirshipPush(this);
            }
            return pushProvider;
        }
    }

    /**
     * Asynchronous Ping service method
     * <p>
     * Performs an authenticated ping against the configured Kinvey backend.  This method must be executed in the context
     * of a logged-in active user.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
        kinveyClient.ping(new KinveyPingCallback() {
            onSuccess(Boolean result) { ... }
            onFailure(Throwable error) { ... }
        }
     * </pre>
     * </p>
     *
     * @param callback object of type {@link KinveyPingCallback} to be notified when request completes
     */
    public void ping(KinveyPingCallback callback) {
        new Ping(callback).execute();
    }


    private class Ping extends AsyncClientRequest<Boolean> {
        private Ping(KinveyPingCallback callback) {
            super(callback);
        }

        @Override
        protected Boolean executeAsync() throws IOException {
            return Client.this.pingBlocking();
        }
    }

    /**
     * Create a client for interacting with Kinvey's services from an Android Activity.
     * <pre>
     * Client myClient =  new Client.Builder(appKey, appSecret, getContext()).build();
     * <pre/>
     * All features of the library are be accessed through an instance of a client.
     * <p/>
     * It is recommended to maintain a single instance of a {@code Client} while developing with Kinvey, either in an
     * Activity, a Service, or an Application.
     * <p/>
     * This Builder class is not thread-safe.
     */
    public static class Builder extends AbstractClient.Builder {

        private Context context = null;

        /**
         * Use this constructor to create a AbstractClient.Builder, which can be used to build a Kinvey AbstractClient with defaults
         * set for the Android Operating System.
         * <p>
         * This constructor does NOT support push notification functionality.
         * If push notifications are necessary, use a properties file and the overloaded constructor.
         * </p>
         *
         * @param appKey Your Kinvey Application Key
         * @param appSecret Your Kinvey Application Secret
         * @param context Your Android Application Context
         */
        public Builder(String appKey, String appSecret, Context context) {
            super(AndroidHttp.newCompatibleTransport(), AndroidJson.newCompatibleJsonFactory(), null
                    , new KinveyClientRequestInitializer(appKey, appSecret, new KinveyHeaders(context)));
            this.context = context.getApplicationContext();
            try {
                this.setCredentialStore(new AndroidCredentialStore(this.context));
            } catch (Exception ex) {
                //TODO Add handling
            }
        }


        /**
         * Use this constructor to create a Client.Builder, which can be used to build a Kinvey Client with defaults
         * set for the Android Operating System.
         * <p>
         * This constructor requires a  properties file, containing configuration for your Kinvey Client.
         * Save this file within your Android project, at:  assets/kinvey.properties
         * </p>
         * <p>
         * This constructor provides support for push notifications.
         * </p>
         * <p>
         * <a href="http://devcenter.kinvey.com/android/guides/getting-started#InitializeClient">Kinvey Guide for initializing Client with a properties file.</a>
         * </p>
         *
         * @param context - Your Android Application Context
         *
         */
        public Builder(Context context) {
            super(AndroidHttp.newCompatibleTransport(), AndroidJson.newCompatibleJsonFactory(), null);

            try {
                final InputStream in = context.getClassLoader().getResourceAsStream(getAndroidPropertyFile());
                super.getProps().load(in);
            } catch (IOException e) {
                Log.w(TAG, "Couldn't load property, trying another approach.  Ensure there is a file:  myProject/assets/kinvey.properties which contains: app.key and app.secret.");
                super.loadPropertiesFromDisk(getAndroidPropertyFile());
            } catch (NullPointerException ex){
                Log.e(TAG, "Builder cannot find properties file at assets/kinvey.properties.  Ensure this file exists, containing app.key and app.secret!");
                Log.e(TAG, "If you are using push notification or offline storage you must configure your client to load from properties, see our online guides for instructions.");
                throw new RuntimeException("Builder cannot find properties file at assets/kinvey.properties.  Ensure this file exists, containing app.key and app.secret!");
            }


            String appKey = Preconditions.checkNotNull(super.getString(Option.APP_KEY), "appKey must not be null");
            String appSecret = Preconditions.checkNotNull(super.getString(Option.APP_SECRET), "appSecret must not be null");

            KinveyClientRequestInitializer initializer = new KinveyClientRequestInitializer(appKey, appSecret, new KinveyHeaders(context));
            this.setKinveyClientRequestInitializer(initializer);

            this.context = context.getApplicationContext();
            try {
                this.setCredentialStore(new AndroidCredentialStore(this.context));
            } catch (AndroidCredentialStoreException ex) {
                Log.e(TAG, "Credential store was in a corrupted state and had to be rebuilt", ex);
            } catch (IOException ex) {
                Log.e(TAG, "Credential store failed to load", ex);
            }
        }


        /**
         * @return an instantiated Kinvey Android Client,
         * which contains factory methods for accessing various functionality.
         */
        @Override
        public Client build() {
            final Client client = new Client(getTransport(),
                    getHttpRequestInitializer(), getRootUrl(),
                    getServicePath(), getObjectParser(), getKinveyClientRequestInitializer(), getCredentialStore());
            client.setContext(context);
            client.clientUsers = AndroidClientUsers.getClientUsers(context);
            try {
                Credential credential = retrieveUserFromCredentialStore(client);
                if (credential != null) {
                    loginWithCredential(client, credential);
                }

            } catch (AndroidCredentialStoreException ex) {
                Log.e(TAG, "Credential store was in a corrupted state and had to be rebuilt", ex);
                client.setCurrentUser(null);
            } catch (IOException ex) {
                Log.e(TAG, "Credential store failed to load", ex);
                client.setCurrentUser(null);
            }
            return client;

        }

        private Credential retrieveUserFromCredentialStore(Client client)
                throws AndroidCredentialStoreException, IOException {
            Credential credential = null;
            if (!client.user().isUserLoggedIn()) {
                String userID = client.getClientUsers().getCurrentUser();
                if (userID != null && !userID.isEmpty()) {
                    AndroidCredentialStore store = new AndroidCredentialStore(context);
                    CredentialManager manager = new CredentialManager(store);
                    credential = manager.loadCredential(userID);
                }
            }
            return credential;
        }

        private void loginWithCredential(final Client client, Credential credential) {
            getKinveyClientRequestInitializer().setCredential(credential);
            try {
                client.user().login(credential).execute();
            } catch (IOException ex) {
                Log.e(TAG, "Could not retrieve user Credentials");
            }

            client.user().retrieveMetadata(new KinveyUserCallback() {
                @Override
                public void onSuccess(User result) {
                    client.setCurrentUser(result);
                }

                @Override
                public void onFailure(Throwable error) {
                    //
                }
            });
        }

        /**
         * The default kinvey settings filename {@code assets/kinvey.properties}
         *
         * @return {@code assets/kinvey.properties}
         */
        protected static String getAndroidPropertyFile() {
            return "assets/kinvey.properties";
        }
    }
}


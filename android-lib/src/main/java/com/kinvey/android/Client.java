/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */

package com.kinvey.android;


import android.content.Context;
import android.support.annotation.NonNull;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.ExponentialBackOffPolicy;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kinvey.android.cache.RealmCacheManager;
import com.kinvey.android.callback.KinveyClientBuilderCallback;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.model.User;
import com.kinvey.android.push.AbstractPush;
import com.kinvey.android.push.GCMPush;
import com.kinvey.android.store.FileStore;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.ClientExtension;
import com.kinvey.java.Constants;
import com.kinvey.java.Logger;
import com.kinvey.java.auth.ClientUser;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.CredentialManager;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.network.NetworkFileManager;
import com.kinvey.java.store.BaseUserStore;
import com.kinvey.java.store.StoreType;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.realm.Realm;

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
public class Client<T extends User> extends AbstractClient<T> {

    /** global TAG used in Android logging **/
    public final static String TAG = "Kinvey - Client";
    private RealmCacheManager syncCacheManager;

    private Context context = null;

//    private ConcurrentHashMap<String, DataStore> appDataInstanceCache;
//    private ConcurrentHashMap<String, LinkedDataStore> linkedDataInstanceCache;
//    private ConcurrentHashMap<String, AsyncCustomEndpoints> customeEndpointsCache;
    private AsyncCustomEndpoints customEndpoints;
    private AbstractPush pushProvider;
    private AsyncUserDiscovery userDiscovery;
    private AsyncUserGroup userGroup;
    private ClientUser clientUser;
//    private AsyncUser currentUser;
    private long syncRate;
    private long batchRate;
    private int batchSize;
    private RealmCacheManager cacheManager;

    private static KinveyHandlerThread kinveyHandlerThread;
    
    private static Client sharedInstance;
    private byte[] encryptionKey;

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
     * @param requestPolicy a {@link BackOffPolicy} for retrying HTTP Requests
     * @param context a {@link Context} android application context
     */
    protected Client(HttpTransport transport, HttpRequestInitializer httpRequestInitializer, String rootUrl,
                     String servicePath, JsonObjectParser objectParser,
                     KinveyClientRequestInitializer kinveyRequestInitializer, CredentialStore store,
                     BackOffPolicy requestPolicy, byte[] encryptionKey, Context context) {
        super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, store,
                requestPolicy);
        Logger.init(new AndroidLogger());
        sharedInstance = this;
        this.context = context;
        cacheManager = new RealmCacheManager(encryptionKey, this);
        syncCacheManager = new RealmCacheManager(encryptionKey, "sync_", this);
        this.encryptionKey = encryptionKey;
    }

    public static Client sharedInstance(){
    	return sharedInstance;
    }

    /**
     * @deprecated Renamed to {@link #setActiveUser(T)}
     */
    @Deprecated
    public void setUser(T user) {
        synchronized (lock) {
            this.user = user;
        }
    }

    @Override
    public void setActiveUser(T user) {
        synchronized (lock) {
            this.user = user;
        }
    }

    @Override
    public T getActiveUser() {
        synchronized (lock) {
            return this.user;
        }
    }

    @Override
    public void performLockDown() {
        //clear data cache and file cache
        cacheManager.clear();
        //clear sync cache
        syncCacheManager.clear();

        cacheManager = new RealmCacheManager(encryptionKey, this);
        syncCacheManager = new RealmCacheManager(encryptionKey, "sync_", this);

        List<ClientExtension> extensions = getExtensions();
        for (ClientExtension e : extensions){
            e.performLockdown(getActiveUser().getId());
        }
    }

    /**
     * Custom Endpoints factory method
     *<p>
     * Returns the instance of {@link com.kinvey.java.CustomEndpoints} used for executing RPC requests.  Only one instance
     * of Custom Endpoints is created for each instance of the Kinvey Client.
     *</p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     {@code
      CustomEndpoints myCustomEndpoints = kinveyClient.customEndpoints();
     }
     * </pre>
     * </p>
     *
     * @return Instance of {@link com.kinvey.java.UserDiscovery} for the defined collection
     */
    @Deprecated
    public AsyncCustomEndpoints customEndpoints(){
        synchronized (lock){
            return  new AsyncCustomEndpoints(GenericJson.class, this);
        }
    }
    /**
     * Custom Endpoints factory method
     *<p>
     * Returns the instance of {@link com.kinvey.java.CustomEndpoints} used for executing RPC requests.  Only one instance
     * of Custom Endpoints is created for each instance of the Kinvey Client.
     *</p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     {@code
     AsyncCustomEndpoints<MyRequestClass, MyResponseClass> endpoints = getClient().customEndpoints(MyResponseClass.class);
     }
     * </pre>
     * </p>
     *
     * @return Instance of {@link com.kinvey.java.UserDiscovery} for the defined collection
     */

/*    public <I extends GenericJson, O> AsyncCustomEndpoints<I, O> customEndpoints(Class<O> myClass) {
        synchronized (lock) {
            return new AsyncCustomEndpoints(myClass, this);
        }
    }*/

    @Override
    public AsyncCustomEndpoints customEndpoints(Class myClass) {
        synchronized (lock) {
            return new AsyncCustomEndpoints(myClass, this);
        }
    }

    @Override
    public ICacheManager getCacheManager() {
        return cacheManager;
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
     {@code
     UserDiscovery myUserDiscovery = kinveyClient.userDiscovery();
     }
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
     {@code
     UserGroup myUserGroup = kinveyClient.userGroup();
     }
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
    public ClientUser getClientUser() {
        synchronized (lock) {
            if (this.clientUser == null) {
                this.clientUser = AndroidUserStore.getUserStore(this.context);
            }
            return this.clientUser;
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
     {@code
        AbstractPush myPush = kinveyClient.push();
     }
     * </pre>
     * </p>
     * @param pushServiceClass Service class for handling push notification
     * @return Instance of {@link AbstractPush} for the defined collection
     */
    public AbstractPush push(Class pushServiceClass) {
        synchronized (lock) {
            //NOTE:  pushProvider is defined as a GCMPush in the ClientBuilder#build() method, if the user has set it in the property file.
            //ONCE Urban Airship has been officially deprecated we can remove the below lines completely (or create GCMPush inline here)
            if (pushProvider == null) {
                pushProvider = new GCMPush(this, true, "");
            }
            if (pushProvider.getPushServiceClass() == null) {
                pushProvider.setPushServiceClass(pushServiceClass);
            }
            return pushProvider;
        }
    }

 /**
     * Asynchronous Ping service method
     * <p>
     * Performs an authenticated ping against the configured Kinvey backend.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     {@code
        kinveyClient.ping(new KinveyPingCallback() {
            onSuccess(Boolean result) { ... }
            onFailure(Throwable error) { ... }
        }
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
     * How long, in milliseconds, should offline wait before retrying a failed request
     *
     * @return the number of milliseconds for offline sync to wait between failures
     */
    public long getSyncRate(){
        return this.syncRate;
    }

    public long getBatchRate(){
        return this.batchRate;
    }

    public int getBatchSize(){
        return this.batchSize;
    }

    /**
     * Get a reference to the Application Context used to create this instance of the Client
     * @return {@code null} or the live Application Context
     */
    public Context getContext(){
        return this.context;
    }

    /**
     * Create a client for interacting with Kinvey's services from an Android Activity.
     * <p/>
     * <pre>
     * Client myClient =  new Client.Builder(appKey, appSecret, getContext()).build();</pre>
     * <p/>
     * All features of the library are be accessed through an instance of a client.
     * <p/>
     * It is recommended to maintain a single instance of a {@code Client} while developing with Kinvey, either in an
     * Activity, a Service, or an Application.
     * <p/>
     * This Builder class is not thread-safe.
     */
    public static class Builder<T extends User> extends AbstractClient.Builder {

        private Context context = null;
        private KinveyUserCallback<User> retrieveUserCallback = null;
        //GCM Push Fields
        private String GCM_SenderID = "";
        private boolean GCM_Enabled = false;
        private boolean GCM_InProduction = true;
        private boolean debugMode = false;
        private long syncRate = 1000 * 60 * 10; //10 minutes
        private int batchSize = 5;
        private long batchRate = 1000L * 30L; //30 seconds
        private JsonFactory factory = AndroidJson.newCompatibleJsonFactory(AndroidJson.JSONPARSER.GSON);
        private String MICVersion;
        private String MICBaseURL;
        private boolean deltaSetCache = false;
        private Class userClass = null;
        private byte[] encryptionKey;

        /**
         * creating new HttpTransport with fix for 401 error that raise an exception MLIBZ-708
         * @return HttpTransport
         */
        @NonNull
        private static HttpTransport newCompatibleTransport(){
            /*  http://developer.android.com/intl/zh-cn/reference/javax/net/ssl/SSLSocket.html
                support for SSL/TLSv1 was disabled in the Kinvey Android SDK 3.1.3 version
                SDK 16-19 are versions where TLSv1.1 and TLSv1.2 are disabled by default
             */
            return android.os.Build.VERSION.SDK_INT >= 16 && android.os.Build.VERSION.SDK_INT <= 19 ?
                    buildSupportHttpTransport() :
                    new NetHttpTransport();
        }

        @NonNull
        private static HttpTransport buildSupportHttpTransport() {
            NetHttpTransport httpTransport;
            try {
                httpTransport = new NetHttpTransport.Builder()
                        .setProxy(null)
                        .setHostnameVerifier(null)
                        .setSslSocketFactory(new KinveySocketFactory())
                        .build();
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                httpTransport = new NetHttpTransport();
            }
            return httpTransport;
        }

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
            this(appKey, appSecret, context, newCompatibleTransport());
        }

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
         * @param transport HttpTransport
         */
        public Builder(String appKey, String appSecret, Context context, HttpTransport transport) {
            super(transport, null
                    , new KinveyClientRequestInitializer(appKey, appSecret, new KinveyHeaders(context)));
            this.setJsonFactory(factory);
            this.context = context.getApplicationContext();
            this.setRequestBackoffPolicy(new ExponentialBackOffPolicy());

            if (getCredentialStore() == null){
                try {
                    this.setCredentialStore(new AndroidCredentialStore(this.context));
                } catch (Exception ex) {
                    //TODO Add handling
                }
            }
        }

        /**
         * Use this constructor to create a Client.Builder, which can be used to build a Kinvey Client with defaults
         * set for the Android Operating System.
         * <p>
         * This constructor requires a  properties file, containing configuration for your Kinvey Client.
         * create kinvey.properties file in your assets folder according kinvey docs
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
        public Builder(Context context, HttpTransport transport) {
            super(transport, null);

            InputStream properties = null;
            try {
                properties = context.getAssets().open("kinvey.properties");
            } catch (IOException e) {
                Logger.WARNING("Couldn't load properties. Ensure there is a file: assets/kinvey.properties which is valid properties file");
                throw new RuntimeException("Builder cannot find properties file kinvey.properties in your assets.  Ensure this file exists, containing app.key and app.secret!");

            }

            Preconditions.checkNotNull(properties, "Builder cannot find properties file kinvey.properties in your assets.  Ensure this file exists, containing app.key and app.secret!");
            loadProperties(properties);

            String key;
            String secret;
            if (BuildConfig.TRAVIS) {
                key = BuildConfig.KINVEY_APP_KEY;
                secret = BuildConfig.KINVEY_APP_SECRET;
                GCM_Enabled = true;
                GCM_SenderID = BuildConfig.SENDER_ID;
            } else {
                key = Preconditions.checkNotNull(super.getString(Option.APP_KEY), "app.key must be defined in your kinvey.properties");
                secret = Preconditions.checkNotNull(super.getString(Option.APP_SECRET), "app.secret must be defined in your kinvey.properties");
            }

            KinveyClientRequestInitializer initializer = new KinveyClientRequestInitializer(key, secret, new KinveyHeaders(context));
            this.setKinveyClientRequestInitializer(initializer);

            this.context = context.getApplicationContext();
            this.setRequestBackoffPolicy(new ExponentialBackOffPolicy());
            if (getCredentialStore() == null){
                try{
                    this.setCredentialStore(new AndroidCredentialStore(this.context));
                } catch (AndroidCredentialStoreException ex) {
                    Logger.ERROR("Credential store was in a corrupted state and had to be rebuilt");
                } catch (IOException ex) {
                    Logger.ERROR("Credential store failed to load");
                }
            }
        }


        /**
         * loading additional properties from file
         * @param properties InputStream of properties file
         */
        private void loadProperties(InputStream properties) {

            try {
                super.getProps().load(properties);
            } catch (IOException e) {
                Logger.WARNING("Couldn't load properties. Please make sure that your properties file is valid");
                throw new RuntimeException("Couldn't load properties. Please make sure that your properties file is valid");
            }

            if (super.getString(Option.BASE_URL) != null) {
                this.setBaseUrl(super.getString(Option.BASE_URL));
            }

            if (super.getString(Option.INSTANCE_ID) != null) {
                this.setInstanceID(super.getString(Option.INSTANCE_ID));
            }

            if (super.getString(Option.REQUEST_TIMEOUT) != null) {
                try {
                    this.setRequestTimeout(Integer.parseInt(super.getString(Option.REQUEST_TIMEOUT)));
                } catch (Exception e) {
                    Logger.WARNING(Option.REQUEST_TIMEOUT.name() + " should have an integer value");
                }
            }

            if (super.getString(Option.PORT) != null) {
                this.setBaseUrl(String.format("%s:%s", super.getBaseUrl(), super.getString(Option.PORT)));
            }

            if (super.getString(Option.DELTA_SET_CACHE) != null) {
                this.useDeltaCache = Boolean.parseBoolean(super.getString(Option.DELTA_SET_CACHE));
            }

            if (super.getString(Option.GCM_PUSH_ENABLED) != null) {
                this.GCM_Enabled = Boolean.parseBoolean(super.getString(Option.GCM_PUSH_ENABLED));
            }

            if (super.getString(Option.GCM_PROD_MODE) != null) {
                this.GCM_InProduction = Boolean.parseBoolean(super.getString(Option.GCM_PROD_MODE));
            }

            if (super.getString(Option.GCM_SENDER_ID) != null) {
                this.GCM_SenderID = super.getString(Option.GCM_SENDER_ID);
            }

            if (super.getString(Option.DEBUG_MODE) != null) {
                this.debugMode = Boolean.parseBoolean(super.getString(Option.DEBUG_MODE));
            }

            if (super.getString(Option.SYNC_RATE) != null) {
                this.syncRate = Long.parseLong(super.getString(Option.SYNC_RATE));
            }

            if (super.getString(Option.BATCH_SIZE) != null) {
                this.batchSize = Integer.parseInt(super.getString(Option.BATCH_SIZE));
            }

            if (super.getString(Option.BATCH_RATE) != null) {
                this.batchRate = Long.parseLong(super.getString(Option.BATCH_RATE));
            }

            if (super.getString(Option.PARSER) != null) {
                try {
                    AndroidJson.JSONPARSER parser = AndroidJson.JSONPARSER.valueOf(super.getString(Option.PARSER));
                    this.factory = AndroidJson.newCompatibleJsonFactory(parser);
                } catch (Exception e) {
                    Logger.WARNING("Invalid Parser name configured, must be one of: " + AndroidJson.JSONPARSER.getOptions());
                    Logger.WARNING("Defaulting to: GSON");
//                    e.printStackTrace();
                    this.factory = AndroidJson.newCompatibleJsonFactory(AndroidJson.JSONPARSER.GSON);
                }
            }
            setJsonFactory(factory);

            if (super.getString(Option.MIC_BASE_URL) != null) {
                this.MICBaseURL = super.getString(Option.MIC_BASE_URL);
            }
            if (super.getString(Option.MIC_VERSION) != null) {
                this.MICVersion = super.getString(Option.MIC_VERSION);
            }
        }


        /**
         * Use this constructor to create a Client.Builder, which can be used to build a Kinvey Client with defaults
         * set for the Android Operating System.
         * <p>
         * This constructor can get configuration for your Kinvey Client from properties file.
         * Save this file within your Android project, and pass InputStream of that file to constructor.
         * You can set appKey and appSecret in  parameters, in this case appKey and appSecret from kinvey.properties
         * will be ignored.
         * </p>
         * <p>
         * This constructor provides support for push notifications.
         * </p>
         * <p>
         * <a href="http://devcenter.kinvey.com/android/guides/getting-started#InitializeClient">Kinvey Guide for initializing Client with a properties file.</a>
         * </p>
         *
         * @param properties - InputStream of properties file
         * @param transport - custom user http transport
         * @param context - Your Android Application Context
         *
         */
        public Builder(InputStream properties, HttpTransport transport, Context context) {
            super(transport, null);

            Preconditions.checkNotNull(properties, "properties must be not null");
            loadProperties(properties);

            String key;
            String secret;
            if (BuildConfig.TRAVIS) {
                key = BuildConfig.KINVEY_APP_KEY;
                secret = BuildConfig.KINVEY_APP_SECRET;
            } else {
                key = Preconditions.checkNotNull(super.getString(Option.APP_KEY), "app.key must not be null");
                secret = Preconditions.checkNotNull(super.getString(Option.APP_SECRET), "app.secret must not be null");
            }

            KinveyClientRequestInitializer initializer = new KinveyClientRequestInitializer(key, secret, new KinveyHeaders(context));
            this.setKinveyClientRequestInitializer(initializer);

            this.context = context.getApplicationContext();
            this.setRequestBackoffPolicy(new ExponentialBackOffPolicy());
            if (getCredentialStore() == null){
                try{
                    this.setCredentialStore(new AndroidCredentialStore(this.context));
                } catch (AndroidCredentialStoreException ex) {
                    Logger.ERROR("Credential store was in a corrupted state and had to be rebuilt");
                } catch (IOException ex) {
                    Logger.ERROR("Credential store failed to load");
                }
            }

        }


        /**
         * Use this constructor to create a Client.Builder, which can be used to build a Kinvey Client with defaults
         * set for the Android Operating System.
         * <p>
         * This constructor can get configuration for your Kinvey Client from properties file.
         * Save this file within your Android project, and pass InputStream of that file to constructor.
         * You can set appKey and appSecret in  parameters, in this case appKey and appSecret from kinvey.properties
         * will be ignored.
         * </p>
         * <p>
         * This constructor provides support for push notifications.
         * </p>
         * <p>
         * <a href="http://devcenter.kinvey.com/android/guides/getting-started#InitializeClient">Kinvey Guide for initializing Client with a properties file.</a>
         * </p>
         *
         * @param properties - InputStream of properties file
         * @param context - Your Android Application Context
         *
         */
        public Builder(InputStream properties, Context context) {
            this(properties, newCompatibleTransport(), context);
        }

        /*
         * (non-Javadoc)
         *
         * @see com.kinvey.java.core.AbstractKinveyJsonClient.Builder#
         * setHttpRequestInitializer
         * (com.google.api.client.http.HttpRequestInitializer)
         */
        @Override
        public Builder setHttpRequestInitializer(
                HttpRequestInitializer httpRequestInitializer) {
            return (Builder) super.setHttpRequestInitializer(httpRequestInitializer);
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
            this(context, newCompatibleTransport());
        }

        public Builder setUserClass(Class<T> userClass){
            this.userClass = userClass;
            return this;
        }

        public Builder setEncryptionKey(byte[] encryptionKey){
            this.encryptionKey = encryptionKey;
            return this;
        }

        /**
         * @param requestTimeout - the request timeout
         */
        public Builder setRequestTimeout(int requestTimeout) {
            super.setRequestTimeout(requestTimeout);
            return this;
        }

        /**
         * @param instanceID
         */
        public Builder setInstanceID(String instanceID) {
            super.setInstanceID(instanceID);
            return this;
        }

        /**
         * @return an instantiated Kinvey Android Client,
         * which contains factory methods for accessing various functionality.
         */
        @Override
        public Client<T> build(){
            kinveyHandlerThread = new KinveyHandlerThread("KinveyHandlerThread");
            kinveyHandlerThread.start();
            Realm.init(context);
            final Client<T> client = new Client<>(getTransport(),
                    getHttpRequestInitializer(), getBaseUrl(),
                    getServicePath(), this.getObjectParser(), getKinveyClientRequestInitializer(), getCredentialStore(),
                    getRequestBackoffPolicy(), this.encryptionKey, this.context);

            client.clientUser = AndroidUserStore.getUserStore(this.context);

            client.setUserClass(userClass != null ? userClass : (Class<T>) User.class);

            //GCM explicitly enabled
            if (this.GCM_Enabled){
                client.pushProvider = new GCMPush(client, this.GCM_InProduction, this.GCM_SenderID);
            }

            if (this.debugMode){
                client.enableDebugLogging();
            }

            client.setRequestTimeout(this.requestTimeout);
            client.syncRate = this.syncRate;
            client.batchRate = this.batchRate;
            client.batchSize = this.batchSize;
            client.setUseDeltaCache(this.deltaSetCache);
            if (this.MICVersion != null){
                client.setMICApiVersion(this.MICVersion);
            } else {
                client.setMICApiVersion(DEFAULT_MIC_API_VERSION);
            }
            if(this.MICBaseURL != null){
               client.setMICHostName(this.MICBaseURL);
            }
            if (!Strings.isNullOrEmpty(this.instanceID)) {
                client.setMICHostName(Constants.PROTOCOL_HTTPS + instanceID + Constants.HYPHEN + Constants.HOSTNAME_AUTH);
            }
            try {
                Credential credential = retrieveUserFromCredentialStore(client);
                if (credential != null) {
                    loginWithCredential(client, credential);
                }

            } catch (AndroidCredentialStoreException ex) {
                Logger.ERROR("Credential store was in a corrupted state and had to be rebuilt");
                client.setActiveUser(null);
            } catch (IOException ex) {
                Logger.ERROR("Credential store failed to load");
                client.setActiveUser(null);
            }


            return client;
        }

        /**
         * Asynchronous Client build method
         *
         * <p>
         *
         * </p>
         *
         * @param buildCallback Instance of {@link KinveyClientBuilderCallback}
         */
        public void build(KinveyClientBuilderCallback buildCallback) {
            new Build(buildCallback).execute();
        }


        /**
         * Define how credentials will be stored
         *
         * @param store something implementing CredentialStore interface
         * @return this builder, with the new credential store set
         */
        public Builder setCredentialStore(CredentialStore store) {
            super.setCredentialStore(store);
            return this;
        }

        /**
         *
         * @param factory - the JSON factory for this client to use
         * @return
         */
        public Builder setJsonFactory(JsonFactory factory){
            super.setJsonFactory(factory);
            return this;
        }





        /**
         * Sets a callback to be called after a client is intialized and BaseUser attributes is being retrieved.
         *
         * <p>
         * When a client is initialized after an initial login, the user's credentials are cached locally and used for the
         * initialization of the client.  As part of the initialization process, a background thread is spawned to retrieve
         * up-to-date user attributes.  This optional callback is called when the retrieval process is complete and passes
         * an instance of the logged in user.
         * </p>
         * <p>Sample Usage:
         * <pre>
         * {@code
            Client myClient = Client.Builder(this)
                    .setRetrieveUserCallback(new KinveyUserCallback() {
                public void onFailure(Throwable t) {
                    CharSequence text = "Error retrieving user attributes.";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }

                public void onSuccess(BaseUser u) {
                    CharSequence text = "Retrieved up-to-date data for " + u.getUserName() + ".";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
            }).build();
         }
         * </pre>
         * ></p>
         *
         * @param callback
         * @return
         */
        public Client.Builder setRetrieveUserCallback(KinveyUserCallback callback) {
            this.retrieveUserCallback = callback;
            return this;
        }

        /**
         * Builder method to enable GCM for the client
         *
         * @param gcmEnabled - should push use GCM, defaults to true
         * @return the current instance of the builder
         */
        public Client.Builder enableGCM(boolean gcmEnabled){
            this.GCM_Enabled = gcmEnabled;
            return this;
        }

        /**
         * Builder method to set sender ID for GCM push
         *
         * @param senderID - the senderID to register
         * @return the current instance of the builder
         */
        public Client.Builder setSenderIDs(String senderID){
            this.GCM_SenderID = senderID;
            return this;
        }
        
        /**
        *
        * @see
        * com.kinvey.java.core.AbstractKinveyJsonClient.Builder#setBaseUrl(String)
        */
        @Override
        public Client.Builder setBaseUrl(String baseUrl) {
            super.setBaseUrl(baseUrl);
            return this;
        }

        public Client.Builder setGcmInProduction(boolean inProduction){
            this.GCM_InProduction = inProduction;
            return this;
        }

        private Credential retrieveUserFromCredentialStore(Client client)
                throws AndroidCredentialStoreException, IOException {
            Credential credential = null;
            if (!client.isUserLoggedIn()) {
                String userID = client.getClientUser().getUser();
                if (userID != null && !userID.equals("")) {
                    CredentialStore store = getCredentialStore();

                    if (store == null){
                        store = new AndroidCredentialStore(context);
                    }
                    CredentialManager manager = new CredentialManager(store);
                    credential = manager.loadCredential(userID);
                }
            }
            return credential;
        }

        private void loginWithCredential(final Client client, Credential credential) {
            getKinveyClientRequestInitializer().setCredential(credential);
            try {
                BaseUserStore.login(credential, client);
            } catch (IOException ex) {
            	Logger.ERROR("Could not retrieve user Credentials");
            }

            Exception exception = null;
            try{
                client.setActiveUser(BaseUserStore.<User>convenience(client));
            }catch (Exception error){
                exception = error;
                if (error instanceof HttpResponseException) {
                    try {
                        BaseUserStore.logout(client);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(retrieveUserCallback == null){
                return;
            }

            if(exception != null){
                retrieveUserCallback.onFailure(exception);
            }else{
                retrieveUserCallback.onSuccess(client.getActiveUser());
            }

        }

        /**
         * The default kinvey settings filename {@code assets/kinvey.properties}
         *
         * @return {@code assets/kinvey.properties}
         */
        protected static String getAndroidPropertyFile() {
            return "assets/kinvey.properties";
        }


        /** Get setting value of delta set caching **/
        public boolean isDeltaSetCache() {
            return deltaSetCache;
        }

        /** Set the setting for delta set cache **/
        public Client.Builder setDeltaSetCache(boolean deltaSetCache) {
            this.deltaSetCache = deltaSetCache;
            return this;
        }

        private class Build extends AsyncClientRequest<Client> {

            private Build(KinveyClientBuilderCallback builderCallback) {
                super(builderCallback);
            }

            @Override
            protected Client executeAsync() {
                return Client.Builder.this.build();
            }
        }
    }

    public KinveyHandlerThread getKinveyHandlerThread() {
        return kinveyHandlerThread;
    }

    @Override
    public String getFileCacheFolder() {
        return context.getExternalFilesDir("KinveyFiles").getAbsolutePath();
    }

    @Override
    public FileStore getFileStore(StoreType storeType) {
        return new FileStore(new NetworkFileManager(this),
                    getCacheManager(), 60*60*1000L, storeType, getFileCacheFolder()
                );
    }

    @Override
    protected ICacheManager getSyncCacheManager() {
        return syncCacheManager;
    }

    /**
     * Terminates KinveyHandlerThread.
     * Should be called if the Client instance is not used anymore to prevent from memory leaks.
     * Currently this method is called from Instrumented tests, since each test has its own Client instance.
     */
    public void stopKinveyHandlerThread() {
        if (kinveyHandlerThread != null) {
            kinveyHandlerThread.stopHandlerThread();
            kinveyHandlerThread.quit();
            kinveyHandlerThread.interrupt();
        }
    }

    @Override
    public String getDeviceId() {
        return new UuidFactory(context).getDeviceUuid().toString();
    }
}


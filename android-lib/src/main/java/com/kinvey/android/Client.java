/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.ExponentialBackOffPolicy;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.common.base.Preconditions;
import com.kinvey.android.cache.RealmCacheManager;
import com.kinvey.android.callback.KinveyClientBuilderCallback;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.network.AndroidNetworkStore;
import com.kinvey.android.push.AbstractPush;
import com.kinvey.android.push.GCMPush;
import com.kinvey.android.store.AsyncDataStore;
import com.kinvey.android.store.AsyncUserStore;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.ClientExtension;
import com.kinvey.java.Logger;
import com.kinvey.java.LinkedResources.LinkedGenericJson;
import com.kinvey.java.auth.ClientUsers;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.CredentialManager;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.User;
import com.kinvey.java.network.NetworkStore;
import com.kinvey.java.network.File;

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

    private Context context = null;

    private ConcurrentHashMap<String, AsyncDataStore> appDataInstanceCache;
    private ConcurrentHashMap<String, AsyncLinkedData> linkedDataInstanceCache;
    private ConcurrentHashMap<String, AsyncCustomEndpoints> customeEndpointsCache;
    private AsyncCustomEndpoints customEndpoints;
    private AbstractPush pushProvider;
    private AsyncUserDiscovery userDiscovery;
    private AsyncFile file;
    private AsyncUserGroup userGroup;
    private ClientUsers clientUsers;
//    private AsyncUser currentUser;
    private long syncRate;
    private long batchRate;
    private int batchSize;
    private RealmCacheManager cacheManager;
    
    
    private static Client _sharedInstance;
    
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
                     BackOffPolicy requestPolicy, Context context) {
        super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, store,
                requestPolicy);
        Logger.init(new AndroidLogger());
        _sharedInstance = this;
        cacheManager = new RealmCacheManager(this, context);
    }

    public static Client sharedInstance(){
    	return _sharedInstance;
    }



    /**
     * DataStore factory method
     * <p>
     * Returns an instance of {@link AsyncDataStore} for the supplied collection.  A new instance is created for each collection, but
     * only one instance of {@link AndroidNetworkStore} is created per collection.  The method is Generic and takes an instance of a
     * {@link com.google.api.client.json.GenericJson} entity type that is used for fetching/saving of {@link com.kinvey.java.store.DataStore}.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     * {@code
        DataStore<myEntity> myAppData = kinveyClient.appData("entityCollection", myEntity.class);
     }
     * </pre>
     * </p>
     *
     * @param collectionName The name of the collection
     * @param myClass The class that defines the entity of type {@link com.google.api.client.json.GenericJson} used
     *                for saving and fetching of data
     * @param <T> Generic of type {@link com.google.api.client.json.GenericJson} of same type as myClass
     * @return Instance of {@link com.kinvey.java.store.DataStore} for the defined collection
     */
    public <T extends GenericJson> AsyncDataStore<T> dataStore(String collectionName, Class<T> myClass) {
        synchronized (lock) {
            Preconditions.checkNotNull(collectionName, "collectionName must not be null");
            if (appDataInstanceCache == null) {
                appDataInstanceCache = new ConcurrentHashMap<String, AsyncDataStore>();
            }
            if (!appDataInstanceCache.containsKey(collectionName)) {            	
            	Logger.INFO("adding new instance of AsyncDataStore, new collection name");
                appDataInstanceCache.put(collectionName, new AsyncDataStore(collectionName, myClass, this));
            }
            if(appDataInstanceCache.containsKey(collectionName) && !appDataInstanceCache.get(collectionName).getCurrentClass().equals(myClass)){
            	Logger.INFO("adding new instance of AsyncDataStore, class doesn't match");
                appDataInstanceCache.put(collectionName, new AsyncDataStore(collectionName, myClass, this));
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
     * {@code
     LinkedData<myEntity> myAppData = kinveyClient.linkedData("entityCollection", myEntity.class);
    }
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
     * File factory method
     * <p>
     * Returns an instance of {@link File} for uploading and downloading of files.  Only one instance is created for each
     * instance of the Kinvey client.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     * {@code
     File myFile = kinveyClient.file();
     }
     * </pre>
     * </p>
     *
     * @return Instance of {@link File} for the defined collection
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

    @Override
    public void performLockDown() {
        if(getCacheManager() != null){
            getCacheManager().clear();
        }
        this.file().clearFileStorage(getContext().getApplicationContext());
        List<ClientExtension> extensions = getExtensions();
        for (ClientExtension e : extensions){
            e.performLockdown(userStore().getCurrentUser().getId());
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
    public <I, O> AsyncCustomEndpoints<I, O> customEndpoints(Class<O> myClass) {
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
    public ClientUsers getClientUsers() {
        synchronized (lock) {
            if (this.clientUsers == null) {
                this.clientUsers = AndroidClientUsers.getClientUsers(this.context);
            }
            return this.clientUsers;
        }

    }


    /**
     * User factory method
     * <p>
     * Returns the instance of {@link com.kinvey.java.store.UserStore} that contains the current active user.  If no active user context
     * has been established, the {@link com.kinvey.java.store.UserStore} object returned will be instantiated and empty.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     * {@code
     User currentUser = kinveyClient.user();
     }
     * </pre>
     * </p>
     * @param <T> the type of the custom `User` class, which must extend {@link com.kinvey.java.store.UserStore}
     * @return Instance of {@link com.kinvey.java.store.UserStore}
     */
    @Override
    public <T extends User> AsyncUserStore<T> userStore(){
        synchronized (lock) {
            if (userStore == null) {
                String appKey = ((KinveyClientRequestInitializer) getKinveyRequestInitializer()).getAppKey();
                String appSecret = ((KinveyClientRequestInitializer) getKinveyRequestInitializer()).getAppSecret();
                userStore = new AsyncUserStore<T>(this, (Class<T>)getUserClass(), new KinveyAuthRequest.Builder(this.getRequestFactory().getTransport(),
                        this.getJsonFactory(), this.getBaseUrl(), appKey, appSecret, null));
            }
            return (AsyncUserStore<T>)userStore;
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
     *
     * @return Instance of {@link AbstractPush} for the defined collection
     */
    public AbstractPush push() {
        synchronized (lock) {
            //NOTE:  pushProvider is defined as a GCMPush in the ClientBuilder#build() method, if the user has set it in the property file.
            //ONCE Urban Airship has been officially deprecated we can remove the below lines completely (or create GCMPush inline here)
            if (pushProvider == null) {
                pushProvider = new GCMPush(this, true, "");
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
    public static class Builder extends AbstractClient.Builder {

        private Context context = null;
        private KinveyUserCallback retrieveUserCallback = null;
        //GCM Push Fields
        private String GCM_SenderID = "";
        private boolean GCM_Enabled = false;
        private boolean GCM_InProduction = true;
        private boolean debugMode = false;
        private long syncRate = 1000 * 60 * 10; //10 minutes
        private Class userClass = User.class;
        private int batchSize = 5;
        private long batchRate = 1000L * 30L; //30 seconds
        private JsonFactory factory = AndroidJson.newCompatibleJsonFactory(AndroidJson.JSONPARSER.GSON);
        private String MICVersion;
        private String MICBaseURL;

        /**
         * creating new HttpTransport with fix for 401 error that rais an exception
         * @return HttpTransport
         */
        private static HttpTransport newCompatibleTransport(){
            return android.os.Build.VERSION.SDK_INT >= 16 && android.os.Build.VERSION.SDK_INT <= 18 ?  //versions affected by no auth challenge exception
                    new ApacheHttpTransport() :
                    AndroidHttp.newCompatibleTransport();
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
            super(newCompatibleTransport(), null
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
            super(newCompatibleTransport(), null);

            try {
                final InputStream in = context.getAssets().open("kinvey.properties");//context.getClassLoader().getResourceAsStream(getAndroidPropertyFile());

                super.getProps().load(in);
            } catch (IOException e) {
            	Logger.WARNING("Couldn't load properties, trying another load approach.  Ensure there is a file:  myProject/assets/kinvey.properties which contains: app.key and app.secret.");
                super.loadPropertiesFromDisk(getAndroidPropertyFile());
            } catch (NullPointerException ex){
                Logger.ERROR("Builder cannot find properties file at assets/kinvey.properties.  Ensure this file exists, containing app.key and app.secret!");
                Logger.ERROR("If you are using push notification or offline storage you must configure your client to load from properties, see our guides for instructions.");
                throw new RuntimeException("Builder cannot find properties file at assets/kinvey.properties.  Ensure this file exists, containing app.key and app.secret!");
            }

            if (super.getString(Option.BASE_URL) != null) {
                this.setBaseUrl(super.getString(Option.BASE_URL));
            }

            if (super.getString(Option.PORT) != null){
                this.setBaseUrl(String.format("%s:%s", super.getBaseUrl(), super.getString(Option.PORT)));
            }

            if (super.getString(Option.GCM_PUSH_ENABLED) != null){
                this.GCM_Enabled = Boolean.parseBoolean(super.getString(Option.GCM_PUSH_ENABLED));
            }

            if (super.getString(Option.GCM_PROD_MODE) != null){
                this.GCM_InProduction = Boolean.parseBoolean(super.getString(Option.GCM_PROD_MODE));
            }

            if (super.getString(Option.GCM_SENDER_ID) != null){
                this.GCM_SenderID = super.getString(Option.GCM_SENDER_ID);
            }

            if (super.getString(Option.DEBUG_MODE) != null){
                this.debugMode = Boolean.parseBoolean(super.getString(Option.DEBUG_MODE));
            }

            if (super.getString(Option.SYNC_RATE) != null){
                this.syncRate = Long.parseLong(super.getString(Option.SYNC_RATE));
            }

            if (super.getString(Option.BATCH_SIZE) != null){
                this.batchSize = Integer.parseInt(super.getString(Option.BATCH_SIZE));
            }

            if (super.getString(Option.BATCH_RATE) != null){
                this.batchRate = Long.parseLong(super.getString(Option.BATCH_RATE));
            }

            if (super.getString(Option.PARSER) != null){
                try {
                    AndroidJson.JSONPARSER parser = AndroidJson.JSONPARSER.valueOf(super.getString(Option.PARSER));
                    this.factory = AndroidJson.newCompatibleJsonFactory(parser);
                }catch (Exception e){
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
            if (super.getString(Option.MIC_VERSION) != null){
                this.MICVersion = super.getString(Option.MIC_VERSION);
            }

            String appKey = Preconditions.checkNotNull(super.getString(Option.APP_KEY), "appKey must not be null");
            String appSecret = Preconditions.checkNotNull(super.getString(Option.APP_SECRET), "appSecret must not be null");

            KinveyClientRequestInitializer initializer = new KinveyClientRequestInitializer(appKey, appSecret, new KinveyHeaders(context));
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

        public Builder setUserClass(Class userClass){
            this.userClass = userClass;
            return this;
        }


        /**
         * @return an instantiated Kinvey Android Client,
         * which contains factory methods for accessing various functionality.
         */
        @Override
        public Client build() {
            final Client client = new Client(getTransport(),
                    getHttpRequestInitializer(), getBaseUrl(),
                    getServicePath(), this.getObjectParser(), getKinveyClientRequestInitializer(), getCredentialStore(),
                    getRequestBackoffPolicy(), context);
            client.setUserClass(userClass);
            client.clientUsers = AndroidClientUsers.getClientUsers(context);
            try {
                Credential credential = retrieveUserFromCredentialStore(client);
                if (credential != null) {
                    loginWithCredential(client, credential);
                }

            } catch (AndroidCredentialStoreException ex) {
            	Logger.ERROR("Credential store was in a corrupted state and had to be rebuilt");
                client.setCurrentUser(null);
            } catch (IOException ex) {
            	Logger.ERROR("Credential store failed to load");
                client.setCurrentUser(null);
            }

            //GCM explicitely enabled
            if (this.GCM_Enabled){
                client.pushProvider = new GCMPush(client, this.GCM_InProduction, this.GCM_SenderID);
            }

            if (this.debugMode){
                client.enableDebugLogging();
            }

            client.syncRate = this.syncRate;
            client.batchRate = this.batchRate;
            client.batchSize = this.batchSize;
            if (this.MICVersion != null){
                client.userStore().setMICApiVersion(this.MICVersion);
            }
            if(this.MICBaseURL != null){
                client.userStore().setMICHostName(this.MICBaseURL);
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
         * @param buildCallback Instance of {@link: KinveyClientBuilderCallback}
         */
        public void build(KinveyClientBuilderCallback buildCallback) {
            new Build(buildCallback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
        }


        /**
         * Define how credentials will be stored
         *
         * @param store something implpemting CredentialStore interface
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
         * Sets a callback to be called after a client is intialized and User attributes is being retrieved.
         *
         * <p>
         * When a client is intialized after an initial login, the user's credentials are cached locally and used for the
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

                public void onSuccess(User u) {
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
        * com.kinvey.java.core.AbstractKinveyJsonClient.Builder#setRootUrl(java
        * .lang.String)
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
            if (!client.userStore().isUserLoggedIn()) {
                String userID = client.getClientUsers().getCurrentUser();
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
                client.userStore().login(credential).execute();
            } catch (IOException ex) {
            	Logger.ERROR("Could not retrieve user Credentials");
            }
            new AsyncTask<Void, Void, User>(){

                private Exception error = null;
                @Override
                protected User doInBackground(Void... voids) {
                    User result = null;
                    try{
                        result = client.userStore().retrieveMetadataBlocking();
                        client.setCurrentUser(result);
                    }catch (Exception error){
                        this.error = error;
                        if ((error instanceof HttpResponseException)) {
                            client.userStore().logout().execute();
                        }
                    }
                    return result;
                }

                @Override
                protected void onPostExecute(User response) {
                    if(retrieveUserCallback == null){
                        return;
                    }

                    if(error != null){
                        retrieveUserCallback.onFailure(error);
                    }else{
                        retrieveUserCallback.onSuccess(response);
                    }
                }

            }.execute();



        }

        /**
         * The default kinvey settings filename {@code assets/kinvey.properties}
         *
         * @return {@code assets/kinvey.properties}
         */
        protected static String getAndroidPropertyFile() {
            return "assets/kinvey.properties";
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

    @Override
    public <T extends GenericJson> NetworkStore<T> networkStore(String collectionName, Class<T> myClass) {
        return new AndroidNetworkStore<T>(collectionName, myClass, this);
    }
}


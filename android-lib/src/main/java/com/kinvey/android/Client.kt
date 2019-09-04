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

package com.kinvey.android


import android.content.Context

import com.google.api.client.http.BackOffPolicy
import com.google.api.client.http.ExponentialBackOffPolicy
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpResponseException
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.JsonObjectParser
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.kinvey.android.cache.RealmCacheManager
import com.kinvey.android.callback.KinveyClientBuilderCallback
import com.kinvey.android.callback.KinveyPingCallback
import com.kinvey.android.callback.KinveyUserCallback
import com.kinvey.android.model.User
import com.kinvey.android.push.AbstractPush
import com.kinvey.android.push.FCMPush
import com.kinvey.android.store.FileStore
import com.kinvey.java.*
import com.kinvey.java.auth.ClientUser
import com.kinvey.java.auth.Credential
import com.kinvey.java.auth.CredentialManager
import com.kinvey.java.auth.CredentialStore
import com.kinvey.java.cache.ICacheManager
import com.kinvey.java.core.AbstractKinveyClient
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.network.NetworkFileManager
import com.kinvey.java.store.BaseUserStore
import com.kinvey.java.store.StoreType

import java.io.IOException
import java.io.InputStream
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException

import io.realm.Realm

/**
 * This class is an implementation of a [com.kinvey.java.AbstractClient] with default settings for the Android operating
 * system.
 *
 *
 *
 * Functionality is provided through a series of factory methods, which return the various API service wrappers.
 * These factory methods are all synchronized, and access is thread-safe.  Once a service API has been retrieved,
 * it can be used to instantiate and execute asynchronous service calls.  The [Client.Builder] is not thread-safe.
 *
 *
 *
 *
 * The calling class can pass in a callback instance. Upon completion of the service call-- either success or failure will
 * be invoked. The Callback mechanism is null-safe, so `null` can be passed if no callbacks are required.
 *
 *
 *
 *
 * All callback methods are *null-safe*, the callback will be ignored if `null` is passed in.
 *
 *
 * @author edwardf
 * @author m0rganic
 * @author mjsalinger
 * @since 2.0
 * @version $Id: $
 */
open class Client<T : User>
/**
 * Protected constructor.  Public AbstractClient.Builder class is used to construct the AbstractClient, so this method shouldn't be
 * called directly.
 *
 * @param transport the transport
 * @param httpRequestInitializer standard request initializer
 * @param rootUrl root url to base all requests
 * @param servicePath standard service path for all requests
 * @param objectParser object parse used in all requests
 * @param kinveyRequestInitializer a [com.kinvey.java.core.KinveyClientRequestInitializer] object.
 * @param store a [com.kinvey.java.auth.CredentialStore] object.
 * @param requestPolicy a [BackOffPolicy] for retrying HTTP Requests
 * @param context a [Context] android application context
 */
protected constructor(transport: HttpTransport?, httpRequestInitializer: HttpRequestInitializer?, rootUrl: String?,
                      servicePath: String?, objectParser: JsonObjectParser?,
                      kinveyRequestInitializer: KinveyClientRequestInitializer?, store: CredentialStore?,
                      requestPolicy: BackOffPolicy?, private val encryptionKey: ByteArray?, context: Context)
    : AbstractClient<T>(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, store, requestPolicy) {

    /**
     * Get a reference to the Application Context used to create this instance of the Client
     * @return `null` or the live Application Context
     */
    var context: Context? = null

    //    private ConcurrentHashMap<String, DataStore> appDataInstanceCache;
    //    private ConcurrentHashMap<String, LinkedDataStore> linkedDataInstanceCache;
    //    private ConcurrentHashMap<String, AsyncCustomEndpoints> customeEndpointsCache;
    private val customEndpoints: AsyncCustomEndpoints<*, *>? = null
    private var pushProvider: AbstractPush? = null
    private var userDiscovery: AsyncUserDiscovery? = null
    private var userGroup: AsyncUserGroup? = null

    override var clientUser: ClientUser? = null
        get() {
            val ctx = this.context ?: return null
            synchronized(lock) {
                if (field == null) {
                    field = AndroidUserStore.getUserStore(ctx)
                }
            }
            return field
        }


    //    private AsyncUser currentUser;
    /**
     * How long, in milliseconds, should offline wait before retrying a failed request
     *
     * @return the number of milliseconds for offline sync to wait between failures
     */
    var syncRate: Long = 0
        private set
    var batchRate: Long = 0
        private set
    var numberThreadPool: Int = 0
        private set
    var batchSize: Int = 0
        private set
    var isClientRequestMultithreading: Boolean = false
        private set

    override var cacheManager: ICacheManager? = null

    var pushServiceClass: Class<*>? = null

    override var activeUser: T? = null
        get() {
            synchronized(lock) {
                return field
            }
        }
        set(value) {
            synchronized(lock) {
                field = value
            }
        }

    override var userCacheManager: ICacheManager? = null

    override var syncCacheManager: ICacheManager? = null

    override var user: T? = null
        get () {
            synchronized(lock) {
                return field
            }
        }

    override val deviceId: String
        get() = UuidFactory(context).deviceUuid.toString()

    override var fileCacheFolder: String? = null
        get() {
            return context?.getExternalFilesDir("KinveyFiles")?.absolutePath
        }

    override fun getFileStore(storeType: StoreType): FileStore {
        return FileStore(NetworkFileManager(this),
                cacheManager, 60 * 1000 * 1000L,
                storeType, fileCacheFolder)
    }

    //override fun getSyncCacheManager(): ICacheManager? {
    //    return syncCacheManager
    //}

    init {
        Logger.init(AndroidLogger())
        sharedInstance = this as Client<User>
        this.context = context
        cacheManager = RealmCacheManager(encryptionKey, this)
        syncCacheManager = RealmCacheManager(encryptionKey, "sync_", this)
        userCacheManager = RealmCacheManager(encryptionKey, this)
    }

    override fun performLockDown() {
        //clear data cache and file cache
        cacheManager?.clear()
        //clear sync cache
        syncCacheManager?.clear()
        //clear user info cache
        userCacheManager?.clear()
        cacheManager = RealmCacheManager(encryptionKey, this)
        syncCacheManager = RealmCacheManager(encryptionKey, "sync_", this)
        userCacheManager = RealmCacheManager(encryptionKey, this)
        val extensions = extensions
        extensions?.onEach { e -> e.performLockdown(activeUser?.id ?: "") }
    }

    /**
     * Custom Endpoints factory method
     *
     *
     * Returns the instance of [com.kinvey.java.CustomEndpoints] used for executing RPC requests.  Only one instance
     * of Custom Endpoints is created for each instance of the Kinvey Client.
     *
     *
     *
     * This method is thread-safe.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `CustomEndpoints myCustomEndpoints = kinveyClient.customEndpoints();
    ` *
    </pre> *
     *
     *
     * @return Instance of [com.kinvey.java.UserDiscovery] for the defined collection
     */
    @Deprecated("It is deprected now, use more extended version customEndpoints(Class<O> myClass)")
    fun <I : GenericJson, O> customEndpoints(): AsyncCustomEndpoints<I, O> {
        synchronized(lock) {
            return AsyncCustomEndpoints(GenericJson::class.java as Class<O>, this)
        }
    }

    /**
     * Custom Endpoints factory method
     *
     *
     * Returns the instance of [com.kinvey.java.CustomEndpoints] used for executing RPC requests.  Only one instance
     * of Custom Endpoints is created for each instance of the Kinvey Client.
     *
     *
     *
     * This method is thread-safe.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `AsyncCustomEndpoints<MyRequestClass, MyResponseClass> endpoints = getClient().customEndpoints(MyResponseClass.class);
    ` *
    </pre> *
     *
     *
     * @return Instance of [com.kinvey.java.UserDiscovery] for the defined collection
     */

    override fun <I : GenericJson, O> customEndpoints(myClass: Class<O>?): AsyncCustomEndpoints<I, O> {
        synchronized(lock) {
            return AsyncCustomEndpoints(myClass as Class<O>, this)
        }
    }

    /**
     * UserDiscovery factory method
     *
     *
     * Returns the instance of [com.kinvey.java.UserDiscovery] used for searching for users. Only one instance of
     * UserDiscovery is created for each instance of the Kinvey Client.
     *
     *
     *
     * This method is thread-safe.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `UserDiscovery myUserDiscovery = kinveyClient.userDiscovery();
    ` *
    </pre> *
     *
     *
     * @return Instance of [com.kinvey.java.UserDiscovery] for the defined collection
     */
    override fun userDiscovery(): AsyncUserDiscovery? {
        synchronized(lock) {
            if (userDiscovery == null) {
                userDiscovery = AsyncUserDiscovery(this,
                        this.kinveyRequestInitializer as KinveyClientRequestInitializer)
            }
            return userDiscovery
        }
    }

    /**
     * UserGroup factory method
     *
     *
     * Returns the instance of [com.kinvey.java.UserGroup] used for managing user groups. Only one instance of
     * UserGroup is created for each instance of the Kinvey Client.
     *
     *
     *
     * This method is thread-safe.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `UserGroup myUserGroup = kinveyClient.userGroup();
    ` *
    </pre> *
     *
     *
     * @return Instance of [com.kinvey.java.UserGroup] for the defined collection
     */
    override fun userGroup(): AsyncUserGroup? {
        synchronized(lock) {
            if (userGroup == null) {
                userGroup = AsyncUserGroup(this,
                        this.kinveyRequestInitializer as KinveyClientRequestInitializer)
            }
            return userGroup
        }

    }

    /**
     * Push factory method
     *
     *
     * Returns the instance of [AbstractPush] used for configuring Push. Only one instance of
     * [AbstractPush] is created for each instance of the Kinvey Client.
     *
     *
     *
     * This method is thread-safe.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `AbstractPush myPush = kinveyClient.push();
    ` *
    </pre> *
     *
     * @param pushServiceClass Service class for handling push notification
     * @return Instance of [AbstractPush] for the defined collection
     */
    fun push(pushServiceClass: Class<*>): AbstractPush? {
        synchronized(lock) {
            //NOTE:  pushProvider is defined as a FCMPush in the ClientBuilder#build() method, if the user has set it in the property file.
            //ONCE Urban Airship has been officially deprecated we can remove the below lines completely (or create FCMPush inline here)
            if (pushProvider == null) {
                pushProvider = FCMPush(this, true, "")
            }
            if (pushProvider?.pushServiceClass == null) {
                pushProvider?.pushServiceClass = pushServiceClass
                this.pushServiceClass = pushServiceClass
            }
            return pushProvider
        }
    }

    /**
     * Asynchronous Ping service method
     *
     *
     * Performs an authenticated ping against the configured Kinvey backend.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.ping(new KinveyPingCallback() {
     * onSuccess(Boolean result) { ... }
     * onFailure(Throwable error) { ... }
     * }
    ` *
    </pre> *
     *
     *
     * @param callback object of type [KinveyPingCallback] to be notified when request completes
     */
    fun ping(callback: KinveyPingCallback) {
        Ping(this, callback).execute()
    }


    private class Ping (val client: Client<*>, callback: KinveyPingCallback) : AsyncClientRequest<Boolean>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): Boolean? {
            return client.pingBlocking()
        }
    }

    /**
     * Create a client for interacting with Kinvey's services from an Android Activity.
     *
     *
     * <pre>
     * Client myClient =  new Client.Builder(appKey, appSecret, getContext()).build();</pre>
     *
     *
     * All features of the library are be accessed through an instance of a client.
     *
     *
     * It is recommended to maintain a single instance of a `Client` while developing with Kinvey, either in an
     * Activity, a Service, or an Application.
     *
     *
     * This Builder class is not thread-safe.
     */
    open class Builder<T : User> : AbstractClient.Builder {

        private var context: Context? = null
        private var retrieveUserCallback: KinveyUserCallback<T>? = null
        //GCM Push Fields
        private var FCM_SenderID: String? = ""
        private var FCM_Enabled = false
        private var GCM_InProduction = true
        private var debugMode = false
        private val syncRate = (1000 * 60 * 10).toLong() //10 minutes
        private var batchSize = 5
        private var batchRate = 1000L * 30L //30 seconds
        private var numberThreadPool = 1
        private var clientRequestMultithreading = false
        private var factory = AndroidJson.newCompatibleJsonFactory(AndroidJson.JSONPARSER.GSON)
        private var MICVersion: String? = null
        private var MICBaseURL: String? = null
        private var deltaSetCache = false
        private var userClass: Class<T>? = null
        private var encryptionKey: ByteArray? = null

        /**
         * Use this constructor to create a AbstractClient.Builder, which can be used to build a Kinvey AbstractClient with defaults
         * set for the Android Operating System.
         *
         *
         * This constructor does NOT support push notification functionality.
         * If push notifications are necessary, use a properties file and the overloaded constructor.
         *
         *
         * @param appKey Your Kinvey Application Key
         * @param appSecret Your Kinvey Application Secret
         * @param context Your Android Application Context
         * @param transport HttpTransport
         */
        @JvmOverloads
        constructor(appKey: String, appSecret: String, context: Context, transport: HttpTransport = newCompatibleTransport())
                : super(transport, null, KinveyClientRequestInitializer(appKey, appSecret, KinveyHeaders(context))) {
            this.setJsonFactory(factory)
            this.context = context.applicationContext
            this.requestBackoffPolicy = ExponentialBackOffPolicy()

            if (store == null) {
                try {
                    this.setCredentialStore(AndroidCredentialStore(this.context!!))
                } catch (ex: Exception) {
                    //TODO Add handling
                }

            }
        }

        /**
         * Use this constructor to create a Client.Builder, which can be used to build a Kinvey Client with defaults
         * set for the Android Operating System.
         *
         *
         * This constructor requires a  properties file, containing configuration for your Kinvey Client.
         * create kinvey.properties file in your assets folder according kinvey docs
         *
         *
         *
         * This constructor provides support for push notifications.
         *
         *
         *
         * [Kinvey Guide for initializing Client with a properties file.](http://devcenter.kinvey.com/android/guides/getting-started#InitializeClient)
         *
         *
         * @param context - Your Android Application Context
         */
        @JvmOverloads
        constructor(context: Context, transport: HttpTransport = newCompatibleTransport()) : super(transport, null) {

            var properties: InputStream? = null
            try {
                properties = context.assets.open("kinvey.properties")
            } catch (e: IOException) {
                Logger.WARNING("Couldn't load properties. Ensure there is a file: assets/kinvey.properties which is valid properties file")
                throw RuntimeException("Builder cannot find properties file kinvey.properties in your assets.  Ensure this file exists, containing app.key and app.secret!")

            }

            Preconditions.checkNotNull(properties, "Builder cannot find properties file kinvey.properties in your assets.  Ensure this file exists, containing app.key and app.secret!")
            loadProperties(properties)

            val key: String
            val secret: String
            if (BuildConfig.TRAVIS) {
                key = BuildConfig.KINVEY_APP_KEY
                secret = BuildConfig.KINVEY_APP_SECRET
                FCM_Enabled = true
                FCM_SenderID = BuildConfig.SENDER_ID
            } else {
                key = Preconditions.checkNotNull(super.getString(Option.APP_KEY), "app.key must be defined in your kinvey.properties")
                secret = Preconditions.checkNotNull(super.getString(Option.APP_SECRET), "app.secret must be defined in your kinvey.properties")
            }

            val initializer = KinveyClientRequestInitializer(key, secret, KinveyHeaders(context))
            this.kinveyClientRequestInitializer = initializer

            this.context = context.applicationContext
            this.requestBackoffPolicy = ExponentialBackOffPolicy()
            if (store == null) {
                try {
                    this.setCredentialStore(AndroidCredentialStore(this.context!!))
                } catch (ex: AndroidCredentialStoreException) {
                    Logger.ERROR("Credential store was in a corrupted state and had to be rebuilt")
                } catch (ex: IOException) {
                    Logger.ERROR("Credential store failed to load")
                }
            }
        }


        /**
         * loading additional properties from file
         * @param properties InputStream of properties file
         */
        private fun loadProperties(properties: InputStream?) {

            try {
                super.props.load(properties)
            } catch (e: IOException) {
                Logger.WARNING("Couldn't load properties. Please make sure that your properties file is valid")
                throw RuntimeException("Couldn't load properties. Please make sure that your properties file is valid")
            }

            if (super.getString(Option.BASE_URL).isNotEmpty()) {
                this.baseUrl = super.getString(Option.BASE_URL)
            }

            if (super.getString(Option.INSTANCE_ID).isNotEmpty()) {
                this.setInstanceID(super.getString(Option.INSTANCE_ID))
            }

            if (super.getString(Option.REQUEST_TIMEOUT).isNotEmpty()) {
                try {
                    this.setRequestTimeout(Integer.parseInt(super.getString(Option.REQUEST_TIMEOUT)))
                } catch (e: Exception) {
                    Logger.WARNING(Option.REQUEST_TIMEOUT.name + " should have an integer value")
                }
            }

            if (super.getString(Option.PORT).isNotEmpty()) {
                this.baseUrl = String.format("%s:%s", super.getBaseUrl(), super.getString(Option.PORT))
            }

            if (super.getString(Option.DELTA_SET_CACHE).isNotEmpty()) {
                this.useDeltaCache = java.lang.Boolean.parseBoolean(super.getString(Option.DELTA_SET_CACHE))
            }

            if (super.getString(Option.FCM_PUSH_ENABLED).isNotEmpty()) {
                this.FCM_Enabled = java.lang.Boolean.parseBoolean(super.getString(Option.FCM_PUSH_ENABLED))
            }

            if (super.getString(Option.GCM_PROD_MODE).isNotEmpty()) {
                this.GCM_InProduction = java.lang.Boolean.parseBoolean(super.getString(Option.GCM_PROD_MODE))
            }

            if (super.getString(Option.FCM_SENDER_ID).isNotEmpty()) {
                this.FCM_SenderID = super.getString(Option.FCM_SENDER_ID)
            }

            if (super.getString(Option.DEBUG_MODE).isNotEmpty()) {
                this.debugMode = java.lang.Boolean.parseBoolean(super.getString(Option.DEBUG_MODE))
            }

            if (super.getString(Option.CLIENT_REQUEST_MULTITHREADING).isNotEmpty()) {
                this.clientRequestMultithreading = java.lang.Boolean.parseBoolean(super.getString(Option.CLIENT_REQUEST_MULTITHREADING))
            }

            if (super.getString(Option.NUMBER_THREAD_POOL).isNotEmpty()) {
                this.numberThreadPool = Integer.parseInt(super.getString(Option.NUMBER_THREAD_POOL))
            }

            if (super.getString(Option.BATCH_SIZE).isNotEmpty()) {
                this.batchSize = Integer.parseInt(super.getString(Option.BATCH_SIZE))
            }

            if (super.getString(Option.BATCH_RATE).isNotEmpty()) {
                this.batchRate = java.lang.Long.parseLong(super.getString(Option.BATCH_RATE))
            }

            if (super.getString(Option.PARSER).isNotEmpty()) {
                try {
                    val parser = AndroidJson.JSONPARSER.valueOf(super.getString(Option.PARSER))
                    this.factory = AndroidJson.newCompatibleJsonFactory(parser)
                } catch (e: Exception) {
                    Logger.WARNING("Invalid Parser name configured, must be one of: " + AndroidJson.JSONPARSER.options)
                    Logger.WARNING("Defaulting to: GSON")
                    //                    e.printStackTrace();
                    this.factory = AndroidJson.newCompatibleJsonFactory(AndroidJson.JSONPARSER.GSON)
                }
            }
            setJsonFactory(factory)

            if (super.getString(Option.MIC_BASE_URL).isNotEmpty()) {
                this.MICBaseURL = super.getString(Option.MIC_BASE_URL)
            }
            if (super.getString(Option.MIC_VERSION).isNotEmpty()) {
                this.MICVersion = super.getString(Option.MIC_VERSION)
            }
            if (super.getString(Option.KINVEY_API_VERSION).isNotEmpty()) {
                KINVEY_API_VERSION = super.getString(Option.KINVEY_API_VERSION)
            }
        }


        /**
         * Use this constructor to create a Client.Builder, which can be used to build a Kinvey Client with defaults
         * set for the Android Operating System.
         *
         *
         * This constructor can get configuration for your Kinvey Client from properties file.
         * Save this file within your Android project, and pass InputStream of that file to constructor.
         * You can set appKey and appSecret in  parameters, in this case appKey and appSecret from kinvey.properties
         * will be ignored.
         *
         *
         *
         * This constructor provides support for push notifications.
         *
         *
         *
         * [Kinvey Guide for initializing Client with a properties file.](http://devcenter.kinvey.com/android/guides/getting-started#InitializeClient)
         *
         *
         * @param properties - InputStream of properties file
         * @param transport - custom user http transport
         * @param context - Your Android Application Context
         */
        constructor(properties: InputStream?, transport: HttpTransport, context: Context) : super(transport, null) {

            Preconditions.checkNotNull(properties, "properties must be not null")
            loadProperties(properties)

            val key: String
            val secret: String
            if (BuildConfig.TRAVIS) {
                key = BuildConfig.KINVEY_APP_KEY
                secret = BuildConfig.KINVEY_APP_SECRET
            } else {
                key = Preconditions.checkNotNull(super.getString(AbstractClient.Builder.Option.APP_KEY), "app.key must not be null")
                secret = Preconditions.checkNotNull(super.getString(AbstractClient.Builder.Option.APP_SECRET), "app.secret must not be null")
            }

            val initializer = KinveyClientRequestInitializer(key, secret, KinveyHeaders(context))
            this.kinveyClientRequestInitializer = initializer

            this.context = context.applicationContext
            this.requestBackoffPolicy = ExponentialBackOffPolicy()
            if (store == null) {
                try {
                    this.setCredentialStore(AndroidCredentialStore(this.context!!))
                } catch (ex: AndroidCredentialStoreException) {
                    Logger.ERROR("Credential store was in a corrupted state and had to be rebuilt")
                } catch (ex: IOException) {
                    Logger.ERROR("Credential store failed to load")
                }

            }

        }


        /**
         * Use this constructor to create a Client.Builder, which can be used to build a Kinvey Client with defaults
         * set for the Android Operating System.
         *
         *
         * This constructor can get configuration for your Kinvey Client from properties file.
         * Save this file within your Android project, and pass InputStream of that file to constructor.
         * You can set appKey and appSecret in  parameters, in this case appKey and appSecret from kinvey.properties
         * will be ignored.
         *
         *
         *
         * This constructor provides support for push notifications.
         *
         *
         *
         * [Kinvey Guide for initializing Client with a properties file.](http://devcenter.kinvey.com/android/guides/getting-started#InitializeClient)
         *
         *
         * @param properties - InputStream of properties file
         * @param context - Your Android Application Context
         */
        constructor(properties: InputStream?, context: Context) : this(properties, newCompatibleTransport(), context) {}

        /*
         * (non-Javadoc)
         *
         * @see com.kinvey.java.core.AbstractKinveyJsonClient.Builder#
         * setHttpRequestInitializer
         * (com.google.api.client.http.HttpRequestInitializer)
         */
        override fun setHttpRequestInitializer(
                httpRequestInitializer: HttpRequestInitializer): Builder<*> {
            return super.setHttpRequestInitializer(httpRequestInitializer) as Builder<*>
        }

        open fun setUserClass(userClass: Class<T>): Builder<*> {
            this.userClass = userClass
            return this
        }

        fun setEncryptionKey(encryptionKey: ByteArray): Builder<*> {
            this.encryptionKey = encryptionKey
            return this
        }

        /**
         * @param requestTimeout - the request timeout
         */
        override fun setRequestTimeout(requestTimeout: Int): Builder<*> {
            super.setRequestTimeout(requestTimeout)
            return this
        }

        /**
         * @param instanceID
         */
        override fun setInstanceID(instanceID: String): Builder<*> {
            super.setInstanceID(instanceID)
            return this
        }

        /**
         * @return an instantiated Kinvey Android Client,
         * which contains factory methods for accessing various functionality.
         */
        override fun build(): Client<T>? {
            kinveyHandlerThread = KinveyHandlerThread("KinveyHandlerThread")
            kinveyHandlerThread?.start()
            val ctx = context ?: return null
            Realm.init(ctx)
            val client = Client<T>(transport,
                    httpRequestInitializer, baseUrl,
                    servicePath, this.objectParser, kinveyClientRequestInitializer, store,
                    requestBackoffPolicy, this.encryptionKey, ctx)

            client.clientUser = AndroidUserStore.getUserStore(ctx)

            val myClass: Class<T> = userClass ?: User::class.java as Class<T>
            client.userClass = myClass

            //FCM explicitly enabled
            if (this.FCM_Enabled) {
                client.pushProvider = FCMPush(client, this.GCM_InProduction, this.FCM_SenderID)
            }

            if (this.debugMode) {
                client.enableDebugLogging()
            }

            client.requestTimeout = this.requestTimeout
            client.syncRate = this.syncRate
            client.batchRate = this.batchRate
            client.isClientRequestMultithreading = this.clientRequestMultithreading
            client.numberThreadPool = this.numberThreadPool
            client.batchSize = this.batchSize
            client.isUseDeltaCache = this.deltaSetCache
            if (this.MICVersion != null) {
                client.micApiVersion = this.MICVersion
            } else {
                client.micApiVersion = DEFAULT_MIC_API_VERSION
            }
            if (this.MICBaseURL != null) {
                client.micHostName = this.MICBaseURL ?: ""
            }
            if (!Strings.isNullOrEmpty(this.instanceID)) {
                client.micHostName = Constants.PROTOCOL_HTTPS + instanceID + Constants.HYPHEN + Constants.HOSTNAME_AUTH
            }
            initUserFromCredentialStore(client)
            return client
        }

        /**
         * Asynchronous Client build method
         *
         *
         *
         *
         *
         *
         * @param buildCallback Instance of [KinveyClientBuilderCallback]
         */
        fun build(buildCallback: KinveyClientBuilderCallback) {
            Build(this, buildCallback).execute()
        }

        protected fun initUserFromCredentialStore(client: Client<T>?) {
            try {
                val credential = retrieveUserFromCredentialStore(client)
                if (credential != null) {
                    loginWithCredential(client, credential)
                }
            } catch (ex: AndroidCredentialStoreException) {
                Logger.ERROR("Credential store was in a corrupted state and had to be rebuilt")
                client?.activeUser = null
            } catch (ex: IOException) {
                Logger.ERROR("Credential store failed to load")
                client?.activeUser = null
            }
        }

        /**
         * Define how credentials will be stored
         *
         * @param store something implementing CredentialStore interface
         * @return this builder, with the new credential store set
         */
        override fun setCredentialStore(store: CredentialStore): Builder<*> {
            super.setCredentialStore(store)
            return this
        }

        /**
         *
         * @param factory - the JSON factory for this client to use
         * @return
         */
        override fun setJsonFactory(factory: JsonFactory): Builder<*> {
            super.setJsonFactory(factory)
            return this
        }


        /**
         * Sets a callback to be called after a client is intialized and BaseUser attributes is being retrieved.
         *
         *
         *
         * When a client is initialized after an initial login, the user's credentials are cached locally and used for the
         * initialization of the client.  As part of the initialization process, a background thread is spawned to retrieve
         * up-to-date user attributes.  This optional callback is called when the retrieval process is complete and passes
         * an instance of the logged in user.
         *
         *
         * Sample Usage:
         * <pre>
         * `Client myClient = Client.Builder(this)
         * .setRetrieveUserCallback(new KinveyUserCallback() {
         * public void onFailure(Throwable t) {
         * CharSequence text = "Error retrieving user attributes.";
         * Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
         * }
         *
         * public void onSuccess(BaseUser u) {
         * CharSequence text = "Retrieved up-to-date data for " + u.getUserName() + ".";
         * Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
         * }
         * }).build();
        ` *
        </pre> *
         * >
         *
         * @param callback
         * @return
         */
        fun setRetrieveUserCallback(callback: KinveyUserCallback<User>?): Builder<T> {
            this.retrieveUserCallback = callback as KinveyUserCallback<T>?
            return this
        }

        /**
         * Builder method to enable FCM for the client
         *
         * @param fcmEnabled - should push use FCM, defaults to true
         * @return the current instance of the builder
         */
        fun enableFCM(fcmEnabled: Boolean): Builder<*> {
            this.FCM_Enabled = fcmEnabled
            return this
        }

        /**
         * Builder method to set sender ID for FCM push
         *
         * @param senderID - the senderID to register
         * @return the current instance of the builder
         */
        fun setSenderIDs(senderID: String): Builder<*> {
            this.FCM_SenderID = senderID
            return this
        }

        /**
         *
         * @see  com.kinvey.java.core.AbstractKinveyJsonClient.Builder.setBaseUrl
         */
        override fun setBaseUrl(baseUrl: String): Builder<*> {
            super.setBaseUrl(baseUrl)
            return this
        }

        fun setGcmInProduction(inProduction: Boolean): Builder<*> {
            this.GCM_InProduction = inProduction
            return this
        }

        @Throws(AndroidCredentialStoreException::class, IOException::class)
        private fun retrieveUserFromCredentialStore(client: Client<T>?): Credential? {
            var credential: Credential? = null
            if (client?.isUserLoggedIn == false) {
                val userID = client.clientUser?.user
                if (userID != null && userID != "") {
                    var store: CredentialStore? = store
                    if (store == null) {
                        store = AndroidCredentialStore(context!!)
                    }
                    val manager = CredentialManager(store)
                    credential = manager.loadCredential(userID)
                }
            }
            return credential
        }

        private fun loginWithCredential(client: Client<T>?, credential: Credential) {
            kinveyClientRequestInitializer.setCredential(credential)
            try {
                BaseUserStore.login(credential, client)
            } catch (ex: IOException) {
                Logger.ERROR("Could not retrieve user Credentials")
            }
            var exception: Exception? = null
            try {
                client?.activeUser = BaseUserStore.convenience(client)
            } catch (error: Exception) {
                exception = error
                if (error is HttpResponseException) {
                    try {
                        BaseUserStore.logout(client)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
            if (exception != null) {
                retrieveUserCallback?.onFailure(exception)
            } else {
                client?.activeUser?.let { retrieveUserCallback?.onSuccess(it) }
            }
        }

        /** Get setting value of delta set caching  */
        fun isDeltaSetCache(): Boolean {
            return deltaSetCache
        }

        /** Set the setting for delta set cache  */
        fun setDeltaSetCache(deltaSetCache: Boolean): Client.Builder<*> {
            this.deltaSetCache = deltaSetCache
            return this
        }

//        private class Build extends AsyncClientRequest<Client> {

//            private Build(KinveyClientBuilderCallback builderCallback) {
//                super(builderCallback);
//            }

//            @Override
//            protected Client executeAsync() {
//                return Client.Builder.this.build();
//            }
//        }

        class Build (val clientBuilder: Builder<*>, builderCallback: KinveyClientBuilderCallback) : AsyncClientRequest<Client<*>>(builderCallback) {

            override fun executeAsync(): Client<*>? {
                return clientBuilder.build() as Client<*>?
            }
        }

        companion object {

            /**
             * creating new HttpTransport with fix for 401 error that raise an exception MLIBZ-708
             * @return HttpTransport
             */
            private fun newCompatibleTransport(): HttpTransport {
                /*  http://developer.android.com/intl/zh-cn/reference/javax/net/ssl/SSLSocket.html
                support for SSL/TLSv1 was disabled in the Kinvey Android SDK 3.1.3 version
                SDK 16-19 are versions where TLSv1.1 and TLSv1.2 are disabled by default
             */
                return if (android.os.Build.VERSION.SDK_INT >= 16 && android.os.Build.VERSION.SDK_INT <= 19)
                    buildSupportHttpTransport()
                else
                    NetHttpTransport()
            }

            private fun buildSupportHttpTransport(): HttpTransport {
                var httpTransport: NetHttpTransport
                try {
                    httpTransport = NetHttpTransport.Builder()
                            .setProxy(null)
                            .setHostnameVerifier(null)
                            .setSslSocketFactory(KinveySocketFactory())
                            .build()
                } catch (e: KeyManagementException) {
                    e.printStackTrace()
                    httpTransport = NetHttpTransport()
                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                    httpTransport = NetHttpTransport()
                }

                return httpTransport
            }

            /**
             * The default kinvey settings filename `assets/kinvey.properties`
             *
             * @return `assets/kinvey.properties`
             */
            protected val androidPropertyFile: String
                get() = "assets/kinvey.properties"
        }
    }
    /**
     * Use this constructor to create a AbstractClient.Builder, which can be used to build a Kinvey AbstractClient with defaults
     * set for the Android Operating System.
     *
     *
     * This constructor does NOT support push notification functionality.
     * If push notifications are necessary, use a properties file and the overloaded constructor.
     *
     *
     * @param appKey Your Kinvey Application Key
     * @param appSecret Your Kinvey Application Secret
     * @param context Your Android Application Context
     */
    /**
     * Use this constructor to create a Client.Builder, which can be used to build a Kinvey Client with defaults
     * set for the Android Operating System.
     *
     *
     * This constructor requires a  properties file, containing configuration for your Kinvey Client.
     * Save this file within your Android project, at:  assets/kinvey.properties
     *
     *
     *
     * This constructor provides support for push notifications.
     *
     *
     *
     * [Kinvey Guide for initializing Client with a properties file.](http://devcenter.kinvey.com/android/guides/getting-started#InitializeClient)
     *
     *
     * @param context - Your Android Application Context
     */

    /**
     * Terminates KinveyHandlerThread.
     * Should be called if the Client instance is not used anymore to prevent from memory leaks.
     * Currently this method is called from Instrumented tests, since each test has its own Client instance.
     */
    fun stopKinveyHandlerThread() {
        kinveyHandlerThread?.stopHandlerThread()
        kinveyHandlerThread?.quit()
        kinveyHandlerThread?.interrupt()
    }

    companion object {

        /** global TAG used in Android logging  */
        const val TAG = "Kinvey - Client"

        @JvmStatic
        var kinveyHandlerThread: KinveyHandlerThread? = null

        //lateinit var sharedInstance: Client<User>
        @JvmStatic
        fun sharedInstance(): Client<User> {
            return sharedInstance as Client<User>
        }
    }
}


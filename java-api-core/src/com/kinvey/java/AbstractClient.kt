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

package com.kinvey.java

import com.google.api.client.http.BackOffPolicy
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.util.GenericData

import java.io.IOException
import java.lang.reflect.Array
import java.net.URL
import java.util.ArrayList
import java.util.Properties
import java.util.logging.Level
import java.util.logging.Logger

import com.kinvey.java.auth.ClientUser
import com.kinvey.java.auth.Credential
import com.kinvey.java.auth.CredentialManager
import com.kinvey.java.auth.CredentialStore
import com.kinvey.java.cache.ICacheManager
import com.kinvey.java.core.AbstractKinveyClientRequest
import com.kinvey.java.core.AbstractKinveyJsonClient
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.network.NetworkFileManager
import com.kinvey.java.query.MongoQueryFilter
import com.kinvey.java.store.BaseDataStore
import com.kinvey.java.store.BaseFileStore
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.SyncManager

/**
 * The core Kinvey client used to access Kinvey's BaaS.
 *
 * All factory methods for retrieving instances of a Service API are threadsafe, however the builder is not.
 */
abstract class AbstractClient<T : BaseUser>
/**
 * Private constructor.  Use AbstractClient.Builder to initialize the AbstractClient.
 *
 * @param transport                HttpTransport
 * @param httpRequestInitializer   HttpRequestInitializer
 * @param rootUrl                  Root URL of service
 * @param servicePath              path of Service
 * @param objectParser             JsonObjectParser
 * @param kinveyRequestInitializer KinveyRequestInitializer
 * @param requestPolicy            BackoffPolicy
 */
protected constructor(transport: HttpTransport?,
                      httpRequestInitializer: HttpRequestInitializer?, rootUrl: String?,
                      servicePath: String?, objectParser: JsonObjectParser?,
                      kinveyRequestInitializer: KinveyClientRequestInitializer?, val store: CredentialStore?,
                      requestPolicy: BackOffPolicy?) : AbstractKinveyJsonClient(transport, httpRequestInitializer,
                      rootUrl, servicePath, objectParser, kinveyRequestInitializer, requestPolicy) {

    /** used to synchronized access to the local api wrappers  */
    protected val lock = Any()

    /** List of extensions, if they need to be locked down  */
    protected var extensions: List<ClientExtension>? = mutableListOf()
        get(): List<ClientExtension>? {
            if (field == null) {
                field = mutableListOf()
            }
            return field
        }

    /** Class to use for representing a BaseUser  */
    var userClass: Class<T> = BaseUser::class.java as Class<T>

    var clientAppVersion: String? = null

    var customRequestProperties: GenericData? = GenericData()
        private set

    /**
     * Value that represents state if delta set caching should be enabled
     */
    /**
     * Getter to check if delta set cache is enabled
     * @return delta set get flag
     */
    /**
     * Setter for delta set get cache flag
     * @param useDeltaCache boolean representing if we should use delta set caching
     */
    @get:Deprecated("use {@link BaseDataStore#isDeltaSetCachingEnabled()} ()} instead.")
    @set:Deprecated("use {@link BaseDataStore#setDeltaSetCachingEnabled(boolean)} ()} instead.")
    var isUseDeltaCache: Boolean = false

    protected open var user: T? = null

    /**
     * The hostname to use for MIC authentication
     */
    var micHostName = "https://auth.kinvey.com/"
        @Throws(IllegalArgumentException::class)
        @JvmName("setMICHostName")
        set(MICHostName) {
            if (!MICHostName.startsWith(Constants.PROTOCOL_HTTPS)) {
                throw IllegalArgumentException("MIC url should be sercure url")
            }
            field = if (MICHostName.endsWith("/")) MICHostName else "$MICHostName/"
        }
        @JvmName("getMICHostName")
        get() = field

    var micApiVersion: String? = null
        @JvmName("setMICApiVersion")
        set(version) {
            var version = version
            if (version?.startsWith("v") == false) {
                version = "v$version"
            }
            field = version
        }
        @JvmName("getMICApiVersion")
        get() = field

    var requestTimeout: Int = 0
    var numberThreadsForDataStoreSaveList = DEFAULT_NUMBER_OF_THREADS_FOR_DATASTORE_SAVE_OF_LIST

    val isUserLoggedIn: Boolean
        get() = activeUser != null && activeUser?.id != null

    val isInitialize: Boolean
        get() = (kinveyRequestInitializer as KinveyClientRequestInitializer).appKey != null && (kinveyRequestInitializer as KinveyClientRequestInitializer).appSecret != null

    open var clientUser: ClientUser? = null

    open var activeUser: T? = null

    open var cacheManager: ICacheManager? = null

    open var userCacheManager: ICacheManager? = null

    open var fileCacheFolder: String? = null

    protected open var syncCacheManager: ICacheManager? = null

    val credentialStore: CredentialStore?
        get() {
            return store
        }

    /**
     *
     * @return SyncManager object
     */
    val syncManager: SyncManager
        get() = SyncManager(syncCacheManager)

    val userArrayClass: Class<*>
        get() = Array.newInstance(userClass, 0)::class.java

    open val deviceId: String = ""

    fun setClientAppVersion(major: Int, minor: Int, revision: Int) {
        clientAppVersion = "$major.$minor.$revision"
    }

    fun setCustomRequestProperties(customheaders: GenericJson?) {
        this.customRequestProperties = customheaders
    }

    fun setCustomRequestProperty(key: String, value: Any) {
        if (this.customRequestProperties == null) {
            this.customRequestProperties = GenericJson()
        }
        this.customRequestProperties!![key] = value
    }


    fun clearCustomRequestProperties() {
        this.customRequestProperties = GenericJson()
    }

    init {
        sharedInstance = this
        this.user = null
        KINVEY_API_VERSION = ""
    }

    fun query(): Query {
        return Query(MongoQueryFilter.MongoQueryFilterBuilder())
    }

    abstract fun userDiscovery(): UserDiscovery<*>?

    abstract fun userGroup(): UserGroup?

    abstract fun <I : GenericJson, O> customEndpoints(myClass: Class<O>?): CustomEndpoints<I, O>

    /**
     * Pings the Kinvey backend service with a logged in user.
     *
     * @return true if service is reachable and user is logged in, false if not
     * @throws IOException
     */
    @Throws(IOException::class)
    fun pingBlocking(): Boolean {
        val util = Util(this)
        util.pingBlocking().execute()
        return true
    }

    /**
     * Checks to see if the credential exists for the given UserID, and initializes the KinveyClientRequestInitializer
     * and the BaseUser if it is.
     *
     * @param userID the ID for the given user.
     * @return true if user credential exists, false if not.
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    private fun getCredential(userID: String): Boolean {

        val credentialManager = CredentialManager(store)
        val storedCredential = credentialManager.loadCredential(userID)
        if (storedCredential != null) {
            (this.kinveyRequestInitializer as KinveyClientRequestInitializer)
                    .setCredential(storedCredential)
            return true
        } else {
            return false
        }
    }

    /**
     * Initializes the Kinvey client request. This method is only used internally to the library.
     */
    @Throws(java.io.IOException::class)
    override fun initializeRequest(httpClientRequest: AbstractKinveyClientRequest<*>) {
        super.initializeRequest(httpClientRequest)
    }

    /**
     * Add logging for the HttpTransport class to Level.FINEST.
     *
     *
     * Request and response log messages will be dumped to LogCat.
     *
     */
    fun enableDebugLogging() {
        Logger.getLogger(HttpTransport::class.java.name).level = Level.FINEST
    }

    fun registerExtension(extension: ClientExtension) {
        if (extensions == null) {
            extensions = mutableListOf()
        }
        (extensions as MutableList).add(extension)
    }

    /**
     * Disable logging for the HttpTransport class to Level.FINEST.
     */
    fun disableDebugLogging() {
        Logger.getLogger(HttpTransport::class.java.name).level = Level.INFO
    }

    /**
     * Builder class for AppdataKinveyClient.
     *
     * This Builder is not thread safe.
     */
    abstract class Builder : AbstractKinveyJsonClient.Builder {
        protected var instanceID: String = ""
        protected val props = Properties()
        protected var requestTimeout = DEFAULT_REQUEST_TIMEOUT
        var useDeltaCache: Boolean = false
        var store: CredentialStore? = null

        /**
         * @param transport              HttpTransport
         * @param httpRequestInitializer HttpRequestInitializer
         */
        constructor(transport: HttpTransport,
                    httpRequestInitializer: HttpRequestInitializer?) : super(transport, DEFAULT_BASE_URL,
                DEFAULT_SERVICE_PATH, httpRequestInitializer) {
        }

        /**
         * @param transport                HttpTransport
         * @param httpRequestInitializer   HttpRequestInitializer
         * @param clientRequestInitializer KinveyClientRequestInitializer
         */
        constructor(transport: HttpTransport,
                    httpRequestInitializer: HttpRequestInitializer?,
                    clientRequestInitializer: KinveyClientRequestInitializer) : super(transport, DEFAULT_BASE_URL,
                DEFAULT_SERVICE_PATH, httpRequestInitializer, clientRequestInitializer) {
        }

        /**
         *
         * @param transport                HttpTransport
         * @param baseUrl
         * @param httpRequestInitializer   HttpRequestInitializer
         * @param clientRequestInitializer KinveyClientRequestInitializer
         */
        constructor(transport: HttpTransport,
                    baseUrl: String,
                    httpRequestInitializer: HttpRequestInitializer,
                    clientRequestInitializer: KinveyClientRequestInitializer) : super(transport, baseUrl,
                DEFAULT_SERVICE_PATH, httpRequestInitializer, clientRequestInitializer) {
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.kinvey.java.core.AbstractKinveyJsonClient.Builder#setRootUrl(java
         * .lang.String)
         */
        override fun setBaseUrl(baseUrl: String): Builder {
            return super.setBaseUrl(baseUrl) as Builder
        }

        open fun setCredentialStore(store: CredentialStore): Builder {
            this.store = store
            return this
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.kinvey.java.core.AbstractKinveyJsonClient.Builder#setServiceUrl
         * (java.lang.String)
         */
        override fun setServiceUrl(serviceUrl: String): Builder {
            return super.setServiceUrl(serviceUrl) as Builder
        }

        /*
         * (non-Javadoc)
         *
         * @see com.kinvey.java.core.AbstractKinveyJsonClient.Builder#
         * setHttpRequestInitializer
         * (com.google.api.client.http.HttpRequestInitializer)
         */
        override fun setHttpRequestInitializer(
                httpRequestInitializer: HttpRequestInitializer): Builder {
            return super.setHttpRequestInitializer(httpRequestInitializer) as Builder
        }

        /*
         * (non-Javadoc)
         *
         * @see com.kinvey.java.core.AbstractKinveyJsonClient.Builder#
         * setKinveyClientRequestInitializer
         * (com.kinvey.java.core.KinveyRequestInitializer)
         */
        override fun setKinveyClientRequestInitializer(
                kinveyRequestInitializer: KinveyClientRequestInitializer): Builder {
            return super.setKinveyClientRequestInitializer(kinveyRequestInitializer) as Builder
        }


        protected fun loadPropertiesFromDisk(propFilename: String) {
            try {
                val res = AbstractClient::class.java.getResource(propFilename)
                props.load(res.openStream())
            } catch (e: Exception) {
                throw RuntimeException("Could not find $propFilename properties file")
            }


        }

        /**
         * Gets the `String` value for the setting loaded for the corresponding option
         *
         * @param opt
         * The option for which to fetch the setting value
         * @return The value of the setting
         */
        fun getString(opt: Option): String {
            return props.getProperty(opt.value) ?: ""
        }

        fun getString(opt: Option, defaultValue: String): String {
            return props.getProperty(opt.value, defaultValue) ?: ""
        }

        /**
         * @param requestTimeout - the request timeout
         */
        open fun setRequestTimeout(requestTimeout: Int): Builder {
            this.requestTimeout = requestTimeout
            return this
        }

        open fun setInstanceID(instanceID: String): Builder {
            this.instanceID = instanceID
            setBaseUrl(Constants.PROTOCOL_HTTPS + instanceID + Constants.HYPHEN + Constants.HOSTNAME_API)
            return this
        }


        /**
         * Standard set of kinvey property names that are set in the `kinvey.properties`
         *
         *
         */
        enum class Option constructor(val value: String?) {
            /** Optional. Used to base url generating  */
            INSTANCE_ID("instance.id"),
            /** Optional. Usually the base url minus the port e.g. `http://api.kinvey.com`  */
            BASE_URL("api.base.url"),
            /** Optional. Usually 80 and used to build the api base url  */
            PORT("api.port"),
            /** Required. Unique id assigned to this app for api access  */
            APP_KEY("app.key"),
            /** Required. Assign at the time the app is created, used to ensure the application communication is trusted  */
            APP_SECRET("app.secret"),
            /** Push options  */
            /** PUSH application key key  */
            PUSH_APP_KEY("push.key"),
            /** PUSH application secret key  */
            PUSH_APP_SECRET("push.secret"),
            /** PUSH mode key  */
            PUSH_MODE("push.mode"),
            /** PUSH enabled key  */
            PUSH_ENABLED("push.enabled"),
            /**FCM Push enabled  */
            FCM_PUSH_ENABLED("fcm.enabled"),
            /**FCM Sender ID  */
            FCM_SENDER_ID("fcm.senderID"),
            /**GCM SERVER URL  */
            GCM_PROD_MODE("gcm.production"),
            /** time limit for retrying failed offline requests  */
            SYNC_RATE("sync.rate"),
            /**time between batches of offline requests  */
            BATCH_RATE("batch.rate"),
            /**size of batch of offline requests  */
            BATCH_SIZE("batch.size"),
            /**enable async requests  */
            CLIENT_REQUEST_MULTITHREADING("client.request.multithreading"),
            /**number of threads for pool of threads  */
            NUMBER_THREAD_POOL("number.thread.pool"),
            /** debug mode, used for HTTP logging  */
            DEBUG_MODE("debug"),
            /** JSON parser */
            PARSER("parser"),
            /**MIC Base URL */
            MIC_BASE_URL("mic.base.url"),
            /**MIC Version */
            MIC_VERSION("mic.version"),
            /**Kinvey API Version */
            KINVEY_API_VERSION("kinvey.api.version"),
            /** Request Timeout for http requests  */
            /**DeltaSet cache enabled  */
            DELTA_SET_CACHE("app.deltaset"),
            /** Request Timeout */
            REQUEST_TIMEOUT("request.timeout")
        }
    }

    override fun getFileStore(storeType: StoreType): BaseFileStore {
        return BaseFileStore(NetworkFileManager(this),
                cacheManager, 60 * 1000 * 1000L,
                storeType, fileCacheFolder)
    }

    companion object {

        /**
         * The default encoded root URL of the service.
         */
        const val DEFAULT_BASE_URL = "https://baas.kinvey.com/"

        /**
         * The default encoded service path of the service.
         */
        const val DEFAULT_SERVICE_PATH = ""

        /**
         * The default request timeout is 60s.
         */
        const val DEFAULT_REQUEST_TIMEOUT = 60 * 1000

        /**
         * The default MIC API version.
         */
        const val DEFAULT_MIC_API_VERSION = "3"

        const val DEFAULT_NUMBER_OF_THREADS_FOR_DATASTORE_SAVE_OF_LIST = 1

        /**
         * Non-default version of API. Developer should initialize it for change API version
         */
        private lateinit var KINVEY_API_VERSION: String
        @JvmStatic var kinveyApiVersion: String
            get() {
                if (!::KINVEY_API_VERSION.isInitialized) {
                    KINVEY_API_VERSION = ""
                }
                return KINVEY_API_VERSION
            }
            set(value) {
                KINVEY_API_VERSION = value
            }

        @JvmStatic
        var sharedInstance: AbstractClient<*>? = null
    }
}

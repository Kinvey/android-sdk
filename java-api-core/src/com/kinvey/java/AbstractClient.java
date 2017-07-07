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

package com.kinvey.java;

import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.GenericData;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kinvey.java.auth.ClientUser;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.CredentialManager;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.BaseUser;
import com.kinvey.java.network.NetworkFileManager;
import com.kinvey.java.query.MongoQueryFilter;
import com.kinvey.java.store.BaseFileStore;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.sync.SyncManager;

/**
 * The core Kinvey client used to access Kinvey's BaaS.
 *
 * All factory methods for retrieving instances of a Service API are threadsafe, however the builder is not.
 */
public abstract class AbstractClient<T extends BaseUser> extends AbstractKinveyJsonClient {

    /**
     * The default encoded root URL of the service.
     */
    public static final String DEFAULT_BASE_URL = "https://baas.kinvey.com/";

    /**
     * The default encoded service path of the service.
     */
    public static final String DEFAULT_SERVICE_PATH = "";
    
    private CredentialStore store;

    /** used to synchronized access to the local api wrappers **/
    protected final Object lock = new Object();

    /** List of extensions, if they need to be locked down **/
    private ArrayList<ClientExtension> extensions;

    /** Class to use for representing a BaseUser **/
    private Class userModelClass = BaseUser.class;
    
    private String clientAppVersion = null;
    
    private GenericData customRequestProperties = new GenericData();

    /**
     * Value that represents state if delta set caching should be enabled
     */
    private boolean useDeltaCache;

    protected T user;

    private static AbstractClient _sharedInstance;

    /**
     * The hostname to use for MIC authentication
     */
    private String MICHostName = "https://auth.kinvey.com/";

    private String MICApiVersion;
    private int requestTimeout;

    public void setMICApiVersion(String version){
        if (!version.startsWith("v")){
            version = "v" + version;
        }
        MICApiVersion = version;
    }

    public void setMICHostName(String MICHostName) throws IllegalArgumentException {
        if (!MICHostName.startsWith("https://")){
            throw new IllegalArgumentException("MIC url should be sercure url");
        }
        this.MICHostName = MICHostName.endsWith("/") ? MICHostName : MICHostName + "/";
    }

    public String getMICHostName() {
        return MICHostName;
    }

    public String getMICApiVersion() {
        return MICApiVersion;
    }

    public void setClientAppVersion(String appVersion){
    	this.clientAppVersion = appVersion;	
    }
    
    public void setClientAppVersion(int major, int minor, int revision){
    	setClientAppVersion(major + "." + minor + "." + revision);
    }
    
    public String getClientAppVersion(){
    	return this.clientAppVersion;
    }
    
    public void setCustomRequestProperties(GenericJson customheaders){
    	this.customRequestProperties = customheaders;
    }
    
    public void setCustomRequestProperty(String key, Object value){
    	if (this.customRequestProperties == null){
    		this.customRequestProperties = new GenericJson();
    	}
    	this.customRequestProperties.put(key, value);
    }

    public boolean isUserLoggedIn() {
        return getActiveUser() != null && getActiveUser().getId() !=null;
    }



    public void clearCustomRequestProperties(){
    	this.customRequestProperties = new GenericJson();
    }
    
    public GenericData getCustomRequestProperties(){
    	return this.customRequestProperties;
    }

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
    protected AbstractClient(HttpTransport transport,
                             HttpRequestInitializer httpRequestInitializer, String rootUrl,
                             String servicePath, JsonObjectParser objectParser,
                             KinveyClientRequestInitializer kinveyRequestInitializer, CredentialStore store,
                             BackOffPolicy requestPolicy) {

        super(transport, httpRequestInitializer, rootUrl, servicePath,
                objectParser, kinveyRequestInitializer, requestPolicy);
        _sharedInstance = this;
        this.user = null;
        this.store = store;
    }

    public static AbstractClient sharedInstance(){
        return _sharedInstance;
    }

    public Query query() {
        return new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
    }

    public abstract UserDiscovery userDiscovery();

    public abstract UserGroup userGroup();

    public boolean isInitialize() {
        return (((KinveyClientRequestInitializer) getKinveyRequestInitializer()).getAppKey() != null &&
                ((KinveyClientRequestInitializer) getKinveyRequestInitializer()).getAppSecret() != null);
    }

    public abstract ClientUser getClientUser();

    public abstract  <I extends GenericJson, O> CustomEndpoints<I, O> customEndpoints(Class<O> myClass);

    /**
     * Pings the Kinvey backend service with a logged in user.
     *
     * @return true if service is reachable and user is logged in, false if not
     * @throws IOException
     */
    public boolean pingBlocking() throws IOException {
        Util util = new Util(this);
        util.pingBlocking().execute();
        return true;
    }

    public abstract void setUser(T user);

    public abstract T getActiveUser();

    public CredentialStore getStore() {
        return store;
    }

    /**
     * Checks to see if the credential exists for the given UserID, and initializes the KinveyClientRequestInitializer
     * and the BaseUser if it is.
     *
     * @param userID the ID for the given user.
     * @return true if user credential exists, false if not.
     * @throws java.io.IOException
     */
    private boolean getCredential(String userID) throws IOException {

        CredentialManager credentialManager = new CredentialManager(store);
        Credential storedCredential = credentialManager.loadCredential(userID);
        if (storedCredential != null) {
            ((KinveyClientRequestInitializer) this.getKinveyRequestInitializer())
                    .setCredential(storedCredential);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Initializes the Kinvey client request. This method is only used internally to the library.
     */
    @Override
    public void initializeRequest(AbstractKinveyClientRequest<?> httpClientRequest)
            throws java.io.IOException {
        super.initializeRequest(httpClientRequest);
    }

    /**
     * Add logging for the HttpTransport class to Level.FINEST.
     * <p>
     * Request and response log messages will be dumped to LogCat.
     * </p>
     */
    public void enableDebugLogging() {
        Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.FINEST);
    }

    public void registerExtension(ClientExtension extension){
        if(extensions == null){
            extensions = new ArrayList<ClientExtension>();
        }
        extensions.add(extension);
    }

    public ArrayList<ClientExtension> getExtensions(){
        if (extensions == null){
            extensions = new ArrayList<ClientExtension>();
        }
        return extensions;
    }

    /**
     * Disable logging for the HttpTransport class to Level.FINEST.
     */
    public void disableDebugLogging() {
        Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.INFO);
    }


    /**
     * Getter to check if delta set cache is enabled
     * @return delta set get flag
     */
    public boolean isUseDeltaCache() {
        return useDeltaCache;
    }

    /**
     * Setter for delta set get cache flag
     * @param useDeltaCache boolean representing if we should use delta set caching
     */
    public void setUseDeltaCache(boolean useDeltaCache) {
        this.useDeltaCache = useDeltaCache;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    /**
     * Builder class for AppdataKinveyClient.
     *
     * This Builder is not thread safe.
     */
    public static abstract class Builder extends AbstractKinveyJsonClient.Builder {
        private CredentialStore store;
        private Properties props = new Properties();
        private int requestTimeout;
        public boolean useDeltaCache;

        /**
         * @param transport              HttpTransport
         * @param httpRequestInitializer HttpRequestInitializer
         */
        public Builder(HttpTransport transport,
                       HttpRequestInitializer httpRequestInitializer) {
            super(transport, DEFAULT_BASE_URL,
                    DEFAULT_SERVICE_PATH, httpRequestInitializer);
        }

        /**
         * @param transport                HttpTransport
         * @param httpRequestInitializer   HttpRequestInitializer
         * @param clientRequestInitializer KinveyClientRequestInitializer
         */
        public Builder(HttpTransport transport,
                       HttpRequestInitializer httpRequestInitializer,
                       KinveyClientRequestInitializer clientRequestInitializer) {
            super(transport, DEFAULT_BASE_URL,
                    DEFAULT_SERVICE_PATH, httpRequestInitializer, clientRequestInitializer);
        }

        /**
         *
         * @param transport                HttpTransport
         * @param baseUrl
         * @param httpRequestInitializer   HttpRequestInitializer
         * @param clientRequestInitializer KinveyClientRequestInitializer
         */
        public Builder(HttpTransport transport,
                       String baseUrl,
                       HttpRequestInitializer httpRequestInitializer,
                       KinveyClientRequestInitializer clientRequestInitializer) {
            super(transport, baseUrl,
                    DEFAULT_SERVICE_PATH, httpRequestInitializer, clientRequestInitializer);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.kinvey.java.core.AbstractKinveyJsonClient.Builder#setRootUrl(java
         * .lang.String)
         */
        @Override
        public Builder setBaseUrl(String baseUrl) {
            return (Builder) super.setBaseUrl(baseUrl);
        }


        public Builder setCredentialStore(CredentialStore store) {
            this.store = store;
            return this;
        }

        public CredentialStore getCredentialStore() {
            return store;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * com.kinvey.java.core.AbstractKinveyJsonClient.Builder#setServiceUrl
         * (java.lang.String)
         */
        @Override
        public Builder setServiceUrl(String serviceUrl) {
            return (Builder) super.setServiceUrl(serviceUrl);
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

        /*
         * (non-Javadoc)
         *
         * @see com.kinvey.java.core.AbstractKinveyJsonClient.Builder#
         * setKinveyClientRequestInitializer
         * (com.kinvey.java.core.KinveyRequestInitializer)
         */
        @Override
        public Builder setKinveyClientRequestInitializer(
                KinveyClientRequestInitializer kinveyRequestInitializer) {
            return (Builder) super.setKinveyClientRequestInitializer(kinveyRequestInitializer);
        }


        protected void loadPropertiesFromDisk(String propFilename){
            try {
                final URL res = AbstractClient.class.getResource(propFilename);
                props.load(res.openStream());
            } catch (Exception e) {
                throw new RuntimeException("Could not find " + propFilename + " properties file");
            }


        }


        protected Properties getProps(){
            return this.props;
        }

        /**
         * Gets the {@code String} value for the setting loaded for the corresponding option
         *
         * @param opt
         *            The option for which to fetch the setting value
         * @return The value of the setting
         */
        public String getString(final Option opt) {
            return getProps().getProperty(opt.value);
        }

        public String getString(final Option opt, final String defaultValue) {
            return getProps().getProperty(opt.value, defaultValue);
        }

        public Builder setRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }


        /**
         * Standard set of kinvey property names that are set in the {@code kinvey.properties}
         *
         *
         */
        public enum Option {
            /** Optional. Usually the base url minus the port e.g. {@code http://api.kinvey.com} */
            BASE_URL("api.base.url"),
            /** Optional. Usually 80 and used to build the api base url */
            PORT("api.port"),
            /** Required. Unique id assigned to this app for api access */
            APP_KEY("app.key"),
            /** Required. Assign at the time the app is created, used to ensure the application communication is trusted */
            APP_SECRET("app.secret"),
            /** Push options **/
            /** PUSH application key key **/
            PUSH_APP_KEY("push.key"),
            /** PUSH application secret key **/
            PUSH_APP_SECRET("push.secret"),
            /** PUSH mode key **/
            PUSH_MODE("push.mode"),
            /** PUSH enabled key **/
            PUSH_ENABLED("push.enabled"),
            /**GCM Push enabled **/
            GCM_PUSH_ENABLED("gcm.enabled"),
            /**GCM Sender ID **/
            GCM_SENDER_ID("gcm.senderID"),
            /**GCM SERVER URL **/
            GCM_PROD_MODE("gcm.production"),
            /** time limit for retrying failed offline requests **/
            SYNC_RATE("sync.rate"),
            /**time between batches of offline requests **/
            BATCH_RATE("batch.rate"),
            /**size of batch of offline requests **/
            BATCH_SIZE("batch.size"),
            /** debug mode, used for HTTP logging **/
            DEBUG_MODE("debug"),
            /** JSON parser**/
            PARSER("parser"),
            /**MIC Base URL**/
            MIC_BASE_URL("mic.base.url"),
            /**MIC Version**/
            MIC_VERSION("mic.version"),
            /** Request Timeout for http requests **/
            /**DeltaSet cache enabled **/
            DELTA_SET_CACHE("app.deltaset"),
            /** Request Timeout**/
            REQUEST_TIMEOUT("request.timeout");


            private final String value;

            private Option(final String val) {
                value = val;
            }

            String getValue() {
                return value;
            }
        }
    }

    public abstract ICacheManager getCacheManager();

    public abstract String getFileCacheFolder();

    public BaseFileStore getFileStore(StoreType storeType){
        return new BaseFileStore(new NetworkFileManager(this),
                getCacheManager(), 60*1000*1000L,
                storeType, getFileCacheFolder());
    }

    protected abstract ICacheManager getSyncCacheManager();

    public SyncManager getSycManager(){
        return new SyncManager(getSyncCacheManager());
    }

    public Class getUserClass(){
        return this.userModelClass;
    }

    public void setUserClass(Class userClass){
        this.userModelClass = userClass;
    }

}

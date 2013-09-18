/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kinvey.java.auth.ClientUsers;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.CredentialManager;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.query.MongoQueryFilter;

/**
 * The core Kinvey client used to access Kinvey's BaaS.
 *
 * All factory methods for retrieving instances of a Service API are threadsafe, however the builder is not.
 */
public abstract class AbstractClient extends AbstractKinveyJsonClient {

    /**
     * The default encoded root URL of the service.
     */
    public static final String DEFAULT_BASE_URL = "https://baas.kinvey.com/";

    /**
     * The default encoded service path of the service.
     */
    public static final String DEFAULT_SERVICE_PATH = "";

    /** allows for finer grained logging, useful when debugging **/
    private static boolean enableRequestLogging;


    private User currentUser;
    private CredentialStore store;

    /** used to synchronized access to the local api wrappers **/
    protected Object lock = new Object();


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
        this.store = store;
    }

    /**
     *
     *
     * @return a new instance of the AppData class
     */
    public abstract <T> AppData<T> appData(String collectionName, Class<T> myClass);


    /**
     * @return a new instance of File api wrapper
     */
    public abstract File file();

    public Query query() {
        return new Query(new MongoQueryFilter.MongoQueryFilterBuilder());
    }

    public abstract UserDiscovery userDiscovery();

    public abstract UserGroup userGroup();

    public User user() {
        synchronized (lock) {
            if (currentUser == null) {
                String appKey = ((KinveyClientRequestInitializer) getKinveyRequestInitializer()).getAppKey();
                String appSecret = ((KinveyClientRequestInitializer) getKinveyRequestInitializer()).getAppSecret();
                this.currentUser = new User(this, new KinveyAuthRequest.Builder(this.getRequestFactory().getTransport(),
                        this.getJsonFactory(), this.getBaseUrl(), appKey, appSecret, null));
            }
            return currentUser;
        }
    }

    protected abstract ClientUsers getClientUsers();

    public abstract CustomEndpoints customEndpoints();

    /**
     * Pings the Kinvey backend service with a logged in user.
     *
     * @return true if service is reachable and user is logged in, false if not
     * @throws IOException
     */
    public boolean pingBlocking() throws IOException {
        Util util = new Util(this);
        return util.pingBlocking().executeUnparsed().getStatusCode() == 200;
    }

    protected void setCurrentUser(User user) {
        synchronized (lock) {
            currentUser = user;
        }
    }

    protected User getCurrentUser() {
        synchronized (lock) {
            return currentUser;
        }
    }

    public CredentialStore getStore() {
        return store;
    }

    /**
     * Checks to see if the credential exists for the given UserID, and initializes the KinveyClientRequestInitializer
     * and the User if it is.
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
        AbstractClient.enableRequestLogging = true;
        Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.FINEST);
    }

    /**
     * Disable logging for the HttpTransport class to Level.FINEST.
     */
    public void disableDebugLogging() {
        AbstractClient.enableRequestLogging = false;
        Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.INFO);
    }

    /**
     * Builder class for AppdataKinveyClient.
     *
     * This Builder is not thread safe.
     */
    public static abstract class Builder extends AbstractKinveyJsonClient.Builder {
        private CredentialStore store;
        private Properties props = new Properties();

        /**
         * @param transport              HttpTransport
         * @param jsonFactory            JsonFactory
         * @param httpRequestInitializer HttpRequestInitializer
         */
        public Builder(HttpTransport transport, JsonFactory jsonFactory,
                       HttpRequestInitializer httpRequestInitializer) {
            super(transport, jsonFactory, DEFAULT_BASE_URL,
                    DEFAULT_SERVICE_PATH, httpRequestInitializer);
        }

        /**
         * @param transport                HttpTransport
         * @param jsonFactory              JsonFactory
         * @param httpRequestInitializer   HttpRequestInitializer
         * @param clientRequestInitializer KinveyClientRequestInitializer
         */
        public Builder(HttpTransport transport, JsonFactory jsonFactory,
                       HttpRequestInitializer httpRequestInitializer,
                       KinveyClientRequestInitializer clientRequestInitializer) {
            super(transport, jsonFactory, DEFAULT_BASE_URL,
                    DEFAULT_SERVICE_PATH, httpRequestInitializer, clientRequestInitializer);
        }

        /**
         *
         * @param transport                HttpTransport
         * @param jsonFactory              JsonFactory
         * @param baseUrl
         * @param httpRequestInitializer   HttpRequestInitializer
         * @param clientRequestInitializer KinveyClientRequestInitializer
         */
        public Builder(HttpTransport transport, JsonFactory jsonFactory,
                       String baseUrl,
                       HttpRequestInitializer httpRequestInitializer,
                       KinveyClientRequestInitializer clientRequestInitializer) {
            super(transport, jsonFactory, baseUrl,
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
            } catch (IOException e) {
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
            /**Shared Pref Credential Store*/
            SHARED_PREF("cred.shared"),
            /** debug mode, used for HTTP logging **/
            DEBUG_MODE("debug");

            private final String value;

            private Option(final String val) {
                value = val;
            }

            String getValue() {
                return value;
            }
        }
    }



}

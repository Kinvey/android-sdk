/*
 * Copyright (c) 2014, Kinvey, Inc.
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
package com.kinvey.nativejava;


import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.ExponentialBackOffPolicy;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Logger;
import com.kinvey.java.CustomEndpoints;
import com.kinvey.java.auth.ClientUser;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.CredentialManager;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.auth.InMemoryCredentialStore;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.BaseUser;
import com.kinvey.java.network.NetworkManager;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/** {@inheritDoc}
 *
 * @author edwardf
 * */
public class Client extends AbstractClient {


    private ConcurrentHashMap<String, com.kinvey.nativejava.NetworkManager> appDataInstanceCache;

    private UserDiscovery userDiscovery;
    private com.kinvey.nativejava.NetworkFileManager file;
    private UserGroup userGroup;
    private ClientUser clientUser;


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
    protected Client(HttpTransport transport, HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath, JsonObjectParser objectParser, KinveyClientRequestInitializer kinveyRequestInitializer, CredentialStore store, BackOffPolicy requestPolicy) {
        super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, store, requestPolicy);
        Logger.init(new JavaLogger());

    }
    /**
     * NetworkManager factory method
     * <p>
     * Returns an instance of {@link NetworkManager} for the supplied collection.  A new instance is created for each collection, but
     * only one instance of {@link com.kinvey.nativejava.NetworkManager} is created per collection.  The method is Generic and takes an instance of a
     * {@link com.google.api.client.json.GenericJson} entity type that is used for fetching/saving of {@link NetworkManager}.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     * {@code
    NetworkManager<myEntity> myAppData = kinveyClient.appData("entityCollection", myEntity.class);
    }
     * </pre>
     * </p>
     *
     * @param collectionName The name of the collection
     * @param myClass The class that defines the entity of type {@link com.google.api.client.json.GenericJson} used
     *                for saving and fetching of data
     * @param <T> Generic of type {@link com.google.api.client.json.GenericJson} of same type as myClass
     * @return Instance of {@link NetworkManager} for the defined collection
     */
    public <T extends GenericJson> NetworkManager<T> appData(String collectionName, Class<T> myClass) {
        synchronized (lock) {
            Preconditions.checkNotNull(collectionName, "collectionName must not be null");
            if (appDataInstanceCache == null) {
                appDataInstanceCache = new ConcurrentHashMap<String, com.kinvey.nativejava.NetworkManager>();
            }
            if (!appDataInstanceCache.containsKey(collectionName)) {
                appDataInstanceCache.put(collectionName, new com.kinvey.nativejava.NetworkManager(collectionName, myClass, this));
            }
            if(appDataInstanceCache.containsKey(collectionName) && !appDataInstanceCache.get(collectionName).getCurrentClass().equals(myClass)){
                appDataInstanceCache.put(collectionName, new com.kinvey.nativejava.NetworkManager(collectionName, myClass, this));
            }

            return appDataInstanceCache.get(collectionName);
        }
    }


    @Override
    public void performLockDown() {
        //native java doesn't have any lockdown support as of yet
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
    public UserDiscovery userDiscovery() {
        synchronized (lock) {
            if (userDiscovery == null) {
                userDiscovery = new UserDiscovery(this,
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
    public UserGroup userGroup() {
        synchronized (lock) {
            if (userGroup == null) {
                userGroup = new UserGroup(this,
                        (KinveyClientRequestInitializer) this.getKinveyRequestInitializer());
            }
            return userGroup;
        }

    }

    @Override
    public ClientUser getClientUser() {
        return null;
    }

    @Override
    public void setActiveUser(BaseUser user) {

    }

    @Override
    public BaseUser getActiveUser() {
        return null;
    }

    @Override
    public CustomEndpoints customEndpoints(Class myClass) {
        return null;
    }

/*    @Override
    public <I extends GenericJson, O> com.kinvey.java.CustomEndpoints<I, O> customEndpoints(Class<O> myClass) {
        return null;
    }*/

    @Override
    public ICacheManager getCacheManager() {
        return null;
    }

    @Override
    public String getFileCacheFolder() {
        return null;
    }

    @Override
    protected ICacheManager getSyncCacheManager() {
        return null;
    }

    @Override
    public String getDeviceId() {
        return null;
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
     boolean ping = kinveyClient.ping().execute();
     }
     * </pre>
     * </p>
     * @return true if ping is successful, false if it fails
     */
    public boolean ping() throws IOException{
    	return super.pingBlocking();
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

        private boolean debugMode = false;
        private JsonFactory factory = JavaJson.newCompatibleJsonFactory(JavaJson.JSONPARSER.GSON);


        /**
         * Use this constructor to create a AbstractClient.Builder, which can be used to build a Kinvey AbstractClient with defaults
         * set for the Java runtime.
         *
         * @param appKey Your Kinvey Application Key
         * @param appSecret Your Kinvey Application Secret
         */
        public Builder(String appKey, String appSecret) {
            super(new NetHttpTransport(), null
                    , new KinveyClientRequestInitializer(appKey, appSecret, new KinveyHeaders()));
            this.setRequestBackoffPolicy(new ExponentialBackOffPolicy());
            try {
                this.setCredentialStore(new InMemoryCredentialStore());
            } catch (Exception ex) {
            	Logger.INFO("KINVEY" +  "Credential store failed to load" + ex);
            }
            this.setJsonFactory(this.factory);

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
            super.setBaseUrl(baseUrl);
            return this;
        }


        @Override
        public Builder setJsonFactory(JsonFactory factory){
            super.setJsonFactory(factory);
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
                    getServicePath(), getObjectParser(), getKinveyClientRequestInitializer(), getCredentialStore(),
                    getRequestBackoffPolicy());
            // TODO: 10.2.17 get user from java user storage
//            client.clientUser =
            try {
                Credential credential = retrieveUserFromCredentialStore(client);
                if (credential != null) {
                    loginWithCredential(client, credential);
                }
            } catch (IOException ex) {
            	Logger.INFO("KINVEY" +  "Credential store failed to load" + ex);
                client.setActiveUser(null);
            }


            if (this.debugMode){
                client.enableDebugLogging();
            }

            return client;
        }

        private Credential retrieveUserFromCredentialStore(Client client)
                throws IOException {
            Credential credential = null;
            if (!client.isUserLoggedIn()) {
                String userID = client.getActiveUser().getId();
                if (userID != null && !userID.equals("")) {
                    CredentialStore store;
                    store = new InMemoryCredentialStore();

                    CredentialManager manager = new CredentialManager(store);
                    credential = manager.loadCredential(userID);
                }
            }
            return credential;
        }

        private void loginWithCredential(final Client client, Credential credential) {
            getKinveyClientRequestInitializer().setCredential(credential);
            try {
                UserStore.login(credential, client);
            } catch (IOException ex) {
            	Logger.INFO("KINVEY" + "Could not retrieve user Credentials");
            }

            try{
            client.setActiveUser(UserStore.retrieve(client));
            }catch (IOException ex){
            	Logger.INFO("KINVEY" +  "Unable to login!" + ex);
            }
        }

    }


}

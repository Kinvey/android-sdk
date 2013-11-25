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
package com.kinvey.nativejava;


import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.auth.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author edwardf
 */
public class Client extends AbstractClient {


    private ConcurrentHashMap<String, AppData> appDataInstanceCache;

    private UserDiscovery userDiscovery;
    private File file;
    private UserGroup userGroup;
    private ClientUsers clientUsers;


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
    }
    /**
     * AppData factory method
     * <p>
     * Returns an instance of {@link com.kinvey.java.AppData} for the supplied collection.  A new instance is created for each collection, but
     * only one instance of {@link AppData} is created per collection.  The method is Generic and takes an instance of a
     * {@link com.google.api.client.json.GenericJson} entity type that is used for fetching/saving of {@link com.kinvey.java.AppData}.
     * </p>
     * <p>
     * This method is thread-safe.
     * </p>
     * <p>
     *     Sample Usage:
     * <pre>
     * {@code
    AppData<myEntity> myAppData = kinveyClient.appData("entityCollection", myEntity.class);
    }
     * </pre>
     * </p>
     *
     * @param collectionName The name of the collection
     * @param myClass The class that defines the entity of type {@link com.google.api.client.json.GenericJson} used
     *                for saving and fetching of data
     * @param <T> Generic of type {@link com.google.api.client.json.GenericJson} of same type as myClass
     * @return Instance of {@link com.kinvey.java.AppData} for the defined collection
     */
    public <T> AppData<T> appData(String collectionName, Class<T> myClass) {
        synchronized (lock) {
            Preconditions.checkNotNull(collectionName, "collectionName must not be null");
            if (appDataInstanceCache == null) {
                appDataInstanceCache = new ConcurrentHashMap<String, AppData>();
            }
            if (!appDataInstanceCache.containsKey(collectionName)) {
                appDataInstanceCache.put(collectionName, new AppData(collectionName, myClass, this));
            }
            if(appDataInstanceCache.containsKey(collectionName) && !appDataInstanceCache.get(collectionName).getCurrentClass().equals(myClass)){
                appDataInstanceCache.put(collectionName, new AppData(collectionName, myClass, this));
            }

            return appDataInstanceCache.get(collectionName);
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
     * {@code
    File myFile = kinveyClient.file();
    }
     * </pre>
     * </p>
     *
     * @return Instance of {@link com.kinvey.java.File} for the defined collection
     */
    @Override
    public File file() {
        synchronized (lock) {
            if (file == null) {
                file = new File(this);
            }
            return file;
        }
    }

    @Override
    public void performLockDown() {
        //native java doesn't have any lockdown support as of yet
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
    public <I extends GenericJson, O extends GenericJson> CustomEndpoints<I, O> customEndpoints(Class<O> myClass) {
        synchronized (lock) {
            return new CustomEndpoints(myClass, this);
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

    /** {@inheritDoc} */
    @Override
    protected ClientUsers getClientUsers() {
        synchronized (lock) {
            if (this.clientUsers == null) {
                this.clientUsers = InMemoryClientUsers.getClientUsers();
            }
            return this.clientUsers;
        }

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
     * {@code
    User currentUser = kinveyClient.currentUser();
    }
     * </pre>
     * </p>
     *
     * @return Instance of {@link com.kinvey.java.User} for the defined collection
     */
    @Override
    public User user() {
        synchronized (lock) {
            if (getCurrentUser() == null) {
                String appKey = ((KinveyClientRequestInitializer) getKinveyRequestInitializer()).getAppKey();
                String appSecret = ((KinveyClientRequestInitializer) getKinveyRequestInitializer()).getAppSecret();
                setCurrentUser(new User(this, new KinveyAuthRequest.Builder(getRequestFactory().getTransport(), getJsonFactory(),
                        getBaseUrl(), appKey, appSecret, null)));
            }
            return (User) getCurrentUser();
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
     boolean ping = kinveyClient.ping().execute();
     }
     * </pre>
     * </p>
     * @return true if ping is successful, false if it fails
     */
    public boolean ping() {
        try{
            Util util = new Util(this);
            return util.pingBlocking().executeUnparsed().getStatusCode() == 200;
        }catch(IOException e){
            return false;
        }

    }


    /**
     * Create a client for interacting with Kinvey's services from an Android Activity.
     * <pre>
     * {@code
     * Client myClient =  new Client.Builder(appKey, appSecret, getContext()).build();
     * }
     * <pre/>
     * All features of the library are be accessed through an instance of a client.
     * <p/>
     * It is recommended to maintain a single instance of a {@code Client} while developing with Kinvey, either in an
     * Activity, a Service, or an Application.
     * <p/>
     * This Builder class is not thread-safe.
     */
    public static class Builder extends AbstractClient.Builder {

        private boolean debugMode = false;


        /**
         * Use this constructor to create a AbstractClient.Builder, which can be used to build a Kinvey AbstractClient with defaults
         * set for the Java runtime.
         *
         * @param appKey Your Kinvey Application Key
         * @param appSecret Your Kinvey Application Secret
         */
        public Builder(String appKey, String appSecret) {
            super(new NetHttpTransport(), JavaJson.newCompatibleJsonFactory(), null
                    , new KinveyClientRequestInitializer(appKey, appSecret, new KinveyHeaders()));
            this.setRequestBackoffPolicy(new ExponentialBackOffPolicy());
            try {
                this.setCredentialStore(new InMemoryCredentialStore());
            } catch (Exception ex) {
                System.out.println("KINVEY" +  "Credential store failed to load" + ex);
            }



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
            client.clientUsers = InMemoryClientUsers.getClientUsers();
            try {
                Credential credential = retrieveUserFromCredentialStore(client);
                if (credential != null) {
                    loginWithCredential(client, credential);
                }
            } catch (IOException ex) {
                System.out.println("KINVEY" +  "Credential store failed to load" + ex);
                client.setCurrentUser(null);
            }


            if (this.debugMode){
                client.enableDebugLogging();
            }

            return client;
        }

        private Credential retrieveUserFromCredentialStore(Client client)
                throws IOException {
            Credential credential = null;
            if (!client.user().isUserLoggedIn()) {
                String userID = client.getClientUsers().getCurrentUser();
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
                client.user().login(credential).execute();
            } catch (IOException ex) {
                System.out.println("KINVEY" + "Could not retrieve user Credentials");
            }

            try{
            client.setCurrentUser(client.user().retrieveMetadataBlocking());
            }catch (IOException ex){
                System.out.println("KINVEY" +  "Unable to login!" + ex);
            }
        }




    }


}

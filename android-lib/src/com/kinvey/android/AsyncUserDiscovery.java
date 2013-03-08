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

import com.google.common.base.Preconditions;

import java.io.IOException;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.UserDiscovery;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyClientRequestInitializer;

/**
 * Wraps the {@link com.kinvey.java.UserDiscovery} public methods in asynchronous functionality using native Android AsyncTask.
 *
 * <p>
 * This functionality can be accessed through the {@link Client#userDiscovery()} convenience method.
 * </p>
 *
 * <p>
 * This API is used to search for users based on select criteria.  Methods in this API return results via a
 * {@link com.kinvey.java.core.KinveyClientCallback}.
 * </p>
 *
 * <p>
 * Sample Usage:
 * <pre>
    public void submit(View view) {
    kinveyClient.userDiscovery().lookupByUserName(username, new KinveyClientCallback<User> () {
        public void onFailure(Throwable t) { ... }
        public void onSuccess(User[] u) { ... }
    });
 * </pre>
 *
 * </p>
 *
 * @author mjsalinger
 * @since 2.0
 */
public class AsyncUserDiscovery extends UserDiscovery {

    AsyncUserDiscovery(AbstractClient client, KinveyClientRequestInitializer initializer) {
        super(client, initializer);
    }

    /**
     * Asynchronous user lookup by first and last name
     * <p>
     * Constructs an asynchronous request to find a user by first and last name, and returns the associated User object
     * via a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
        kinveyClient.userDiscovery().lookupByFullName("John","Smith", new KinveyClientCallback<User>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(User u) { ... }
        });
     *  </pre>
     *
     * @param firstname a {@link java.lang.String} object.
     * @param lastname a {@link java.lang.String} object.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @throws java.io.IOException if any.
     */
    public <T> void lookupByFullName(String firstname, String lastname, KinveyClientCallback<T> callback)
            throws IOException{
        Preconditions.checkNotNull(firstname, "firstname must not be null.");
        Preconditions.checkNotNull(lastname, "lastname must not be null.");
        UserLookup lookup = new UserLookup();
        lookup.setFirstName(firstname);
        lookup.setLastName(lastname);
        lookup(lookup, callback);
    }



    /**
     * Asynchronous user lookup by username
     * <p>
     * Constructs an asynchronous request to find a user by username, and returns the associated User object
     * via a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
        kinveyClient.userDiscovery().lookupByFullName("jsmith", new KinveyClientCallback<User>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(User u) { ... }
        });
     * </pre>
     *
     * @param username a {@link java.lang.String} object.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @throws java.io.IOException if any.
     */
    public <T> void lookupByUserName(String username, KinveyClientCallback<T> callback) throws IOException{
        Preconditions.checkNotNull(username, "username must not be null.");
        UserLookup lookup = new UserLookup();
        lookup.setUsername(username);
        lookup(lookup, callback);
    }

     /**
     * Asynchronous user lookup by Facebook ID
     * <p>
     * Constructs an asynchronous request to find a user by facebook ID, and returns the associated User object
     * via a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
        kinveyClient.userDiscovery().lookupByFacebookID("jsmith", new KinveyClientCallback<User>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(User u) { ... }
        });
     * </pre>
      *
     * @param facebookID a {@link java.lang.String} object.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @throws java.io.IOException if any.
     */
    public <T> void lookupByFacebookID(String facebookID, KinveyClientCallback<T> callback) throws IOException{
        Preconditions.checkNotNull(facebookID, "facebookID must not be null.");
        UserLookup lookup = new UserLookup();
        lookup.setFacebookID(facebookID);
        lookup(lookup, callback);
    }


    /**
     * Asynchronous user lookup method
     * <p>
     * Constructs an asynchronous request to find a user, and returns the associated User object
     * via a KinveyClientCallback.   Requests are constructed with a {@link com.google.api.client.json.GenericJson}
     * {@link UserLookup} object, which can be instantiated via the {@link com.kinvey.java.UserDiscovery#userLookup()}
     * factory method.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
        UserLookup lookup = kinveyClient.userDiscovery().userLookup();
        lookup.put("age",21);
        kinveyClient.userDiscovery().lookup(lookup, new KinveyClientCallback<User> {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(User u) { ... }
     });
     * </pre>
     *
     * @param userlookup a UserLookup object.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @throws java.io.IOException if any.
     */
    public <T> void lookup(UserLookup userlookup, KinveyClientCallback<T> callback) throws IOException{

        Preconditions.checkNotNull(userlookup, "userlookup must not be null.");
        new Lookup(userlookup, callback).execute();

    }

    private class Lookup extends AsyncClientRequest<UserLookup> {

        private final UserLookup userLookup;

        private Lookup(UserLookup lookup, KinveyClientCallback callback) {
            super(callback);
            this.userLookup = lookup;
        }

        protected UserLookup executeAsync() throws IOException {
            return AsyncUserDiscovery.this.lookup(userLookup).execute();
        }
    }
}

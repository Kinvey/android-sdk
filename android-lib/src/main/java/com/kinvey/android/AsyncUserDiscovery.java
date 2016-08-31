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

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.kinvey.android.callback.KinveyUserListCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.UserDiscovery;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.User;
import com.kinvey.java.model.UserLookup;

/**
 * Wraps the {@link com.kinvey.java.UserDiscovery} public methods in asynchronous functionality using native Android AsyncTask.
 *
 * <p>
 * This functionality can be accessed through the {@link Client#userDiscovery()} convenience method.
 * </p>
 *
 * <p>
 * This API is used to search for users based on select criteria.  Methods in this API return results via a
 * {@link com.kinvey.android.callback.KinveyUserListCallback}.
 * </p>
 *
 * <p>
 * Sample Usage:
 * <pre>
 * {@code
    public void submit(View view) {
    kinveyClient.userDiscovery().lookupByUserName(username, new KinveyUserListCallback () {
        public void onFailure(Throwable t) { ... }
        public void onSuccess(User[] u) { ... }
    });
}
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

    @Override
    public UserLookup userLookup() {
        return super.userLookup();    //To change body of overridden methods use NetworkFileManager | Settings | NetworkFileManager Templates.
    }

    /**
     * Asynchronous user lookup by first and last name
     * <p>
     * Constructs an asynchronous request to find a user by first and last name, and returns the associated User object
     * via a KinveyUserListCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     *  {@code
        kinveyClient.userDiscovery().lookupByFullName("John","Smith", new KinveyUserListCallback() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(User[] u) { ... }
        });
     *  </pre>
     * }
     *
     * @param firstname a {@link java.lang.String} object.
     * @param lastname a {@link java.lang.String} object.
     * @param callback a {@link com.kinvey.android.callback.KinveyUserListCallback} object.
     */
    public void lookupByFullName(String firstname, String lastname, KinveyUserListCallback callback) {
        Preconditions.checkNotNull(firstname, "firstname must not be null.");
        Preconditions.checkNotNull(lastname, "lastname must not be null.");
        UserLookup userCollectionLookup = userLookup();
        userCollectionLookup.setFirstName(firstname);
        userCollectionLookup.setLastName(lastname);
        lookup(userCollectionLookup, callback);
    }



    /**
     * Asynchronous user lookup by username
     * <p>
     * Constructs an asynchronous request to find a user by username, and returns the associated User object
     * via a KinveyUserListCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
        kinveyClient.userDiscovery().lookupByFullName("jsmith", new KinveyUserListCallback() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(User[] u) { ... }
        });
    }
     * </pre>
     *
     * @param username a {@link java.lang.String} object.
     * @param callback a {@link com.kinvey.android.callback.KinveyUserListCallback} object.
     */
    public void lookupByUserName(String username, KinveyUserListCallback callback) {
        Preconditions.checkNotNull(username, "username must not be null.");
        UserLookup userCollectionLookup = userLookup();
        userCollectionLookup.setUsername(username);
        lookup(userCollectionLookup, callback);
    }

     /**
     * Asynchronous user lookup by Facebook ID
     * <p>
     * Constructs an asynchronous request to find a user by facebook ID, and returns the associated User object
     * via a KinveyUserListCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
      {@code
        kinveyClient.userDiscovery().lookupByFacebookID("jsmith", new KinveyUserListCallback() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(User[] u) { ... }
        });
      }
     * </pre>
      *
     * @param facebookID a {@link java.lang.String} object.
     * @param callback a {@link com.kinvey.android.callback.KinveyUserListCallback} object.
     */
    public void lookupByFacebookID(String facebookID, KinveyUserListCallback callback) {
        Preconditions.checkNotNull(facebookID, "facebookID must not be null.");
        UserLookup userCollectionLookup = userLookup();
        userCollectionLookup.setFacebookID(facebookID);
        lookup(userCollectionLookup, callback);
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
     * {@code
        UserLookup lookup = kinveyClient.userDiscovery().userLookup();
        lookup.put("age",21);
        kinveyClient.userDiscovery().lookup(lookup, new KinveyUserListCallback() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(User[] u) { ... }
        });
    }
     * </pre>
     *
     * @param userlookup a UserLookup object.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     */
    public void lookup(UserLookup userlookup, KinveyUserListCallback callback) {

        Preconditions.checkNotNull(userlookup, "userlookup must not be null.");
        new Lookup(userlookup, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);

    }

    private class Lookup extends AsyncClientRequest<User[]> {

        private final UserLookup userLookup;

        private Lookup(UserLookup lookup, KinveyUserListCallback callback) {
            super(callback);
            this.userLookup = lookup;
        }

        protected User[] executeAsync() throws IOException {
            return AsyncUserDiscovery.this.lookupBlocking(userLookup).execute();
        }
    }
}

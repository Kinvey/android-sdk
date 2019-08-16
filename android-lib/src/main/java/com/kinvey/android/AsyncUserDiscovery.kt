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

import java.io.IOException

import com.google.common.base.Preconditions
import com.kinvey.android.callback.KinveyUserListCallback
import com.kinvey.android.model.User
import com.kinvey.java.AbstractClient
import com.kinvey.java.UserDiscovery
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.model.UserLookup

/**
 * Wraps the [com.kinvey.java.UserDiscovery] public methods in asynchronous functionality using native Android AsyncTask.
 *
 *
 *
 * This functionality can be accessed through the [Client.userDiscovery] convenience method.
 *
 *
 *
 *
 * This API is used to search for users based on select criteria.  Methods in this API return results via a
 * [com.kinvey.android.callback.KinveyUserListCallback].
 *
 *
 *
 *
 * Sample Usage:
 * <pre>
 * `public void submit(View view) {
 * kinveyClient.userDiscovery().lookupByUserName(username, new KinveyUserListCallback () {
 * public void onFailure(Throwable t) { ... }
 * public void onSuccess(User[] u) { ... }
 * });
 * }
 * </pre>
 *
 * </p>
 *
 * mjsalinger
 * 2.0`
</pre> */
class AsyncUserDiscovery internal constructor(client: AbstractClient<*>, initializer: KinveyClientRequestInitializer) : UserDiscovery<BaseUser>(client, initializer) {

    override fun userLookup(): UserLookup {
        return super.userLookup()    //To change body of overridden methods use NetworkFileManager | Settings | NetworkFileManager Templates.
    }

    /**
     * Asynchronous user lookup by first and last name
     *
     *
     * Constructs an asynchronous request to find a user by first and last name, and returns the associated User object
     * via a KinveyUserListCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userDiscovery().lookupByFullName("John","Smith", new KinveyUserListCallback() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(User[] u) { ... }
     * });
     * </pre>
    ` *
     *
     * @param firstname a [java.lang.String] object.
     * @param lastname a [java.lang.String] object.
     * @param callback a [com.kinvey.android.callback.KinveyUserListCallback] object.
    </pre> */
    fun lookupByFullName(firstname: String, lastname: String, callback: KinveyUserListCallback) {
        Preconditions.checkNotNull(firstname, "firstname must not be null.")
        Preconditions.checkNotNull(lastname, "lastname must not be null.")
        val userCollectionLookup = userLookup()
        userCollectionLookup.firstName = firstname
        userCollectionLookup.lastName = lastname
        lookup(userCollectionLookup, callback)
    }


    /**
     * Asynchronous user lookup by username
     *
     *
     * Constructs an asynchronous request to find a user by username, and returns the associated User object
     * via a KinveyUserListCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userDiscovery().lookupByFullName("jsmith", new KinveyUserListCallback() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(User[] u) { ... }
     * });
    ` *
    </pre> *
     *
     * @param username a [java.lang.String] object.
     * @param callback a [com.kinvey.android.callback.KinveyUserListCallback] object.
     */
    fun lookupByUserName(username: String, callback: KinveyUserListCallback) {
        Preconditions.checkNotNull(username, "username must not be null.")
        val userCollectionLookup = userLookup()
        userCollectionLookup.username = username
        lookup(userCollectionLookup, callback)
    }

    /**
     * Asynchronous user lookup by Facebook ID
     *
     *
     * Constructs an asynchronous request to find a user by facebook ID, and returns the associated User object
     * via a KinveyUserListCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userDiscovery().lookupByFacebookID("jsmith", new KinveyUserListCallback() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(User[] u) { ... }
     * });
    ` *
    </pre> *
     *
     * @param facebookID a [java.lang.String] object.
     * @param callback a [com.kinvey.android.callback.KinveyUserListCallback] object.
     */
    fun lookupByFacebookID(facebookID: String, callback: KinveyUserListCallback) {
        Preconditions.checkNotNull(facebookID, "facebookID must not be null.")
        val userCollectionLookup = userLookup()
        userCollectionLookup.facebookID = facebookID
        lookup(userCollectionLookup, callback)
    }


    /**
     * Asynchronous user lookup method
     *
     *
     * Constructs an asynchronous request to find a user, and returns the associated User object
     * via a KinveyClientCallback.   Requests are constructed with a [com.google.api.client.json.GenericJson]
     * [UserLookup] object, which can be instantiated via the [com.kinvey.java.UserDiscovery.userLookup]
     * factory method.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `UserLookup lookup = kinveyClient.userDiscovery().userLookup();
     * lookup.put("age",21);
     * kinveyClient.userDiscovery().lookup(lookup, new KinveyUserListCallback() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(User[] u) { ... }
     * });
    ` *
    </pre> *
     *
     * @param userlookup a UserLookup object.
     * @param callback a [com.kinvey.java.core.KinveyClientCallback] object.
     */
    fun lookup(userlookup: UserLookup, callback: KinveyUserListCallback) {

        Preconditions.checkNotNull(userlookup, "userlookup must not be null.")
        Lookup(userlookup, callback).execute()

    }

    private inner class Lookup internal constructor(private val userLookup: UserLookup, callback: KinveyUserListCallback) : AsyncClientRequest<Array<User>>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): Array<User>? {
            return this@AsyncUserDiscovery.lookupArrayBlocking(userLookup, Array<User>::class.java).execute()
        }
    }
}

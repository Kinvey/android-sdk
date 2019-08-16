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


import com.google.common.base.Preconditions

import java.io.IOException

import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.model.UserLookup

/**
 *
 * Get access to immutable user data for the app.
 *
 *
 * This class is not thread-safe.
 *
 * @author edwardf
 * @since 2.0
 */
open class UserDiscovery<T : BaseUser>
/**
 *
 * Constructor to access Kinvey's UserDiscovery management.
 *
 * @param client - an instance of Kinvey AbstractClient, configured for the application
 * @param initializer
 */
protected constructor(private val client: AbstractClient<*>, private val requestInitializer: KinveyClientRequestInitializer) {

    /**
     * Factory method to return a new UserLookup object
     *
     * @return
     */
    open fun userLookup(): UserLookup {
        return UserLookup()
    }

    init {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkNotNull(requestInitializer, "initializer must not be null.")
    }

    //some convenience wrappers

    @Throws(IOException::class)
    fun lookupByFullNameBlocking(firstname: String, lastname: String, myClass: Class<T>): Lookup<T> {

        Preconditions.checkNotNull(firstname, "firstname must not be null.")
        Preconditions.checkNotNull(lastname, "lastname must not be null.")
        val lookup = UserLookup()
        lookup.firstName = firstname
        lookup.lastName = lastname
        return lookupBlocking(lookup, myClass)
    }

    @Throws(IOException::class)
    fun lookupByUserNameBlocking(username: String, myClass: Class<T>): Lookup<T> {
        Preconditions.checkNotNull(username, "username must not be null.")
        val lookup = UserLookup()
        lookup.username = username
        return lookupBlocking(lookup, myClass)
    }

    @Throws(IOException::class)
    fun lookupByFacebookIDBlocking(facebookID: String, myClass: Class<T>): Lookup<T> {
        Preconditions.checkNotNull(facebookID, "facebookID must not be null.")
        val lookup = UserLookup()
        lookup.facebookID = facebookID
        return lookupBlocking(lookup, myClass)
    }

    @Throws(IOException::class)
    fun lookupBlocking(userlookup: UserLookup, myClass: Class<T>): Lookup<T> {

        Preconditions.checkNotNull(userlookup, "userlookup must not be null.")
        val lookup = Lookup(userlookup, myClass)
        client.initializeRequest(lookup)
        return lookup

    }

    @Throws(IOException::class)
    fun <T1: T> lookupArrayBlocking(userlookup: UserLookup, myClass: Class<Array<T1>>): Lookup<Array<T1>> {

        Preconditions.checkNotNull(userlookup, "userlookup must not be null.")
        val lookup = Lookup(userlookup, myClass)
        client.initializeRequest(lookup)
        return lookup

    }

    companion object {
        private val REST_PATH = "user/{appKey}/_lookup"
    }

    inner class Lookup<T> internal constructor(lookup: UserLookup, myClass: Class<T>) : AbstractKinveyJsonClientRequest<T>(client, "POST", REST_PATH, lookup, myClass)

}

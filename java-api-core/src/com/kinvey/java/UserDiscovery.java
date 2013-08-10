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


import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.lang.reflect.Array;

import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.model.UserLookup;

/**
 * <p>Get access to immutable user data for the app.</p>
 *
 * <p>This class is not thread-safe.</p>
 *
 * @author edwardf
 * @since 2.0
 */
public class UserDiscovery {

    private AbstractClient client;
    private KinveyClientRequestInitializer requestInitializer;

    /**
     * Factory method to return a new UserLookup object
     *
     * @return
     */
    public UserLookup userLookup() {
        return new UserLookup();
    }

    /**
     *
     * Constructor to access Kinvey's UserDiscovery management.
     *
     * @param client - an instance of Kinvey AbstractClient, configured for the application
     * @param initializer
     */
    protected UserDiscovery(AbstractClient client, KinveyClientRequestInitializer initializer) {
        Preconditions.checkNotNull(client, "client must not be null.");
        Preconditions.checkNotNull(initializer, "initializer must not be null.");
        this.client = client;
        this.requestInitializer = initializer;
    }

    //some convenience wrappers

    public Lookup lookupByFullNameBlocking(String firstname, String lastname) throws IOException{

        Preconditions.checkNotNull(firstname, "firstname must not be null.");
        Preconditions.checkNotNull(lastname, "lastname must not be null.");
        UserLookup lookup = new UserLookup();
        lookup.setFirstName(firstname);
        lookup.setLastName(lastname);
        return lookupBlocking(lookup);
    }

    public Lookup lookupByUserNameBlocking(String username) throws IOException{
        Preconditions.checkNotNull(username, "username must not be null.");
        UserLookup lookup = new UserLookup();
        lookup.setUsername(username);
        return lookupBlocking(lookup);
    }

    public Lookup lookupByFacebookIDBlocking(String facebookID) throws IOException{
        Preconditions.checkNotNull(facebookID, "facebookID must not be null.");
        UserLookup lookup = new UserLookup();
        lookup.setFacebookID(facebookID);
        return lookupBlocking(lookup);
    }

    public Lookup lookupBlocking(UserLookup userlookup) throws IOException{

        Preconditions.checkNotNull(userlookup, "userlookup must not be null.");
        Lookup lookup = new Lookup(userlookup, Array.newInstance(User.class,0).getClass());
        client.initializeRequest(lookup);
        return lookup;

    }

    public class Lookup extends AbstractKinveyJsonClientRequest<User[]> {
        private static final String REST_PATH = "user/{appKey}/_lookup";

        Lookup(UserLookup lookup, Class myClass) {
            super(client, "POST", REST_PATH, lookup, myClass);
        }
    }
}

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
package com.kinvey.java;


import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

import java.io.IOException;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;

/**
 * <p>Get access to immutable user data for the app.</p>
 *
 * <p>This class is not thread-safe.</p>
 *
 * @author edwardf
 * @since 2.0
 */
public class UserDiscovery {

    /**
     * Construct a user lookup object via {@link com.kinvey.java.UserDiscovery#userLookup()}.
     *
     * <p>After configuring the lookup set it using {@link UserDiscovery#lookup(com.kinvey.java.UserDiscovery.UserLookup)}</p>
     */
    public class UserLookup extends GenericJson{

        @Key("_id")
        private String id;
        @Key
        private String username;
        @Key("first_name")
        private String firstName;
        @Key("last_name")
        private String lastName;
        @Key
        private String email;
        @Key("_socialIdentity.facebook.id")
        private String facebookID;

        private UserLookup(){}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFacebookID() {
            return facebookID;
        }

        public void setFacebookID(String facebookID) {
            this.facebookID = facebookID;
        }
    }




    private AbstractClient client;
    private KinveyClientRequestInitializer requestInitializer;

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

    public Lookup lookupByFullName(String firstname, String lastname) throws IOException{

        Preconditions.checkNotNull(firstname, "firstname must not be null.");
        Preconditions.checkNotNull(lastname, "lastname must not be null.");
        UserLookup lookup = new UserLookup();
        lookup.setFirstName(firstname);
        lookup.setLastName(lastname);
        return lookup(lookup);
    }

    public Lookup lookupByUserName(String username) throws IOException{
        Preconditions.checkNotNull(username, "username must not be null.");
        UserLookup lookup = new UserLookup();
        lookup.setUsername(username);
        return lookup(lookup);
    }

    public Lookup lookupByFacebookID(String facebookID) throws IOException{
        Preconditions.checkNotNull(facebookID, "facebookID must not be null.");
        UserLookup lookup = new UserLookup();
        lookup.setFacebookID(facebookID);
        return lookup(lookup);
    }


    public Lookup lookup(UserLookup userlookup) throws IOException{

        Preconditions.checkNotNull(userlookup, "userlookup must not be null.");
        Lookup lookup = new Lookup(userlookup);
        client.initializeRequest(lookup);
        return lookup;

    }

    public class Lookup extends AbstractKinveyJsonClientRequest<UserLookup> {
        private static final String REST_PATH = "user/{appKey}/_lookup";

        Lookup(UserLookup lookup) {
            super(client, "POST", REST_PATH, lookup, UserLookup.class);
        }
    }
}

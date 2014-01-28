/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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
package com.kinvey.java.model;

/**
 * @author mjsalinger
 * @since 2.0
 */

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Construct a user lookup object via {@link com.kinvey.java.UserDiscovery#userLookup()}.
 *
 * <p>After configuring the lookup set it using {@link com.kinvey.java.UserDiscovery#lookupBlocking(com.kinvey.java.model.UserLookup)}</p>
 */
public class UserLookup extends GenericJson {

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

    public UserLookup(){}

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

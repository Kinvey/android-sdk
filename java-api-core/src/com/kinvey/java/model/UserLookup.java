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

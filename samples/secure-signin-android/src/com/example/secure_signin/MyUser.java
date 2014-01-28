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
package com.example.secure_signin;

import com.google.api.client.util.Key;
import com.kinvey.android.AbstractAsyncUser;
import com.kinvey.android.AsyncUser;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.auth.KinveyAuthRequest;

/**
 * @author edwardf
 */
public class MyUser extends AbstractAsyncUser {


    @Key
    private String dateOfBirth;


    /**
     * Base constructor requires the client instance and a {@link com.kinvey.java.auth.KinveyAuthRequest.Builder} to be passed in.
     * <p>
     * {@link com.kinvey.java.core.AbstractKinveyClient#initializeRequest(com.kinvey.java.core.AbstractKinveyClientRequest)} is used to initialize all
     * requests constructed by this api.
     * </p>
     *
     * @param client instance of current client
     * @throws NullPointerException if the client parameter and KinveyAuthRequest.Builder is non-null
     */
    public MyUser(AbstractClient client, KinveyAuthRequest.Builder builder) {
        super(client, MyUser.class, builder);
    }
}

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
package com.kinvey.nativejava;

import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.auth.KinveyAuthRequest;

/** {@inheritDoc}
 *
 * @author edwardf
 * */
public class User extends com.kinvey.java.User {


    protected User(AbstractClient client, KinveyAuthRequest.Builder builder) {
        super(client, User.class, builder);
    }

    public User(){}
}

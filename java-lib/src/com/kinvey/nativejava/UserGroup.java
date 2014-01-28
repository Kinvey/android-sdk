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

import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.KinveyClientRequestInitializer;

/** {@inheritDoc}
 *
 * @author edwardf
 * */
public class UserGroup extends com.kinvey.java.UserGroup {
    /**
     * Constructor to access Kinvey's UserGroup management.
     *
     * @param client      - an instance of Kinvey AbstractClient, configured for the application
     * @param initializer
     */
    protected UserGroup(AbstractClient client, KinveyClientRequestInitializer initializer) {
        super(client, initializer);
    }
}

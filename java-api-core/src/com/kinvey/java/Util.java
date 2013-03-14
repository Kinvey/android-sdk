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
import com.google.common.base.Preconditions;

import java.io.IOException;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class Util {
    private AbstractClient client;

    public Util(AbstractClient client) {
        Preconditions.checkNotNull(client, "client must not be null.");
        this.client = client;
    }

    /**
     * Gets current client for this Util
     * @return current client instance
     */
    protected AbstractClient getClient(){
        return this.client;
    }

    /**
     * Method to ping service.
     *
     * @return Ping object
     * @throws java.io.IOException
     */
    public Ping pingBlocking() throws IOException {
        Ping ping = new Ping();
        client.initializeRequest(ping);
        return ping;
    }

    /**
     * Method to ping service.
     *
     * @return Ping object
     * @throws java.io.IOException
     * @deprecated Renamed to {@link #pingBlocking()}
     */
    @Deprecated
    public Ping ping() throws IOException {
        Ping ping = new Ping();
        client.initializeRequest(ping);
        return ping;
    }

    public class Ping extends AbstractKinveyJsonClientRequest<GenericJson> {
        private static final String REST_PATH = "appdata/{appKey}";

        Ping() {
            super(client, "GET", REST_PATH, null, null);
        }
    }
}
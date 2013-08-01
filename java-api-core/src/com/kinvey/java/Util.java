/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
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
        ping.setRequireAppCredentials(true);
        return ping;
    }

    public class Ping extends AbstractKinveyJsonClientRequest<GenericJson> {
        private static final String REST_PATH = "appdata/{appKey}";

        Ping() {
            super(client, "GET", REST_PATH, null, null);
        }
    }
}
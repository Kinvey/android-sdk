/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.testing;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.json.MockJsonFactory;

import com.kinvey.java.File;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.core.KinveyRequestInitializer;

/**
* @author m0rganic
* @since 2.0
*/
public class MockKinveyJsonClient extends AbstractKinveyJsonClient {

    public MockKinveyJsonClient(HttpTransport transport, HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath, JsonObjectParser objectParser, KinveyRequestInitializer kinveyRequestInitializer) {
        super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, null);
    }

    @Override
    public File file() {
        return null;
    }

    public static class Builder extends AbstractKinveyJsonClient.Builder {

        public Builder() {
            super(new MockHttpTransport(), new MockJsonFactory(), HttpTesting.SIMPLE_URL, "", null, new SpyKinveyClientRequestInitializer());
        }

        @Override
        public MockKinveyJsonClient build() {
            return new MockKinveyJsonClient(getTransport(), getHttpRequestInitializer(), getBaseUrl(), getServicePath(), getObjectParser(), getKinveyClientRequestInitializer());
        }
    }
}

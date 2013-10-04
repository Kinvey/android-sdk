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

import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.kinvey.java.auth.ClientUsers;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.core.KinveyClientRequestInitializer;

/**
 * @author edwardf
 */
public class Client extends AbstractClient{


    /**
     * Private constructor.  Use AbstractClient.Builder to initialize the AbstractClient.
     *
     * @param transport                HttpTransport
     * @param httpRequestInitializer   HttpRequestInitializer
     * @param rootUrl                  Root URL of service
     * @param servicePath              path of Service
     * @param objectParser             JsonObjectParser
     * @param kinveyRequestInitializer KinveyRequestInitializer
     * @param requestPolicy            BackoffPolicy
     */
    protected Client(HttpTransport transport, HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath, JsonObjectParser objectParser, KinveyClientRequestInitializer kinveyRequestInitializer, CredentialStore store, BackOffPolicy requestPolicy) {
        super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, store, requestPolicy);
    }

    @Override
    public <T> AppData<T> appData(String collectionName, Class<T> myClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public File file() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserDiscovery userDiscovery() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserGroup userGroup() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected ClientUsers getClientUsers() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CustomEndpoints customEndpoints() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

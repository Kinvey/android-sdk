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
package com.kinvey.java.core;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.json.MockJsonFactory;
import com.kinvey.java.*;
import com.kinvey.java.auth.ClientUsers;
import junit.framework.TestCase;

import java.util.LinkedHashMap;

import com.kinvey.java.query.MongoQueryFilter;

/**
 * @author edwardf
 * @since 2.0
 */
public abstract class KinveyMockUnitTest extends TestCase {

    public MockTestClient mockClient;


    public KinveyClientRequestInitializer getKinveyRequestInitializer(){

        return (KinveyClientRequestInitializer) mockClient.getKinveyRequestInitializer();
    }

    protected static class MockTestClient extends AbstractClient {

        MockTestClient(HttpTransport transport, HttpRequestInitializer httpRequestInitializer,
                       String rootUrl, String servicePath, JsonObjectParser objectParser,
                       KinveyClientRequestInitializer kinveyRequestInitializer) {
            super(transport, null, "https://baas.kinvey.com/", "" , objectParser, kinveyRequestInitializer,null,null);
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
        public CustomEndpoints customEndpoints(){
            return null;
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
        public ClientUsers getClientUsers() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public static final class Builder extends AbstractClient.Builder {

            public Builder(HttpTransport transport, JsonFactory jsonFactory,
                           HttpRequestInitializer httpRequestInitializer,
                           KinveyClientRequestInitializer clientRequestInitializer) {
                super(transport, jsonFactory, null , null);
            }

            @Override
            public MockTestClient build() {
                return new MockTestClient(getTransport(), getHttpRequestInitializer(), getBaseUrl(), getServicePath(),
                        getObjectParser(), new MockKinveyClientRequestInitializer());
            }
        }
    }

    @Override
    protected void setUp() {
        HttpTransport transport = new MockHttpTransport();
        JsonFactory factory = new MockJsonFactory();

        MockTestClient.Builder builder = new MockTestClient.Builder(transport,factory,
                null, new MockKinveyClientRequestInitializer());

        mockClient = builder.build();
    }

    static class MockKinveyClientRequestInitializer extends KinveyClientRequestInitializer {

        boolean isCalled;

        MockKinveyClientRequestInitializer() {
            super("appkey", "appsecret", new KinveyHeaders());
        }

        @Override
        public void initialize(AbstractKinveyClientRequest<?> request) {
            isCalled = true;
        }
    }

    public static class MockQuery extends Query {

        public MockQuery(MockQueryFilter.MockBuilder builder) {
            super(null);
        }

        @Override
        public LinkedHashMap<String, Object> getQueryFilterMap() {
            LinkedHashMap<String,Object> filter = new LinkedHashMap<String,Object>();
            LinkedHashMap<String,Object> innerFilter = new LinkedHashMap<String, Object>();
            filter.put("city", "boston");
            innerFilter.put("$gt", 18);
            innerFilter.put("$lt", 21);
            filter.put("age", innerFilter);
            return filter;
        }

        @Override
        public String getQueryFilterJson(JsonFactory factory) {
            LinkedHashMap<String,Object> filter = new LinkedHashMap<String,Object>();
            LinkedHashMap<String,Object> innerFilter = new LinkedHashMap<String, Object>();
            filter.put("city","boston");
            innerFilter.put("$gt",18);
            innerFilter.put("$lt",21);
            filter.put("age",innerFilter);
            return filter.toString();
        }
    }

    public static class MockQueryFilter extends MongoQueryFilter {
        public static class MockBuilder extends MongoQueryFilterBuilder {
            public MockBuilder() {
                super();
            }

        }
    }

}

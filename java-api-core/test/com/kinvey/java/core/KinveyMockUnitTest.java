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
package com.kinvey.java.core;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.testing.http.MockHttpTransport;

import com.google.api.client.testing.json.MockJsonFactory;
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
        public void performLockDown() {
            //To change body of implemented methods use File | Settings | File Templates.
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

        @Override
        public <I extends GenericJson, O extends GenericJson> CustomEndpoints<I, O> customEndpoints(Class<O> myClass) {
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

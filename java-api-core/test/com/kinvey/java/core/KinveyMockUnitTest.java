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
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.json.MockJsonFactory;
import com.google.common.base.Preconditions;
import com.kinvey.java.*;
import com.kinvey.java.auth.ClientUsers;

import junit.framework.TestCase;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.kinvey.java.query.MongoQueryFilter;

/**
 * @author edwardf
 * @since 2.0
 */
public abstract class KinveyMockUnitTest extends TestCase {

    private MockTestClient mockClient;
        
    public MockTestClient getClient(){
    	if (mockClient == null){
    		mockClient = new MockTestClient.Builder(new MockHttpTransport(),new MockJsonFactory(), null, new MockKinveyClientRequestInitializer()).build();
    	}
    	return mockClient;
    }
    
    public MockTestClient getClient(HttpTransport transport){
    	return new MockTestClient.Builder(transport, new GsonFactory(), null, new MockKinveyClientRequestInitializer()).build();
    }



    public KinveyClientRequestInitializer getKinveyRequestInitializer(){

        return (KinveyClientRequestInitializer) mockClient.getKinveyRequestInitializer();
    }

    protected static class MockTestClient extends AbstractClient {

        private ConcurrentHashMap<String, AppData> appDataInstanceCache;


        MockTestClient(HttpTransport transport, HttpRequestInitializer httpRequestInitializer,
                       String rootUrl, String servicePath, JsonObjectParser objectParser,
                       KinveyClientRequestInitializer kinveyRequestInitializer) {
            super(transport, null, "https://baas.kinvey.com/", "" , objectParser, kinveyRequestInitializer,null,null);
        }

        @Override
        public <T> AppData<T> appData(String collectionName, Class<T> myClass) {
            synchronized (lock) {
                Preconditions.checkNotNull(collectionName, "collectionName must not be null");
                if (appDataInstanceCache == null) {
                    appDataInstanceCache = new ConcurrentHashMap<String, AppData>();
                }
                if (!appDataInstanceCache.containsKey(collectionName)) {
                    appDataInstanceCache.put(collectionName, new MockAppData(collectionName, myClass, this));
                }
                if(appDataInstanceCache.containsKey(collectionName) && !appDataInstanceCache.get(collectionName).getCurrentClass().equals(myClass)){
                    appDataInstanceCache.put(collectionName, new MockAppData(collectionName, myClass, this));
                }

                return appDataInstanceCache.get(collectionName);
            }        }

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
            return new ClientUsers() {
				
				@Override
				public void switchUser(String userID) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void setCurrentUser(String userID) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void removeUser(String userID) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public String getCurrentUserType() {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public String getCurrentUser() {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public void addUser(String userID, String type) {
					// TODO Auto-generated method stub
					
				}
			};
        }

        @Override
        public <I, O> CustomEndpoints<I, O> customEndpoints(Class<O> myClass) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public static final class Builder extends AbstractClient.Builder {

            public Builder(HttpTransport transport, JsonFactory jsonFactory,
                           HttpRequestInitializer httpRequestInitializer,
                           KinveyClientRequestInitializer clientRequestInitializer) {
                super(transport, null , null);
                this.setJsonFactory(jsonFactory);
            }

            @Override
            public MockTestClient build() {
                return new MockTestClient(getTransport(), getHttpRequestInitializer(), getBaseUrl(), getServicePath(),
                        getObjectParser(), new MockKinveyClientRequestInitializer());
            }
        }
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

    public static class MockAppData extends AppData{
        /**
         * Constructor to instantiate the AppData class.
         *
         * @param collectionName Name of the appData collection
         * @param myClass        Class Type to marshall data between.
         * @param client
         */
        protected MockAppData(String collectionName, Class myClass, AbstractClient client) {
            super(collectionName, myClass, client);
        }
    }

}

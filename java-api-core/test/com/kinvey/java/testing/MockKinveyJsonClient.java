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
package com.kinvey.java.testing;

import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.json.MockJsonFactory;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.network.NetworkStore;
import com.kinvey.java.CustomEndpoints;
import com.kinvey.java.network.File;
import com.kinvey.java.UserDiscovery;
import com.kinvey.java.UserGroup;
import com.kinvey.java.auth.ClientUsers;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.core.KinveyClientRequestInitializer;

/**
* @author m0rganic
* @since 2.0
*/
public class MockKinveyJsonClient extends AbstractClient {



    
    protected MockKinveyJsonClient(HttpTransport transport, HttpRequestInitializer httpRequestInitializer, String rootUrl,
            String servicePath, JsonObjectParser objectParser,
            KinveyClientRequestInitializer kinveyRequestInitializer, CredentialStore store,
            BackOffPolicy requestPolicy) {
super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, store,
       requestPolicy);
}
    @Override
    public File file() {
        return null;
    }

    @Override
    public void performLockDown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static class Builder extends AbstractKinveyJsonClient.Builder {

        public Builder() {
            super(new MockHttpTransport(), "https://www.google.com", "", null, new SpyKinveyClientRequestInitializer());
            this.setJsonFactory(new MockJsonFactory());
        }

        @Override
        public MockKinveyJsonClient build() {
            return new MockKinveyJsonClient(getTransport(), getHttpRequestInitializer(), getBaseUrl(), getServicePath(), getObjectParser(), getKinveyClientRequestInitializer(), null, null);
        }
    }

	@Override
	public <T> NetworkStore<T> appData(String collectionName, Class<T> myClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserDiscovery userDiscovery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserGroup userGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ClientUsers getClientUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <I, O> CustomEndpoints<I, O> customEndpoints(Class<O> myClass) {
		// TODO Auto-generated method stub
		return null;
	}
}

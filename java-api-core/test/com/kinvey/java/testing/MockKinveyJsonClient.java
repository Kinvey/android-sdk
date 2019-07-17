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
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.json.MockJsonFactory;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.CustomEndpoints;
import com.kinvey.java.UserDiscovery;
import com.kinvey.java.UserGroup;
import com.kinvey.java.auth.ClientUser;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.BaseUser;

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
    public void performLockDown() {
        //To change body of implemented methods use NetworkFileManager | Settings | NetworkFileManager Templates.
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
	public UserDiscovery userDiscovery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserGroup userGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	public ClientUser getClientUser() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public void setActiveUser(BaseUser user) {

    }

    @Override
    public BaseUser getActiveUser() {
        return null;
    }

    @Override
    public CustomEndpoints customEndpoints(Class myClass) {
        return null;
    }

/*    @Override
    public <I extends GenericJson, O> CustomEndpoints<I, O> customEndpoints(Class<O> myClass) {
        return null;
    }*/

    @Override
    public ICacheManager getCacheManager() {
        return null;
    }

    @Override
    public ICacheManager getUserCacheManager() {
        return null;
    }

    @Override
    public String getFileCacheFolder() {
        return null;
    }

    @Override
    protected ICacheManager getSyncCacheManager() {
        return null;
    }

    @Override
    public String getDeviceId() {
        return null;
    }
}

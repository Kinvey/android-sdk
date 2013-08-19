/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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

import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.json.MockJsonFactory;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.Map;

import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.auth.KinveyAuthResponse;
import com.kinvey.java.auth.ThirdPartyIdentity;

/**
* @author m0rganic
* @since 2.0
*/
public class MockKinveyAuthRequest extends KinveyAuthRequest {

    public MockKinveyAuthRequest(HttpTransport transport, JsonFactory jsonFactory,
                                 BasicAuthentication appKeyAuthentication, String username, String password,
                                 Map<String, Object> metaData, boolean create) {
        super(new MockHttpTransport(), new MockJsonFactory(), null, appKeyAuthentication, username, password,
                null, create);
    }

    public MockKinveyAuthRequest(HttpTransport transport, JsonFactory jsonFactory,
                                 BasicAuthentication appKeyAuthentication, ThirdPartyIdentity thirdPartyIdentity,
                                 Map<String, Object> metaData) {
        super(new MockHttpTransport(), new MockJsonFactory(), null, appKeyAuthentication, thirdPartyIdentity, null, true);
    }

    @Override
    public KinveyAuthResponse execute() throws IOException {
        MockKinveyAuthResponse mockResponse =  new MockKinveyAuthResponse();
        mockResponse.metadata = new MockKinveyAuthResponse.KinveyUserMetadata();
        mockResponse.metadata.put("authKey","mockAuthKey");
        mockResponse.userId = "mockUserId";
        mockResponse.metadata.putAll(getUnknownKeys());
        return mockResponse;
    }

    public static class MockBuilder extends Builder {
        public MockBuilder(HttpTransport transport, JsonFactory jsonFactory, String appKey, String appSecret, GenericJson user) {
            super(transport, jsonFactory, null, appKey, appSecret, null);
        }

        public MockBuilder(HttpTransport transport, JsonFactory jsonFactory, String appKey, String appSecret,
                       String username, String password, GenericJson user) {
            super(transport, jsonFactory, null, appKey, appSecret, username, password, null);
        }

        public MockBuilder(HttpTransport transport, JsonFactory jsonFactory, String appKey, String appSecret,
                       ThirdPartyIdentity identity, GenericJson user) {
            super(transport, jsonFactory, null, appKey, appSecret, identity);
        }

        @Override
        public MockKinveyAuthRequest build() {
            if (!getThirdPartyAuthStatus()) {
                return new MockKinveyAuthRequest(getTransport()
                        , getJsonFactory()
                        , getAppKeyAuthentication()
                        , getUsername()
                        , getPassword(),null, getCreate());
            }
            return new MockKinveyAuthRequest(getTransport()
                    , getJsonFactory()
                    , getAppKeyAuthentication()
                    , getThirdPartyIdentity(),null);
        }
    }

    public static class MockKinveyAuthResponse extends KinveyAuthResponse {
        @Key("_id")
        public String userId;
        public KinveyUserMetadata metadata;

        @Override
        public KinveyUserMetadata getMetadata() {
            return metadata;
        }

        @Override
        public String getUserId() {
            return userId;
        }
    }
}

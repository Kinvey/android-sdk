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
package com.kinvey.java.testing;

import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.json.MockJsonFactory;
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
        super(new MockHttpTransport(), new MockJsonFactory(), appKeyAuthentication, username, password,
                null, create);
    }

    public MockKinveyAuthRequest(HttpTransport transport, JsonFactory jsonFactory,
                                 BasicAuthentication appKeyAuthentication, ThirdPartyIdentity thirdPartyIdentity,
                                 Map<String, Object> metaData) {
        super(new MockHttpTransport(), new MockJsonFactory(), appKeyAuthentication, thirdPartyIdentity, null, true);
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
            super(transport, jsonFactory, appKey, appSecret, null);
        }

        public MockBuilder(HttpTransport transport, JsonFactory jsonFactory, String appKey, String appSecret,
                       String username, String password, GenericJson user) {
            super(transport, jsonFactory, appKey, appSecret, username, password, null);
        }

        public MockBuilder(HttpTransport transport, JsonFactory jsonFactory, String appKey, String appSecret,
                       ThirdPartyIdentity identity, GenericJson user) {
            super(transport, jsonFactory, appKey, appSecret, identity);
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

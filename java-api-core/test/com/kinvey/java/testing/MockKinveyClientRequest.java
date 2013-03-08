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

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequestFactory;

import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.MediaHttpUploader;

import static org.mockito.Mockito.mock;

/**
 * @author m0rganic
 * @since 2.0
 */
public class MockKinveyClientRequest<T> extends AbstractKinveyClientRequest<T> {
    public MockKinveyClientRequest(String requestMethod, String uriTemplate, HttpContent httpContent, Class<T> responseClass) {
        super(new MockKinveyJsonClient.Builder().build(), requestMethod, uriTemplate, httpContent, responseClass);
    }

    private MediaHttpUploader mockUploader;

    @Override
    protected MediaHttpUploader createMediaHttpUploader(AbstractInputStreamContent content, HttpRequestFactory requestFactory) {
        mockUploader = mock(MediaHttpUploader.class);
        return mockUploader;
    }

    public MediaHttpUploader getMockMediaUploader() {
        return mockUploader;
    }
}

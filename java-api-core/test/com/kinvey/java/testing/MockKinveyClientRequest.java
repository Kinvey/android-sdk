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

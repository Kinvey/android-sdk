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

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.testing.http.HttpTesting;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import com.kinvey.java.testing.MockKinveyClientRequest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author m0rganic
 * @since 2.0
 */
@RunWith(MockitoJUnitRunner.class)
public class KinveyClientRequestTest extends TestCase {

    @Test
    public void shouldExecuteSuccessfully() {
        MockKinveyClientRequest<Void> mockClient = new MockKinveyClientRequest<Void>("GET", HttpTesting.SIMPLE_URL, null, Void.class);
        try {
            mockClient.execute();
        } catch (IOException e) {
            fail("KinveyClientRequest should not throw an exception on execute");
        }
    }

    @Test
    public void uploaderShouldBeCalled() {
        MockKinveyClientRequest<Void> mockClientRequest = new MockKinveyClientRequest<Void>("GET", HttpTesting.SIMPLE_URL, null, Void.class);

        try {
            mockClientRequest.initializeMediaHttpUploader(mock(AbstractInputStreamContent.class));
            mockClientRequest.executeUnparsed();

            MediaHttpUploader mockMediaUploader = mockClientRequest.getMockMediaUploader();
            verify(mockMediaUploader, times(1)).upload(any(AbstractKinveyClientRequest.class));
            verify(mockMediaUploader, times(1)).setDirectUploadEnabled(true);
        } catch (IOException e) {
            fail("KinveyClientRequest should not throw an exception on execute");
        }
    }

}

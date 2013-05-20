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
            verify(mockMediaUploader, times(1)).setDisableGZipContent(true);
            verify(mockMediaUploader, times(1)).setDirectUploadEnabled(true);
        } catch (IOException e) {
            fail("KinveyClientRequest should not throw an exception on execute");
        }
    }

}

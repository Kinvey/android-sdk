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
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.testing.http.MockHttpTransport;
import com.kinvey.java.File;
import com.kinvey.java.model.FileMetaData;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import com.kinvey.java.testing.HttpTesting;
import com.kinvey.java.testing.MockKinveyClientRequest;

import static org.mockito.Mockito.*;

/**
 * @author m0rganic
 * @since 2.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MediaHttpUploaderTest extends TestCase {

    @Test
    public void shouldNotifyProgressListenerInOrder() throws IOException {

        //setup and stub out the mocks
        AbstractInputStreamContent mockContent = mock(AbstractInputStreamContent.class);
        when(mockContent.getInputStream())
                .thenReturn(mock(InputStream.class));
        MediaHttpUploader objectUnderTest = spy(new MediaHttpUploader(mockContent, new MockHttpTransport(), null));
        FileMetaData mockUriResponse = mock(FileMetaData.class);
        when(mockUriResponse.getUploadUrl()).thenReturn(HttpTesting.SIMPLE_URL);
        doReturn(mockUriResponse)
                .when(objectUnderTest)
                .parse(any(JsonObjectParser.class), any(HttpResponse.class));

        // Record UploadState values passed to progress listener
        final Stack<MediaHttpUploader.UploadState> argValueRecorder = new Stack<MediaHttpUploader.UploadState>();
        UploaderProgressListener mockListener = spy(new UploaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpUploader uploader) throws IOException {
                argValueRecorder.push(uploader.getUploadState());
            }

            @Override
            public void metaDataUploaded(FileMetaData metaData) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onFailure(Throwable error) {
            }
        });
        MockKinveyClientRequest<Void> initiationClientRequest = new MockKinveyClientRequest<Void>("GET", HttpTesting.SIMPLE_URL, null, Void.class);

        // Run the simulation
        objectUnderTest.setProgressListener(mockListener);
        objectUnderTest.upload(initiationClientRequest);

        // Verify the values were what we expected
        verify(mockListener, times(4)).progressChanged(any(MediaHttpUploader.class));
        assertEquals(MediaHttpUploader.UploadState.UPLOAD_COMPLETE, argValueRecorder.pop());
        assertEquals(MediaHttpUploader.UploadState.INITIATION_COMPLETE, argValueRecorder.pop());
        assertEquals(MediaHttpUploader.UploadState.INITIATION_STARTED, argValueRecorder.pop());
        assertEquals(MediaHttpUploader.UploadState.UPLOAD_IN_PROGRESS, argValueRecorder.pop());

    }
}

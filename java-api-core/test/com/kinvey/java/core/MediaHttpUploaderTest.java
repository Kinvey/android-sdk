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
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.testing.http.MockHttpTransport;
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
        });
        MockKinveyClientRequest<Void> initiationClientRequest = new MockKinveyClientRequest<Void>("GET", HttpTesting.SIMPLE_URL, null, Void.class);

        // Run the simulation
        objectUnderTest.setProgressListener(mockListener);
        objectUnderTest.setFileMetaDataForUploading(new FileMetaData());
        objectUnderTest.upload(initiationClientRequest);

        // Verify the values were what we expected
        verify(mockListener, times(4)).progressChanged(any(MediaHttpUploader.class));
        assertEquals(MediaHttpUploader.UploadState.UPLOAD_COMPLETE, argValueRecorder.pop());
        assertEquals(MediaHttpUploader.UploadState.INITIATION_COMPLETE, argValueRecorder.pop());
        assertEquals(MediaHttpUploader.UploadState.INITIATION_STARTED, argValueRecorder.pop());
        assertEquals(MediaHttpUploader.UploadState.UPLOAD_IN_PROGRESS, argValueRecorder.pop());

    }
}

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
package com.kinvey.java.core;

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
import java.io.OutputStream;
import java.util.Stack;

import com.kinvey.java.testing.HttpTesting;
import com.kinvey.java.testing.MockKinveyClientRequest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author m0rganic
 * @since 2.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MediaHttpDownloaderTest extends TestCase {

    @Test
    public void shouldNotifyProgressListenerInOrder() throws IOException {

        //setup and stub out the mocks
        MediaHttpDownloader objectUnderTest = spy(new MediaHttpDownloader(new MockHttpTransport(), null));
        FileMetaData mockUriResponse = mock(FileMetaData.class);
        when(mockUriResponse.getDownloadURL()).thenReturn(HttpTesting.SIMPLE_URL);
        doReturn(mockUriResponse)
                .when(objectUnderTest)
                .parse(any(JsonObjectParser.class), any(HttpResponse.class));

        // Record UploadState values passed to progress listener
        final Stack<MediaHttpDownloader.DownloadState> argValueRecorder = new Stack<MediaHttpDownloader.DownloadState>();
        DownloaderProgressListener mockListener = spy(new DownloaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpDownloader downloader) throws IOException {
                argValueRecorder.push(downloader.getDownloadState());
            }

            public void onSuccess(Void result) {}

            public void onFailure(Throwable error) {
                fail(error.getMessage());
            }

        });
        MockKinveyClientRequest<Void> mockRequest = new MockKinveyClientRequest<Void>("GET", HttpTesting.SIMPLE_URL, null, Void.class);

        // Run the simulation
        objectUnderTest.setProgressListener(mockListener);
        objectUnderTest.download(mockRequest, mock(OutputStream.class));

        // Verify the values were what we expected
        verify(mockListener, times(4)).progressChanged(any(MediaHttpDownloader.class));
        assertEquals(MediaHttpDownloader.DownloadState.DOWNLOAD_COMPLETE, argValueRecorder.pop());
        assertEquals(MediaHttpDownloader.DownloadState.INITIATION_COMPLETE, argValueRecorder.pop());
        assertEquals(MediaHttpDownloader.DownloadState.INITIATION_STARTED, argValueRecorder.pop());
        assertEquals(MediaHttpDownloader.DownloadState.DOWNLOAD_IN_PROGRESS, argValueRecorder.pop());

    }
}

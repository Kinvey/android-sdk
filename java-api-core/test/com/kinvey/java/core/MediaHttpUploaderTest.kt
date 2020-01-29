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
package com.kinvey.java.core

import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.HttpResponse
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.client.testing.http.MockHttpTransport
import com.kinvey.java.core.MediaHttpUploader.UploadState
import com.kinvey.java.model.FileMetaData
import com.kinvey.java.testing.HttpTesting
import com.kinvey.java.testing.MockKinveyClientRequest
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * @author m0rganic
 * @since 2.0
 */
@RunWith(MockitoJUnitRunner::class)
class MediaHttpUploaderTest : TestCase() {
    @Test
    @Throws(IOException::class)
    fun shouldNotifyProgressListenerInOrder() {

        //setup and stub out the mocks
        val mockContent: AbstractInputStreamContent = mock(AbstractInputStreamContent::class.java)
        `when`(mockContent.inputStream)
                .thenReturn(mock(InputStream::class.java))
        val objectUnderTest: MediaHttpUploader = spy(MediaHttpUploader(mockContent, MockHttpTransport(), null))
        val mockUriResponse: FileMetaData = mock(FileMetaData::class.java)
        `when`(mockUriResponse.uploadUrl).thenReturn(HttpTesting.SIMPLE_URL)
        `when`(objectUnderTest.parse(any(JsonObjectParser::class.java), any(HttpResponse::class.java))).thenReturn(mockUriResponse)

        // Record UploadState values passed to progress listener
        val argValueRecorder = Stack<UploadState>()
        val mockListener: UploaderProgressListener = spy(object : UploaderProgressListener {
            @Throws(IOException::class)
            override fun progressChanged(uploader: MediaHttpUploader?) {
                argValueRecorder.push(uploader?.uploadState)
            }
        })
        val initiationClientRequest = MockKinveyClientRequest("GET", HttpTesting.SIMPLE_URL, null, Void::class.java)

        // Run the simulation
        objectUnderTest.setProgressListener(mockListener)
        objectUnderTest.fileMetaDataForUploading = FileMetaData()
        objectUnderTest.upload(initiationClientRequest)

        // Verify the values were what we expected
        verify(mockListener, times(4)).progressChanged(any(MediaHttpUploader::class.java))
        assertEquals(UploadState.UPLOAD_COMPLETE, argValueRecorder.pop())
        assertEquals(UploadState.INITIATION_COMPLETE, argValueRecorder.pop())
        assertEquals(UploadState.INITIATION_STARTED, argValueRecorder.pop())
        assertEquals(UploadState.UPLOAD_IN_PROGRESS, argValueRecorder.pop())
    }
}
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
package com.kinvey.java

import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.json.GenericJson
import com.kinvey.java.core.KinveyMockUnitTest
import com.kinvey.java.core.MediaHttpUploader
import com.kinvey.java.core.UploaderProgressListener
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.model.FileMetaData
import com.kinvey.java.network.NetworkFileManager
import com.kinvey.java.network.NetworkFileManager.UploadMetadataAndFile
import com.kinvey.java.testing.HttpTesting
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.reset
import org.mockito.runners.MockitoJUnitRunner
import java.io.IOException

/**
 * @author m0rganic
 * @since 2.0
 */
@RunWith(MockitoJUnitRunner::class)
class NetworkFileManagerTest : KinveyMockUnitTest<BaseUser>() {
    @Mock
    lateinit var mockContent: AbstractInputStreamContent
    private var networkFileManagerApiUnderTest: NetworkFileManager? = null
    @Before
    public override fun setUp() {
        reset(mockContent)
    }

    @Test
    @Throws(IOException::class)
    fun uploadUrlEndpointMatches() {
        val networkFileManagerApi: NetworkFileManager = MockNetworkFileManager(super.client)
        val meta = FileMetaData("testfilename.txt")
        val upload = networkFileManagerApi.prepUploadBlocking(meta, mockContent,
                object : UploaderProgressListener {
            @Throws(IOException::class)
            override fun progressChanged(uploader: MediaHttpUploader?) {
            }
        })
        val request = upload.buildHttpRequest()
        val expectedPath = HttpTesting.SIMPLE_URL + "/blob//testfilename.txt?tls=true"
        assertEquals(expectedPath, request?.url.toString())
    }

    @Test
    @Throws(IOException::class)
    fun downloadUrlEndpointMatches() {
        val networkFileManagerApi: NetworkFileManager = MockNetworkFileManager(super.client)
        val meta = FileMetaData("testfilename.txt")
        val download = networkFileManagerApi.prepDownloadBlocking(meta)
        val request = download.buildHttpRequest()
        val expectedPath = HttpTesting.SIMPLE_URL + "/blob//testfilename.txt?tls=true"
        assertEquals(expectedPath, request?.url.toString())
    }

    @Test
    fun testFileUploadInitializer() {
        networkFileManagerApiUnderTest = MockNetworkFileManager(super.client)
        try {
            networkFileManagerApiUnderTest?.prepUploadBlocking(FileMetaData("testfilename.txt"),
                    mockContent, object : UploaderProgressListener {
                @Throws(IOException::class)
                override fun progressChanged(uploader: MediaHttpUploader?) {
                }
            })
        } catch (e: IOException) {
            fail("file api should not be throw exception on upload")
        }
    }

    @Test
    @Throws(IOException::class)
    fun testFileDownloadWithTTL() {
        networkFileManagerApiUnderTest = MockNetworkFileManager(super.client)
        val download = networkFileManagerApiUnderTest?.prepDownloadWithTTLBlocking("testfilename.txt", 120)
        val req = download?.buildHttpRequest()
        val expectedPath = HttpTesting.SIMPLE_URL + "/blob//testfilename.txt"
        assertEquals(expectedPath, req?.url.toString())
    }

    @Test
    fun testFileDownloadInitializer() {
        networkFileManagerApiUnderTest = MockNetworkFileManager(super.client)
        try {
            networkFileManagerApiUnderTest?.prepDownloadBlocking(FileMetaData("testfilename.txt"))
        } catch (e: IOException) {
            fail("file api should not throw an exception on download")
        }
    }

    @Test
    fun downloadShouldThrowNpeOnNullFilename() {
        networkFileManagerApiUnderTest = MockNetworkFileManager(super.client)
        try {
            networkFileManagerApiUnderTest?.prepDownloadBlocking(FileMetaData(null))
            fail("file api should throw exception on null filename")
        } catch (e: IOException) {
            fail("file api should throw a NullPointerException on null filename")
        } catch (e: NullPointerException) {
            // expected
        }
    }

    @Test
    fun testFileDelete() {
        networkFileManagerApiUnderTest = MockNetworkFileManager(super.client)
        try {
            networkFileManagerApiUnderTest?.deleteBlocking(FileMetaData("testfilename.txt"))
        } catch (e: IOException) {
            fail("file api should not throw an exception on remove")
        }
    }

    @Throws(IOException::class)
    fun testFileCustomVersion() {
        val networkFileManagerApi: NetworkFileManager = MockNetworkFileManager(super.client)
        networkFileManagerApi.setClientAppVersion("1.2.3")
        val meta = FileMetaData("testfilename.txt")
        val request = networkFileManagerApi.prepDownloadBlocking(meta)
        val header = request.getRequestHeaders()["X-Kinvey-Client-App-Version"]
        assertEquals("1.2.3", header as String?)
    }

    @Throws(IOException::class)
    fun testFileCustomHeader() {
        val networkFileManagerApi: NetworkFileManager = MockNetworkFileManager(super.client)
        val custom = GenericJson()
        custom["First"] = 1
        custom["Second"] = "two"
        networkFileManagerApi.setCustomRequestProperties(custom)
        val meta = FileMetaData("testfilename.txt")
        val request = networkFileManagerApi.prepDownloadBlocking(meta)
        val header = request.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"]
        assertEquals("{\"First\":1,\"Second\":\"two\"}", header as String?)
    }

    @Throws(IOException::class)
    fun testFileCustomVersionNull() {
        val networkFileManagerApi: NetworkFileManager = MockNetworkFileManager(super.client)
        networkFileManagerApi.setClientAppVersion("")
        val meta = FileMetaData("testfilename.txt")
        val request = networkFileManagerApi.prepDownloadBlocking(meta)
        val header = request.getRequestHeaders()["X-Kinvey-Client-App-Version"]
        assertEquals(null, header)
    }

    @Throws(IOException::class)
    fun testFileCustomHeaderNull() {
        val networkFileManagerApi: NetworkFileManager = MockNetworkFileManager(super.client)
        networkFileManagerApi.setCustomRequestProperties(GenericJson())
        val meta = FileMetaData("testfilename.txt")
        val request = networkFileManagerApi.prepDownloadBlocking(meta)
        val header = request.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"]
        assertEquals(null, header)
    }

    private class MockNetworkFileManager
    /**
     * Base constructor requires the client instance to be passed in.
     *
     *
     * [com.kinvey.java.core.AbstractKinveyClient.initializeRequest] is used to initialize all requests
     * constructed by this api.
     *
     *
     * @param client required instance
     * @throws NullPointerException if the client parameter is non-null
     */(client: AbstractClient<*>?) : NetworkFileManager(client!!)
}
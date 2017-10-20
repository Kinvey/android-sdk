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
package com.kinvey.java;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import com.kinvey.java.core.KinveyMockUnitTest;
import com.kinvey.java.network.NetworkFileManager;
import com.kinvey.java.testing.HttpTesting;

import static org.mockito.Mockito.*;

/**
 * @author m0rganic
 * @since 2.0
 */
@RunWith(MockitoJUnitRunner.class)
public class NetworkFileManagerTest extends KinveyMockUnitTest {

    @Mock private AbstractInputStreamContent mockContent;

    private NetworkFileManager networkFileManagerApiUnderTest;

    @Before
    public void setUp() {

        reset(mockContent);
    }


    @Test
    public void uploadUrlEndpointMatches() throws IOException {
        NetworkFileManager networkFileManagerApi = new MockNetworkFileManager(super.getClient());
        FileMetaData meta = new FileMetaData("testfilename.txt");
        NetworkFileManager.UploadMetadataAndFile upload = networkFileManagerApi.prepUploadBlocking(meta
                , mockContent, new UploaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpUploader uploader) throws IOException {

            }
        });
        HttpRequest request = upload.buildHttpRequest();
        String expectedPath = HttpTesting.SIMPLE_URL + "/blob//testfilename.txt";
        assertEquals(expectedPath, request.getUrl().toString());
    }


    @Test
    public void downloadUrlEndpointMatches() throws IOException {
        NetworkFileManager networkFileManagerApi = new MockNetworkFileManager(super.getClient());
        FileMetaData meta = new FileMetaData("testfilename.txt");
        NetworkFileManager.DownloadMetadataAndFile download = networkFileManagerApi.prepDownloadBlocking(meta);
        HttpRequest request = download.buildHttpRequest();
        String expectedPath = HttpTesting.SIMPLE_URL + "/blob//testfilename.txt";
        assertEquals(expectedPath, request.getUrl().toString());
    }

    @Test
    public void testFileUploadInitializer() {
        networkFileManagerApiUnderTest = new MockNetworkFileManager(super.getClient());
        try {
            networkFileManagerApiUnderTest.prepUploadBlocking(new FileMetaData("testfilename.txt"),
                    mockContent, new UploaderProgressListener() {
                        @Override
                        public void progressChanged(MediaHttpUploader uploader) throws IOException {

                        }
                    });

        } catch (IOException e) {
            fail("file api should not be throw exception on upload");
        }
    }

    @Test
    public void testFileDownloadWithTTL() throws  IOException{
        networkFileManagerApiUnderTest = new MockNetworkFileManager(super.getClient());
        NetworkFileManager.DownloadMetadataQuery download =
                networkFileManagerApiUnderTest.prepDownloadWithTTLBlocking("testfilename.txt", 120);
        HttpRequest req = download.buildHttpRequest();
        String expectedPath = HttpTesting.SIMPLE_URL + "/blob//testfilename.txt";
        assertEquals(expectedPath, req.getUrl().toString());
    }

    @Test
    public void testFileDownloadInitializer() {
        networkFileManagerApiUnderTest = new MockNetworkFileManager(super.getClient());
        try {
            networkFileManagerApiUnderTest.prepDownloadBlocking(new FileMetaData("testfilename.txt"));
        } catch (IOException e) {
            fail("file api should not throw an exception on download");
        }

    }

    @Test
    public void downloadShouldThrowNpeOnNullFilename() {
        networkFileManagerApiUnderTest = new MockNetworkFileManager(super.getClient()) ;
        try {
            networkFileManagerApiUnderTest.prepDownloadBlocking(new FileMetaData(null));
            fail("file api should throw exception on null filename");
        } catch (IOException e) {
            fail("file api should throw a NullPointerException on null filename");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testFileDelete() {
        networkFileManagerApiUnderTest = new MockNetworkFileManager(super.getClient());
        try {
            networkFileManagerApiUnderTest.deleteBlocking(new FileMetaData("testfilename.txt"));
        } catch (IOException e) {
            fail("file api should not throw an exception on remove");
        }
    }

    public void testFileCustomVersion() throws IOException {
        NetworkFileManager networkFileManagerApi = new MockNetworkFileManager(super.getClient());
        networkFileManagerApi.setClientAppVersion("1.2.3");
        FileMetaData meta = new FileMetaData("testfilename.txt");
        NetworkFileManager.DownloadMetadataAndFile request = networkFileManagerApi.prepDownloadBlocking(meta);

    	Object header = request.getRequestHeaders().get("X-Kinvey-Client-App-Version");
    	assertEquals("1.2.3", (String) header);
    }

    public void testFileCustomHeader() throws IOException {
        NetworkFileManager networkFileManagerApi = new MockNetworkFileManager(super.getClient());
    	GenericJson custom = new GenericJson();
    	custom.put("First", 1);
    	custom.put("Second", "two");
    	networkFileManagerApi.setCustomRequestProperties(custom);
    	FileMetaData meta = new FileMetaData("testfilename.txt");
        NetworkFileManager.DownloadMetadataAndFile request = networkFileManagerApi.prepDownloadBlocking(meta);
        Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	assertEquals("{\"First\":1,\"Second\":\"two\"}", (String) header);

    }

    public void testFileCustomVersionNull() throws IOException {
        NetworkFileManager networkFileManagerApi = new MockNetworkFileManager(super.getClient());
    	networkFileManagerApi.setClientAppVersion(null);
    	FileMetaData meta = new FileMetaData("testfilename.txt");
        NetworkFileManager.DownloadMetadataAndFile request = networkFileManagerApi.prepDownloadBlocking(meta);
        Object header = request.getRequestHeaders().get("X-Kinvey-Client-App-Version");
    	assertEquals(null, header);
    }

    public void testFileCustomHeaderNull() throws IOException {
        NetworkFileManager networkFileManagerApi = new MockNetworkFileManager(super.getClient());
    	networkFileManagerApi.setCustomRequestProperties(null);
    	FileMetaData meta = new FileMetaData("testfilename.txt");
        NetworkFileManager.DownloadMetadataAndFile request = networkFileManagerApi.prepDownloadBlocking(meta);
        Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	assertEquals(null, header);
    }

    private static class MockNetworkFileManager extends NetworkFileManager {


        /**
         * Base constructor requires the client instance to be passed in.
         * <p>
         * {@link com.kinvey.java.core.AbstractKinveyClient#initializeRequest(com.kinvey.java.core.AbstractKinveyClientRequest)} is used to initialize all requests
         * constructed by this api.
         * </p>
         *
         * @param client required instance
         * @throws NullPointerException if the client parameter is non-null
         */
        protected MockNetworkFileManager(AbstractClient client) {
            super(client);
        }




    }

}

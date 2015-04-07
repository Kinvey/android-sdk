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
import com.kinvey.java.model.FileMetaData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import com.kinvey.java.AppDataTest.Entity;
import com.kinvey.java.core.KinveyMockUnitTest;
import com.kinvey.java.testing.HttpTesting;

import static org.mockito.Mockito.*;

/**
 * @author m0rganic
 * @since 2.0
 */
@RunWith(MockitoJUnitRunner.class)
public class FileTest extends KinveyMockUnitTest {

    @Mock private AbstractInputStreamContent mockContent;

    private File fileApiUnderTest;

    @Before
    public void setUp() {

        reset(mockContent);
    }


    @Test
    public void uploadUrlEndpointMatches() throws IOException {
        File fileApi = new MockFile(super.getClient());
        FileMetaData meta = new FileMetaData("testfilename.txt");
        File.UploadMetadataAndFile upload = fileApi.uploadBlocking(meta, mockContent);
        HttpRequest request = upload.buildHttpRequest();
        String expectedPath = HttpTesting.SIMPLE_URL + "/blob//testfilename.txt";
        assertEquals(expectedPath, request.getUrl().toString());
    }


    @Test
    public void downloadUrlEndpointMatches() throws IOException {
        File fileApi = new MockFile(super.getClient());
        FileMetaData meta = new FileMetaData("testfilename.txt");
        File.DownloadMetadataAndFile download = fileApi.downloadBlocking(meta);
        HttpRequest request = download.buildHttpRequest();
        String expectedPath = HttpTesting.SIMPLE_URL + "/blob//testfilename.txt";
        assertEquals(expectedPath, request.getUrl().toString());
    }

    @Test
    public void testFileUploadInitializer() {
        fileApiUnderTest = new MockFile(super.getClient());
        try {
            fileApiUnderTest.uploadBlocking(new FileMetaData("testfilename.txt"), mockContent);

        } catch (IOException e) {
            fail("file api should not be throw exception on upload");
        }
    }

    @Test
    public void testFileDownloadWithTTL() throws  IOException{
        fileApiUnderTest = new MockFile(super.getClient());
        File.DownloadMetadataAndFileQuery download =  fileApiUnderTest.downloadWithTTLBlocking("testfilename.txt", 120);
        HttpRequest req = download.buildHttpRequest();
        String expectedPath = HttpTesting.SIMPLE_URL + "/blob//testfilename.txt?query";
        assertEquals(expectedPath, req.getUrl().toString());
    }

    @Test
    public void testFileDownloadInitializer() {
        fileApiUnderTest = new MockFile(super.getClient());
        try {
            fileApiUnderTest.downloadBlocking(new FileMetaData("testfilename.txt"));
        } catch (IOException e) {
            fail("file api should not throw an exception on download");
        }

    }

    @Test
    public void downloadShouldThrowNpeOnNullFilename() {
        fileApiUnderTest = new MockFile(super.getClient()) ;
        try {
            fileApiUnderTest.downloadBlocking(new FileMetaData(null));
            fail("file api should throw exception on null filename");
        } catch (IOException e) {
            fail("file api should throw a NullPointerException on null filename");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testFileDelete() {
        fileApiUnderTest = new MockFile(super.getClient());
        try {
            fileApiUnderTest.deleteBlocking(new FileMetaData("testfilename.txt"));
        } catch (IOException e) {
            fail("file api should not throw an exception on delete");
        }
    }
    
    public void testFileCustomVersion() throws IOException {
        File fileApi = new MockFile(super.getClient());
        fileApi.setClientAppVersion("1.2.3");
        FileMetaData meta = new FileMetaData("testfilename.txt");
        File.DownloadMetadataAndFile request = fileApi.downloadBlocking(meta);

    	Object header = request.getRequestHeaders().get("X-Kinvey-Client-App-Version");
    	assertEquals("1.2.3", (String) header);
    }
    
    public void testFileCustomHeader() throws IOException {
        File fileApi = new MockFile(super.getClient());
    	GenericJson custom = new GenericJson();
    	custom.put("First", 1);
    	custom.put("Second", "two");
    	fileApi.setCustomRequestProperties(custom);
    	FileMetaData meta = new FileMetaData("testfilename.txt");
        File.DownloadMetadataAndFile request = fileApi.downloadBlocking(meta);
        Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	assertEquals("{\"First\":1,\"Second\":\"two\"}", (String) header);    	
    	
    }
    
    public void testFileCustomVersionNull() throws IOException {
        File fileApi = new MockFile(super.getClient());
    	fileApi.setClientAppVersion(null);
    	FileMetaData meta = new FileMetaData("testfilename.txt");
        File.DownloadMetadataAndFile request = fileApi.downloadBlocking(meta);
        Object header = request.getRequestHeaders().get("X-Kinvey-Client-App-Version");
    	assertEquals(null, header);    	
    }
    
    public void testFileCustomHeaderNull() throws IOException {
        File fileApi = new MockFile(super.getClient());
    	fileApi.setCustomRequestProperties(null);
    	FileMetaData meta = new FileMetaData("testfilename.txt");
        File.DownloadMetadataAndFile request = fileApi.downloadBlocking(meta);
        Object header = request.getRequestHeaders().get("X-Kinvey-Custom-Request-Properties");
    	assertEquals(null, header);      	
    }

    private static class MockFile extends File{


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
        protected MockFile(AbstractClient client) {
            super(client);
        }




    }

}

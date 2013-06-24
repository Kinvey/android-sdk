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
package com.kinvey.java;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpRequest;
import com.kinvey.java.model.FileMetaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

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
        super.setUp();
        reset(mockContent);
    }


//    @Test
//    public void uploadUrlEndpointMatches() throws IOException {
//        File fileApi = new MockFile(super.mockClient);
//        File.Upload upload = fileApi.uploadBlocking("testfilename.txt", mockContent);
//        HttpRequest request = upload.buildHttpRequest();
//        String expectedPath = HttpTesting.SIMPLE_URL + "/blob//upload-loc/testfilename.txt";
//        assertEquals(expectedPath, request.getUrl().toString());
//    }
//
//
//    @Test
//    public void downloadUrlEndpointMatches() throws IOException {
//        File fileApi = new MockFile(super.mockClient);
//        File.Download download = fileApi.downloadBlocking("testfilename.txt");
//        HttpRequest request = download.buildHttpRequest();
//        String expectedPath = HttpTesting.SIMPLE_URL + "/blob//download-loc/testfilename.txt";
//        assertEquals(expectedPath, request.getUrl().toString());
//    }

    @Test
    public void testFileUploadInitializer() {
        fileApiUnderTest = new MockFile(super.mockClient);
        try {
            fileApiUnderTest.uploadBlocking(new FileMetaData("testfilename.txt"), mockContent);

        } catch (IOException e) {
            fail("file api should not be throw exception on upload");
        }
    }

//    @Test
//    public void uploadShouldThrowNpeOnNullFilename() {
//        fileApiUnderTest = new File(super.mockClient);
//        try {
//            fileApiUnderTest.uploadBlocking(new File.FileMetaData(null), mock(AbstractInputStreamContent.class));
//            fail("file api should throw exception on null filename");
//        } catch (IOException e) {
//            fail("file api should throw a NullPointerException on null filename");
//        } catch (NullPointerException e) {
//            // expected
//        }
//    }


//    @Test
//    public void testFileDownloadInitializer() {
//        fileApiUnderTest = new MockFile(super.mockClient);
//        try {
//            fileApiUnderTest.downloadBlocking(new File.FileMetaData("testfilename.txt"));
//        } catch (IOException e) {
//            fail("file api should not throw an exception on download");
//        }
//
//    }

//    @Test
//    public void downloadShouldThrowNpeOnNullFilename() {
//        fileApiUnderTest = new File(super.mockClient);
//        try {
//            fileApiUnderTest.downloadBlocking(new File.FileMetaData(null));
//            fail("file api should throw exception on null filename");
//        } catch (IOException e) {
//            fail("file api should throw a NullPointerException on null filename");
//        } catch (NullPointerException e) {
//            // expected
//        }
//    }

//    @Test
//    public void testFileDelete() {
//        fileApiUnderTest = new MockFile(super.mockClient);
//        try {
//            fileApiUnderTest.deleteBlocking(new File.FileMetaData("testfilename.txt"));
//        } catch (IOException e) {
//            fail("file api should not throw an exception on delete");
//        }
//    }

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

        @Override
        protected void initMimeTypeAndSize(FileMetaData meta, java.io.File file) {
            //To change body of implemented methods use File | Settings | File Templates.
        }



    }

}

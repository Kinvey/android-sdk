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
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

import java.io.IOException;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyHeaders;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.UriLocResponse;

/**
 Wraps the {@link com.kinvey.java.File} public methods in asynchronous functionality using native Android AsyncTask.
 *
 * <p>
 * This class is constructed via {@link com.kinvey.java.AbstractClient#file()} factory method.
 * </p>
 *
 * <p>
 * The callback mechanism for this api is extended to include the {@link UploaderProgressListener#progressChanged(com.kinvey.java.core.MediaHttpUploader)}
 * method, which receives notifications as the upload process transitions through and progresses with the upload.
 * process.
 * </p>
 *
 * <p>
 * Sample usage:
 * <pre>
 *    mKinveyClient.file().upload(file,  new UploaderProgressListener() {
 *        @Override
 *        public void onSuccess(Void result) {
 *            Log.i(TAG, "successfully upload file");
 *        }
 *        @Override
 *        public void onFailure(Throwable error) {
 *            Log.e(TAG, "failed to upload file.", error);
 *        }
 *        @Override
 *        public void progressChanged(MediaHttpUploader uploader) throws IOException {
 *            Log.i(TAG, "upload progress: " + uploader.getUploadState());
 *            switch (uploader.getUploadState()) {
 *                case INITIATION_STARTED:
 *                    Log.i(TAG, "Initiation Started");
 *                    break;
 *                case INITIATION_COMPLETE:
 *                    Log.i(TAG, "Initiation Completed");
 *                    break;
 *                case DOWNLOAD_IN_PROGRESS:
 *                    Log.i(TAG, "Upload in progress");
 *                    Log.i(TAG, "Upload percentage: " + uploader.getProgress());
 *                    break;
 *                case DOWNLOAD_COMPLETE:
 *                    Log.i(TAG, "Upload Completed!");
 *                    break;
 *            }
 *    });
 *
 *
 * </pre>
 *
 * </p>
 * @author m0rganic
 * @since 2.0
 */
public class File {

    /** the client for this api **/
    private AbstractClient client;

    /** the upload request listener, can be {@code null} if hte calling code has not explicitly set it **/
    private UploaderProgressListener uploadProgressListener;

    /** the download request listener, can be {@code null} if the call code has not set it **/
    private DownloaderProgressListener downloaderProgressListener;

    /**
     * Base constructor requires the client instance to be passed in.
     * <p>
     * {@link com.kinvey.java.core.AbstractKinveyClient#initializeRequest(com.kinvey.java.core.AbstractKinveyClientRequest)} is used to initialize all requests
     * constructed by this api.
     * </p>
     * @param client required instance
     * @throws NullPointerException if the client parameter is non-null
     */
    protected File(AbstractClient client) {
        this.client = Preconditions.checkNotNull(client);
    }

    /**
     * Constructs a request to retrieve a temporary url for purposes of downloading a given file already known to Kinvey.
     * <p>
     * The url expires within 30 secs. of calling {@link com.kinvey.java.File.GetDownloadUrl#execute()}.
     * </p>
     * @param fileName name of the file for which kinvey service is aware
     * @return request for which the temporary url can be retrieved
     * @throws IOException if initializing the request fails
     */
    public GetDownloadUrl getDownloadUrl(String fileName) throws IOException {
        GetDownloadUrl getDownloadUrl = new GetDownloadUrl(fileName);
        client.initializeRequest(getDownloadUrl);
        return getDownloadUrl;
    }

    /**
     * Constructs a request to retrieve a temporary url for purposes of uploading a given file.
     * <p>
     * The url expires within 30 secs. of calling {@link com.kinvey.java.File.GetUploadUrl#execute()}.
     * </p>
     *
     * @param fileName the name of the file used in metadata
     * @return valid request for which the temporary upload url can be retrieved
     * @throws IOException if initializing the request fails
     */
    public GetUploadUrl getUploadUrl (String fileName) throws IOException {
        GetUploadUrl getUploadUrl = new GetUploadUrl(fileName);
        client.initializeRequest(getUploadUrl);
        return getUploadUrl;
    }

    /**
     * Uploads a given file and its contents to the Kinvey file service.
     *
     * @param fileName the filename used for the metadata
     * @param content the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     */
    public Upload upload(String fileName, AbstractInputStreamContent content) throws IOException {
        Upload upload = new Upload(fileName, content, uploadProgressListener);
        client.initializeRequest(upload);
        return upload;
    }

    /**
     * Download a given file from the Kinvey file service.
     * <p>
     *
     * </p>
     *
     * @param filename the name used in metadata for downloadable file.
     * @return a valid request to be executed for the download operation from Kinvey file service
     * @throws IOException
     */
    public Download download(String filename) throws IOException {
        Download download = new Download(filename, downloaderProgressListener);
        client.initializeRequest(download);
        return download;
    }

    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param filename the name of used in metadata to refer to the file
     * @return a valid DELETE request to be executed
     * @throws IOException
     */
    public Delete delete(String filename) throws IOException {
        Delete delete = new Delete(filename);
        client.initializeRequest(delete);
        return delete;
    }

    /**
     * @param uploadProgressListener the listener to receive notifications as the upload progresses
     */
    public void setUploadProgressListener(UploaderProgressListener uploadProgressListener) {
        this.uploadProgressListener = uploadProgressListener;
    }

    /**
     * @param downloaderProgressListener the listener to receive notifications as the download progresses
     */
    public void setDownloaderProgressListener(DownloaderProgressListener downloaderProgressListener) {
        this.downloaderProgressListener = downloaderProgressListener;
    }


    /**
     * GET a temporary url for download.
     */
    public class GetDownloadUrl extends AbstractKinveyJsonClientRequest<UriLocResponse> {

        private final static String REST_URL = "blob/{appKey}/download-loc/{fileName}";

        @Key
        private final String fileName;

        private GetDownloadUrl(String filename) {
            super(client, "GET", REST_URL, null, UriLocResponse.class);
            this.fileName = Preconditions.checkNotNull(filename);
        }


    }

    /**
     * GET a temporary url for uploading file contents.
     */
    public class GetUploadUrl extends AbstractKinveyJsonClientRequest<UriLocResponse> {

        private final static String REST_URL = "blob/{appKey}/upload-loc/{fileName}";

        @Key
        private final String fileName;

        private GetUploadUrl(String filename) {
            super(client, "GET", REST_URL, null, UriLocResponse.class);
            this.fileName = Preconditions.checkNotNull(filename);
        }
    }

    /**
     * Initiate an upload of a particular file and its contents.
     */
    public class Upload extends AbstractKinveyJsonClientRequest<Void> {

        private final static String REST_URL = "blob/{appKey}/upload-loc/{fileName}";

        @Key
        private String fileName;

        private Upload(String fileName, AbstractInputStreamContent mediaContent, UploaderProgressListener progressListener) {
            super(client, "GET", REST_URL, null, Void.class);
            initializeMediaHttpUploader(mediaContent, progressListener);
            this.fileName = Preconditions.checkNotNull(fileName);
        }
    }

    /**
     * Initiate a download of a particular file already known to Kinvey.
     */
    public class Download extends AbstractKinveyJsonClientRequest<Void> {

        private final static String REST_URL = "blob/{appKey}/download-loc/{fileName}";

        @Key
        private String fileName;

        private Download(String fileName, DownloaderProgressListener progressListener) {
            super(client, "GET", REST_URL, null, Void.class);
            initializeMediaHttpDownloader(progressListener);
            this.fileName = Preconditions.checkNotNull(fileName);
        }
    }

    /**
     * Initiate a delete of a particular file already known to Kinvey.
     * <p>
     * {@link com.kinvey.java.core.AbstractKinveyJsonClientRequest#executeUnparsed()} is overridden to first GET
     * the URI for which the file should be deleted.
     * </p>
     */
    public class Delete extends AbstractKinveyJsonClientRequest<Void> {

        private final static String REST_URL = "blob/{appKey}/remove-loc/{fileName}";

        @Key
        private String fileName;

        private Delete(String fileName) {
            super(client, "GET", REST_URL, null, Void.class);
            this.fileName = Preconditions.checkNotNull(fileName);
        }

        @Override
        public HttpResponse executeUnparsed() throws IOException {
            HttpResponse uriResponse = super.executeUnparsed();
            JsonObjectParser parser = client.getJsonFactory().createJsonObjectParser();
            UriLocResponse uri = parser.parseAndClose(uriResponse.getContent(), uriResponse.getContentCharset(), UriLocResponse.class);
            HttpRequest request = client.getRequestFactory().buildDeleteRequest(uri.newGenericUrl());
            request.setSuppressUserAgentSuffix(true);
            request.setHeaders(new KinveyHeaders());
            return request.execute();
        }
    }


// TODO (mbickle): once GET on _blobs collection is possible without mastersecret credentials
//    public class List extends AbstractKinveyJsonClientRequest<Metadata[]> {
//
//        private static final String REST_URL = "appdata/{appKey}/_blob";
//
//        public List() {
//            super(client, "GET", REST_URL, null, File.Metadata[].class);
//        }
//    }

//    public List list() throws IOException {
//        List list = new List();
//        client.initialize(list);
//        return list;
//    }
//
//    public static class Metadata extends GenericJson {
//
//        @Key("_id")
//        private String id;
//
//        @Key("objectname")
//        private String fileName;
//
//        @Key("_acl")
//        private Permission accessControlList;
//
//        public class Permission extends GenericJson {
//
//            @Key("creator")
//            private String creator;
//
//            public String getCreator() {
//                return creator;
//            }
//        }
//
//        public String getFileName() {
//            return fileName;
//        }
//
//        public String getId() {
//            return id;
//        }
//
//        public Permission getAccessControlList() {
//            return accessControlList;
//        }
//
//    }
}

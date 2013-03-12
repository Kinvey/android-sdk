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
package com.kinvey.android;

import com.google.api.client.http.InputStreamContent;
import com.google.common.base.Preconditions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.File;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.UriLocResponse;

/**
 * Wraps the {@link com.kinvey.java.File} public methods in asynchronous functionality using native Android AsyncTask.
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#file()} convenience method.
 * </p>
 *
 * <p>
 * The callback mechanism for this api is extended to include the {@link UploaderProgressListener#progressChanged(com.kinvey.java.core.MediaHttpUploader)}
 * method, which receives notifications as the uploadBlocking process transitions through and progresses with the uploadBlocking.
 * process.
 * </p>
 *
 * <p>
 * Sample usage:
 * <pre>
 *    mKinveyClient.file().uploadBlocking(file,  new UploaderProgressListener() {
 *        @Override
 *        public void onSuccess(Void result) {
 *            Log.i(TAG, "successfully uploadBlocking file");
 *        }
 *        @Override
 *        public void onFailure(Throwable error) {
 *            Log.e(TAG, "failed to uploadBlocking file.", error);
 *        }
 *        @Override
 *        public void progressChanged(MediaHttpUploader uploader) throws IOException {
 *            Log.i(TAG, "uploadBlocking progress: " + uploader.getUploadState());
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
 *
 * @author edwardf
 * @author m0rganic
 * @since 2.0
 * @version $Id: $
 */
public class AsyncFile extends File {


    /**
     * Base constructor requires the client instance to be passed in.
     * <p>
     * {@link com.kinvey.java.core.AbstractKinveyClient#initializeRequest(com.kinvey.java.core.AbstractKinveyClientRequest)} is used to initialize all
     * requests constructed by this api.
     * </p>
     *
     * @param client required instance
     * @throws NullPointerException if the client parameter is non-null
     */
    AsyncFile(AbstractClient client) {
        super(client);
    }

    /**
     * Constructs a request to retrieveBlocking a temporary url for purposes of downloading a given file already known to Kinvey.
     * <p>
     * The url expires within 60 secs. of calling execute on the request returned.
     * </p>
     *
     * @param fileName name of the file for which kinvey service is aware
     * @param callback an implementation of a client callback to getBlocking results on the UI thread from the async call.
     */
    public void getDownloadUrl(String fileName, KinveyClientCallback<UriLocResponse> callback) {
        new GetDownloadUrl(fileName, callback).execute();
    }

    /**
     * Constructs a request to retrieveBlocking a temporary url for purposes of uploading a given file.
     * <p>
     * The url expires within 60 secs. of calling execute on the request returned.
     * </p>
     *
     * @param fileName the name of the file used in metadata
     * @param callback an implementation of a client callback to getBlocking results on the UI thread from the async call.
     */
    public void getUploadUrl(String fileName, KinveyClientCallback<UriLocResponse> callback) {
        new GetUploadUrl(fileName, callback).execute();
    }

    /**
     * Uploads a given file and its contents to the Kinvey file service.
     *
     * <p>
     * The UI thread can receive updates on the status by passing in a progress listener.
     * </p>
     * <p>
     *     Sample usage:
     * </p>
     * <pre>
     *            public static class MyUploadProgressListener implements UploaderProgressListener {
     *
     *                public void progressChanged(MediaHttpUploader uploader) throws IOException {
     *                    switch (uploader.getUploadState()) {
     *                        case INITIATION_STARTED:
     *                            Log.i(TAG, "Initiation Started");
     *                            break;
     *                        case INITIATION_COMPLETE:
     *                            Log.i(TAG, "Initiation Completed");
     *                            break;
     *                        case DOWNLOAD_IN_PROGRESS:
     *                            Log.i(TAG, "Upload in progress");
     *                            Log.i(TAG, "Upload percentage: " + uploader.getProgress());
     *                            break;
     *                        case DOWNLOAD_COMPLETE:
     *                            Log.i(TAG, "Upload Completed!");
     *                            break;
     *                    }
     *                }
     *
     *                public void onSuccess(Void result) {}
     *
     *                public void onFailure(Throwable error) {
     *                    Log.e(TAG, "Upload failed", error);
     *                }
     *            }
     * </pre>
     *
     * @param file     the file to be uploaded
     * @param listener an implementation of a client listener that gets notifications on the UI thread from the underlying thread.
     */
    public void upload(java.io.File file, UploaderProgressListener listener) {
        this.setUploadProgressListener(listener);
        new FileUpload(file, listener).execute();
    }

    /**
     * Uploads the contents of the stream to the Kinvey file service endpoint.
     *
     * @param name name to refer to the blob when stored with the file service endpoint
     * @param inputStream  stream to be uploaded
     * @param listener an implementation of a client listener to getBlocking results on the UI thread from the async call.
     */
    public void upload(String name, InputStream inputStream, UploaderProgressListener listener) {
        this.setUploadProgressListener(listener);
        new FileUpload(name, inputStream, listener).execute();
    }


    /**
     * Download a given file from the Kinvey file service.
     * <p>
     * The UI thread can receive updates on the status by passing in a progress listener.
     * </p>
     *
     * <pre>
     *             public static class MyDownloadProgressListener implements DownloaderProgressListener {
     *
     *                 public void progressChanged(MediaHttpDownloader downloader) throws IOException {
     *                     switch (downloader.getDownloadState()) {
     *                         case DOWNLOAD_IN_PROGRESS:
     *                             Log.i(TAG, "Download in progress");
     *                             Log.i(TAG, "Download percentage: " + downloader.getProgress());
     *                             break;
     *                         case DOWNLOAD_COMPLETE:
     *                             Log.i(TAG, "Download Completed!");
     *                             break;
     *                     }
     *                 }
     *
     *
     *                 public void onSuccess(Void result) {}
     *
     *                 public void onFailure(Throwable error) {
     *                     Log.e(TAG, "Upload failed", error);
     *                 }
     *
     *            }
     * </pre>
     *
     * @param filename the name used in metadata for downloadable file.
     * @param listener an implementation of a client listener to getBlocking results on the UI thread from the async call.
     * @param out a {@link java.io.OutputStream} object.
     */
    public void download(String filename, OutputStream out, DownloaderProgressListener listener) {
        this.setDownloaderProgressListener(listener);
        new FileDownload(filename, out, listener).execute();
    }

    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param filename the name of used in metadata to refer to the file
     * @param callback an implementation of a client callback to getBlocking results on the UI thread from the async call.
     */
    public void delete(String filename, KinveyClientCallback<Void> callback) {
        new FileDelete(filename, callback).execute();
    }


    /**
     * GET a temporary url for downloadBlocking.
     */
    private class GetDownloadUrl extends AsyncClientRequest<UriLocResponse> {

        private final String fileName;

        private GetDownloadUrl(String filename, KinveyClientCallback callback) {
            super(callback);
            this.fileName = filename;
        }


        @Override
        protected UriLocResponse executeAsync() throws IOException {
            return AsyncFile.this.getDownloadUrlBlocking(fileName).execute();
        }
    }

    /**
     * GET a temporary url for uploading file contents.
     */
    private class GetUploadUrl extends AsyncClientRequest<UriLocResponse> {

        private final String fileName;

        private GetUploadUrl(String filename, KinveyClientCallback callback) {
            super(callback);
            this.fileName = filename;
        }

        @Override
        protected UriLocResponse executeAsync() throws IOException {
            return AsyncFile.this.getUploadUrlBlocking(fileName).execute();
        }
    }


    private class FileUpload extends AsyncClientRequest<Void> {

        private java.io.File file;
        private byte [] byteContent;
        private InputStream inputStream;
        private String name;

        public FileUpload(java.io.File file, KinveyClientCallback callback) {
            super(callback);
            Preconditions.checkNotNull(file, "file must not be null");
            this.file = file;
            this.name = file.getName();
        }

        public FileUpload(byte [] byteContent, String name, KinveyClientCallback callback) {
            super(callback);
            Preconditions.checkNotNull(byteContent, "byteContent must not be null");
            Preconditions.checkNotNull(name, "name must not be null");
            this.byteContent = byteContent;
            this.name = name;
        }

        public FileUpload(String name, InputStream inputStream, KinveyClientCallback callback) {
            super(callback);
            Preconditions.checkNotNull(inputStream, "byteContent must not be null");
            Preconditions.checkNotNull(name, "name must not be null");
            this.inputStream = inputStream;
            this.name = name;
        }

        @Override
        protected Void executeAsync() throws IOException {

            InputStreamContent mediaContent = null;
            String mimetype = "application/octet-stream";
            if (file != null) {
                mediaContent = new InputStreamContent(mimetype, new FileInputStream(file));
                mediaContent.setLength(file.length());
            } else {
                mediaContent = new InputStreamContent(mimetype, inputStream);
                mediaContent.setLength(inputStream.available());
            }
            mediaContent.setCloseInputStream(false);
            mediaContent.setRetrySupported(false);

            return AsyncFile.this.uploadBlocking(name, mediaContent).execute();
        }

    }


    private class FileDownload extends AsyncClientRequest<Void> {

        private String filename;
        private OutputStream out;

        public FileDownload(String filename, OutputStream out, KinveyClientCallback callback) {
            super(callback);
            this.filename = filename;
            this.out = out;
        }

        @Override
        protected Void executeAsync() throws IOException {
            AsyncFile.this.downloadBlocking(filename).executeAndDownloadTo(out);
            return null;
        }

    }


    private class FileDelete extends AsyncClientRequest<Void> {

        private String filename;

        public FileDelete(String filename, KinveyClientCallback callback) {
            super(callback);
            this.filename = filename;
        }

        @Override
        protected Void executeAsync() throws IOException {
            AsyncFile.this.deleteBlocking(filename).execute();
            return null;
        }
    }

}

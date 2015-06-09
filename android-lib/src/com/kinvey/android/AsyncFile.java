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
package com.kinvey.android;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;

import com.google.api.client.http.InputStreamContent;
import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.File;
import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.model.KinveyDeleteResponse;

/**
 * Wraps the {@link com.kinvey.java.File} public methods in asynchronous functionality using native Android AsyncTask.
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#file()} convenience method.
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
 * {@code
 *    mKinveyClient.file().upload(file,  new UploaderProgressListener() {
 *        public void onSuccess(Void result) {
 *            //successfully upload file!
 *        }
 *        public void onFailure(Throwable error) {
 *            //"failed to upload file!
 *        }
 *        public void progressChanged(MediaHttpUploader uploader) throws IOException {
 *            //"upload progress: " + uploader.getUploadState());
 *            switch (uploader.getUploadState()) {
 *                case INITIATION_STARTED:
 *                    //Initiation Started
 *                    break;
 *                case INITIATION_COMPLETE:
 *                    //Initiation Completed
 *                    break;
 *                case DOWNLOAD_IN_PROGRESS:
 *                    //Upload in progress
 *                    //Upload percentage:  + uploader.getProgress()
 *                    break;
 *                case DOWNLOAD_COMPLETE:
 *                    //Upload Completed!
 *                    break;
 *            }
 *        }
 *    });
 * }
 * </pre>
 *
 * </p>
 *
 * @author edwardf
 * @since 2.5
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
        setMimeTypeManager(new AndroidMimeTypeFinder());
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
     * {@code
     *     public static class MyUploadProgressListener implements UploaderProgressListener {
     *
     *         public void progressChanged(MediaHttpUploader uploader) throws IOException {
     *             switch (uploader.getUploadState()) {
     *                 case INITIATION_STARTED:
     *                     //Initiation Started
     *                     break;
     *                 case INITIATION_COMPLETE:
     *                     //Initiation Completed
     *                     break;
     *                 case DOWNLOAD_IN_PROGRESS:
     *                     //Upload in progress
     *                     //Upload percentage:  + uploader.getProgress()
     *                     break;
     *                 case DOWNLOAD_COMPLETE:
     *                     //Upload Completed!
     *                     break;
     *             }
     *         }
     *
     *         public void onSuccess(Void result) {}
     *
     *         public void onFailure(Throwable error) {
     *             //Upload failed
     *         }
     *     }
     * }
     * </pre>
     *
     * @param file     the file to be uploaded
     * @param listener an implementation of a client listener that gets notifications on the UI thread from the underlying thread.
     */
    public void upload(java.io.File file, UploaderProgressListener listener) {
        this.setUploadProgressListener(listener);
        FileMetaData meta = new FileMetaData(file.getName());
        meta.setFileName(file.getName());
        this.mimeTypeFinder.getMimeType(meta, file);
        new FileUpload(meta, file, listener).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Upload a java.io.File with it's associated metadata
     *
     *
     * @param meta the metadata of the file to upload
     * @param file the file itself
     * @param listener listener for callbacks about upload progress
     */
    public void upload(FileMetaData meta, java.io.File file, UploaderProgressListener listener){
        this.setUploadProgressListener(listener);
        this.mimeTypeFinder.getMimeType(meta, file);
        new FileUpload(meta, file, listener).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Uploads the contents of the stream to the Kinvey file service endpoint.
     *
     * @param name name to refer to the blob when stored with the file service endpoint
     * @param inputStream  stream to be uploaded
     * @param listener an implementation of a client listener to get results on the UI thread from the async call.
     */
    public void upload(String name, InputStream inputStream, UploaderProgressListener listener) {
        this.setUploadProgressListener(listener);
        FileMetaData meta = new FileMetaData(name);
        this.mimeTypeFinder.getMimeType(meta, inputStream);
        new FileUpload(meta, inputStream, listener).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Uploads the contents of the stream to the Kinvey file service endpoint.
     *
     * @param meta the metadata of the file to upload
     * @param inputStream  stream to be uploaded
     * @param listener an implementation of a client listener to get results on the UI thread from the async call.
     */
    public void upload(FileMetaData meta, InputStream inputStream, UploaderProgressListener listener) {
        this.setUploadProgressListener(listener);
        this.mimeTypeFinder.getMimeType(meta, inputStream);
        new FileUpload(meta, inputStream, listener).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }




    /**
     * Download a given file from the Kinvey file service.
     * <p>
     * The UI thread can receive updates on the status by passing in a progress listener.
     * </p>
     *
     * <pre>
     * {@code
     *     public static class MyDownloadProgressListener implements DownloaderProgressListener {
     *
     *         public void progressChanged(MediaHttpDownloader downloader) throws IOException {
     *             switch (downloader.getDownloadState()) {
     *                 case DOWNLOAD_IN_PROGRESS:
     *                     //Download in progress
     *                     //Download percentage: + downloader.getProgress()
     *                     break;
     *                 case DOWNLOAD_COMPLETE:
     *                     //Download Completed!
     *                     break;
     *             }
     *         }
     *
     *
     *         public void onSuccess(Void result) {}
     *
     *         public void onFailure(Throwable error) {
     *             //Upload failed", error
     *         }
     *
     *     }
     * }
     * </pre>
     *
     * @param metaData the metadata of the file.
     * @param listener an implementation of a client listener to get results on the UI thread from the async call.
     * @param out a {@link java.io.OutputStream} object.
     */
    public void download(FileMetaData metaData, OutputStream out, DownloaderProgressListener listener) {
        this.setDownloaderProgressListener(listener);
        new FileDownload(metaData, out, listener).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }


    /**
     * Download a file by Query, only the first result will be downloaded.
     *
     *
     * @param q - the query, with a limit of 1
     * @param out - where to download the file
     * @param listener - for progress notifications
     */
    public void download(Query q, OutputStream out, DownloaderProgressListener listener){
        this.setDownloaderProgressListener(listener);
        new FileQueryDownload(q, out, listener).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }


    /**
     * Download a file asyncronously with a custom time to live.
     *
     * @param id - the id of the file to download
     * @param ttl - the custom ttl to use for the download URL
     * @param out - where to download the file
     * @param listener - for progress notifications
     */
    public void downloadWithTTL(String id, int ttl, OutputStream out, DownloaderProgressListener listener){
        this.setDownloaderProgressListener(listener);
        new FileQueryWithIDDownload(id, ttl, out, listener).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param metadata the metadata of the file
     * @param callback an implementation of a client callback to get results on the UI thread from the async call.
     */
    public void delete(FileMetaData metadata, KinveyClientCallback<KinveyDeleteResponse> callback) {
        new FileDelete(metadata.getId(), callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }




    /**
     * Downloads just the metadata of a file
     *
     * @param id the metadata of the file
     * @param callback an implementation of a client callback to get results on the UI thread from the async call.
     */
    public void downloadMetaData(String id, KinveyClientCallback<FileMetaData> callback) {
        new DownloadMetaData(id, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }


    /**
     * Upload metadata for an existing file
     *
     * @param meta the {@link FileMetaData} to update
     * @param callback an implementation of a client callback to get results on the UI thread from the async call.
     */
    public void uploadMetaData(FileMetaData meta, KinveyClientCallback<FileMetaData> callback) {
        new UploadMetaData(meta, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    public void clearFileStorage(Context applicationContext){
        deleteRecursive(applicationContext.getFilesDir());

    }

    private void deleteRecursive(java.io.File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()){
            for (java.io.File child : fileOrDirectory.listFiles()){
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }


    private class FileUpload extends AsyncClientRequest<FileMetaData> {

        private java.io.File file;
//        private byte [] byteContent;
        private InputStream inputStream;

        private FileMetaData meta;

        public FileUpload(FileMetaData meta, java.io.File file, KinveyClientCallback callback) {
            super(callback);
            Preconditions.checkNotNull(file, "file must not be null");
            this.file = file;
            this.meta = meta;
        }

        //TODO somebody might want this someday
//        public FileUpload(FileMetaData meta, byte [] byteContent, KinveyClientCallback callback) {
//            super(callback);
//            Preconditions.checkNotNull(byteContent, "byteContent must not be null");
//            Preconditions.checkNotNull(meta, "metadata must not be null");
//            this.byteContent = byteContent;
//            this.meta = meta;
//
//        }

        public FileUpload(FileMetaData meta, InputStream inputStream, KinveyClientCallback callback) {
            super(callback);
            Preconditions.checkNotNull(inputStream, "byteContent must not be null");
            Preconditions.checkNotNull(meta, "metadata must not be null");
            this.inputStream = inputStream;
            this.meta = meta;

        }

        @Override
        protected FileMetaData executeAsync() throws IOException {

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
            return AsyncFile.this.uploadBlocking(meta, mediaContent).execute();
        }
    }


    private class FileDownload extends AsyncClientRequest<Void> {

        private FileMetaData meta;
        private OutputStream out;

        public FileDownload(FileMetaData metaData, OutputStream out, KinveyClientCallback callback) {
            super(callback);
            this.meta = metaData;
            this.out = out;
        }

        @Override
        protected Void executeAsync() throws IOException {
            AsyncFile.this.downloadBlocking(meta).executeAndDownloadTo(out);
            return null;
        }

    }
//
    private class FileQueryDownload extends AsyncClientRequest<Void>{

        private OutputStream out;
        private Query query;

        public FileQueryDownload(Query query, OutputStream out, KinveyClientCallback callback) {
            super(callback);
            this.query = query;
            this.out = out;
        }

        @Override
        protected Void executeAsync() throws IOException {
            AsyncFile.this.downloadBlocking(query).executeAndDownloadTo(out);
            return null;
        }

    }

    private class FileQueryWithIDDownload extends AsyncClientRequest<Void>{

        private OutputStream out;
        private String id;
        private int ttl;

        public FileQueryWithIDDownload(String id, int ttl, OutputStream out, KinveyClientCallback callback){
            super(callback);
            this.id = id;
            this.out = out;
            this.ttl = ttl;
        }

        @Override
        protected Void executeAsync() throws IOException {
            AsyncFile.this.downloadWithTTLBlocking(this.id, this.ttl).executeAndDownloadTo(out);
            return null;
        }


    }


    private class FileDelete extends AsyncClientRequest<KinveyDeleteResponse> {
     //TODO uncomment out delete by query support when it is added to kcs

        private String id = null;
//        private Query q = null;

        public FileDelete(String id, KinveyClientCallback callback) {
            super(callback);
            this.id = id;
        }

//        public FileDelete(Query query, KinveyClientCallback callback){
//            super(callback);
//            this.q = query;
//        }

        @Override
        protected KinveyDeleteResponse executeAsync() throws IOException {
//            if (id != null){
                return AsyncFile.this.deleteBlockingById(id).execute();
//            }else{
//                return AsyncFile.this.deleteBlocking(q).execute();
//            }
        }
    }



    private class UploadMetaData extends AsyncClientRequest<FileMetaData>{
        private FileMetaData meta;

        public UploadMetaData(FileMetaData metaData, KinveyClientCallback callback) {
            super(callback);
            this.meta = metaData;
        }

        @Override
        protected FileMetaData executeAsync() throws IOException {
            return AsyncFile.this.uploadMetaDataBlocking(meta).execute();
        }

    }

    private class DownloadMetaData extends AsyncClientRequest<FileMetaData>{
        private String id;

        public DownloadMetaData(String id, KinveyClientCallback callback) {
            super(callback);
            this.id = id;
        }

        @Override
        protected FileMetaData executeAsync() throws IOException {
            return AsyncFile.this.downloadMetaDataBlocking(id).execute();
        }

    }



}
/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyHeaders;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.model.KinveyMetaData;
import com.kinvey.java.model.UriLocResponse;
import com.kinvey.java.query.AbstractQuery;

/**
 * Wraps the {@link com.kinvey.java.File} public methods in asynchronous functionality using native Android AsyncTask.
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
 * {@code
 *    mKinveyClient.file().uploadBlocking("myFileName.txt", file,  new UploaderProgressListener() {
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
 * }
 *
 * </pre>
 *
 * </p>
 * @author edwardf
 * @since 2.4
 */
public abstract class File {

    /** the client for this api **/
    private AbstractClient client;

    /** the upload request listener, can be {@code null} if the calling code has not explicitly set it **/
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
     *
     * @return an instance of a client associated with this instance of File
     */
    protected AbstractClient getClient(){
        return this.client;
    }
    /**
     * Uploads a given file and its contents to the Kinvey file service.
     *
     * @param fileMetaData Metadata object about the file to uplaod
     * @param content  the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     */
    public UploadMetadataAndFile uploadBlocking(FileMetaData fileMetaData, AbstractInputStreamContent content) throws IOException {
        Preconditions.checkNotNull(fileMetaData, "file meta data cannot be null");
        AppData.SaveMode mode;
        if (fileMetaData.containsKey(AppData.ID_FIELD_NAME)){
            mode = AppData.SaveMode.PUT;
        }else{
            mode = AppData.SaveMode.POST;
        }

        UploadMetadataAndFile upload = new UploadMetadataAndFile(fileMetaData, mode, content, uploadProgressListener);

        client.initializeRequest(upload);
        upload.getRequestHeaders().put("x-Kinvey-content-type","application/octet-stream" );
        return upload;
    }



    /**
     * Uploads a given file and its contents to the Kinvey file service.
     *
     * @param fileName the filename used for the metadata
     * @param content the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     */
    public UploadMetadataAndFile uploadBlocking(String fileName, AbstractInputStreamContent content) throws IOException {
        FileMetaData meta = new FileMetaData();
        if (fileName != null){
            meta.setFileName(fileName);
        }
        return this.uploadBlocking(meta, content);
    }

    /**
     * Download a given file from the Kinvey file service.
     * <p>
     *
     * @param metaData the metadata of the file
     * @return a valid request to be executed for the download operation from Kinvey file service
     * @throws IOException
     */
    public DownloadMetadataAndFile downloadBlocking(FileMetaData metaData) throws IOException {
        DownloadMetadataAndFile download = new DownloadMetadataAndFile(metaData, downloaderProgressListener);
        client.initializeRequest(download);
        download.getRequestHeaders().put("x-Kinvey-content-type","application/octet-stream" );
        return download;
    }

    /**
     * Query for files to download
     *
     *
     * @param q the query to execute for file metadata
     * @return a valid request to be executed
     * @throws IOException
     */
    public DownloadMetadataAndFileQuery downloadBlocking(Query q) throws IOException {
        DownloadMetadataAndFileQuery download = new DownloadMetadataAndFileQuery(null, q, downloaderProgressListener);
        client.initializeRequest(download);
        download.getRequestHeaders().put("x-Kinvey-content-type","application/octet-stream" );
        return download;

    }

    /**
     * Attach query parameters when requesting metadata for a specific file.
     *
     * Use this method to specify a custom time to live.
     *
     * <p>
     * Sample usage:
     * <pre>
     * {@code
     *
     *    Query q = new Query();
     *    q.equals("ttl_in_seconds", 3600);  //set a new ttl for the download URL
     *    mKinveyClient.file().downloadBlocking("myFileName.txt", q,  new UploaderProgressListener() {
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
     * }
     *
     *
     *
     * @param id - the unique id of the file
     * @param q - the query to execute
     * @return a valid download request ready to be executed
     * @throws IOException
     */
    public DownloadMetadataAndFileQuery downloadBlocking(String id, Query q) throws IOException {

        DownloadMetadataAndFileQuery download = new DownloadMetadataAndFileQuery(id, q, downloaderProgressListener);
        client.initializeRequest(download);
        download.getRequestHeaders().put("x-Kinvey-content-type","application/octet-stream" );
        return download;

    }


    /**
     * This method performs a query to find a file by it's filename.
     *
     * As Kinvey File now supports non-unique file names, this method will only return a single file with this name.
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public DownloadMetadataAndFileQuery downloadBlocking(String filename) throws IOException{
        Query q = new Query();
        q.equals("_filename", filename);
//        q.setLimit(1);
        q.addSort("_kmd.lmt", Query.SortOrder.DESC);
        DownloadMetadataAndFileQuery download = new DownloadMetadataAndFileQuery(null, q, downloaderProgressListener);
        client.initializeRequest(download);
        download.getRequestHeaders().put("x-Kinvey-content-type","application/octet-stream" );
        return download;

    }


    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param metaData the metadata of the File to delete (requires an ID)
     * @return a valid DELETE request to be executed
     * @throws IOException
     */
    public DeleteFile deleteBlocking(FileMetaData metaData) throws IOException {
        DeleteFile delete = new DeleteFile(metaData);
        client.initializeRequest(delete);
        return delete;
    }

    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param id the metadata of the File to delete (requires an ID)
     * @return a valid DELETE request to be executed
     * @throws IOException
     */
    public DeleteFile deleteBlockingById(String id) throws IOException {
        DeleteFile delete = new DeleteFile(id);
        client.initializeRequest(delete);
        return delete;
    }


    /**
     * Uploads metadata for a file, without modifying the file iteself.
     *
     * @param metaData the metadata of the File to upload
     * @return a valid PUT or POST request to be executed
     * @throws IOException
     */
    public UploadMetadata uploadMetaDataBlocking(FileMetaData metaData) throws IOException{
        AppData.SaveMode mode;
        if (metaData.containsKey(AppData.ID_FIELD_NAME)){
            mode = AppData.SaveMode.PUT;
        }else{
            mode = AppData.SaveMode.POST;
        }
        UploadMetadata upload = new UploadMetadata(metaData, mode);
        client.initializeRequest(upload);
        return upload;
    }



    /**
     * Downloads metadata for a file, without returning the file iteself.
     *
     * @param id the id of the metadata
     * @return a valid PUT or POST request to be executed
     * @throws IOException
     */
    public DownloadMetadata downloadMetaDataBlocking(String id) throws IOException{
        DownloadMetadata download = new DownloadMetadata(id);
        client.initializeRequest(download);
        return download;
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

    //----------------------------------------------------Client Requests

    /**
     * This class uploads a {@link FileMetaData} object to Kinvey, returning another {@link FileMetaData} containing the upload URL
     *
     *
     */
    public class UploadMetadataAndFile extends AbstractKinveyJsonClientRequest<FileMetaData>{
        private final static String REST_URL = "blob/{appKey}/{id}";

        @Key
        private String id;

        private UploadMetadataAndFile(FileMetaData meta, AppData.SaveMode verb, AbstractInputStreamContent mediaContent, UploaderProgressListener progressListener) {
            super(client, verb.toString(), REST_URL, meta, FileMetaData.class);
            initializeMediaHttpUploader(mediaContent, progressListener);
            if (verb.equals(AppData.SaveMode.PUT)){
                this.id = Preconditions.checkNotNull(meta.getId());
            }
            this.getRequestHeaders().set("x-Kinvey-content-type", "application/octet-stream");
        }
    }

    /**
     *  This class will upload new file metadata without actually effecting the file
     *
     *  Note it is not recommended to change the filename without ensuring a file exists with the new name.
     */
    public class UploadMetadata extends AbstractKinveyJsonClientRequest<FileMetaData>{
        private final static String REST_URL = "blob/{appKey}/{id}";

        @Key
        private String id;

        private UploadMetadata(FileMetaData meta, AppData.SaveMode verb) {
            super(client, verb.toString(), REST_URL, meta, FileMetaData.class);
            if (verb.equals(AppData.SaveMode.PUT)){
                this.id = Preconditions.checkNotNull(meta.getId());
            }
        }
    }

    /**
     *  This class will upload new file metadata without actually effecting the file
     *
     *  Note it is not recommended to change the filename without ensuring a file exists with the new name.
     */
    public class DownloadMetadata extends AbstractKinveyJsonClientRequest<FileMetaData>{
        private static final String REST_URL = "blob/{appKey}/{id}";

        @Key
        private String id;

        private DownloadMetadata(String id) {
            super(client, "GET", REST_URL, null, FileMetaData.class);
            this.id = id;
        }
    }


    /**
     * This class gets a {@link FileMetaData} object from Kinvey, and then downloads the associated File
     */
    public class DownloadMetadataAndFile extends AbstractKinveyJsonClientRequest<FileMetaData> {

        private final static String REST_URL = "blob/{appKey}/{id}";

        @Key
        private String id;


        private DownloadMetadataAndFile(FileMetaData meta, DownloaderProgressListener progressListener) {
            super(client, "GET", REST_URL, null, FileMetaData.class);
            initializeMediaHttpDownloader(progressListener);
            this.id = Preconditions.checkNotNull(meta.getId());
        }

    }

    /**
     * This class gets a {@link FileMetaData} object from Kinvey, and then downloads the associated File
     */
    public class DownloadMetadataAndFileQuery extends AbstractKinveyJsonClientRequest<FileMetaData[]> {

        private final static String REST_URL = "blob/{appKey}/{id} + {?query}";

        @Key("query")
        private String queryFilter;

        @Key("id")
        private String id;


        private DownloadMetadataAndFileQuery(String id, Query query, DownloaderProgressListener progressListener){
            super(client, "GET", REST_URL, null, FileMetaData[].class);
            initializeMediaHttpDownloader(progressListener);
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            this.id = id;
        }
    }



    public class DeleteFile extends AbstractKinveyJsonClientRequest<KinveyDeleteResponse>{

        private final static String REST_URL = "blob/{appKey}/{id}";// + "{?query}";

        @Key
        private String id;
//        @Key("query")
//        private String queryFilter;

        public DeleteFile(FileMetaData metaData){
            super(client, "DELETE", REST_URL, null, KinveyDeleteResponse.class);
            this.id = Preconditions.checkNotNull(metaData.getId(), "cannot delete a file without an _id!");
        }

        //TODO edwardf re-add delete by query support once it is supported in kcs
//        public DeleteFile(Query q){
//            super(client, "DELETE", REST_URL, null, KinveyDeleteResponse.class);
//            this.queryFilter = q.getQueryFilterJson(client.getJsonFactory());
//
//        }

        public DeleteFile(String id){
            super(client, "DELETE", REST_URL, null, KinveyDeleteResponse.class);
            this.id = Preconditions.checkNotNull(id);;


        }

    }





}

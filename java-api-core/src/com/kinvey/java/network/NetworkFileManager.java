/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
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

package com.kinvey.java.network;


import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.MimeTypeFinder;
import com.kinvey.java.Query;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.model.KinveyDeleteResponse;

/**
 * Wraps the {@link NetworkFileManager} public methods in asynchronous functionality using native Android AsyncTask.
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
 *            //successfully upload file
 *        }
 *        @Override
 *        public void onFailure(Throwable error) {
 *            //failed to upload file.", error
 *        }
 *        @Override
 *        public void progressChanged(MediaHttpUploader uploader) throws IOException {
 *            //upload progress: " + uploader.getUploadState()
 *            switch (uploader.getUploadState()) {
 *                case INITIATION_STARTED:
 *                    //Initiation Started
 *                    break;
 *                case INITIATION_COMPLETE:
 *                    //Initiation Completed
 *                    break;
 *                case DOWNLOAD_IN_PROGRESS:
 *                    //Upload in progress
 *                    //Upload percentage: + uploader.getProgress()
 *                    break;
 *                case DOWNLOAD_COMPLETE:
 *                    //Upload Completed!;
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
public class NetworkFileManager {

    /** the client for this api **/
    private AbstractClient client;

    /** the upload request listener, can be {@code null} if the calling code has not explicitly set it **/
    private UploaderProgressListener uploadProgressListener;

    /** the download request listener, can be {@code null} if the call code has not set it **/
    private DownloaderProgressListener downloaderProgressListener;

    /** Used to calculate the MimeType of files **/
    protected MimeTypeFinder mimeTypeFinder;


    private String clientAppVersion = null;
    
    private GenericData customRequestProperties = new GenericData();

    public void setClientAppVersion(String appVersion){
    	this.clientAppVersion = appVersion;	
    }
    
    public void setClientAppVersion(int major, int minor, int revision){
    	setClientAppVersion(major + "." + minor + "." + revision);
    }
    
    public void setCustomRequestProperties(GenericJson customheaders){
    	this.customRequestProperties = customheaders;
    }
    
    public void setCustomRequestProperty(String key, Object value){
    	if (this.customRequestProperties == null){
    		this.customRequestProperties = new GenericJson();
    	}
    	this.customRequestProperties.put(key, value);
    }
    
    public void clearCustomRequestProperties(){
    	this.customRequestProperties = new GenericJson();
    }



    /**
     * Calculate and set metadata for file upload request according FileMetadata
     * @param metaData
     * @param request
     */
    private void setUploadHeader(FileMetaData metaData, AbstractKinveyJsonClientRequest<?> request){
        if (metaData != null) {
            if (metaData.getMimetype() == null) {
                if (mimeTypeFinder != null) {
                    mimeTypeFinder.getMimeType(metaData);
                }
            }
            if (metaData.getMimetype() == null){
                metaData.setMimetype("application/octet-stream");
            }

            request.getRequestHeaders().put("x-Kinvey-content-type", metaData.getMimetype());

        }else {
            request.getRequestHeaders().put("x-Kinvey-content-type", "application/octet-stream");
        }
    }


    /**
     * Base constructor requires the client instance to be passed in.
     * <p>
     * {@link com.kinvey.java.core.AbstractKinveyClient#initializeRequest(com.kinvey.java.core.AbstractKinveyClientRequest)} is used to initialize all requests
     * constructed by this api.
     * </p>
     * @param client required instance
     * @throws NullPointerException if the client parameter is non-null
     */
    public NetworkFileManager(AbstractClient client) {
        this.client = Preconditions.checkNotNull(client);
        this.clientAppVersion = client.getClientAppVersion();
        this.customRequestProperties = client.getCustomRequestProperties();
    }

    /**
     * Set a {@code MimeTypeFinder}
     *
     * @param finder an implementaiton of a {@code MimeTypeFinder} to use
     */
    protected void setMimeTypeManager(MimeTypeFinder finder){
        this.mimeTypeFinder = finder;
    }
    /**
     *
     * @return an instance of a client associated with this instance of NetworkFileManager
     */
    public AbstractClient getClient(){
        return this.client;
    }
    
    /**
     * Prepares a request to upload a given file and its contents to the Kinvey file service.
     *
     * @param fileMetaData Metadata object about the file to uplaod
     * @param content  the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     */
    public UploadMetadataAndFile prepUploadBlocking(FileMetaData fileMetaData, AbstractInputStreamContent content,
                                                    UploaderProgressListener uploadProgressListener) throws IOException {
        Preconditions.checkNotNull(fileMetaData, "file meta data cannot be null");
        NetworkManager.SaveMode mode;
        if (fileMetaData.containsKey(NetworkManager.ID_FIELD_NAME)){
            mode = NetworkManager.SaveMode.PUT;
        }else{
            mode = NetworkManager.SaveMode.POST;
        }

        UploadMetadataAndFile upload = new UploadMetadataAndFile(fileMetaData, mode, content, uploadProgressListener);

        client.initializeRequest(upload);

        return upload;
    }
    
    /**
     * Uploads a given file and its contents to the Kinvey file service.
     *
     * @param fileMetaData Metadata object about the file to uplaod
     * @param content  the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     * @deprecated use upload methods which take an `InputStream` or a `NetworkFileManager`
     */
    public UploadMetadataAndFile uploadBlocking(FileMetaData fileMetaData, AbstractInputStreamContent content,
                                                UploaderProgressListener uploadProgressListener) throws IOException {
        Preconditions.checkNotNull(fileMetaData, "file meta data cannot be null");
        NetworkManager.SaveMode mode;
        if (fileMetaData.containsKey(NetworkManager.ID_FIELD_NAME)){
            mode = NetworkManager.SaveMode.PUT;
        }else{
            mode = NetworkManager.SaveMode.POST;
        }

        UploadMetadataAndFile upload = new UploadMetadataAndFile(fileMetaData, mode, content, uploadProgressListener);

        client.initializeRequest(upload);

        return upload;
    }



    /**
     * Prepares a request to upload a given file and its contents to the Kinvey file service.
     *
     * @param fileName the filename used for the metadata
     * @param content the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     */
    public UploadMetadataAndFile prepUploadBlocking(String fileName, AbstractInputStreamContent content,
                                                    UploaderProgressListener uploadProgressListener) throws IOException {
        FileMetaData meta = new FileMetaData();
        if (fileName != null){
            meta.setFileName(fileName);
        }
        return this.prepUploadBlocking(meta, content, uploadProgressListener);
    }
    
    /**
     * Uploads a given file and its contents to the Kinvey file service.
     *
     * @param fileName the filename used for the metadata
     * @param content the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     * @deprecated use upload methods which take an `InputStream` or a `NetworkFileManager`
     */
    public UploadMetadataAndFile uploadBlocking(String fileName, AbstractInputStreamContent content,
                                                UploaderProgressListener uploadProgressListener) throws IOException {
        FileMetaData meta = new FileMetaData();
        if (fileName != null){
            meta.setFileName(fileName);
        }
        return this.prepUploadBlocking(meta, content, uploadProgressListener);
    }

    /**
     * Prepares a request to download a given file from the Kinvey file service.
     * <p>
     *
     * @param metaData the metadata of the file
     * @return a valid request to be executed for the download operation from Kinvey file service
     * @throws IOException
     */
    public DownloadMetadataAndFile prepDownloadBlocking(FileMetaData metaData) throws IOException {
        DownloadMetadataAndFile download = new DownloadMetadataAndFile(metaData, downloaderProgressListener);
        client.initializeRequest(download);
        return download;
    }
    
    /**
     * Download a given file from the Kinvey file service.
     * <p>
     *
     * @param metaData the metadata of the file
     * @return a valid request to be executed for the download operation from Kinvey file service
     * @throws IOException
     * @deprecated use the download methods which take an `Outputstream` or a `NetworkFileManager`
     */
    public DownloadMetadataAndFile downloadBlocking(FileMetaData metaData) throws IOException {
        DownloadMetadataAndFile download = new DownloadMetadataAndFile(metaData, downloaderProgressListener);
        client.initializeRequest(download);
        return download;
    }

    /**
     * Prepares a request to Query for files to download
     *
     * @param q the query to execute for file metadata
     * @return a valid request to be executed
     * @throws IOException
     */
    public DownloadMetadataQuery prepDownloadBlocking(Query q) throws IOException {
        DownloadMetadataQuery download = new DownloadMetadataQuery(null, q);
        client.initializeRequest(download);
        return download;

    }
    
    /**
     * Query for files to download
     *
     * @param q the query to execute for file metadata
     * @return a valid request to be executed
     * @throws IOException
     * @deprecated use the download methods which take an `Outputstream` or a `NetworkFileManager`
     */
    public DownloadMetadataQuery downloadBlocking(Query q) throws IOException {
        DownloadMetadataQuery download = new DownloadMetadataQuery(null, q);
        client.initializeRequest(download);
        return download;

    }

    /**
     * Prepares a request to attach query parameters when requesting metadata for a specific file.
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
     *    OutputStream out = new ByteArrayOutputStream(...);
     *    mKinveyClient.file().downloadBlocking("myFileName.txt", q).executeAndDownloadTo(out);
     * }
     *
     *
     *
     * @param id - the unique id of the file
     * @param q - the query to execute
     * @return a valid download request ready to be executed
     * @throws IOException
     */
    public DownloadMetadataQuery prepDownloadBlocking(String id, Query q) throws IOException {

        DownloadMetadataQuery download = new DownloadMetadataQuery(id, q);
        client.initializeRequest(download);
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
     *    OutputStream out = new ByteArrayOutputStream(...);
     *    mKinveyClient.file().downloadBlocking("myFileName.txt", q).executeAndDownloadTo(out);
     * }
     *
     *
     *
     * @param id - the unique id of the file
     * @param q - the query to execute
     * @return a valid download request ready to be executed
     * @throws IOException
     * @deprecated use the download methods which take an `Outputstream` or a `NetworkFileManager`
     */
    public DownloadMetadataQuery downloadBlocking(String id, Query q) throws IOException {

        DownloadMetadataQuery download = new DownloadMetadataQuery(id, q);
        client.initializeRequest(download);
        return download;

    }

    /**
     * Prepares a request to download a file with a custom Time-To-Live
     *
     * <p>
     * Sample usage:
     * <pre>
     * {@code
     *
     *    OutputStream out = new ByteArrayOutputStream(...);
     *    mKinveyClient.file().downloadBlocking("myFileName.txt", 3600).executeAndDownloadTo(out);
     * }
     *
     *
     *
     * @param id - the unique _id of the file to download
     * @param ttl - a custom TTL, in milliseconds
     * @return a {@link DownloadMetadataQuery} request ready to be executed.
     * @throws IOException
     */
    public DownloadMetadataQuery prepDownloadWithTTLBlocking(String id, int ttl) throws IOException{
        Query q = new Query();
        q.equals("ttl_in_seconds", ttl);
        return prepDownloadBlocking(id, q);
    }
    
    /**
     * Download a file with a custom Time-To-Live
     *
     * <p>
     * Sample usage:
     * <pre>
     * {@code
     *
     *    OutputStream out = new ByteArrayOutputStream(...);
     *    mKinveyClient.file().downloadBlocking("myFileName.txt", 3600).executeAndDownloadTo(out);
     * }
     *
     *
     *
     * @param id - the unique _id of the file to download
     * @param ttl - a custom TTL, in milliseconds
     * @return a {@link DownloadMetadataQuery} request ready to be executed.
     * @throws IOException
     * @deprecated use the download methods which take an `Outputstream` or a `NetworkFileManager`
     */
    public DownloadMetadataQuery downloadWithTTLBlocking(String id, int ttl) throws IOException{
        Query q = new Query();
        q.equals("ttl_in_seconds", ttl);
        return prepDownloadBlocking(id, q);
    }


    /**
     * Prepares a request to query to find a file by it's filename.
     *
     * As Kinvey NetworkFileManager now supports non-unique file names, this method will only return a single file with this name.
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public DownloadMetadataQuery prepDownloadBlocking(String filename) throws IOException{
        Query q = new Query();
        q.equals("_filename", filename);
        q.addSort("_kmd.lmt", Query.SortOrder.DESC);
        DownloadMetadataQuery download = new DownloadMetadataQuery(null, q);
        client.initializeRequest(download);
        download.getRequestHeaders().put("x-Kinvey-content-type","application/octet-stream" );
        return download;

    }
    
    /**
     * This method performs a query to find a file by it's filename.
     *
     * As Kinvey NetworkFileManager now supports non-unique file names, this method will only return a single file with this name.
     *
     * @param filename
     * @return
     * @throws IOException
     * @deprecated use the download methods which take an `Outputstream` or a `NetworkFileManager`
     */
    public DownloadMetadataQuery downloadBlocking(String filename) throws IOException{
        Query q = new Query();
        q.equals("_filename", filename);
        q.addSort("_kmd.lmt", Query.SortOrder.DESC);
        DownloadMetadataQuery download = new DownloadMetadataQuery(null, q);
        client.initializeRequest(download);
        download.getRequestHeaders().put("x-Kinvey-content-type","application/octet-stream" );
        return download;

    }


    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param metaData the metadata of the NetworkFileManager to remove (requires an ID)
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
     * @param fileID the _id of the file to remove
     * @return a valid DELETE request to be executed
     * @throws IOException
     */
    public DeleteFile deleteBlocking(String fileID) throws IOException {
    	return deleteBlocking(new FileMetaData(fileID));
    }


    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param id the metadata of the NetworkFileManager to remove (requires an ID)
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
     * @param metaData the metadata of the NetworkFileManager to upload
     * @return a valid PUT or POST request to be executed
     * @throws IOException
     */
    public UploadMetadata uploadMetaDataBlocking(FileMetaData metaData) throws IOException{
        NetworkManager.SaveMode mode;
        if (metaData.containsKey(NetworkManager.ID_FIELD_NAME)){
            mode = NetworkManager.SaveMode.PUT;
        }else{
            mode = NetworkManager.SaveMode.POST;
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

        private MediaHttpUploader uploader;

        private UploadMetadataAndFile(FileMetaData meta, NetworkManager.SaveMode verb, AbstractInputStreamContent mediaContent, UploaderProgressListener progressListener) {
            super(client, verb.toString(), REST_URL, meta, FileMetaData.class);
            initializeMediaHttpUploader(mediaContent, progressListener);
            if (verb.equals(NetworkManager.SaveMode.PUT)){
                this.id = Preconditions.checkNotNull(meta.getId());
            }
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkFileManager.this.clientAppVersion);
            if (NetworkFileManager.this.customRequestProperties != null && !NetworkFileManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkFileManager.this.customRequestProperties) );
            }

            setUploadHeader(meta, this);
        }

        @Override
        public FileMetaData execute() throws IOException {
            return uploader.upload(this);
        }

        /**
         * Sets up this request object to be used for uploading media.
         *
         * @param content data to be uploaded
         * @param progressListener an object to be notified of the different state changes as the upload progresses.
         *                         Optional {@code null} can be passed in.
         */
        protected void initializeMediaHttpUploader(AbstractInputStreamContent content, UploaderProgressListener progressListener) {
            HttpRequestFactory requestFactory = client.getRequestFactory();
            uploader = createMediaHttpUploader(content, requestFactory);
            uploader.setDirectUploadEnabled(false);
            uploader.setProgressListener(progressListener);
        }
        /**
         * Factory to instantiate a new http uploader object during the {@link #initializeMediaHttpUploader(com.google.api.client.http.AbstractInputStreamContent, UploaderProgressListener)}
         *
         * @param content data to be uploaded
         * @param requestFactory request factory to be used
         * @return a valid http uploader with default settings
         */
        protected MediaHttpUploader createMediaHttpUploader(AbstractInputStreamContent content, HttpRequestFactory requestFactory) {
            return new MediaHttpUploader(content, requestFactory.getTransport(), requestFactory.getInitializer());
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

        private UploadMetadata(FileMetaData meta, NetworkManager.SaveMode verb) {
            super(client, verb.toString(), REST_URL, meta, FileMetaData.class);
            if (verb.equals(NetworkManager.SaveMode.PUT)){
                this.id = Preconditions.checkNotNull(meta.getId());
            }
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkFileManager.this.clientAppVersion);
            if (NetworkFileManager.this.customRequestProperties != null && !NetworkFileManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkFileManager.this.customRequestProperties) );
            }
            setUploadHeader(meta, this);
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
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkFileManager.this.clientAppVersion);
            if (NetworkFileManager.this.customRequestProperties != null && !NetworkFileManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkFileManager.this.customRequestProperties) );
            }
        }
        
    }


    /**
     * This class gets a {@link FileMetaData} object from Kinvey, and then downloads the associated NetworkFileManager
     */
    public class DownloadMetadataAndFile extends AbstractKinveyJsonClientRequest<FileMetaData> {

        private final static String REST_URL = "blob/{appKey}/{id}";

        @Key
        private String id;


        private DownloadMetadataAndFile(FileMetaData meta, DownloaderProgressListener progressListener) {
            super(client, "GET", REST_URL, null, FileMetaData.class);
            this.id = Preconditions.checkNotNull(meta.getId());
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkFileManager.this.clientAppVersion);
            if (NetworkFileManager.this.customRequestProperties != null && !NetworkFileManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkFileManager.this.customRequestProperties) );
            }
            setUploadHeader(meta, this);
        }

    }

    /**
     * This class gets a {@link FileMetaData} object from Kinvey, and then downloads the associated NetworkFileManager
     */
    public class DownloadMetadataQuery extends AbstractKinveyJsonClientRequest<FileMetaData[]> {

        //private final static String REST_URL = "blob/{appKey}/{id}" + "?query={query}";
        private final static String REST_URL = "blob/{appKey}/{id}" + "{?query,sort,limit,skip}";

        @Key("id")
        private String id;
        
        @Key("query")
        private String queryFilter;
        @Key("sort")
        private String sortFilter;
        @Key("limit")
        private String limit;
        @Key("skip")
        private String skip;


        private DownloadMetadataQuery(String id, Query query){
            super(client, "GET", REST_URL, null, FileMetaData[].class);
            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
            int queryLimit = query.getLimit();
            int querySkip = query.getSkip();
            this.limit = queryLimit > 0 ? Integer.toString(queryLimit) : null;
            this.skip = querySkip > 0 ? Integer.toString(querySkip) : null;
            String sortString = query.getSortString();
            this.sortFilter = !(sortString.equals("")) ? sortString : null;
            this.id = id;
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkFileManager.this.clientAppVersion);
            if (NetworkFileManager.this.customRequestProperties != null && !NetworkFileManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkFileManager.this.customRequestProperties) );
            }
            getRequestHeaders().put("x-Kinvey-content-type","application/octet-stream" );
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
            this.id = Preconditions.checkNotNull(metaData.getId(), "cannot remove a file without an _id!");
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkFileManager.this.clientAppVersion);
            if (NetworkFileManager.this.customRequestProperties != null && !NetworkFileManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkFileManager.this.customRequestProperties) );
            }
            setUploadHeader(metaData, this);
        }

        //TODO edwardf re-add remove by query support once it is supported in kcs
//        public DeleteFile(Query q){
//            super(client, "DELETE", REST_URL, null, KinveyDeleteResponse.class);
//            this.queryFilter = q.getQueryFilterJson(client.getJsonFactory());
//
//        }

        public DeleteFile(String id){
            super(client, "DELETE", REST_URL, null, KinveyDeleteResponse.class);
            this.id = Preconditions.checkNotNull(id);
            this.getRequestHeaders().put("X-Kinvey-Client-App-Version", NetworkFileManager.this.clientAppVersion);
            if (NetworkFileManager.this.customRequestProperties != null && !NetworkFileManager.this.customRequestProperties.isEmpty()){
            	this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(NetworkFileManager.this.customRequestProperties) );
            }


        }

    }





}

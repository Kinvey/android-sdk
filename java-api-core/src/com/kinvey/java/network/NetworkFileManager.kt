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

package com.kinvey.java.network


import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.json.GenericJson
import com.google.api.client.util.GenericData
import com.google.api.client.util.Key
import com.google.common.base.Preconditions
import com.google.gson.Gson

import java.io.IOException

import com.kinvey.java.AbstractClient
import com.kinvey.java.MimeTypeFinder
import com.kinvey.java.Query
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.core.DownloaderProgressListener
import com.kinvey.java.core.MediaHttpUploader
import com.kinvey.java.core.UploaderProgressListener
import com.kinvey.java.model.FileMetaData
import com.kinvey.java.model.KinveyDeleteResponse
import com.kinvey.java.network.NetworkManager.ID_FIELD_NAME
import com.kinvey.java.query.AbstractQuery

/**
 * Wraps the [NetworkFileManager] public methods in asynchronous functionality using native Android AsyncTask.
 *
 *
 *
 * This class is constructed via [com.kinvey.java.AbstractClient.file] factory method.
 *
 *
 *
 *
 * The callback mechanism for this api is extended to include the [UploaderProgressListener.progressChanged]
 * method, which receives notifications as the upload process transitions through and progresses with the upload.
 * process.
 *
 *
 *
 *
 * Sample usage:
 * <pre>
 * `mKinveyClient.file().uploadBlocking("myFileName.txt", file,  new UploaderProgressListener() {
 *
 * public void onSuccess(Void result) {
 * //successfully upload file
 * }
 *
 * public void onFailure(Throwable error) {
 * //failed to upload file.", error
 * }
 *
 * public void progressChanged(MediaHttpUploader uploader) throws IOException {
 * //upload progress: " + uploader.getUploadState()
 * switch (uploader.getUploadState()) {
 * case INITIATION_STARTED:
 * //Initiation Started
 * break;
 * case INITIATION_COMPLETE:
 * //Initiation Completed
 * break;
 * case DOWNLOAD_IN_PROGRESS:
 * //Upload in progress
 * //Upload percentage: + uploader.getProgress()
 * break;
 * case DOWNLOAD_COMPLETE:
 * //Upload Completed!;
 * break;
 * }
 * });
 * }
 *
 * </pre>
 *
 * </p>
 * edwardf
 * 2.4`
</pre> */
open class NetworkFileManager
/**
 * Base constructor requires the client instance to be passed in.
 *
 *
 * [com.kinvey.java.core.AbstractKinveyClient.initializeRequest] is used to initialize all requests
 * constructed by this api.
 *
 * @param client required instance
 * @throws NullPointerException if the client parameter is non-null
 */
(client: AbstractClient<*>) {

    /** the client for this api  */
    /**
     *
     * @return an instance of a client associated with this instance of NetworkFileManager
     */
    val client: AbstractClient<*>

    /** the upload request listener, can be `null` if the calling code has not explicitly set it  */
    private var uploadProgressListener: UploaderProgressListener? = null

    /** the download request listener, can be `null` if the call code has not set it  */
    private var downloaderProgressListener: DownloaderProgressListener? = null

    /** Used to calculate the MimeType of files  */
    protected var mimeTypeFinder: MimeTypeFinder? = null


    private var clientAppVersion: String? = null

    private var customRequestProperties: GenericData? = GenericData()

    fun setClientAppVersion(appVersion: String) {
        this.clientAppVersion = appVersion
    }

    fun setClientAppVersion(major: Int, minor: Int, revision: Int) {
        setClientAppVersion("$major.$minor.$revision")
    }

    fun setCustomRequestProperties(customheaders: GenericJson) {
        this.customRequestProperties = customheaders
    }

    fun setCustomRequestProperty(key: String, value: Any) {
        if (this.customRequestProperties == null) {
            this.customRequestProperties = GenericJson()
        }
        this.customRequestProperties!![key] = value
    }

    fun clearCustomRequestProperties() {
        this.customRequestProperties = GenericJson()
    }


    /**
     * Calculate and set metadata for file upload request according FileMetadata
     * @param metaData
     * @param request
     */
    protected fun setUploadHeader(metaData: FileMetaData?, request: AbstractKinveyJsonClientRequest<*>) {
        if (metaData != null) {
            if (metaData.mimetype == null) {
                if (mimeTypeFinder != null) {
                    mimeTypeFinder!!.getMimeType(metaData)
                }
            }
            if (metaData.mimetype == null) {
                metaData.mimetype = "application/octet-stream"
            }

            request.getRequestHeaders()["x-Kinvey-content-type"] = metaData.mimetype

        } else {
            request.getRequestHeaders()["x-Kinvey-content-type"] = "application/octet-stream"
        }
    }


    init {
        this.client = Preconditions.checkNotNull(client)
        this.clientAppVersion = client.clientAppVersion
        this.customRequestProperties = client.customRequestProperties
    }

    /**
     * Set a `MimeTypeFinder`
     *
     * @param finder an implementaiton of a `MimeTypeFinder` to use
     */
    protected fun setMimeTypeManager(finder: MimeTypeFinder) {
        this.mimeTypeFinder = finder
    }

    /**
     * Prepares a request to upload a given file and its contents to the Kinvey file service.
     *
     * @param fileMetaData Metadata object about the file to uplaod
     * @param content  the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     */
    @Throws(IOException::class)
    fun prepUploadBlocking(fileMetaData: FileMetaData, content: AbstractInputStreamContent,
                           uploadProgressListener: UploaderProgressListener): UploadMetadataAndFile {
        Preconditions.checkNotNull(fileMetaData, "file meta data cannot be null")
        val mode = if (fileMetaData.containsKey(ID_FIELD_NAME)) {
            NetworkManager.SaveMode.PUT
        } else {
            NetworkManager.SaveMode.POST
        }

        val upload = UploadMetadataAndFile(this, fileMetaData, mode, content, uploadProgressListener)

        client.initializeRequest(upload)

        return upload
    }

    /**
     * Uploads a given file and its contents to the Kinvey file service.
     *
     * @param fileMetaData Metadata object about the file to uplaod
     * @param content  the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     */
    @Deprecated("use upload methods which take an `InputStream` or a `NetworkFileManager`")
    @Throws(IOException::class)
    fun uploadBlocking(fileMetaData: FileMetaData, content: AbstractInputStreamContent,
                       uploadProgressListener: UploaderProgressListener): UploadMetadataAndFile {
        Preconditions.checkNotNull(fileMetaData, "file meta data cannot be null")
        val mode = if (fileMetaData.containsKey(ID_FIELD_NAME)) {
            NetworkManager.SaveMode.PUT
        } else {
            NetworkManager.SaveMode.POST
        }

        val upload = UploadMetadataAndFile(this, fileMetaData, mode, content, uploadProgressListener)

        client.initializeRequest(upload)

        return upload
    }


    /**
     * Prepares a request to upload a given file and its contents to the Kinvey file service.
     *
     * @param fileName the filename used for the metadata
     * @param content the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     */
    @Throws(IOException::class)
    fun prepUploadBlocking(fileName: String?, content: AbstractInputStreamContent,
                           uploadProgressListener: UploaderProgressListener): UploadMetadataAndFile {
        val meta = FileMetaData()
        if (fileName != null) {
            meta.fileName = fileName
        }
        return this.prepUploadBlocking(meta, content, uploadProgressListener)
    }

    /**
     * Uploads a given file and its contents to the Kinvey file service.
     *
     * @param fileName the filename used for the metadata
     * @param content the input stream from which the file contents will be sourced
     * @return a valid request to be executed for the upload operation to Kinvey
     * @throws IOException if initializing the request fails
     */
    @Deprecated("use upload methods which take an `InputStream` or a `NetworkFileManager`")
    @Throws(IOException::class)
    fun uploadBlocking(fileName: String?, content: AbstractInputStreamContent,
                       uploadProgressListener: UploaderProgressListener): UploadMetadataAndFile {
        val meta = FileMetaData()
        if (fileName != null) {
            meta.fileName = fileName
        }
        return this.prepUploadBlocking(meta, content, uploadProgressListener)
    }

    /**
     * Prepares a request to download a given file from the Kinvey file service.
     *
     *
     *
     * @param metaData the metadata of the file
     * @return a valid request to be executed for the download operation from Kinvey file service
     * @throws IOException
     */
    @Throws(IOException::class)
    fun prepDownloadBlocking(metaData: FileMetaData): DownloadMetadataAndFile {
        val download = DownloadMetadataAndFile(this, metaData, downloaderProgressListener)
        client.initializeRequest(download)
        return download
    }

    /**
     * Download a given file from the Kinvey file service.
     *
     *
     *
     * @param metaData the metadata of the file
     * @return a valid request to be executed for the download operation from Kinvey file service
     * @throws IOException
     */
    @Deprecated("use the download methods which take an `Outputstream` or a `NetworkFileManager`")
    @Throws(IOException::class)
    fun downloadBlocking(metaData: FileMetaData): DownloadMetadataAndFile {
        val download = DownloadMetadataAndFile(this, metaData, downloaderProgressListener)
        client.initializeRequest(download)
        return download
    }

    /**
     * Prepares a request to Query for files to download
     *
     * @param q the query to execute for file metadata
     * @return a valid request to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun prepDownloadBlocking(q: Query): DownloadMetadataQuery {
        val download = DownloadMetadataQuery(this, null, q)
        client.initializeRequest(download)
        return download

    }

    /**
     * Query for files to download
     *
     * @param q the query to execute for file metadata
     * @return a valid request to be executed
     * @throws IOException
     */
    @Deprecated("use the download methods which take an `Outputstream` or a `NetworkFileManager`")
    @Throws(IOException::class)
    fun downloadBlocking(q: Query): DownloadMetadataQuery {
        val download = DownloadMetadataQuery(this, null, q)
        client.initializeRequest(download)
        return download

    }

    /**
     * Prepares a request to attach query parameters when requesting metadata for a specific file.
     *
     * Use this method to specify a custom time to live.
     *
     *
     *
     * Sample usage:
     * <pre>
     * `Query q = new Query();
     * q.equals("ttl_in_seconds", 3600);  //set a new ttl for the download URL
     * OutputStream out = new ByteArrayOutputStream(...);
     * mKinveyClient.file().downloadBlocking("myFileName.txt", q).executeAndDownloadTo(out);
    ` *
     *
     *
     *
     * @param id - the unique id of the file
     * @param q - the query to execute
     * @return a valid download request ready to be executed
     * @throws IOException
    </pre> */
    @Throws(IOException::class)
    fun prepDownloadBlocking(id: String, q: Query): DownloadMetadataQuery {

        val download = DownloadMetadataQuery(this, id, q)
        client.initializeRequest(download)
        return download

    }

    /**
     * Attach query parameters when requesting metadata for a specific file.
     *
     * Use this method to specify a custom time to live.
     *
     *
     *
     * Sample usage:
     * <pre>
     * `Query q = new Query();
     * q.equals("ttl_in_seconds", 3600);  //set a new ttl for the download URL
     * OutputStream out = new ByteArrayOutputStream(...);
     * mKinveyClient.file().downloadBlocking("myFileName.txt", q).executeAndDownloadTo(out);
    ` *
     *
     *
     *
     * @param id - the unique id of the file
     * @param q - the query to execute
     * @return a valid download request ready to be executed
     * @throws IOException
    </pre> */
    @Deprecated("use the download methods which take an `Outputstream` or a `NetworkFileManager`")
    @Throws(IOException::class)
    fun downloadBlocking(id: String, q: Query): DownloadMetadataQuery {

        val download = DownloadMetadataQuery(this, id, q)
        client.initializeRequest(download)
        return download

    }

    /**
     * Prepares a request to download a file with a custom Time-To-Live
     *
     *
     *
     * Sample usage:
     * <pre>
     * `OutputStream out = new ByteArrayOutputStream(...);
     * mKinveyClient.file().downloadBlocking("myFileName.txt", 3600).executeAndDownloadTo(out);
    ` *
     *
     *
     *
     * @param id - the unique _id of the file to download
     * @param ttl - a custom TTL, in milliseconds
     * @return a [DownloadMetadataQuery] request ready to be executed.
     * @throws IOException
    </pre> */
    @Throws(IOException::class)
    fun prepDownloadWithTTLBlocking(id: String, ttl: Int): DownloadMetadataQuery {
        val q = Query()
        q.equals("ttl_in_seconds", ttl)
        return prepDownloadBlocking(id, q)
    }

    /**
     * Download a file with a custom Time-To-Live
     *
     *
     *
     * Sample usage:
     * <pre>
     * `OutputStream out = new ByteArrayOutputStream(...);
     * mKinveyClient.file().downloadBlocking("myFileName.txt", 3600).executeAndDownloadTo(out);
    ` *
     *
     *
     *
     * @param id - the unique _id of the file to download
     * @param ttl - a custom TTL, in milliseconds
     * @return a [DownloadMetadataQuery] request ready to be executed.
     * @throws IOException
    </pre> */
    @Deprecated("use the download methods which take an `Outputstream` or a `NetworkFileManager`")
    @Throws(IOException::class)
    fun downloadWithTTLBlocking(id: String, ttl: Int): DownloadMetadataQuery {
        val q = Query()
        q.equals("ttl_in_seconds", ttl)
        return prepDownloadBlocking(id, q)
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
    @Throws(IOException::class)
    fun prepDownloadBlocking(filename: String): DownloadMetadataQuery {
        val q = Query()
        q.equals("_filename", filename)
        q.addSort("_kmd.lmt", AbstractQuery.SortOrder.DESC)
        val download = DownloadMetadataQuery(this, null, q)
        client.initializeRequest(download)
        download.getRequestHeaders()["x-Kinvey-content-type"] = "application/octet-stream"
        return download

    }

    /**
     * This method performs a query to find a file by it's filename.
     *
     * As Kinvey NetworkFileManager now supports non-unique file names, this method will only return a single file with this name.
     *
     * @param filename
     * @return
     * @throws IOException
     */
    @Deprecated("use the download methods which take an `Outputstream` or a `NetworkFileManager`")
    @Throws(IOException::class)
    fun downloadBlocking(filename: String): DownloadMetadataQuery {
        val q = Query()
        q.equals("_filename", filename)
        q.addSort("_kmd.lmt", AbstractQuery.SortOrder.DESC)
        val download = DownloadMetadataQuery(this, null, q)
        client.initializeRequest(download)
        download.getRequestHeaders()["x-Kinvey-content-type"] = "application/octet-stream"
        return download

    }


    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param metaData the metadata of the NetworkFileManager to remove (requires an ID)
     * @return a valid DELETE request to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deleteBlocking(metaData: FileMetaData): DeleteFile {
        val delete = DeleteFile(this, metaData)
        client.initializeRequest(delete)
        return delete
    }

    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param fileID the _id of the file to remove
     * @return a valid DELETE request to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deleteBlocking(fileID: String): DeleteFile {
        return deleteBlocking(FileMetaData(fileID))
    }


    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param id the metadata of the NetworkFileManager to remove (requires an ID)
     * @return a valid DELETE request to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deleteBlockingById(id: String): DeleteFile {
        val delete = DeleteFile(this, id)
        client.initializeRequest(delete)
        return delete
    }


    /**
     * Uploads metadata for a file, without modifying the file iteself.
     *
     * @param metaData the metadata of the NetworkFileManager to upload
     * @return a valid PUT or POST request to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun uploadMetaDataBlocking(metaData: FileMetaData): UploadMetadata {
        val mode = if (metaData.containsKey(ID_FIELD_NAME)) {
            NetworkManager.SaveMode.PUT
        } else {
            NetworkManager.SaveMode.POST
        }
        val upload = UploadMetadata(this, metaData, mode)
        client.initializeRequest(upload)
        return upload
    }


    /**
     * Downloads metadata for a file, without returning the file iteself.
     *
     * @param id the id of the metadata
     * @return a valid PUT or POST request to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun downloadMetaDataBlocking(id: String): DownloadMetadata {
        val download = DownloadMetadata(this, id)
        client.initializeRequest(download)
        return download
    }


    /**
     * @param uploadProgressListener the listener to receive notifications as the upload progresses
     */
    fun setUploadProgressListener(uploadProgressListener: UploaderProgressListener) {
        this.uploadProgressListener = uploadProgressListener
    }

    /**
     * @param downloaderProgressListener the listener to receive notifications as the download progresses
     */
    fun setDownloaderProgressListener(downloaderProgressListener: DownloaderProgressListener) {
        this.downloaderProgressListener = downloaderProgressListener
    }

    open class MetadataRequest (client: AbstractClient<*>,
            httpMethod: String, jsonContent: GenericJson?, responseClass: Class<FileMetaData>) :
            AbstractKinveyJsonClientRequest<FileMetaData>(client, httpMethod, REST_URL, jsonContent, responseClass) {
        companion object {
            private val REST_URL = "blob/{appKey}/{id}?tls=true"
        }
    }


    //----------------------------------------------------Client Requests

    /**
     * This class uploads a [FileMetaData] object to Kinvey, returning another [FileMetaData] containing the upload URL
     *
     *
     */
    class UploadMetadataAndFile (private val networkFileManager: NetworkFileManager, val meta: FileMetaData, val mode: NetworkManager.SaveMode,
                                 mediaContent: AbstractInputStreamContent, progressListener: UploaderProgressListener):
            MetadataRequest(networkFileManager.client, mode.toString(), meta, FileMetaData::class.java) {

        @Key
        private var id: String? = null

        var uploader: MediaHttpUploader? = null
            private set

        init {
            initializeMediaHttpUploader(mediaContent, progressListener)
            if (mode == NetworkManager.SaveMode.PUT) {
                this.id = Preconditions.checkNotNull(meta.id)
            }
            this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = networkFileManager.clientAppVersion
            if (networkFileManager.customRequestProperties != null && networkFileManager.customRequestProperties?.isEmpty() == false) {
                this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(networkFileManager.customRequestProperties)
            }

            networkFileManager.setUploadHeader(meta, this)
            uploader?.fileMetaDataForUploading = meta
        }

        @Throws(IOException::class)
        override fun execute(): FileMetaData? {
            return uploader?.upload(this)
        }

        /**
         * Sets up this request object to be used for uploading media.
         *
         * @param content data to be uploaded
         * @param progressListener an object to be notified of the different state changes as the upload progresses.
         * Optional `null` can be passed in.
         */
        protected fun initializeMediaHttpUploader(content: AbstractInputStreamContent, progressListener: UploaderProgressListener) {
            val requestFactory = networkFileManager.client.requestFactory
            uploader = createMediaHttpUploader(content, requestFactory)
            uploader?.directUploadEnabled = false
            uploader?.progressListener = progressListener
        }

        /**
         * Factory to instantiate a new http uploader object during the [.initializeMediaHttpUploader]
         *
         * @param content data to be uploaded
         * @param requestFactory request factory to be used
         * @return a valid http uploader with default settings
         */
        protected fun createMediaHttpUploader(content: AbstractInputStreamContent, requestFactory: HttpRequestFactory): MediaHttpUploader {
            return MediaHttpUploader(content, requestFactory.transport, requestFactory.initializer)
        }
    }

    /**
     * This class will upload new file metadata without actually effecting the file
     *
     * Note it is not recommended to change the filename without ensuring a file exists with the new name.
     */
    class UploadMetadata (private val networkFileManager: NetworkFileManager, meta: FileMetaData, val mode: NetworkManager.SaveMode):
            MetadataRequest(networkFileManager.client, mode.toString(), meta, FileMetaData::class.java) {

        @Key
        private var id: String? = null

        init {
            if (mode == NetworkManager.SaveMode.PUT) {
                this.id = Preconditions.checkNotNull(meta.id)
            }
            this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = networkFileManager.clientAppVersion
            if (networkFileManager.customRequestProperties != null && networkFileManager.customRequestProperties?.isEmpty() == false) {
                this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(networkFileManager.customRequestProperties)
            }
            networkFileManager.setUploadHeader(meta, this)
        }
    }

    /**
     * This class will upload new file metadata without actually effecting the file
     *
     * Note it is not recommended to change the filename without ensuring a file exists with the new name.
     */
    class DownloadMetadata (private val networkFileManager: NetworkFileManager, @Key private val id: String) :
        MetadataRequest(networkFileManager.client, "GET", null, FileMetaData::class.java) {
        init {
            this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = networkFileManager.clientAppVersion
            if (networkFileManager.customRequestProperties != null && networkFileManager.customRequestProperties?.isEmpty() == false) {
                this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(networkFileManager.customRequestProperties)
            }
        }
    }

    /**
     * This class gets a [FileMetaData] object from Kinvey, and then downloads the associated NetworkFileManager
     */
    class DownloadMetadataAndFile (private val networkFileManager: NetworkFileManager, meta: FileMetaData, progressListener: DownloaderProgressListener?) :
            MetadataRequest(networkFileManager.client, "GET", null, FileMetaData::class.java) {

        @Key
        private var id: String? = null

        init {
            this.id = Preconditions.checkNotNull(meta.id)
            this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = networkFileManager.clientAppVersion
            if (networkFileManager.customRequestProperties != null && networkFileManager.customRequestProperties?.isEmpty() == false) {
                this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(networkFileManager.customRequestProperties)
            }
            networkFileManager.setUploadHeader(meta, this)
        }
    }

    /**
     * This class gets a [FileMetaData] object from Kinvey, and then downloads the associated NetworkFileManager
     */
    class DownloadMetadataQuery (private val networkFileManager: NetworkFileManager,
                                 @Key private val id: String?, query: Query):
            AbstractKinveyJsonClientRequest<Array<FileMetaData>>(networkFileManager.client, "GET",
            REST_URL, null, Array<FileMetaData>::class.java) {

        @Key("query")
        private val queryFilter: String?
        @Key("sort")
        private val sortFilter: String?
        @Key("limit")
        private val limit: String?
        @Key("skip")
        private val skip: String?

        init {
            this.queryFilter = query.getQueryFilterJson(networkFileManager.client.jsonFactory)
            val queryLimit = query.limit
            val querySkip = query.skip
            this.limit = if (queryLimit > 0) Integer.toString(queryLimit) else null
            this.skip = if (querySkip > 0) Integer.toString(querySkip) else null
            val sortString = query.sortString
            this.sortFilter = if (sortString != "") sortString else null
            this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = networkFileManager.clientAppVersion
            if (customRequestProperties != null && !customRequestProperties!!.isEmpty()) {
                this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(customRequestProperties)
            }
            getRequestHeaders()["x-Kinvey-content-type"] = "application/octet-stream"
        }

        companion object {
            //private final static String REST_URL = "blob/{appKey}/{id}" + "?query={query}";
            private val REST_URL = "blob/{appKey}/{id}" + "{?query,sort,limit,skip}"
        }
    }


    class DeleteFile : AbstractKinveyJsonClientRequest<KinveyDeleteResponse> {

        private var networkFileManager: NetworkFileManager

        @Key
        private var id: String? = null
        //        @Key("query")
        //        private String queryFilter;

        constructor(networkFileManager: NetworkFileManager, metaData: FileMetaData) : super(networkFileManager.client, "DELETE", REST_URL,
                null, KinveyDeleteResponse::class.java) {
            this.networkFileManager = networkFileManager
            this.id = Preconditions.checkNotNull(metaData.id, "cannot remove a file without an _id!")
            this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = networkFileManager.clientAppVersion
            if (customRequestProperties != null && !customRequestProperties!!.isEmpty()) {
                this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(customRequestProperties)
            }
            networkFileManager.setUploadHeader(metaData, this)
        }

        //TODO edwardf re-add remove by query support once it is supported in kcs
        //        public DeleteFile(Query q){
        //            super(client, "DELETE", REST_URL, null, KinveyDeleteResponse.class);
        //            this.queryFilter = q.getQueryFilterJson(client.getJsonFactory());
        //
        //        }

        constructor(networkFileManager: NetworkFileManager, id: String) : super(networkFileManager.client, "DELETE",
                REST_URL, null, KinveyDeleteResponse::class.java) {
            this.networkFileManager = networkFileManager
            this.id = Preconditions.checkNotNull(id)
            this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = networkFileManager.clientAppVersion
            if (customRequestProperties != null && !customRequestProperties!!.isEmpty()) {
                this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(customRequestProperties)
            }
        }

        companion object {
            private val REST_URL = "blob/{appKey}/{id}"// + "{?query}";
        }
    }
}

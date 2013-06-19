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
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.model.KinveyMetaData;
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
 * {@code
 *    mKinveyClient.file().uploadBlocking(file,  new UploaderProgressListener() {
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
 * @author m0rganic
 * @since 2.0
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
        initSize(fileMetaData, content);

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
    public UploadMetadataAndFile uploadBlockingByFilename(String fileName, AbstractInputStreamContent content) throws IOException {
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

//    public DownloadMetadataAndFileQuery downloadBlocking(Query q) throws IOException {
//        DownloadMetadataAndFileQuery download = new DownloadMetadataAndFileQuery(q, downloaderProgressListener);
//        client.initializeRequest(download);
//        download.getRequestHeaders().put("x-Kinvey-content-type","application/octet-stream" );
//        return download;
//
//    }
//
//    public DownloadMetadataAndFileQuery downloadBlockingByFilename(String filename) throws IOException{
//        Query q = new Query();
//        q.equals("_filename", filename);
//        return downloadBlocking(q);
//
//    }


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


//    /**
//     * Deletes the given file from the Kinvey file service.
//     *
//     * @param query the metadata of the File to delete (requires an ID)
//     * @return a valid DELETE request to be executed
//     * @throws IOException
//     */
//    public DeleteFile deleteBlocking(Query query) throws IOException {
//        DeleteFile delete = new DeleteFile(query);
//        client.initializeRequest(delete);
//        return delete;
//    }
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



    //------------------------------------------------vv deprecated file v1
//    /**
//     * Constructs a request to retrieve a temporary url for purposes of downloading a given file already known to Kinvey.
//     * <p>
//     * The url expires within 30 secs. of calling {@link com.kinvey.java.File.GetDownloadUrl#execute()}.
//     * </p>
//     * @param fileName name of the file for which kinvey service is aware
//     * @return request for which the temporary url can be retrieved
//     * @throws IOException if initializing the request fails
//     */
//    @Deprecated
//    public GetDownloadUrl getDownloadUrlBlocking(String fileName) throws IOException {
//        GetDownloadUrl getDownloadUrl = new GetDownloadUrl(fileName);
//        client.initializeRequest(getDownloadUrl);
//        return getDownloadUrl;
//    }
//
//    /**
//     * Constructs a request to retrieve a temporary url for purposes of downloading a given file already known to Kinvey.
//     * <p>
//     * The url expires within 30 secs. of calling {@link com.kinvey.java.File.GetDownloadUrl#execute()}.
//     * </p>
//     * @param fileName name of the file for which kinvey service is aware
//     * @return request for which the temporary url can be retrieved
//     * @throws IOException if initializing the request fails
//     * @deprecated Renamed to {@link #getDownloadUrlBlocking(String)}
//     */
//    @Deprecated
//    public GetDownloadUrl getDownloadUrl(String fileName) throws IOException {
//        GetDownloadUrl getDownloadUrl = new GetDownloadUrl(fileName);
//        client.initializeRequest(getDownloadUrl);
//        return getDownloadUrl;
//    }
//
//    /**
//     * Constructs a request to retrieve a temporary url for purposes of uploading a given file.
//     * <p>
//     * The url expires within 30 secs. of calling {@link com.kinvey.java.File.GetUploadUrl#execute()}.
//     * </p>
//     *
//     * @param fileName the name of the file used in metadata
//     * @return valid request for which the temporary upload url can be retrieved
//     * @throws IOException if initializing the request fails
//     */
//    @Deprecated
//    public GetUploadUrl getUploadUrlBlocking(String fileName) throws IOException {
//        GetUploadUrl getUploadUrl = new GetUploadUrl(fileName);
//        client.initializeRequest(getUploadUrl);
//        return getUploadUrl;
//    }
//
//    /**
//     * Constructs a request to retrieve a temporary url for purposes of uploading a given file.
//     * <p>
//     * The url expires within 30 secs. of calling {@link com.kinvey.java.File.GetUploadUrl#execute()}.
//     * </p>
//     *
//     * @param fileName the name of the file used in metadata
//     * @return valid request for which the temporary upload url can be retrieved
//     * @throws IOException if initializing the request fails
//     * @deprecated Renamed to {@link #getUploadUrlBlocking(String)}
//     */
//    @Deprecated
//    public GetUploadUrl getUploadUrl(String fileName) throws IOException {
//        GetUploadUrl getUploadUrl = new GetUploadUrl(fileName);
//        client.initializeRequest(getUploadUrl);
//        return getUploadUrl;
//    }
//
//    /**
//     * Uploads a given file and its contents to the Kinvey file service.
//     *
//     * @param fileName the filename used for the metadata
//     * @param content the input stream from which the file contents will be sourced
//     * @return a valid request to be executed for the upload operation to Kinvey
//     * @throws IOException if initializing the request fails
//     */
//    @Deprecated
//    public Upload uploadBlocking(String fileName, AbstractInputStreamContent content) throws IOException {
//        Upload upload = new Upload(fileName, content, uploadProgressListener);
//        client.initializeRequest(upload);
//        return upload;
//    }
//
//    /**
//     * Uploads a given file and its contents to the Kinvey file service.
//     *
//     * @param fileName the filename used for the metadata
//     * @param content the input stream from which the file contents will be sourced
//     * @return a valid request to be executed for the upload operation to Kinvey
//     * @throws IOException if initializing the request fails
//     * @deprecated Renamed to {@link #uploadBlocking(String, com.google.api.client.http.AbstractInputStreamContent)}
//     */
//    @Deprecated
//    public Upload upload(String fileName, AbstractInputStreamContent content) throws IOException {
//        Upload upload = new Upload(fileName, content, uploadProgressListener);
//        client.initializeRequest(upload);
//        return upload;
//    }
//
//    /**
//     * Download a given file from the Kinvey file service.
//     * <p>
//     *
//     * </p>
//     *
//     * @param filename the name used in metadata for downloadable file.
//     * @return a valid request to be executed for the download operation from Kinvey file service
//     * @throws IOException
//     */
//    @Deprecated
//    public Download downloadBlocking(String filename) throws IOException {
//        Download download = new Download(filename, downloaderProgressListener);
//        client.initializeRequest(download);
//        return download;
//    }
//
//    /**
//     * Download a given file from the Kinvey file service.
//     * <p>
//     *
//     * </p>
//     *
//     * @param filename the name used in metadata for downloadable file.
//     * @return a valid request to be executed for the download operation from Kinvey file service
//     * @throws IOException
//     * @deprecated Renamed to {@link #downloadBlocking(String)}
//     */
//    @Deprecated
//    public Download download(String filename) throws IOException {
//        Download download = new Download(filename, downloaderProgressListener);
//        client.initializeRequest(download);
//        return download;
//    }
//
//    /**
//     * Deletes the given file from the Kinvey file service.
//     *
//     * @param filename the name of used in metadata to refer to the file
//     * @return a valid DELETE request to be executed
//     * @throws IOException
//     */
//    @Deprecated
//    public Delete deleteBlocking(String filename) throws IOException {
//        Delete delete = new Delete(filename);
//        client.initializeRequest(delete);
//        return delete;
//    }
//
//    /**
//     * Deletes the given file from the Kinvey file service.
//     *
//     * @param filename the name of used in metadata to refer to the file
//     * @return a valid DELETE request to be executed
//     * @throws IOException
//     * @deprecated Rename to {@link #deleteBlocking(String)}
//     */
//    @Deprecated
//    public Delete delete(String filename) throws IOException {
//        Delete delete = new Delete(filename);
//        client.initializeRequest(delete);
//        return delete;
//    }

    //-----------------------^^ File v1, deprecated

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




    //---------------------vv file v1 deprecated
//
//    /**
//     * GET a temporary url for download.
//     * @deprecated  for {@link DownloadMetadata}
//     */
//    @Deprecated
//    public class GetDownloadUrl extends AbstractKinveyJsonClientRequest<UriLocResponse> {
//
//        private final static String REST_URL = "blob/{appKey}/download-loc/{fileName}";
//
//        @Key
//        private final String fileName;
//
//        private GetDownloadUrl(String filename) {
//            super(client, "GET", REST_URL, null, UriLocResponse.class);
//            this.fileName = Preconditions.checkNotNull(filename);
//        }
//
//
//    }
//
//    /**
//     * GET a temporary url for uploading file contents.
//     * @deprecated  for {@link DownloadMetadata}
//     */
//    @Deprecated
//    public class GetUploadUrl extends AbstractKinveyJsonClientRequest<UriLocResponse> {
//
//        private final static String REST_URL = "blob/{appKey}/upload-loc/{fileName}";
//
//        @Key
//        private final String fileName;
//
//        private GetUploadUrl(String filename) {
//            super(client, "GET", REST_URL, null, UriLocResponse.class);
//            this.fileName = Preconditions.checkNotNull(filename);
//        }
//    }
//
//    /**
//     * Initiate an upload of a particular file and its contents.
//     * @deprecated for {@link UploadMetadataAndFile}
//     */
//    @Deprecated
//    public class Upload extends AbstractKinveyJsonClientRequest<Void> {
//
//        private final static String REST_URL = "blob/{appKey}/upload-loc/{fileName}";
//
//        @Key
//        private String fileName;
//
//        private Upload(String fileName, AbstractInputStreamContent mediaContent, UploaderProgressListener progressListener) {
//            super(client, "GET", REST_URL, null, Void.class);
//            initializeMediaHttpUploader(mediaContent, progressListener);
//            this.fileName = Preconditions.checkNotNull(fileName);
//        }
//    }
//
//    /**
//     * Initiate a download of a particular file already known to Kinvey.
//     *
//     * @deprecated for {@link DownloadMetadataAndFile}
//     */
//    @Deprecated
//    public class Download extends AbstractKinveyJsonClientRequest<Void> {
//
//        private final static String REST_URL = "blob/{appKey}/download-loc/{fileName}";
//
//        @Key
//        private String fileName;
//
//        private Download(String fileName, DownloaderProgressListener progressListener) {
//            super(client, "GET", REST_URL, null, Void.class);
//            initializeMediaHttpDownloader(progressListener);
//            this.fileName = Preconditions.checkNotNull(fileName);
//        }
//    }
//
//    /**
//     * Initiate a delete of a particular file already known to Kinvey.
//     * <p>
//     * {@link com.kinvey.java.core.AbstractKinveyJsonClientRequest#executeUnparsed()} is overridden to first GET
//     * the URI for which the file should be deleted.
//     * </p>
//     *
//     * @deprecated for {@link DeleteFile}
//     */
//    @Deprecated
//    public class Delete extends AbstractKinveyJsonClientRequest<Void> {
//
//        private final static String REST_URL = "blob/{appKey}/remove-loc/{fileName}";
//
//        @Key
//        private String fileName;
//
//        private Delete(String fileName) {
//            super(client, "GET", REST_URL, null, Void.class);
//            this.fileName = Preconditions.checkNotNull(fileName);
//        }
//
//        @Override
//        public HttpResponse executeUnparsed() throws IOException {
//            HttpResponse uriResponse = super.executeUnparsed();
//            JsonObjectParser parser = client.getJsonFactory().createJsonObjectParser();
//            UriLocResponse uri = parser.parseAndClose(uriResponse.getContent(), uriResponse.getContentCharset(), UriLocResponse.class);
//            HttpRequest request = client.getRequestFactory().buildDeleteRequest(uri.newGenericUrl());
//            request.setSuppressUserAgentSuffix(true);
//            request.setHeaders(new KinveyHeaders());
//            return request.execute();
//        }
//    }




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
        private final static String REST_URL = "blob/{appKey}/{id}";

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

//    /**
//     * This class gets a {@link FileMetaData} object from Kinvey, and then downloads the associated File
//     */
//    public class DownloadMetadataAndFileQuery extends AbstractKinveyJsonClientRequest<FileMetaData[]> {
//
//        private final static String REST_URL = "blob/{appKey}/{id}" + "{?query}";
//
//        @Key
//        private String id;
//        @Key("query")
//        private String queryFilter;
//
//
//        private DownloadMetadataAndFileQuery(Query query, DownloaderProgressListener progressListener){
//            super(client, "GET", REST_URL, null, FileMetaData[].class);
//            initializeMediaHttpDownloader(progressListener);
//            this.queryFilter = query.getQueryFilterJson(client.getJsonFactory());
//        }
//
//
//
//    }

    public class DeleteFile extends AbstractKinveyJsonClientRequest<KinveyDeleteResponse>{

        private final static String REST_URL = "blob/{appKey}/{id}";// + "{?query}";

        @Key
        private String id;
//        @Key("query")
//        private String queryFilter;

        public DeleteFile(FileMetaData metaData){
            super(client, "DELETE", REST_URL, null, KinveyDeleteResponse.class);
            this.id = Preconditions.checkNotNull(metaData.getId());
        }

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



    public static class FileMetaData extends GenericJson {

        @Key("_id")
        private String id;

        @Key("_filename")
        private String fileName;

        @Key("size")
        private long size;

        @Key("mimetype")
        private String mimetype;

        @Key("_acl")
        private KinveyMetaData.AccessControlList acl;

        @Key("_uploadURL")
        private String uploadUrl;

        @Key("_downloadURL")
        private String downloadURL;

        public FileMetaData() {
        }

        public FileMetaData(String fileName){
            setFileName(fileName);
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getMimetype() {
            return mimetype;
        }

        public void setMimetype(String mimetype) {
            this.mimetype = mimetype;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public KinveyMetaData.AccessControlList getAcl() {
            return acl;
        }

        public void setAcl(KinveyMetaData.AccessControlList acl) {
            this.acl = acl;
        }

        public String getUploadUrl() {
            return uploadUrl;
        }

        public void setUploadUrl(String uploadUrl) {
            this.uploadUrl = uploadUrl;
        }

        public String getDownloadURL() {
            return downloadURL;
        }

        public void setDownloadURL(String downloadURL) {
            this.downloadURL = downloadURL;
        }
    }


    protected abstract void initSize(FileMetaData meta, AbstractInputStreamContent content);

}

/*
 * Copyright (c) 2014, Kinvey, Inc.
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
package com.kinvey.nativejava;

import com.google.api.client.http.InputStreamContent;
import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.model.KinveyDeleteResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** {@inheritDoc}
 *
 * @author edwardf
 * */
public class File extends com.kinvey.java.File{


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
    protected File(AbstractClient client) {
        super(client);
        setMimeTypeManager(new JavaMimeTypeFinder());
    }


    public void uploadBlocking(java.io.File file, UploaderProgressListener listener) throws IOException{
        Preconditions.checkNotNull(file, "file must not be null");
        this.setUploadProgressListener(listener);
        FileMetaData meta = new FileMetaData(file.getName());
        meta.setFileName(file.getName());
        this.mimeTypeFinder.getMimeType(meta, file);

        InputStreamContent mediaContent = null;
        String mimetype = "application/octet-stream";
        mediaContent = new InputStreamContent(mimetype, new FileInputStream(file));
        mediaContent.setLength(file.length());

        mediaContent.setCloseInputStream(false);
        mediaContent.setRetrySupported(false);
        this.uploadBlocking(meta, mediaContent).execute();
    }

    /**
     * Upload a java.io.File with it's associated metadata
     *
     *
     * @param meta the metadata of the file to upload
     * @param file the file itself
     * @param listener listener for callbacks about upload progress
     */
    public void uploadBlocking(FileMetaData meta, java.io.File file, UploaderProgressListener listener) throws IOException{
        this.setUploadProgressListener(listener);
        this.mimeTypeFinder.getMimeType(meta, file);

        InputStreamContent mediaContent = null;
        String mimetype = "application/octet-stream";
        mediaContent = new InputStreamContent(mimetype, new FileInputStream(file));
        mediaContent.setLength(file.length());

        mediaContent.setCloseInputStream(false);
        mediaContent.setRetrySupported(false);
        this.uploadBlocking(meta, mediaContent).execute();        }

    /**
     * Uploads the contents of the stream to the Kinvey file service endpoint.
     *
     * @param name name to refer to the blob when stored with the file service endpoint
     * @param inputStream  stream to be uploaded
     * @param listener an implementation of a client listener to get results on the UI thread from the async call.
     */
    public void uploadBlocking(String name, InputStream inputStream, UploaderProgressListener listener) throws IOException{
        Preconditions.checkNotNull(inputStream, "byteContent must not be null");

        this.setUploadProgressListener(listener);
        FileMetaData meta = new FileMetaData(name);
        this.mimeTypeFinder.getMimeType(meta, inputStream);

        InputStreamContent mediaContent = null;
        String mimetype = "application/octet-stream";
        mediaContent = new InputStreamContent(mimetype, inputStream);
        mediaContent.setLength(inputStream.available());

        mediaContent.setCloseInputStream(false);
        mediaContent.setRetrySupported(false);
        uploadBlocking(meta, mediaContent).execute();
    }

    /**
     * Uploads the contents of the stream to the Kinvey file service endpoint.
     *
     * @param meta the metadata of the file to upload
     * @param inputStream  stream to be uploaded
     * @param listener an implementation of a client listener to get results on the UI thread from the async call.
     */
    public void uploadBlocking(FileMetaData meta, InputStream inputStream, UploaderProgressListener listener) throws IOException{
        Preconditions.checkNotNull(inputStream, "byteContent must not be null");

        this.setUploadProgressListener(listener);
        this.mimeTypeFinder.getMimeType(meta, inputStream);

        InputStreamContent mediaContent = null;
        String mimetype = "application/octet-stream";
        mediaContent = new InputStreamContent(mimetype, inputStream);
        mediaContent.setLength(inputStream.available());

        mediaContent.setCloseInputStream(false);
        mediaContent.setRetrySupported(false);
        uploadBlocking(meta, mediaContent).execute();
    }

    public void downloadBlocking(FileMetaData metaData, OutputStream out, DownloaderProgressListener listener) throws IOException{
        this.setDownloaderProgressListener(listener);
        this.downloadBlocking(metaData).executeAndDownloadTo(out);

    }


    /**
     * Download a file by Query, only the first result will be downloaded.
     *
     *
     * @param query - the query, with a limit of 1
     * @param out - where to download the file
     * @param listener - for progress notifications
     */
    public void downloadBlocking(Query query, OutputStream out, DownloaderProgressListener listener) throws IOException{
        this.setDownloaderProgressListener(listener);
        this.downloadBlocking(query).executeAndDownloadTo(out);
    }


    /**
     * Download a file asyncronously with a custom time to live.
     *
     * @param id - the id of the file to download
     * @param ttl - the custom ttl to use for the download URL
     * @param out - where to download the file
     * @param listener - for progress notifications
     */
    public void downloadWithTTLBlocking(String id, int ttl, OutputStream out, DownloaderProgressListener listener) throws IOException{
        this.setDownloaderProgressListener(listener);
        this.downloadWithTTLBlocking(id, ttl).executeAndDownloadTo(out);
    }

    /**
     * Deletes the given file from the Kinvey file service.
     *
     * @param metadata the metadata of the file
     * @param callback an implementation of a client callback to get results on the UI thread from the async call.
     */
    public void deleteBlocking(FileMetaData metadata, KinveyClientCallback<KinveyDeleteResponse> callback) throws IOException{
        this.deleteBlockingById(metadata.getId()).execute();
    }




    /**
     * Downloads just the metadata of a file
     *
     * @param id the metadata of the file
     * @param callback an implementation of a client callback to get results on the UI thread from the async call.
     */
    public void downloadMetaDataBlocking(String id, KinveyClientCallback<Void> callback) throws IOException{
        this.downloadMetaDataBlocking(id).execute();
    }


    /**
     * Upload metadata for an existing file
     *
     * @param meta the {@link FileMetaData} to update
     * @param callback an implementation of a client callback to get results on the UI thread from the async call.
     */
    public void uploadMetaDataBlocking(FileMetaData meta, KinveyClientCallback<Void> callback) throws IOException{
        this.uploadMetaDataBlocking(meta).execute();
    }



}

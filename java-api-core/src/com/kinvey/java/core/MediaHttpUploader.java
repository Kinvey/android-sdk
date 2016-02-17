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

/*
 * Copyright (c) 2012 Google Inc.
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

package com.kinvey.java.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonObjectParser;
import com.google.common.base.Preconditions;
import com.kinvey.java.KinveyException;
import com.kinvey.java.LinkedResources.SaveLinkedResourceClientRequest;
import com.kinvey.java.model.FileMetaData;

/**
 * Media HTTP Uploader, with support for both direct and resumable media uploads. Documentation is
 * available <a href='http://code.google.com/p/google-api-java-client/wiki/MediaUpload'>here</a>.
 *
 * <p>
 * For resumable uploads, when the media content length is known, if the provided
 * {@link InputStream} has {@link InputStream#markSupported} as {@code false} then it is wrapped in
 * an {@link BufferedInputStream} to support the {@link InputStream#mark} and
 * {@link InputStream#reset} methods required for handling server errors. If the media content
 * length is unknown then each chunk is stored temporarily in memory. This is required to determine
 * when the last chunk is reached.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.9
 *
 * @author rmistry@google.com (Ravi Mistry)
 * @author morgan@kinvey.com  Portions of this code have been modified for Kinvey purposes. 
 *      Specifically Kinvey needed it to interact with AbstractKinveyClient class and upload 
 *      media files using Kinvey's propritary upload protocol.
 */
public class MediaHttpUploader {

    /**
     * Upload content type header.
     *
     * @since 1.13
     */
    public static final String CONTENT_LENGTH_HEADER = "X-Upload-Content-Length";

    /**
     * Upload content length header.
     *
     * @since 1.13
     */
    public static final String CONTENT_TYPE_HEADER = "X-Upload-Content-Type";

    /**
     * Upload state associated with the Media HTTP uploader.
     */
    public enum UploadState {
        /** The upload process has not started yet. */
        NOT_STARTED,

        /** Set before the initiation request is sent. */
        INITIATION_STARTED,

        /** Set after the initiation request completes. */
        INITIATION_COMPLETE,

        /** Set after a media file chunk is uploaded. */
        UPLOAD_IN_PROGRESS,

        /** Set after the complete media file is successfully uploaded. */
        UPLOAD_COMPLETE
    }

    /** The current state of the uploader. */
    private UploadState uploadState = UploadState.NOT_STARTED;

    static final int MB = 0x100000;
    private static final int KB = 0x400;

    /**
     * Minimum number of bytes that can be uploaded to the server (set to 256KB).
     */
    public static final int MINIMUM_CHUNK_SIZE = 256 * KB;

    /**
     * Default maximum number of bytes that will be uploaded to the server in any single HTTP request
     * (set to 10 MB).
     */
    public static final int DEFAULT_CHUNK_SIZE = 10 * MB;

    /** The HTTP content of the media to be uploaded. */
    private final AbstractInputStreamContent mediaContent;

    /** The request factory for connections to the server. */
    private final HttpRequestFactory requestFactory;

    /** The transport to use for requests. */
    private final HttpTransport transport;

    /** HTTP content metadata of the media to be uploaded or {@code null} for none. */
    private HttpContent metadata;

    /**
     * has the request been cancelled?
     */
    private boolean cancelled = false;

    /**
     * The length of the HTTP media content.
     *
     * <p>
     * {@code 0} before it is lazily initialized in {@link #getMediaContentLength()} after which it
     * could still be {@code 0} for empty media content. Will be {@code < 0} if the media content
     * length has not been specified.
     * </p>
     */
    private long mediaContentLength;

    /**
     * Determines if media content length has been calculated yet in {@link #getMediaContentLength()}.
     */
    private boolean isMediaContentLengthCalculated;

    /**
     * The HTTP method used for the initiation request.
     *
     * <p>
     * Can only be {@link HttpMethods#POST} (for media upload) or {@link HttpMethods#PUT} (for media
     * update). The default value is {@link HttpMethods#POST}.
     * </p>
     */
    private String initiationRequestMethod = HttpMethods.POST;

    /** The HTTP headers used in the initiation request. */
    private HttpHeaders initiationHeaders = new HttpHeaders();

    /**
     * The HTTP request object that is currently used to send upload requests or {@code null} before
     * {@link #upload}.
     */
    private HttpRequest currentRequest;

    /** An Input stream of the HTTP media content or {@code null} before {@link #upload}. */
    private InputStream contentInputStream;

    /**
     * Determines whether the back off policy is enabled or disabled. If value is set to {@code false}
     * then server errors are not handled and the upload process will fail if a server error is
     * encountered. Defaults to {@code true}.
     */
    private boolean backOffPolicyEnabled = true;

    /**
     * Determines whether direct media upload is enabled or disabled. If value is set to {@code true}
     * then a direct upload will be done where the whole media content is uploaded in a single request
     * If value is set to {@code false} then the upload uses the resumable media upload protocol to
     * upload in data chunks. Defaults to {@code false}.
     */
    private boolean directUploadEnabled;

    /**
     * Progress listener to send progress notifications to or {@code null} for none.
     */
    private UploaderProgressListener progressListener;

    /**
     * The total number of bytes uploaded by this uploader. This value will not be calculated for
     * direct uploads when the content length is not known in advance.
     */
    // TODO(rmistry): Figure out a way to compute the content length using CountingInputStream.
    private long bytesUploaded;

    /**
     * Maximum size of individual chunks that will get uploaded by single HTTP requests. The default
     * value is {@link #DEFAULT_CHUNK_SIZE}.
     */
    private int chunkSize = DEFAULT_CHUNK_SIZE;

    /**
     * Construct the {@link MediaHttpUploader}.
     *
     * <p>
     * The input stream received by calling {@link AbstractInputStreamContent#getInputStream} is
     * closed when the upload process is successfully completed. For resumable uploads, when the
     * media content length is known, if the input stream has {@link InputStream#markSupported} as
     * {@code false} then it is wrapped in an {@link BufferedInputStream} to support the
     * {@link InputStream#mark} and {@link InputStream#reset} methods required for handling server
     * errors. If the media content length is unknown then each chunk is stored temporarily in memory.
     * This is required to determine when the last chunk is reached.
     * </p>
     *
     * @param mediaContent The Input stream content of the media to be uploaded
     * @param transport The transport to use for requests
     * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
     *        {@code null} for none
     */
    public MediaHttpUploader(AbstractInputStreamContent mediaContent, HttpTransport transport,
                             HttpRequestInitializer httpRequestInitializer) {
        this.mediaContent = Preconditions.checkNotNull(mediaContent);
        this.transport = Preconditions.checkNotNull(transport);
        this.requestFactory = httpRequestInitializer == null
                ? transport.createRequestFactory() : transport.createRequestFactory(httpRequestInitializer);
    }

    /**
     *
     * Executes a direct media upload or resumable media upload conforming to the specifications
     * listed <a href='http://code.google.com/apis/gdata/docs/resumable_upload.html'>here.</a>
     *
     * <p>
     * This method is not reentrant. A new instance of {@link MediaHttpUploader} must be instantiated
     * before upload called be called again.
     * </p>
     *
     * <p>
     * If an error is encountered during the request execution the caller is responsible for parsing
     * the response correctly. For example for JSON errors:
     *
     * <pre>
     if (!response.isSuccessStatusCode()) {
     throw GoogleJsonResponseException.from(jsonFactory, response);
     }
     * </pre>
     * </p>
     *
     * <p>
     * Callers should call {@link HttpResponse#disconnect} when the returned HTTP response object is
     * no longer needed. However, {@link HttpResponse#disconnect} does not have to be called if the
     * response stream is properly closed. Example usage:
     * </p>
     *
     * <pre>
     HttpResponse response = batch.upload(initiationRequestUrl);
     try {
     // process the HTTP response object
     } finally {
     response.disconnect();
     }
     * </pre>
     *
     * @param initiationClientRequest
     * @return HTTP response
     * @throws IOException
     */
    public FileMetaData upload(AbstractKinveyClientRequest initiationClientRequest) throws IOException {
        Preconditions.checkArgument(uploadState == UploadState.NOT_STARTED);
        updateStateAndNotifyListener(UploadState.UPLOAD_IN_PROGRESS);
        FileMetaData meta = null;
        // Make initial request to get the unique upload URL.
        HttpResponse initialResponse = executeUploadInitiation(initiationClientRequest);
        if (!initialResponse.isSuccessStatusCode()) {
            // If the initiation request is not successful return it immediately.
            throw  new KinveyException("Uploading Metadata Failed");
        }
        GenericUrl uploadUrl;
        Map<String, String> headers = null;
        try {
            JsonObjectParser jsonObjectParser = (JsonObjectParser) initiationClientRequest.getAbstractKinveyClient().getObjectParser();
            meta = parse(jsonObjectParser, initialResponse);

            if (meta.containsKey("_requiredHeaders")){
                //then there are special headers to use in the request to google
                headers = (Map<String, String>) meta.get("_requiredHeaders");
            }


            notifyListenerWithMetaData(meta);
            if(meta.getUploadUrl() != null){
                uploadUrl = new GenericUrl(meta.getUploadUrl());
            }else{
                throw new KinveyException("_uploadURL is null!","do not remove _uploadURL in collection hooks for NetworkFileManager!","The library cannot upload a file without this url");
            }
            uploadUrl = new GenericUrl(meta.getUploadUrl());
        } finally {
            initialResponse.disconnect();
        }

        // Convert media content into a byte stream to upload in chunks.
        contentInputStream = mediaContent.getInputStream();
        if (!contentInputStream.markSupported() && getMediaContentLength() >= 0) {
            // If we know the media content length then wrap the stream into a Buffered input stream to
            // support the {@link InputStream#mark} and {@link InputStream#reset} methods required for
            // handling server errors.
            contentInputStream = new BufferedInputStream(contentInputStream);
        }

        HttpResponse response;
        
        currentRequest = requestFactory.buildPutRequest(uploadUrl, null);
        currentRequest.setSuppressUserAgentSuffix(true);
        setContentAndHeadersOnCurrentRequest(meta.getMimetype(), bytesUploaded);

        currentRequest.setThrowExceptionOnExecuteError(false);
        currentRequest.setRetryOnExecuteIOException(true);
        // if there are custom headers, add them
		if (headers != null) {
			for (String header : headers.keySet()) {

				String curHeader = headers.get(header);
				// then it's a list
				if (curHeader.contains(", ")) {
					String[] listheaders = curHeader.split(", ");
					currentRequest.getHeaders().put(header,
							Arrays.asList(listheaders));
				} else {
					currentRequest.getHeaders().put(header, curHeader);
				}
			}
		}
		response = currentRequest.execute();
		if (response.isSuccessStatusCode()) {
			bytesUploaded = getMediaContentLength();
			contentInputStream.close();
			updateStateAndNotifyListener(UploadState.UPLOAD_COMPLETE);
			
		} else {
            throw  new KinveyException("Uploading NetworkFileManager Failed");
        }
		return meta;
		
    }

    /**
     * Uses lazy initialization to compute the media content length.
     *
     * <p>
     * This is done to avoid throwing an {@link IOException} in the constructor.
     * </p>
     */
    private long getMediaContentLength() throws IOException {
        if (!isMediaContentLengthCalculated) {
            mediaContentLength = mediaContent.getLength();
            isMediaContentLengthCalculated = true;
        }
        return mediaContentLength;
    }


    /** package-level for testing **/
    FileMetaData parse(JsonObjectParser initationResponseParser, HttpResponse response) throws IOException {
        try{
            return initationResponseParser.parseAndClose(response.getContent(), response.getContentCharset(), FileMetaData.class);
        }catch(Exception e){
            return initationResponseParser.parseAndClose(response.getContent(), response.getContentCharset(), FileMetaData[].class)[0];
        }
    }

    /**
     * This method sends a GET request with empty content to get the unique upload URL.
     */
    private HttpResponse executeUploadInitiation(AbstractKinveyClientRequest abstractKinveyClientRequest) throws IOException {
        updateStateAndNotifyListener(UploadState.INITIATION_STARTED);

        HttpResponse response = abstractKinveyClientRequest.executeUnparsed(false);
        boolean notificationCompleted = false;

        try {
            updateStateAndNotifyListener(UploadState.INITIATION_COMPLETE);
            notificationCompleted = true;
        } finally {
            if (!notificationCompleted) {
                response.disconnect();
            }
        }
        return response;
    }

    /**
     * Sets the HTTP media content chunk and the required headers that should be used in the upload
     * request.
     *
     * @param bytesWritten The number of bytes that have been successfully uploaded on the server
     */
    private void setContentAndHeadersOnCurrentRequest(String mimeType, long bytesWritten) throws IOException {

        AbstractInputStreamContent contentChunk;
            contentChunk = new InputStreamContent(mimeType, contentInputStream)
                    .setRetrySupported(true)
                    .setCloseInputStream(false);

        currentRequest.setContent(contentChunk);
    }


    /** Returns HTTP content metadata for the media request or {@code null} for none. */
    public HttpContent getMetadata() {
        return metadata;
    }

    /** Sets HTTP content metadata for the media request or {@code null} for none. */
    public MediaHttpUploader setMetadata(HttpContent metadata) {
        this.metadata = metadata;
        return this;
    }

    /** Returns the HTTP content of the media to be uploaded. */
    public HttpContent getMediaContent() {
        return mediaContent;
    }

    /** Returns the transport to use for requests. */
    public HttpTransport getTransport() {
        return transport;
    }

    /**
     * Sets whether the back off policy is enabled or disabled. If value is set to {@code false} then
     * server errors are not handled and the upload process will fail if a server error is
     * encountered. Defaults to {@code true}.
     */
    public MediaHttpUploader setBackOffPolicyEnabled(boolean backOffPolicyEnabled) {
        this.backOffPolicyEnabled = backOffPolicyEnabled;
        return this;
    }

    /**
     * Returns whether the back off policy is enabled or disabled. If value is set to {@code false}
     * then server errors are not handled and the upload process will fail if a server error is
     * encountered. Defaults to {@code true}.
     */
    public boolean isBackOffPolicyEnabled() {
        return backOffPolicyEnabled;
    }

    /**
     * Sets whether direct media upload is enabled or disabled. If value is set to {@code true} then a
     * direct upload will be done where the whole media content is uploaded in a single request. If
     * value is set to {@code false} then the upload uses the resumable media upload protocol to
     * upload in data chunks. Defaults to {@code false}.
     *
     * @since 1.9
     */
    public MediaHttpUploader setDirectUploadEnabled(boolean directUploadEnabled) {
        this.directUploadEnabled = directUploadEnabled;
        return this;
    }

    /**
     * Returns whether direct media upload is enabled or disabled. If value is set to {@code true}
     * then a direct upload will be done where the whole media content is uploaded in a single
     * request. If value is set to {@code false} then the upload uses the resumable media upload
     * protocol to upload in data chunks. Defaults to {@code false}.
     *
     * @since 1.9
     */
    public boolean isDirectUploadEnabled() {
        return directUploadEnabled;
    }

    /**
     * Sets the progress listener to send progress notifications to or {@code null} for none.
     */
    public MediaHttpUploader setProgressListener(UploaderProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    /**
     * Returns the progress listener to send progress notifications to or {@code null} for none.
     */
    public UploaderProgressListener getProgressListener() {
        return progressListener;
    }

    /**
     * Sets the maximum size of individual chunks that will get uploaded by single HTTP requests. The
     * default value is {@link #DEFAULT_CHUNK_SIZE}.
     *
     * <p>
     * The minimum allowable value is {@link #MINIMUM_CHUNK_SIZE} and the specified chunk size must
     * be a multiple of {@link #MINIMUM_CHUNK_SIZE}.
     * </p>
     *
     * <p>
     * Upgrade warning: Prior to version 1.13.0-beta {@link #setChunkSize} accepted any chunk size
     * above {@link #MINIMUM_CHUNK_SIZE}, it now accepts only multiples of
     * {@link #MINIMUM_CHUNK_SIZE}.
     * </p>
     */
    public MediaHttpUploader setChunkSize(int chunkSize) {
        Preconditions.checkArgument(chunkSize > 0 && chunkSize % MINIMUM_CHUNK_SIZE == 0);
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * Returns the maximum size of individual chunks that will get uploaded by single HTTP requests.
     * The default value is {@link #DEFAULT_CHUNK_SIZE}.
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Returns the HTTP method used for the initiation request.
     *
     * <p>
     * The default value is {@link HttpMethods#POST}.
     * </p>
     *
     * @since 1.12
     */
    public String getInitiationRequestMethod() {
        return initiationRequestMethod;
    }

    /**
     * Sets the HTTP method used for the initiation request.
     *
     * <p>
     * Can only be {@link HttpMethods#POST} (for media upload) or {@link HttpMethods#PUT} (for media
     * update). The default value is {@link HttpMethods#POST}.
     * </p>
     *
     * @since 1.12
     */
    public MediaHttpUploader setInitiationRequestMethod(String initiationRequestMethod) {
        Preconditions.checkArgument(initiationRequestMethod.equals(HttpMethods.POST)
                || initiationRequestMethod.equals(HttpMethods.PUT));
        this.initiationRequestMethod = initiationRequestMethod;
        return this;
    }

    /**
     * Sets the HTTP headers used for the initiation request.
     *
     * <p>
     * Upgrade warning: in prior version 1.12 the initiation headers were of type
     * {@code GoogleHeaders}, but as of version 1.13 that type is deprecated, so we now use type
     * {@link HttpHeaders}.
     * </p>
     */
    public MediaHttpUploader setInitiationHeaders(HttpHeaders initiationHeaders) {
        this.initiationHeaders = initiationHeaders;
        return this;
    }

    /**
     * Returns the HTTP headers used for the initiation request.
     *
     * <p>
     * Upgrade warning: in prior version 1.12 the initiation headers were of type
     * {@code GoogleHeaders}, but as of version 1.13 that type is deprecated, so we now use type
     * {@link HttpHeaders}.
     * </p>
     */
    public HttpHeaders getInitiationHeaders() {
        return initiationHeaders;
    }

    /**
     * Gets the total number of bytes uploaded by this uploader or {@code 0} for direct uploads when
     * the content length is not known.
     *
     * @return the number of bytes uploaded
     */
    public long getNumBytesUploaded() {
        return bytesUploaded;
    }

    /**
     * Sets the upload state and notifies the progress listener.
     *
     * @param uploadState value to set to
     */
    private void updateStateAndNotifyListener(UploadState uploadState) throws IOException {
        this.uploadState = uploadState;
        if (progressListener != null) {
            progressListener.progressChanged(this);
        }
    }

    /**
     * Notifies the listener (if there is one) of the updated metadata
     */
    private void notifyListenerWithMetaData(FileMetaData meta){
        if (this.progressListener != null){
            if(this.progressListener instanceof SaveLinkedResourceClientRequest.MetaUploadListener){
                ((SaveLinkedResourceClientRequest.MetaUploadListener)this.progressListener).metaDataUploaded(meta);
            }
            if (this.progressListener instanceof MetaUploadProgressListener){
                ((MetaUploadProgressListener) this.progressListener).metaDataRetrieved(meta);
            }

        }
    }

    /**
     * Gets the current upload state of the uploader.
     *
     * @return the upload state
     */
    public UploadState getUploadState() {
        return uploadState;
    }

    /**
     * Gets the upload progress denoting the percentage of bytes that have been uploaded, represented
     * between 0.0 (0%) and 1.0 (100%).
     *
     * <p>
     * Do not use if the specified {@link AbstractInputStreamContent} has no content length specified.
     * Instead, consider using {@link #getNumBytesUploaded} to denote progress.
     * </p>
     *
     * @throws IllegalArgumentException if the specified {@link AbstractInputStreamContent} has no
     *         content length
     * @return the upload progress
     */
    public double getProgress() throws IOException {
        Preconditions.checkArgument(getMediaContentLength() >= 0, "Cannot call getProgress() if " +
                "the specified AbstractInputStreamContent has no content length. Use " +
                " getNumBytesUploaded() to denote progress instead.");
        return getMediaContentLength() == 0 ? 0 : (double) bytesUploaded / getMediaContentLength();
    }

    public void cancel(){
        this.cancelled = true;
    }

    public boolean isCancelled(){
        return this.cancelled;
    }
}

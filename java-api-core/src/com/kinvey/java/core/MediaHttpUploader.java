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

package com.kinvey.java.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GZipEncoding;
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
import com.google.api.client.util.ByteStreams;
import com.google.api.client.util.IOUtils;
import com.google.common.base.Preconditions;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.KinveyException;
import com.kinvey.java.LinkedResources.SaveLinkedResourceClientRequest;
import com.kinvey.java.UploadFileException;
import com.kinvey.java.model.FileMetaData;

import org.apache.http.protocol.HTTP;
import org.apache.tools.ant.taskdefs.condition.Http;

/**
 * Media HTTP Uploader, with support for both direct and resumable media uploads. Documentation is
 * available <a href='http://code.google.com/p/google-api-java-client/wiki/MediaUpload'>here</a>.
 * <p>
 * <p>
 * For resumable uploads, when the media content length is known, if the provided
 * {@link InputStream} has {@link InputStream#markSupported} as {@code false} then it is wrapped in
 * an {@link BufferedInputStream} to support the {@link InputStream#mark} and
 * {@link InputStream#reset} methods required for handling server errors. If the media content
 * length is unknown then each chunk is stored temporarily in memory. This is required to determine
 * when the last chunk is reached.
 * </p>
 * <p>
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author rmistry@google.com (Ravi Mistry)
 * @author morgan@kinvey.com  Portions of this code have been modified for Kinvey purposes.
 *         Specifically Kinvey needed it to interact with AbstractKinveyClient class and upload
 *         media files using Kinvey's propritary upload protocol.
 * @since 1.9
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
    private int retryBackOffCounter;

    /**
     * Upload state associated with the Media HTTP uploader.
     */
    public enum UploadState {
        /**
         * The upload process has not started yet.
         */
        NOT_STARTED,

        /**
         * Set before the initiation request is sent.
         */
        INITIATION_STARTED,

        /**
         * Set after the initiation request completes.
         */
        INITIATION_COMPLETE,

        /**
         * Set after a media file chunk is uploaded.
         */
        UPLOAD_IN_PROGRESS,

        /**
         * Set after the complete media file is successfully uploaded.
         */
        UPLOAD_COMPLETE
    }

    /**
     * The current state of the uploader.
     */
    private UploadState uploadState = UploadState.NOT_STARTED;

    static final int MB = 0x100000;
    private static final int KB = 0x400;

    /**
     * Minimum number of bytes that can be uploaded to the server (set to 256KB).
     */
    public static final int MINIMUM_CHUNK_SIZE = 256 * KB;

    /**
     * Default maximum number of bytes that will be uploaded to the server in any single HTTP request
     * (set to 512 KB).
     */
    // TODO: 09.11.2016 change size, default was 10MB
    public static final int DEFAULT_CHUNK_SIZE = 512 * KB;

    /**
     * The HTTP content of the media to be uploaded.
     */
    private final AbstractInputStreamContent mediaContent;

    /**
     * The request factory for connections to the server.
     */
    private final HttpRequestFactory requestFactory;

    /**
     * The transport to use for requests.
     */
    private final HttpTransport transport;

    /**
     * HTTP content metadata of the media to be uploaded or {@code null} for none.
     */
    private HttpContent metadata;

    /**
     * has the request been cancelled?
     */
    private boolean cancelled = false;

    /**
     * The length of the HTTP media content.
     * <p>
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
     * <p>
     * <p>
     * Can only be {@link HttpMethods#POST} (for media upload) or {@link HttpMethods#PUT} (for media
     * update). The default value is {@link HttpMethods#POST}.
     * </p>
     */
    private String initiationRequestMethod = HttpMethods.POST;

    /**
     * The HTTP headers used in the initiation request.
     */
    private HttpHeaders initiationHeaders = new HttpHeaders();

    /**
     * The HTTP request object that is currently used to send upload requests or {@code null} before
     * {@link #upload}.
     */
    private HttpRequest currentRequest;

    /**
     * An Input stream of the HTTP media content or {@code null} before {@link #upload}.
     */
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
     * The media content length is used in the "Content-Range" header. If we reached the end of the
     * stream, this variable will be set with the length of the stream. This value is used only in
     * resumable media upload.
     */
    String mediaContentLengthStr = "*";

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
     * Used to cache a single byte when the media content length is unknown or {@code null} for none.
     */
    private Byte cachedByte;

    /**
     * The number of bytes the client had sent to the server so far or {@code 0} for none. It is used
     * for resumable media upload when the media content length is not specified.
     */
    private long totalBytesClientSent;

    /**
     * The number of bytes of the current chunk which was sent to the server or {@code 0} for none.
     * This value equals to chunk size for each chunk the client send to the server, except for the
     * ending chunk.
     */
    private int currentChunkLength;

    /**
     * The content buffer of the current request or {@code null} for none. It is used for resumable
     * media upload when the media content length is not specified. It is instantiated for every
     * request in {@link #setContentAndHeadersOnCurrentRequest} and is set to {@code null} when the
     * request is completed in {@link #upload}.
     */
    private byte currentRequestContentBuffer[];

    /**
     * Whether to disable GZip compression of HTTP content.
     * <p>
     * <p>
     * The default value is {@code false}.
     * </p>
     */
    private boolean disableGZipContent;

    /**
     * The number of bytes the server received so far. This value will not be calculated for direct
     * uploads when the content length is not known in advance.
     */
    // TODO(rmistry): Figure out a way to compute the content length using CountingInputStream.
    private long totalBytesServerReceived;

    private FileMetaData fileMetaDataForUploading;

    boolean isResume;

    /**
     * Construct the {@link MediaHttpUploader}.
     * <p>
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
     * @param mediaContent           The Input stream content of the media to be uploaded
     * @param transport              The transport to use for requests
     * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
     *                               {@code null} for none
     */
    public MediaHttpUploader(AbstractInputStreamContent mediaContent, HttpTransport transport,
                             HttpRequestInitializer httpRequestInitializer) {
        this.mediaContent = Preconditions.checkNotNull(mediaContent);
        this.transport = Preconditions.checkNotNull(transport);
        this.requestFactory = httpRequestInitializer == null
                ? transport.createRequestFactory() : transport.createRequestFactory(httpRequestInitializer);
    }

    /**
     * Executes a direct media upload or resumable media upload conforming to the specifications
     * listed <a href='http://code.google.com/apis/gdata/docs/resumable_upload.html'>here.</a>
     * <p>
     * <p>
     * This method is not reentrant. A new instance of {@link MediaHttpUploader} must be instantiated
     * before upload called be called again.
     * </p>
     * <p>
     * <p>
     * If an error is encountered during the request execution the caller is responsible for parsing
     * the response correctly. For example for JSON errors:
     * <p>
     * <pre>
     * if (!response.isSuccessStatusCode()) {
     * throw GoogleJsonResponseException.from(jsonFactory, response);
     * }
     * </pre>
     * </p>
     * <p>
     * <p>
     * Callers should call {@link HttpResponse#disconnect} when the returned HTTP response object is
     * no longer needed. However, {@link HttpResponse#disconnect} does not have to be called if the
     * response stream is properly closed. Example usage:
     * </p>
     * <p>
     * <pre>
     * HttpResponse response = batch.upload(initiationRequestUrl);
     * try {
     * // process the HTTP response object
     * } finally {
     * response.disconnect();
     * }
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
        Map<String, String> headers = null;
        GenericUrl uploadUrl;
        isResume = fileMetaDataForUploading.getUploadUrl() != null;

        if (isResume) {
            uploadUrl = new GenericUrl(fileMetaDataForUploading.getUploadUrl());
            meta = fileMetaDataForUploading;
        } else {
            // Make initial request to get the unique upload URL.
            HttpResponse initialResponse = executeUploadInitiation(initiationClientRequest);
            if (!initialResponse.isSuccessStatusCode()) {
                // If the initiation request is not successful return it immediately.
                throw new KinveyException("Uploading Metadata Failed");
            }

            try {
                JsonObjectParser jsonObjectParser = (JsonObjectParser) initiationClientRequest.getAbstractKinveyClient().getObjectParser();
                meta = parse(jsonObjectParser, initialResponse);

                if (meta.containsKey("_requiredHeaders")) {
                    //then there are special headers to use in the request to google
                    headers = (Map<String, String>) meta.get("_requiredHeaders");
                }

                notifyListenerWithMetaData(meta);
                if (meta.getUploadUrl() != null) {
                    uploadUrl = new GenericUrl(meta.getUploadUrl());
                } else {
                    throw new KinveyException("_uploadURL is null!", "do not remove _uploadURL in collection hooks for NetworkFileManager!", "The library cannot upload a file without this url");
                }
            } finally {
                initialResponse.disconnect();
            }
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
        while (true) {
            currentRequest = requestFactory.buildPutRequest(uploadUrl, null);
            currentRequest.setSuppressUserAgentSuffix(true);
            setContentAndHeadersOnCurrentRequest(meta.getMimetype());

            // if there are custom headers, add them
            if (headers != null) {
                for (String header : headers.keySet()) {

                    String curHeader = headers.get(header);
                    // then it's a list
                    if (curHeader.contains(", ")) {
                        String[] listheaders = curHeader.split(", ");
                        currentRequest.getHeaders().put(header.toLowerCase(Locale.US),
                                Arrays.asList(listheaders));
                    } else {
                        currentRequest.getHeaders().put(header.toLowerCase(Locale.US), curHeader);
                    }
                }
            }

            try {
                if (isMediaLengthKnown()) {
                    // TODO(rmistry): Support gzipping content for the case where media content length is
                    // known (https://code.google.com/p/google-api-java-client/issues/detail?id=691).
                    response = executeCurrentRequestWithoutGZip(currentRequest);
                } else {
                    response = executeCurrentRequest(currentRequest);
                }
            } catch (IOException e) {
                UploadFileException exception = new UploadFileException("Connection was interrupted", "Retry request", e.getMessage());
                exception.setUploadedFileMetaData(meta);
                throw exception;
            }

            boolean returningResponse = false;
            try {

                if (response.isSuccessStatusCode()) {
                    totalBytesServerReceived = getMediaContentLength();
                    if (mediaContent.getCloseInputStream()) {
                        contentInputStream.close();
                    }
                    updateStateAndNotifyListener(UploadState.UPLOAD_COMPLETE);
                    returningResponse = true;
                    return meta;
                }

                if (response.getStatusCode() != 308) {
                    returningResponse = true;
                    return meta;
                }

                // Check to see if the upload URL has changed on the server.
                String updatedUploadUrl = response.getHeaders().getLocation();
                if (updatedUploadUrl != null) {
                    uploadUrl = new GenericUrl(updatedUploadUrl);
                }

                // we check the amount of bytes the server received so far, because the server may process
                // fewer bytes than the amount of bytes the client had sent
                long newBytesServerReceived = getNextByteIndex(response.getHeaders().getRange());
                // the server can receive any amount of bytes from 0 to current chunk length
                long currentBytesServerReceived = newBytesServerReceived - totalBytesServerReceived;
                if (!isResume) {
                    com.google.api.client.util.Preconditions.checkState(
                            currentBytesServerReceived >= 0 && currentBytesServerReceived <= currentChunkLength);
                }
                long copyBytes = currentChunkLength - currentBytesServerReceived;
                if (isMediaLengthKnown()) {
                    if (copyBytes != 0) {
                        // If the server didn't receive all the bytes the client sent the current position of
                        // the input stream is incorrect. So we should reset the stream and skip those bytes
                        // that the server had already received.
                        // Otherwise (the server got all bytes the client sent), the stream is in its right
                        // position, and we can continue from there
                        contentInputStream.reset();
                        long actualSkipValue = contentInputStream.skip(currentBytesServerReceived);
                        com.google.api.client.util.Preconditions.checkState(currentBytesServerReceived == actualSkipValue);
                    }
                } else if (copyBytes == 0) {
                    // server got all the bytes, so we don't need to use this buffer. Otherwise, we have to
                    // keep the buffer and copy part (or all) of its bytes to the stream we are sending to the
                    // server
                    currentRequestContentBuffer = null;
                }
                totalBytesServerReceived = newBytesServerReceived;

                isResume = false;
                updateStateAndNotifyListener(UploadState.UPLOAD_IN_PROGRESS);
            } finally {
                if (response != null && !returningResponse) {
                    response.disconnect();
                }
            }
        }
    }

    private HttpResponse updateUploadedDataInfo(GenericUrl uploadUrl) throws IOException {
        HttpResponse response = null;
        HttpRequest httpRequest;
        try {
            httpRequest = requestFactory.buildPutRequest(uploadUrl, null);
            httpRequest.setContent(new EmptyContent());
            httpRequest.setSuppressUserAgentSuffix(true);
            httpRequest.getHeaders().setContentRange("bytes */" + getMediaContentLength());
            httpRequest.setThrowExceptionOnExecuteError(false);
            response = httpRequest.execute();
            if (response != null && (response.isSuccessStatusCode() || response.getStatusCode() == 308)) {
                String range = response.getHeaders().getRange();
                range = range.substring(range.lastIndexOf("-") + 1);
                totalBytesServerReceived = Long.parseLong(range) + 1;
                totalBytesClientSent = Long.parseLong(range) + 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.disconnect();
            }
        }
        return response;
    }

    /**
     * Returns the next byte index identifying data that the server has not yet received, obtained
     * from the HTTP Range header (E.g a header of "Range: 0-55" would cause 56 to be returned).
     * <code>null</code> or malformed headers cause 0 to be returned.
     *
     * @param rangeHeader in the HTTP response
     * @return the byte index beginning where the server has yet to receive data
     */
    private long getNextByteIndex(String rangeHeader) {
        if (rangeHeader == null) {
            return 0L;
        }
        return Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('-') + 1)) + 1;
    }

    /**
     * @return {@code true} if the media length is known, otherwise {@code false}
     */
    private boolean isMediaLengthKnown() throws IOException {
        return getMediaContentLength() >= 0;
    }

    /**
     * Uses lazy initialization to compute the media content length.
     * <p>
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


    /**
     * package-level for testing
     **/
    FileMetaData parse(JsonObjectParser initationResponseParser, HttpResponse response) throws IOException {
        try {
            return initationResponseParser.parseAndClose(response.getContent(), response.getContentCharset(), FileMetaData.class);
        } catch (Exception e) {
            return initationResponseParser.parseAndClose(response.getContent(), response.getContentCharset(), FileMetaData[].class)[0];
        }
    }

    /**
     * This method sends a GET request with empty content to get the unique upload URL.
     */
    private HttpResponse executeUploadInitiation(AbstractKinveyClientRequest abstractKinveyClientRequest) throws IOException {
        updateStateAndNotifyListener(UploadState.INITIATION_STARTED);
//        abstractKinveyClientRequest.put("uploadType", "resumable");

        if (abstractKinveyClientRequest.getRequestMethod().equals(HttpMethods.PUT)) {
            abstractKinveyClientRequest.getRequestHeaders().setRange("bytes */" + getMediaContentLength());
            abstractKinveyClientRequest.getRequestHeaders().setContentLength(0L);
        } else {
            initiationHeaders.set(CONTENT_TYPE_HEADER, mediaContent.getType());
            if (isMediaLengthKnown()) {
                initiationHeaders.set(CONTENT_LENGTH_HEADER, getMediaContentLength());
            }
            abstractKinveyClientRequest.setInitiationHeaders(initiationHeaders);
        }

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
     * Executes the current request with some minimal common code.
     *
     * @param request current request
     * @return HTTP response
     */
    private HttpResponse executeCurrentRequestWithoutGZip(HttpRequest request) throws IOException {
        // don't throw an exception so we can let a custom Google exception be thrown
        request.setThrowExceptionOnExecuteError(false);
        // execute the request
        return request.execute();
    }

    /**
     * Executes the current request with some common code that includes exponential backoff and GZip
     * encoding.
     *
     * @param request current request
     * @return HTTP response
     */
    private HttpResponse executeCurrentRequest(HttpRequest request) throws IOException {
        // enable GZip encoding if necessary
        if (!disableGZipContent && !(request.getContent() instanceof EmptyContent)) {
            request.setEncoding(new GZipEncoding());
        }
        // execute request
        return executeCurrentRequestWithoutGZip(request);
    }

    /**
     * Sets the HTTP media content chunk and the required headers that should be used in the upload
     * request.
     */
    private void setContentAndHeadersOnCurrentRequest(String mimeType) throws IOException {

        int blockSize;
        if (isMediaLengthKnown()) {
            // We know exactly what the blockSize will be because we know the media content length.
            blockSize = (int) Math.min(chunkSize, getMediaContentLength() - totalBytesServerReceived);
        } else {
            // Use the chunkSize as the blockSize because we do know what what it is yet.
            blockSize = chunkSize;
        }

        if (isResume) {
            blockSize = 0;
        }

        AbstractInputStreamContent contentChunk;
        int actualBlockSize = blockSize;
        if (isMediaLengthKnown()) {
            // Mark the current position in case we need to retry the request.
            contentInputStream.mark(blockSize);

            InputStream limitInputStream = ByteStreams.limit(contentInputStream, blockSize);
            contentChunk = new InputStreamContent(
                    mimeType, limitInputStream).setRetrySupported(true)
                    .setLength(blockSize).setCloseInputStream(false);
            mediaContentLengthStr = String.valueOf(getMediaContentLength());
        } else {
            // If the media content length is not known we implement a custom buffered input stream that
            // enables us to detect the length of the media content when the last chunk is sent. We
            // accomplish this by always trying to read an extra byte further than the end of the current
            // chunk.
            int actualBytesRead;
            int bytesAllowedToRead;

            // amount of bytes which need to be copied from last chunk buffer
            int copyBytes = 0;
            if (currentRequestContentBuffer == null) {
                bytesAllowedToRead = cachedByte == null ? blockSize + 1 : blockSize;
                currentRequestContentBuffer = new byte[blockSize + 1];
                if (cachedByte != null) {
                    currentRequestContentBuffer[0] = cachedByte;
                }
            } else {
                // currentRequestContentBuffer is not null that means one of the following:
                // 1. This is a request to recover from a server error (e.g. 503)
                // or
                // 2. The server received less bytes than the amount of bytes the client had sent. For
                // example, the client sends bytes 100-199, but the server returns back status code 308,
                // and its "Range" header is "bytes=0-150".
                // In that case, the new request will be constructed from the previous request's byte buffer
                // plus new bytes from the stream.
                copyBytes = (int) (totalBytesClientSent - totalBytesServerReceived);
                // shift copyBytes bytes to the beginning - those are the bytes which weren't received by
                // the server in the last chunk.
                System.arraycopy(currentRequestContentBuffer, currentChunkLength - copyBytes,
                        currentRequestContentBuffer, 0, copyBytes);
                if (cachedByte != null) {
                    // add the last cached byte to the buffer
                    currentRequestContentBuffer[copyBytes] = cachedByte;
                }

                bytesAllowedToRead = blockSize - copyBytes;
            }

            actualBytesRead = ByteStreams.read(
                    contentInputStream, currentRequestContentBuffer, blockSize + 1 - bytesAllowedToRead,
                    bytesAllowedToRead);

            if (actualBytesRead < bytesAllowedToRead) {
                actualBlockSize = copyBytes + Math.max(0, actualBytesRead);
                if (cachedByte != null) {
                    actualBlockSize++;
                    cachedByte = null;
                }

                if (mediaContentLengthStr.equals("*")) {
                    // At this point we know we reached the media content length because we either read less
                    // than the specified chunk size or there is no more data left to be read.
                    mediaContentLengthStr = String.valueOf(totalBytesServerReceived + actualBlockSize);
                }
            } else {
                cachedByte = currentRequestContentBuffer[blockSize];
            }

            contentChunk = new ByteArrayContent(
                    mediaContent.getType(), currentRequestContentBuffer, 0, actualBlockSize);
            totalBytesClientSent = totalBytesServerReceived + actualBlockSize;
        }


        currentChunkLength = actualBlockSize;
        currentRequest.setContent(contentChunk);
        if (actualBlockSize == 0) {
            currentRequest.getHeaders().setContentRange("bytes */" + mediaContentLengthStr);
        } else {
            currentRequest.getHeaders().setContentRange("bytes " + totalBytesServerReceived + "-"
                    + (totalBytesServerReceived + actualBlockSize - 1) + "/" + mediaContentLengthStr);
        }
    }


    /**
     * Returns HTTP content metadata for the media request or {@code null} for none.
     */
    public HttpContent getMetadata() {
        return metadata;
    }

    /**
     * Sets HTTP content metadata for the media request or {@code null} for none.
     */
    public MediaHttpUploader setMetadata(HttpContent metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Returns the HTTP content of the media to be uploaded.
     */
    public HttpContent getMediaContent() {
        return mediaContent;
    }

    /**
     * Returns the transport to use for requests.
     */
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
     * <p>
     * <p>
     * The minimum allowable value is {@link #MINIMUM_CHUNK_SIZE} and the specified chunk size must
     * be a multiple of {@link #MINIMUM_CHUNK_SIZE}.
     * </p>
     * <p>
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
     * Returns whether to disable GZip compression of HTTP content.
     */
    public boolean getDisableGZipContent() {
        return disableGZipContent;
    }

    /**
     * Sets whether to disable GZip compression of HTTP content.
     * <p>
     * <p>
     * By default it is {@code false}.
     * </p>
     * <p>
     * <p>
     * If {@link #setDisableGZipContent(boolean)} is set to false (the default value) then content is
     * gzipped for direct media upload and resumable media uploads when content length is not known.
     * Due to a current limitation, content is not gzipped for resumable media uploads when content
     * length is known; this limitation will be removed in the future.
     * </p>
     */
    public void setDisableGZipContent(boolean disableGZipContent) {
        this.disableGZipContent = disableGZipContent;
    }

    /**
     * Returns the HTTP method used for the initiation request.
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
    private void notifyListenerWithMetaData(FileMetaData meta) {
        if (this.progressListener != null) {
            if (this.progressListener instanceof SaveLinkedResourceClientRequest.MetaUploadListener) {
                ((SaveLinkedResourceClientRequest.MetaUploadListener) this.progressListener).metaDataUploaded(meta);
            }
            if (this.progressListener instanceof MetaUploadProgressListener) {
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
     * <p>
     * <p>
     * Do not use if the specified {@link AbstractInputStreamContent} has no content length specified.
     * Instead, consider using {@link #getNumBytesUploaded} to denote progress.
     * </p>
     *
     * @return the upload progress
     * @throws IllegalArgumentException if the specified {@link AbstractInputStreamContent} has no
     *                                  content length
     */
    public double getProgress() throws IOException {
        Preconditions.checkArgument(getMediaContentLength() >= 0, "Cannot call getProgress() if " +
                "the specified AbstractInputStreamContent has no content length. Use " +
                " getNumBytesUploaded() to denote progress instead.");
        return getMediaContentLength() == 0 ? 0 : (double) bytesUploaded / getMediaContentLength();
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public FileMetaData getFileMetaDataForUploading() {
        return fileMetaDataForUploading;
    }

    public void setFileMetaDataForUploading(FileMetaData fileMetaDataForUploading) {
        this.fileMetaDataForUploading = fileMetaDataForUploading;
    }
}

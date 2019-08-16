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

package com.kinvey.java.core

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.Random

import com.google.api.client.http.AbstractInputStreamContent
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.EmptyContent
import com.google.api.client.http.GZipEncoding
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpContent
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpMethods
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.util.ByteStreams
import com.google.api.client.util.Preconditions.checkState
import com.google.common.base.Preconditions
import com.kinvey.java.KinveyException
import com.kinvey.java.LinkedResources.SaveLinkedResourceClientRequest
import com.kinvey.java.KinveyUploadFileException
import com.kinvey.java.Logger
import com.kinvey.java.model.FileMetaData
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Media HTTP Uploader, with support for both direct and resumable media uploads. Documentation is
 * available [here](http://code.google.com/p/google-api-java-client/wiki/MediaUpload).
 *
 *
 *
 *
 * For resumable uploads, when the media content length is known, if the provided
 * [InputStream] has [InputStream.markSupported] as `false` then it is wrapped in
 * an [BufferedInputStream] to support the [InputStream.mark] and
 * [InputStream.reset] methods required for handling server errors. If the media content
 * length is unknown then each chunk is stored temporarily in memory. This is required to determine
 * when the last chunk is reached.
 *
 *
 *
 *
 *
 * Implementation is not thread-safe.
 *
 *
 * @author rmistry@google.com (Ravi Mistry)
 * @author morgan@kinvey.com  Portions of this code have been modified for Kinvey purposes.
 * Specifically Kinvey needed it to interact with AbstractKinveyClient class and upload
 * media files using Kinvey's propritary upload protocol.
 * @since 1.9
 */
class MediaHttpUploader {

    /**
 * Construct the [MediaHttpUploader].
 *
 * The input stream received by calling [AbstractInputStreamContent.getInputStream] is
 * closed when the upload process is successfully completed. For resumable uploads, when the
 * media content length is known, if the input stream has [InputStream.markSupported] as
 * `false` then it is wrapped in an [BufferedInputStream] to support the
 * [InputStream.mark] and [InputStream.reset] methods required for handling server
 * errors. If the media content length is unknown then each chunk is stored temporarily in memory.
 * This is required to determine when the last chunk is reached.
 *
 *
 * @param mediaContent           The Input stream content of the media to be uploaded
 * @param transport              The transport to use for requests
 * @param httpRequestInitializer The initializer to use when creating an [HttpRequest] or
 * `null` for none
 */
constructor (mediaContent: AbstractInputStreamContent,
             transport: HttpTransport, httpRequestInitializer: HttpRequestInitializer?) {
        this.mediaContent = Preconditions.checkNotNull(mediaContent)
        this.transport = Preconditions.checkNotNull(transport)
        this.retryBackOffCounter = 0
        this.retry404ErrorCounter = 0
        this.requestFactory = if (httpRequestInitializer == null)
            transport.createRequestFactory()
        else
            transport.createRequestFactory(httpRequestInitializer)
    }

    /**
     * The HTTP content of the media to be uploaded.
     */
    private val mediaContent: AbstractInputStreamContent

    /**
     * The transport to use for requests.
     */
    /**
     * Returns the transport to use for requests.
     */
    val transport: HttpTransport

    /**
     * The current state of the uploader.
     */
    /**
     * Gets the current upload state of the uploader.
     *
     * @return the upload state
     */
    var uploadState = UploadState.NOT_STARTED
        private set

    /**
     * The request factory for connections to the server.
     */
    private val requestFactory: HttpRequestFactory

    /**
     * HTTP content metadata of the media to be uploaded or `null` for none.
     */
    private var metadata: HttpContent? = null

    /**
     * has the request been cancelled?
     */
    var isCancelled = false
        private set

    /**
     * The length of the HTTP media content.
     *
     *
     *
     *
     * `0` before it is lazily initialized in [.getMediaContentLength] after which it
     * could still be `0` for empty media content. Will be `< 0` if the media content
     * length has not been specified.
     *
     */
    private var mediaContentLength: Long = 0

    /**
     * Determines if media content length has been calculated yet in [.getMediaContentLength].
     */
    private var isMediaContentLengthCalculated: Boolean = false

    /**
     * The HTTP method used for the initiation request.
     *
     *
     *
     *
     * Can only be [HttpMethods.POST] (for media upload) or [HttpMethods.PUT] (for media
     * update). The default value is [HttpMethods.POST].
     *
     */
    private var initiationRequestMethod = HttpMethods.POST

    /**
     * The HTTP headers used in the initiation request.
     */
    private var initiationHeaders = HttpHeaders()

    /**
     * The HTTP request object that is currently used to send upload requests or `null` before
     * [.upload].
     */
    private var currentRequest: HttpRequest? = null

    /**
     * An Input stream of the HTTP media content or `null` before [.upload].
     */
    private var contentInputStream: InputStream? = null

    /**
     * Determines whether the back off policy is enabled or disabled. If value is set to `false`
     * then server errors are not handled and the upload process will fail if a server error is
     * encountered. Defaults to `true`.
     */
    private var backOffPolicyEnabled = true

    /* Retries after this point do not need to continue increasing backoff time. */
    private val MAXIMUM_BACKOFF_TIME_WAITING = 64000

    /* Retries after this point do not need to continue increasing backoff retry counter. */
    private val MAXIMUM_BACKOFF_RETRY_CONT = 10

    /* Retries after this point do not need to continue increasing error 404 retry counter. */
    private val MAXIMUM_ERROR404_RETRY_CONT = 5

    /**
     * Determines whether direct media upload is enabled or disabled. If value is set to `true`
     * then a direct upload will be done where the whole media content is uploaded in a single request
     * If value is set to `false` then the upload uses the resumable media upload protocol to
     * upload in data chunks. Defaults to `false`.
     */
    private var directUploadEnabled: Boolean = false

    /**
     * Progress listener to send progress notifications to or `null` for none.
     */
    private var progressListener: UploaderProgressListener? = null

    /**
     * The media content length is used in the "Content-Range" header. If we reached the end of the
     * stream, this variable will be set with the length of the stream. This value is used only in
     * resumable media upload.
     */
    private var mediaContentLengthStr = "*"

    /**
     * The total number of bytes uploaded by this uploader. This value will not be calculated for
     * direct uploads when the content length is not known in advance.
     */
    // TODO(rmistry): Figure out a way to compute the content length using CountingInputStream.
    /**
     * Gets the total number of bytes uploaded by this uploader or `0` for direct uploads when
     * the content length is not known.
     *
     * @return the number of bytes uploaded
     */
    val numBytesUploaded: Long = 0

    /**
     * Maximum size of individual chunks that will get uploaded by single HTTP requests. The default
     * value is [.DEFAULT_CHUNK_SIZE].
     */
    private var chunkSize = DEFAULT_CHUNK_SIZE

    /**
     * Used to cache a single byte when the media content length is unknown or `null` for none.
     */
    private var cachedByte: Byte? = null

    /**
     * The number of bytes the client had sent to the server so far or `0` for none. It is used
     * for resumable media upload when the media content length is not specified.
     */
    private var totalBytesClientSent: Long = 0

    /**
     * The number of bytes of the current chunk which was sent to the server or `0` for none.
     * This value equals to chunk size for each chunk the client send to the server, except for the
     * ending chunk.
     */
    private var currentChunkLength: Int = 0

    /**
     * The content buffer of the current request or `null` for none. It is used for resumable
     * media upload when the media content length is not specified. It is instantiated for every
     * request in [.setContentAndHeadersOnCurrentRequest] and is set to `null` when the
     * request is completed in [.upload].
     */
    private var currentRequestContentBuffer: ByteArray? = null

    /**
     * Whether to disable GZip compression of HTTP content.
     *
     *
     *
     *
     * The default value is `false`.
     *
     */
    /**
     * Returns whether to disable GZip compression of HTTP content.
     */
    /**
     * Sets whether to disable GZip compression of HTTP content.
     *
     *
     *
     *
     * By default it is `false`.
     *
     *
     *
     *
     *
     * If [.setDisableGZipContent] is set to false (the default value) then content is
     * gzipped for direct media upload and resumable media uploads when content length is not known.
     * Due to a current limitation, content is not gzipped for resumable media uploads when content
     * length is known; this limitation will be removed in the future.
     *
     */
    var disableGZipContent: Boolean = false

    /**
     * The number of bytes the server received so far. This value will not be calculated for direct
     * uploads when the content length is not known in advance.
     */
    // TODO(rmistry): Figure out a way to compute the content length using CountingInputStream.
    private var totalBytesServerReceived: Long = 0

    /* *
    * Metadata for uploading if previous uploading was interrupted
    * If fileMetaDataForUploading != null then previous uploading was interrupted
    * and some chunk of file was uploaded.
    */
    var fileMetaDataForUploading: FileMetaData? = null

    /**
     * If isResume == true then previous uploading was interrupted
     */
    var isResume: Boolean = false

    /**
     * Counter for backoff retry if connection was interrupted
     */
    private var retryBackOffCounter: Int = 0

    /**
     * Counter for uploading retry if connection was interrupted and error 404 was reason
     */
    private var retry404ErrorCounter: Int = 0

    /**
     * @return `true` if the media length is known, otherwise `false`
     */
    private val isMediaLengthKnown: Boolean
        @Throws(IOException::class)
        get() = getMediaContentLength() >= 0

    /**
     * Gets the upload progress denoting the percentage of bytes that have been uploaded, represented
     * between 0.0 (0%) and 1.0 (100%).
     *
     *
     *
     *
     * Do not use if the specified [AbstractInputStreamContent] has no content length specified.
     * Instead, consider using [.getNumBytesUploaded] to denote progress.
     *
     *
     * @return the upload progress
     * @throws IllegalArgumentException if the specified [AbstractInputStreamContent] has no
     * content length
     */
    val progress: Double
        @Throws(IOException::class)
        get() {
            Preconditions.checkArgument(getMediaContentLength() >= 0, "Cannot call getProgress() if " +
                "the specified AbstractInputStreamContent has no content length. Use " +
                " getNumBytesUploaded() to denote progress instead.")
            return if (getMediaContentLength() == 0L) 0.0 else totalBytesServerReceived.toDouble() / getMediaContentLength()
        }


    /*
        upload process variable to store info of file metadata
    */
    private var meta: FileMetaData? = null

    /*
        upload process variable to store info of request headers
    */
    private var headers: Map<String, String>? = null
    /*
        upload process variable to store upload url path
    */
    private var uploadUrl: GenericUrl? = null

    /**
     * Upload state associated with the Media HTTP uploader.
     */
    enum class UploadState {
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
     * Executes a direct media upload or resumable media upload conforming to the specifications
     * listed [here.](http://code.google.com/apis/gdata/docs/resumable_upload.html)
     *
     *
     *
     *
     * This method is not reentrant. A new instance of [MediaHttpUploader] must be instantiated
     * before upload called be called again.
     *
     *
     *
     *
     *
     * If an error is encountered during the request execution the caller is responsible for parsing
     * the response correctly. For example for JSON errors:
     *
     *
     * <pre>
     * if (!response.isSuccessStatusCode()) {
     * throw GoogleJsonResponseException.from(jsonFactory, response);
     * }
    </pre> *
     *
     *
     *
     *
     *
     * Callers should call [HttpResponse.disconnect] when the returned HTTP response object is
     * no longer needed. However, [HttpResponse.disconnect] does not have to be called if the
     * response stream is properly closed. Example usage:
     *
     *
     *
     * <pre>
     * HttpResponse response = batch.upload(initiationRequestUrl);
     * try {
     * // process the HTTP response object
     * } finally {
     * response.disconnect();
     * }
    </pre> *
     *
     * @param initiationClientRequest
     * @return HTTP response
     * @throws IOException
     */
    @Throws(IOException::class)
    fun upload(initiationClientRequest: AbstractKinveyClientRequest<*>): FileMetaData? {

        isResume = fileMetaDataForUploading?.uploadUrl != null

        if (isResume) {
            uploadUrl = GenericUrl(fileMetaDataForUploading?.uploadUrl)
            meta = fileMetaDataForUploading
        } else {
            Preconditions.checkArgument(uploadState == UploadState.NOT_STARTED)
            updateStateAndNotifyListener(UploadState.UPLOAD_IN_PROGRESS)
            //Make initial request to get the unique upload URL.
            makeInitiationClientRequest(initiationClientRequest)
        }

        createInputStream()

        var response: HttpResponse? = null
        var statusCode: Int

        while (!isCancelled) {
            currentRequest = requestFactory.buildPutRequest(uploadUrl, null)
            currentRequest?.suppressUserAgentSuffix = true
            setContentAndHeadersOnCurrentRequest(meta?.mimetype)

            // if there are custom headers, add them
            addCustomHeaders(headers)

            try {
                response = if (isMediaLengthKnown) {
                    // TODO(rmistry): Support gzipping content for the case where media content length is
                    // known (https://code.google.com/p/google-api-java-client/issues/detail?id=691).
                    executeCurrentRequestWithoutGZip(currentRequest!!)
                } else {
                    executeCurrentRequest(currentRequest)
                }
            } catch (e: IOException) {
                if (retryBackOffCounter < MAXIMUM_BACKOFF_RETRY_CONT) {
                    backOffThreadSleep()
                    retryBackOffCounter++
                    invalidateUnUploadedChunk()
                    isResume = true
                    continue
                } else {
                    throw KinveyUploadFileException("Connection was interrupted", "Retry request", e.message, meta)
                }
            }

            try {

                if (response?.isSuccessStatusCode == true) {
                    totalBytesServerReceived = getMediaContentLength()
                    if (mediaContent.closeInputStream) {
                        contentInputStream?.close()
                    }
                    meta?.uploadUrl = null
                    updateStateAndNotifyListener(UploadState.UPLOAD_COMPLETE)
                    return meta
                }
                statusCode = response?.statusCode ?: 0

                if ((statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504)
                        && retryBackOffCounter < MAXIMUM_BACKOFF_RETRY_CONT) {
                    backOffThreadSleep()
                    retryBackOffCounter++
                    invalidateUnUploadedChunk()
                    isResume = true
                    continue
                }

                if (statusCode == 404 && retry404ErrorCounter < MAXIMUM_ERROR404_RETRY_CONT) {
                    //start upload from the beginning
                    retry404ErrorCounter++
                    invalidateUnUploadedChunk()
                    continue
                }

                if (statusCode != 308) {
                    throw KinveyUploadFileException("File upload failed",
                        "Try to upload file again",
                        "This error usually means that server error on backend side occurs, that could be resolver by reupload", meta)
                }

                //upload was resumed
                retry404ErrorCounter = 0
                retryBackOffCounter = 0

                // Check to see if the upload URL has changed on the server.
                val updatedUploadUrl = response?.headers?.location
                if (updatedUploadUrl != null) {
                    uploadUrl = GenericUrl(updatedUploadUrl)
                }

                // we check the amount of bytes the server received so far, because the server may process
                // fewer bytes than the amount of bytes the client had sent
                val newBytesServerReceived = getNextByteIndex(response?.headers?.range)
                // the server can receive any amount of bytes from 0 to current chunk length
                val currentBytesServerReceived = newBytesServerReceived - totalBytesServerReceived
                if (!isResume) {
                    checkState(currentBytesServerReceived in 0..currentChunkLength)
                }
                val copyBytes = currentChunkLength - currentBytesServerReceived
                if (isMediaLengthKnown) {
                    if (copyBytes != 0L) {
                        // If the server didn't receive all the bytes the client sent the current position of
                        // the input stream is incorrect. So we should reset the stream and skip those bytes
                        // that the server had already received.
                        // Otherwise (the server got all bytes the client sent), the stream is in its right
                        // position, and we can continue from there
                        contentInputStream?.reset()
                        val actualSkipValue = contentInputStream?.skip(currentBytesServerReceived)
                        com.google.api.client.util.Preconditions.checkState(currentBytesServerReceived == actualSkipValue)
                    }
                } else if (copyBytes == 0L) {
                    // server got all the bytes, so we don't need to use this buffer. Otherwise, we have to
                    // keep the buffer and copy part (or all) of its bytes to the stream we are sending to the
                    // server
                    currentRequestContentBuffer = null
                }
                totalBytesServerReceived = newBytesServerReceived

                isResume = false
                updateStateAndNotifyListener(UploadState.UPLOAD_IN_PROGRESS)
            } finally {
                if (response != null) {
                    try {
                        response.disconnect()
                    } catch (t: Throwable) {
                        Logger.INFO("close response failed")
                    }

                }
            }
        }
        return if (isCancelled) null else meta
    }

    /*
        Make initial request to get the unique upload URL.
    */
    @Throws(KinveyException::class)
    private fun makeInitiationClientRequest(initiationClientRequest: AbstractKinveyClientRequest<*>) {
        val initialResponse = executeUploadInitiation(initiationClientRequest)
        if (!initialResponse.isSuccessStatusCode) {
            // If the initiation request is not successful return it immediately.
            throw KinveyException("Uploading Metadata Failed")
        }
        try {
            val jsonObjectParser = initiationClientRequest.abstractKinveyClient.objectParser as JsonObjectParser
            meta = parse(jsonObjectParser, initialResponse)
            meta?.let { metadata ->
                if (metadata.containsKey("_requiredHeaders")) {
                    //then there are special headers to use in the request to google
                    headers = metadata["_requiredHeaders"] as Map<String, String>?
                }
                notifyListenerWithMetaData(metadata)
                if (metadata.uploadUrl != null) {
                    uploadUrl = GenericUrl(metadata.uploadUrl)
                } else {
                    throw KinveyException("_uploadURL is null!",
                        "do not remove _uploadURL in collection hooks for NetworkFileManager!",
                        "The library cannot upload a file without this url")
                }
            }
        } finally {
            initialResponse.disconnect()
            fileMetaDataForUploading = meta
        }
    }

    // if there are custom headers, add them
    private fun addCustomHeaders(headers: Map<String, String>?) {
        headers?.let { map ->
            for (header in map.keys) {
                val curHeader = map[header]
                val curHeaderStr = header.toLowerCase(Locale.US)
                // then it's a list
                currentRequest?.let { request ->
                    if (curHeader?.contains(", ") == true) {
                        val listheaders = curHeader.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        request.headers[curHeaderStr] = listOf(*listheaders)
                    } else {
                        request.headers[curHeaderStr] = curHeader
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun invalidateUnUploadedChunk() {
        currentChunkLength = 0
        totalBytesServerReceived = 0
        createInputStream()
    }

    @Throws(IOException::class)
    private fun createInputStream() {
        // Convert media content into a byte stream to upload in chunks.
        contentInputStream = mediaContent.inputStream
        contentInputStream?.let { stream ->
            if (!stream.markSupported() && getMediaContentLength() >= 0) {
                // If we know the media content length then wrap the stream into a Buffered input stream to
                // support the {@link InputStream#mark} and {@link InputStream#reset} methods required for
                // handling server errors.
                contentInputStream = BufferedInputStream(stream)
            }
        }
    }

    private fun backOffThreadSleep() {
        //use exponential backoff
        try {
            Thread.sleep(min(2.0.pow(retryBackOffCounter.toDouble()) * 1000 + getRandom(1, 1000),
                    MAXIMUM_BACKOFF_TIME_WAITING.toDouble()).toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun updateUploadedDataInfo(uploadUrl: GenericUrl): HttpResponse? {
        var response: HttpResponse? = null
        val httpRequest: HttpRequest
        try {
            httpRequest = requestFactory.buildPutRequest(uploadUrl, null)
            httpRequest.setContent(EmptyContent())
            httpRequest.setSuppressUserAgentSuffix(true)
            httpRequest.getHeaders().contentRange = "bytes */" + getMediaContentLength()
            httpRequest.setThrowExceptionOnExecuteError(false)
            response = httpRequest.execute()
            if (response != null && (response.isSuccessStatusCode || response.statusCode == 308)) {
                var range = response.headers.range
                range = range.substring(range.lastIndexOf("-") + 1)
                val rangeVal = range.toLongOrNull() ?: 0
                totalBytesServerReceived = rangeVal + 1
                totalBytesClientSent = rangeVal + 1
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            response?.disconnect()
        }
        return response
    }

    private fun getRandom(min: Int, max: Int): Int = Random().nextInt(max - min + 1) + min

    /**
     * Returns the next byte index identifying data that the server has not yet received, obtained
     * from the HTTP Range header (E.g a header of "Range: 0-55" would cause 56 to be returned).
     * `null` or malformed headers cause 0 to be returned.
     *
     * @param rangeHeader in the HTTP response
     * @return the byte index beginning where the server has yet to receive data
     */
    private fun getNextByteIndex(rangeHeader: String?): Long {
        return if (rangeHeader == null) {
            0L
        } else {
            val idxDash = rangeHeader.indexOf('-')
            val idx = rangeHeader.substring(idxDash + 1).toLongOrNull() ?: 0
            return idx + 1
        }
    }

    /**
     * Uses lazy initialization to compute the media content length.
     *
     *
     *
     *
     * This is done to avoid throwing an [IOException] in the constructor.
     *
     */
    @Throws(IOException::class)
    private fun getMediaContentLength(): Long {
        if (!isMediaContentLengthCalculated) {
            mediaContentLength = mediaContent.length
            isMediaContentLengthCalculated = true
        }
        return mediaContentLength
    }

    /**
     * package-level for testing
     */
    @Throws(IOException::class)
    fun parse(initationResponseParser: JsonObjectParser, response: HttpResponse): FileMetaData {
        return try {
            initationResponseParser.parseAndClose(response.content, response.contentCharset, FileMetaData::class.java)
        } catch (e: Exception) {
            initationResponseParser.parseAndClose(response.content, response.contentCharset, Array<FileMetaData>::class.java)[0]
        }
    }

    /**
     * This method sends a GET request with empty content to get the unique upload URL.
     */
    @Throws(IOException::class)
    private fun executeUploadInitiation(abstractKinveyClientRequest: AbstractKinveyClientRequest<*>): HttpResponse {
        updateStateAndNotifyListener(UploadState.INITIATION_STARTED)
        //        abstractKinveyClientRequest.put("uploadType", "resumable");

        if (abstractKinveyClientRequest.requestMethod == HttpMethods.PUT) {
            abstractKinveyClientRequest.getRequestHeaders().range = "bytes */" + getMediaContentLength()
            abstractKinveyClientRequest.getRequestHeaders().contentLength = 0L
        } else {
            abstractKinveyClientRequest.getRequestHeaders().set(CONTENT_TYPE_HEADER, mediaContent.type)
            if (isMediaLengthKnown) {
                abstractKinveyClientRequest.getRequestHeaders().set(CONTENT_LENGTH_HEADER, getMediaContentLength())
            }
        }

        val response = abstractKinveyClientRequest.executeUnparsed(false)
        var notificationCompleted = false

        try {
            updateStateAndNotifyListener(UploadState.INITIATION_COMPLETE)
            notificationCompleted = true
        } finally {
            if (!notificationCompleted) {
                response.disconnect()
            }
        }
        return response
    }

    /**
     * Executes the current request with some minimal common code.
     *
     * @param request current request
     * @return HTTP response
     */
    @Throws(IOException::class)
    private fun executeCurrentRequestWithoutGZip(request: HttpRequest?): HttpResponse? {
        // don't throw an exception so we can let a custom Google exception be thrown
        request?.throwExceptionOnExecuteError = false
        // execute the request
        return request?.execute()
    }

    /**
     * Executes the current request with some common code that includes exponential backoff and GZip
     * encoding.
     *
     * @param request current request
     * @return HTTP response
     */
    @Throws(IOException::class)
    private fun executeCurrentRequest(request: HttpRequest?): HttpResponse? {
        // enable GZip encoding if necessary
        if (!disableGZipContent && request?.content !is EmptyContent) { request?.encoding = GZipEncoding() }
        // execute request
        return executeCurrentRequestWithoutGZip(request)
    }

    /**
     * Sets the HTTP media content chunk and the required headers that should be used in the upload
     * request.
     */
    @Throws(IOException::class)
    private fun setContentAndHeadersOnCurrentRequest(mimeType: String?) {

        var blockSize: Int
        if (isMediaLengthKnown) {
            // We know exactly what the blockSize will be because we know the media content length.
            blockSize = Math.min(chunkSize.toLong(), getMediaContentLength() - totalBytesServerReceived).toInt()
        } else {
            // Use the chunkSize as the blockSize because we do know what what it is yet.
            blockSize = chunkSize
        }

        if (isResume) { blockSize = 0 }

        val contentChunk: AbstractInputStreamContent
        var actualBlockSize = blockSize
        if (isMediaLengthKnown) {
            // Mark the current position in case we need to retry the request.
            contentInputStream?.mark(blockSize)

            val limitInputStream = ByteStreams.limit(contentInputStream, blockSize.toLong())
            contentChunk = InputStreamContent(
                mimeType, limitInputStream).setRetrySupported(true)
                .setLength(blockSize.toLong()).setCloseInputStream(false)
            mediaContentLengthStr = getMediaContentLength().toString()
        } else {
            // If the media content length is not known we implement a custom buffered input stream that
            // enables us to detect the length of the media content when the last chunk is sent. We
            // accomplish this by always trying to read an extra byte further than the end of the current
            // chunk.
            val actualBytesRead: Int
            val bytesAllowedToRead: Int
            // amount of bytes which need to be copied from last chunk buffer
            var copyBytes = 0

            if (currentRequestContentBuffer == null) {
                bytesAllowedToRead = if (cachedByte == null) blockSize + 1 else blockSize
                currentRequestContentBuffer = ByteArray(blockSize + 1)
                if (cachedByte != null) {
                    currentRequestContentBuffer!![0] = cachedByte ?: 0
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
                copyBytes = (totalBytesClientSent - totalBytesServerReceived).toInt()
                // shift copyBytes bytes to the beginning - those are the bytes which weren't received by
                // the server in the last chunk.
                System.arraycopy(currentRequestContentBuffer, currentChunkLength - copyBytes,
                        currentRequestContentBuffer, 0, copyBytes)
                if (cachedByte != null) {
                    // add the last cached byte to the buffer
                    currentRequestContentBuffer!![copyBytes] = cachedByte ?: 0
                }

                bytesAllowedToRead = blockSize - copyBytes
            }

            actualBytesRead = ByteStreams.read(
                contentInputStream, currentRequestContentBuffer,
                blockSize + 1 - bytesAllowedToRead, bytesAllowedToRead)

            if (actualBytesRead < bytesAllowedToRead) {
                actualBlockSize = copyBytes + max(0, actualBytesRead)
                if (cachedByte != null) {
                    actualBlockSize++
                    cachedByte = null
                }
                if (mediaContentLengthStr == "*") {
                    // At this point we know we reached the media content length because we either read less
                    // than the specified chunk size or there is no more data left to be read.
                    mediaContentLengthStr = (totalBytesServerReceived + actualBlockSize).toString()
                }
            } else {
                cachedByte = currentRequestContentBuffer!![blockSize]
            }
            contentChunk = ByteArrayContent(mediaContent.type,
                currentRequestContentBuffer, 0, actualBlockSize)
            totalBytesClientSent = totalBytesServerReceived + actualBlockSize
        }
        currentChunkLength = actualBlockSize
        currentRequest?.content = contentChunk
        if (actualBlockSize == 0) {
            currentRequest?.headers?.contentRange = "bytes */$mediaContentLengthStr"
        } else {
            currentRequest?.headers?.contentRange = ("bytes " + totalBytesServerReceived + "-"
                + (totalBytesServerReceived + actualBlockSize - 1) + "/" + mediaContentLengthStr)
        }
    }

    /**
     * Returns HTTP content metadata for the media request or `null` for none.
     */
    fun getMetadata(): HttpContent? = metadata

    /**
     * Sets HTTP content metadata for the media request or `null` for none.
     */
    fun setMetadata(metadata: HttpContent): MediaHttpUploader {
        this.metadata = metadata
        return this
    }

    /**
     * Returns the HTTP content of the media to be uploaded.
     */
    fun getMediaContent(): HttpContent = mediaContent

    /**
     * Sets whether the back off policy is enabled or disabled. If value is set to `false` then
     * server errors are not handled and the upload process will fail if a server error is
     * encountered. Defaults to `true`.
     */
    fun setBackOffPolicyEnabled(backOffPolicyEnabled: Boolean): MediaHttpUploader {
        this.backOffPolicyEnabled = backOffPolicyEnabled
        return this
    }

    /**
     * Returns whether the back off policy is enabled or disabled. If value is set to `false`
     * then server errors are not handled and the upload process will fail if a server error is
     * encountered. Defaults to `true`.
     */
    fun isBackOffPolicyEnabled(): Boolean = backOffPolicyEnabled

    /**
     * Sets whether direct media upload is enabled or disabled. If value is set to `true` then a
     * direct upload will be done where the whole media content is uploaded in a single request. If
     * value is set to `false` then the upload uses the resumable media upload protocol to
     * upload in data chunks. Defaults to `false`.
     *
     * @since 1.9
     */
    fun setDirectUploadEnabled(directUploadEnabled: Boolean): MediaHttpUploader {
        this.directUploadEnabled = directUploadEnabled
        return this
    }

    /**
     * Returns whether direct media upload is enabled or disabled. If value is set to `true`
     * then a direct upload will be done where the whole media content is uploaded in a single
     * request. If value is set to `false` then the upload uses the resumable media upload
     * protocol to upload in data chunks. Defaults to `false`.
     *
     * @since 1.9
     */
    fun isDirectUploadEnabled(): Boolean = directUploadEnabled

    /**
     * Sets the progress listener to send progress notifications to or `null` for none.
     */
    fun setProgressListener(progressListener: UploaderProgressListener): MediaHttpUploader {
        this.progressListener = progressListener
        return this
    }

    /**
     * Returns the progress listener to send progress notifications to or `null` for none.
     */
    fun getProgressListener(): UploaderProgressListener? = progressListener

    /**
     * Sets the maximum size of individual chunks that will get uploaded by single HTTP requests. The
     * default value is [.DEFAULT_CHUNK_SIZE].
     *
     *
     *
     *
     * The minimum allowable value is [.MINIMUM_CHUNK_SIZE] and the specified chunk size must
     * be a multiple of [.MINIMUM_CHUNK_SIZE].
     *
     *
     *
     *
     *
     * Upgrade warning: Prior to version 1.13.0-beta [.setChunkSize] accepted any chunk size
     * above [.MINIMUM_CHUNK_SIZE], it now accepts only multiples of
     * [.MINIMUM_CHUNK_SIZE].
     *
     */
    fun setChunkSize(chunkSize: Int): MediaHttpUploader {
        Preconditions.checkArgument(chunkSize > 0 && chunkSize % MINIMUM_CHUNK_SIZE == 0)
        this.chunkSize = chunkSize
        return this
    }

    /**
     * Returns the maximum size of individual chunks that will get uploaded by single HTTP requests.
     * The default value is [.DEFAULT_CHUNK_SIZE].
     */
    fun getChunkSize(): Int = chunkSize

    /**
     * Returns the HTTP method used for the initiation request.
     *
     *
     *
     *
     * The default value is [HttpMethods.POST].
     *
     *
     * @since 1.12
     */
    fun getInitiationRequestMethod(): String = initiationRequestMethod

    /**
     * Sets the HTTP method used for the initiation request.
     *
     *
     *
     *
     * Can only be [HttpMethods.POST] (for media upload) or [HttpMethods.PUT] (for media
     * update). The default value is [HttpMethods.POST].
     *
     *
     * @since 1.12
     */
    fun setInitiationRequestMethod(initiationRequestMethod: String): MediaHttpUploader {
        Preconditions.checkArgument(initiationRequestMethod == HttpMethods.POST || initiationRequestMethod == HttpMethods.PUT)
        this.initiationRequestMethod = initiationRequestMethod
        return this
    }

    /**
     * Sets the HTTP headers used for the initiation request.
     *
     *
     *
     *
     * Upgrade warning: in prior version 1.12 the initiation headers were of type
     * `GoogleHeaders`, but as of version 1.13 that type is deprecated, so we now use type
     * [HttpHeaders].
     *
     */
    fun setInitiationHeaders(initiationHeaders: HttpHeaders): MediaHttpUploader {
        this.initiationHeaders = initiationHeaders
        return this
    }

    /**
     * Returns the HTTP headers used for the initiation request.
     *
     *
     *
     *
     * Upgrade warning: in prior version 1.12 the initiation headers were of type
     * `GoogleHeaders`, but as of version 1.13 that type is deprecated, so we now use type
     * [HttpHeaders].
     *
     */
    fun getInitiationHeaders(): HttpHeaders = initiationHeaders

    /**
     * Sets the upload state and notifies the progress listener.
     *
     * @param uploadState value to set to
     */
    @Throws(IOException::class)
    private fun updateStateAndNotifyListener(uploadState: UploadState) {
        this.uploadState = uploadState
        progressListener?.progressChanged(this)
    }

    /**
     * Notifies the listener (if there is one) of the updated metadata
     */
    private fun notifyListenerWithMetaData(meta: FileMetaData?) {
        if (this.progressListener != null) {
            if (this.progressListener is SaveLinkedResourceClientRequest.MetaUploadListener) {
                (this.progressListener as SaveLinkedResourceClientRequest.MetaUploadListener).metaDataUploaded(meta)
            }
            if (this.progressListener is MetaUploadProgressListener) {
                (this.progressListener as MetaUploadProgressListener).metaDataRetrieved(meta)
            }
        }
    }

    fun cancel() { this.isCancelled = true }

    companion object {

        /**
         * Upload content type header.
         *
         * @since 1.13
         */
        val CONTENT_LENGTH_HEADER = "X-Upload-Content-Length"

        /**
         * Upload content length header.
         *
         * @since 1.13
         */
        val CONTENT_TYPE_HEADER = "X-Upload-Content-Type"

        internal val MB = 0x100000
        private val KB = 0x400

        /**
         * Minimum number of bytes that can be uploaded to the server (set to 256KB).
         */
        val MINIMUM_CHUNK_SIZE = 256 * KB

        /**
         * Default maximum number of bytes that will be uploaded to the server in any single HTTP request
         * (set to 5 MB).
         */
        val DEFAULT_CHUNK_SIZE = 10 * MB
    }
}

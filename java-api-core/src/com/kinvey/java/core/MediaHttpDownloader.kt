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


import com.google.api.client.http.ExponentialBackOffPolicy
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.json.JsonParser
import com.google.api.client.util.IOUtils
import com.google.common.base.Preconditions

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.util.HashMap
import java.util.Random

import com.google.common.io.ByteStreams
import com.kinvey.java.KinveyDownloadFileException
import com.kinvey.java.KinveyException
import com.kinvey.java.KinveyUploadFileException
import com.kinvey.java.Logger
import com.kinvey.java.model.FileMetaData

/**
 * Media HTTP Downloader, with support for both direct and resumable media downloads. Documentation
 * is available [here](http://code.google.com/p/google-api-java-client/wiki/MediaDownload).
 *
 *
 *
 *
 * Implementation is not thread-safe.
 *
 *
 * @author rmistry@google.com (Ravi Mistry)
 * @author morgan@kinvey.com  Portions of this code have been modified for Kinvey purposes.
 * Specifically Kinvey needed it to interact with AbstractKinveyClient class and download
 * media files using Kinvey's propritary download protocol.
 * @since 1.9
 */
class MediaHttpDownloader
/**
 * Construct the [MediaHttpDownloader].
 *
 * @param transport              The transport to use for requests
 * @param httpRequestInitializer The initializer to use when creating an [HttpRequest] or
 * `null` for none
 */
(transport: HttpTransport,
 httpRequestInitializer: HttpRequestInitializer?) {

    /* Retries after this point do not need to continue increasing backoff time. */
    private val MAXIMUM_BACKOFF_TIME_WAITING = 64000

    /* Retries after this point do not need to continue increasing backoff retry counter. */
    private val MAXIMUM_BACKOFF_RETRY_CONT = 10

    /**
     * The request factory for connections to the server.
     */
    private val requestFactory: HttpRequestFactory

    /**
     * The transport to use for requests.
     */
    /**
     * Returns the transport to use for requests.
     */
    val transport: HttpTransport

    /**
     * Determines whether the back off policy is enabled or disabled. If value is set to `false`
     * then server errors are not handled and the download process will fail if a server error is
     * encountered. Defaults to `true`.
     */
    private var backOffPolicyEnabled = true

    /**
     * Determines whether direct media download is enabled or disabled. If value is set to
     * `true` then a direct download will be done where the whole media content is downloaded in
     * a single request. If value is set to `false` then the download uses the resumable media
     * download protocol to download in data chunks. Defaults to `false`.
     */
    private var directDownloadEnabled = false

    /**
     * Progress listener to send progress notifications to or `null` for none.
     */
    internal var progressListener: DownloaderProgressListener? = null

    /**
     * Maximum size of individual chunks that will get downloaded by single HTTP requests. The default
     * value is [.MAXIMUM_CHUNK_SIZE].
     */
    private var chunkSize = MAXIMUM_CHUNK_SIZE

    /**
     * The length of the HTTP media content or `0` before it is initialized in
     * [.setMediaContentLength].
     */
    private var mediaContentLength: Long = 0

    /**
     * The current state of the downloader.
     */
    /**
     * Gets the current download state of the downloader.
     *
     * @return the download state
     */
    var downloadState = DownloadState.NOT_STARTED
        private set

    /**
     * The total number of bytes downloaded by this downloader.
     */
    /**
     * Gets the total number of bytes downloaded by this downloader.
     *
     * @return the number of bytes downloaded
     */
    var numBytesDownloaded: Long = 0
        private set

    /**
     * The last byte position of the media file we want to download, default value is `-1`.
     *
     *
     *
     *
     * If its value is `-1` it means there is no upper limit on the byte position.
     *
     */
    /**
     * Gets the last byte position of the media file we want to download or `-1` if there is no
     * upper limit on the byte position.
     *
     * @return the last byte position
     * @since 1.13
     */
    var lastBytePosition: Long = -1
        private set

    /**
     * has the request been cancelled?
     */
    var isCancelled = false
        private set

    /**
     * Counter for backoff retry if connection was interrupted
     */
    private var retryBackOffCounter: Int = 0

    /**
     * Gets the download progress denoting the percentage of bytes that have been downloaded,
     * represented between 0.0 (0%) and 1.0 (100%).
     *
     * @return the download progress
     */
    val progress: Double
        get() = if (mediaContentLength == 0L) 0.0 else numBytesDownloaded.toDouble() / mediaContentLength

    /**
     * Download state associated with the Media HTTP downloader.
     */
    enum class DownloadState {
        /**
         * The download process has not started yet.
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
         * Set after a media file chunk is downloaded.
         */
        DOWNLOAD_IN_PROGRESS,

        /**
         * Set after the complete media file is successfully downloaded.
         */
        DOWNLOAD_COMPLETE,

        /**
         * Set when a download cannot be completed because the metadata doesn't exist
         */
        DOWNLOAD_FAILED_FILE_NOT_FOUND

    }

    init {
        this.transport = Preconditions.checkNotNull(transport)
        this.requestFactory = if (httpRequestInitializer == null)
            transport.createRequestFactory()
        else
            transport
                    .createRequestFactory(httpRequestInitializer)
    }


    /**
     * package-level for testing *
     */
    @Throws(IOException::class)
    internal fun parse(initationResponseParser: JsonObjectParser, response: HttpResponse): FileMetaData? {

        val parser = initationResponseParser.jsonFactory.createJsonParser(response.content, response.contentCharset)
        var meta: FileMetaData? = null
        try {
            meta = parser.parse(FileMetaData::class.java, false, null) as FileMetaData
        } catch (e: Exception) {
            try {
                meta = (parser.parse(Array<FileMetaData>::class.java, false, null) as Array<FileMetaData>)[0]
            } catch (arrayError: Exception) {
            }

        } finally {
            response.content.close()
        }
        return meta
    }

    /**
     * Executes a direct media download or a resumable media download.
     *
     *
     *
     *
     * This method does not close the given output stream.
     *
     *
     *
     *
     *
     * This method is not reentrant. A new instance of [MediaHttpDownloader] must be
     * instantiated before download called be called again.
     *
     *
     * @param metaData metadata taken feom kinvey backend that contains download url and other info required for file download
     * @param out      output stream to dump bytes as they stream off the wire
     * @throws IOException
     */
    @Throws(IOException::class)
    fun download(metaData: FileMetaData, out: OutputStream): FileMetaData? {
        Preconditions.checkArgument(downloadState == DownloadState.NOT_STARTED)
        updateStateAndNotifyListener(DownloadState.DOWNLOAD_IN_PROGRESS)
        var isDownloaded = false

        // Make initial request to get the unique upload URL.


        val downloadUrl: GenericUrl
        if (metaData.downloadURL != null) {
            downloadUrl = GenericUrl(metaData.downloadURL!!)
        } else {
            throw KinveyException("_downloadURL is null!", "do not remove _downloadURL in collection hooks for NetworkFileManager!", "The library cannot download a file without this url")
        }

        updateStateAndNotifyListener(DownloadState.INITIATION_COMPLETE)

        val map = metaData.resumeDownloadData
        if (map != null && map.containsKey("LastBytePosition") && map.containsKey("NumBytesDownloaded")) {
            lastBytePosition = metaData.resumeDownloadData!!["LastBytePosition"] as Long
            numBytesDownloaded = metaData.resumeDownloadData!!["NumBytesDownloaded"] as Long
        }

        while (!isCancelled) {
            val currentRequest = requestFactory.buildGetRequest(downloadUrl)
            currentRequest.suppressUserAgentSuffix = true
            var currentRequestLastBytePos = numBytesDownloaded + chunkSize - 1
            if (lastBytePosition != -1L) {
                // If last byte position has been specified use it iff it is smaller than the chunksize.
                currentRequestLastBytePos = Math.min(lastBytePosition, currentRequestLastBytePos)
            }

            // set Range header (if necessary)
            if (metaData.fileSize !== 0L && (numBytesDownloaded != 0L || currentRequestLastBytePos != -1L)) {
                val rangeHeader = StringBuilder()
                rangeHeader.append("bytes=").append(numBytesDownloaded).append("-")
                if (currentRequestLastBytePos != -1L) {
                    rangeHeader.append(currentRequestLastBytePos)
                }
                currentRequest.headers.range = rangeHeader.toString()
            }
            if (backOffPolicyEnabled) {
                // Set ExponentialBackOffPolicy as the BackOffPolicy of the HTTP Request which will
                // retry the same request again if there is a server error.
                currentRequest.backOffPolicy = ExponentialBackOffPolicy()
            }

            var response: HttpResponse? = null
            try {
                response = currentRequest.execute()
            } catch (e: Exception) {
                if (retryBackOffCounter < MAXIMUM_BACKOFF_RETRY_CONT) {
                    backOffThreadSleep()
                    retryBackOffCounter++
                    continue
                } else {
                    val kinveyUploadFileException = KinveyDownloadFileException(e.message ?: "")
                    val hashMap = HashMap<String, Any>()
                    hashMap["LastBytePosition"] = lastBytePosition
                    hashMap["NumBytesDownloaded"] = numBytesDownloaded
                    metaData.resumeDownloadData = hashMap
                    kinveyUploadFileException.downloadedFileMetaData = metaData
                    throw kinveyUploadFileException
                }
            }

            try {
                if (response != null && response.content != null) {
                    val `is` = response.content
                    val bytes = ByteStreams.toByteArray(`is`)
                    val bis = ByteArrayInputStream(bytes)
                    IOUtils.copy(bis, out)
                }
            } catch (e: Exception) {
                if (retryBackOffCounter < MAXIMUM_BACKOFF_RETRY_CONT) {
                    backOffThreadSleep()
                    retryBackOffCounter++
                    continue
                } else {
                    val kinveyUploadFileException = KinveyDownloadFileException(e.message ?: "")
                    val hashMap = HashMap<String, Any>()
                    hashMap["LastBytePosition"] = lastBytePosition
                    hashMap["NumBytesDownloaded"] = numBytesDownloaded
                    metaData.resumeDownloadData = hashMap
                    kinveyUploadFileException.downloadedFileMetaData = metaData
                    throw kinveyUploadFileException
                }
            } finally {
                response?.disconnect()
            }

            val contentRange = response!!.headers.contentRange
            val nextByteIndex = getNextByteIndex(contentRange)
            setMediaContentLength(contentRange)
            if (mediaContentLength <= nextByteIndex) {
                // All required bytes have been downloaded from the server.
                numBytesDownloaded = mediaContentLength
                updateStateAndNotifyListener(DownloadState.DOWNLOAD_COMPLETE)
                isDownloaded = true
                return metaData
            }

            numBytesDownloaded = nextByteIndex
            updateStateAndNotifyListener(DownloadState.DOWNLOAD_IN_PROGRESS)
        }

        if (isCancelled) {
            Logger.INFO("DOWNLOAD REQUEST cancelled")
            out.flush()
        }

        Logger.INFO("isDownloaded: $isDownloaded")
        return if (isDownloaded) metaData else null
    }


    private fun backOffThreadSleep() {
        //use exponential backoff
        try {
            Thread.sleep(Math.min(Math.pow(2.0, retryBackOffCounter.toDouble()) * 1000 + getRandom(1, 1000), MAXIMUM_BACKOFF_TIME_WAITING.toDouble()).toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    private fun getRandom(min: Int, max: Int): Int {
        return Random().nextInt(max - min + 1) + min
    }

    /**
     * Returns the next byte index identifying data that the server has not yet sent out, obtained
     * from the HTTP Content-Range header (E.g a header of "Content-Range: 0-55/1000" would cause 56
     * to be returned). `null` headers cause 0 to be returned.
     *
     * @param rangeHeader in the HTTP response
     * @return the byte index beginning where the server has yet to send out data
     */
    private fun getNextByteIndex(rangeHeader: String?): Long {
        return if (rangeHeader == null) {
            0L
        } else java.lang.Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('-') + 1,
                rangeHeader.indexOf('/'))) + 1
    }

    /**
     * Sets the total number of bytes that have been downloaded of the media resource.
     *
     *
     *
     *
     * If a download was aborted mid-way due to a connection failure then users can resume the
     * download from the point where it left off.
     *
     *
     *
     *
     *
     * Use [.setContentRange] if you need to specify both the bytes downloaded and the last byte
     * position.
     *
     *
     * @param bytesDownloaded The total number of bytes downloaded
     */
    fun setBytesDownloaded(bytesDownloaded: Long): MediaHttpDownloader {
        Preconditions.checkArgument(bytesDownloaded >= 0)
        this.numBytesDownloaded = bytesDownloaded
        return this
    }

    /**
     * Sets the content range of the next download request. Eg: bytes=firstBytePos-lastBytePos.
     *
     *
     *
     *
     * If a download was aborted mid-way due to a connection failure then users can resume the
     * download from the point where it left off.
     *
     *
     *
     *
     *
     * Use [.setBytesDownloaded] if you only need to specify the first byte position.
     *
     *
     * @param firstBytePos The first byte position in the content range string
     * @param lastBytePos  The last byte position in the content range string.
     * @since 1.13
     */
    fun setContentRange(firstBytePos: Long, lastBytePos: Int): MediaHttpDownloader {
        Preconditions.checkArgument(lastBytePos >= firstBytePos)
        setBytesDownloaded(firstBytePos)
        this.lastBytePosition = lastBytePos.toLong()
        return this
    }

    /**
     * Sets the media content length from the HTTP Content-Range header (E.g a header of
     * "Content-Range: 0-55/1000" would cause 1000 to be set. `null` headers do not set
     * anything.
     *
     * @param rangeHeader in the HTTP response
     */
    private fun setMediaContentLength(rangeHeader: String?) {
        if (rangeHeader == null) {
            return
        }
        if (mediaContentLength == 0L) {
            mediaContentLength = java.lang.Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('/') + 1))
        }
    }

    /**
     * Returns whether direct media download is enabled or disabled. If value is set to `true`
     * then a direct download will be done where the whole media content is downloaded in a single
     * request. If value is set to `false` then the download uses the resumable media download
     * protocol to download in data chunks. Defaults to `false`.
     */
    fun isDirectDownloadEnabled(): Boolean {
        return directDownloadEnabled
    }

    /**
     * Returns whether direct media download is enabled or disabled. If value is set to `true`
     * then a direct download will be done where the whole media content is downloaded in a single
     * request. If value is set to `false` then the download uses the resumable media download
     * protocol to download in data chunks. Defaults to `false`.
     */
    fun setDirectDownloadEnabled(directDownloadEnabled: Boolean): MediaHttpDownloader {
        this.directDownloadEnabled = directDownloadEnabled
        return this
    }

    /**
     * Sets the progress listener to send progress notifications to or `null` for none.
     */
    fun setProgressListener(
            progressListener: DownloaderProgressListener): MediaHttpDownloader {
        this.progressListener = progressListener
        return this
    }

    /**
     * Returns the progress listener to send progress notifications to or `null` for none.
     */
    fun getProgressListener(): DownloaderProgressListener? {
        return progressListener
    }

    /**
     * Sets whether the back off policy is enabled or disabled. If value is set to `false` then
     * server errors are not handled and the download process will fail if a server error is
     * encountered. Defaults to `true`.
     */
    fun setBackOffPolicyEnabled(backOffPolicyEnabled: Boolean): MediaHttpDownloader {
        this.backOffPolicyEnabled = backOffPolicyEnabled
        return this
    }

    /**
     * Returns whether the back off policy is enabled or disabled. If value is set to `false`
     * then server errors are not handled and the download process will fail if a server error is
     * encountered. Defaults to `true`.
     */
    fun isBackOffPolicyEnabled(): Boolean {
        return backOffPolicyEnabled
    }

    /**
     * Sets the maximum size of individual chunks that will get downloaded by single HTTP requests.
     * The default value is [.MAXIMUM_CHUNK_SIZE].
     *
     *
     *
     *
     * The maximum allowable value is [.MAXIMUM_CHUNK_SIZE].
     *
     */
    fun setChunkSize(chunkSize: Int): MediaHttpDownloader {
        Preconditions.checkArgument(chunkSize > 0 && chunkSize <= MAXIMUM_CHUNK_SIZE)
        this.chunkSize = chunkSize
        return this
    }

    /**
     * Returns the maximum size of individual chunks that will get downloaded by single HTTP requests.
     * The default value is [.MAXIMUM_CHUNK_SIZE].
     */
    fun getChunkSize(): Int {
        return chunkSize
    }

    /**
     * Sets the download state and notifies the progress listener.
     *
     * @param downloadState value to set to
     */
    @Throws(IOException::class)
    protected fun updateStateAndNotifyListener(downloadState: DownloadState) {
        this.downloadState = downloadState
        if (progressListener != null) {
            progressListener!!.progressChanged(this)
        }
    }

    fun cancel() {
        this.isCancelled = true
    }

    companion object {

        /**
         * Default maximum number of bytes that will be downloaded from the server in any single HTTP
         * request. Set to 5MB because that is average value in terms of performance and resume if chunk download fails.
         * for resumable download - the lower chunk - the more precise we can define the last known successfull download position,
         * but more requests will go to the server, so we decide to stay with 5MB
         */
        val MAXIMUM_CHUNK_SIZE = 5 * MediaHttpUploader.MB
    }

}

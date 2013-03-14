/*
 * Copyright (c) 2013 Kinvey Inc.
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


import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ExponentialBackOffPolicy;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.OutputStream;

import com.kinvey.java.model.UriLocResponse;

/**
 * Media HTTP Downloader, with support for both direct and resumable media downloads. Documentation
 * is available <a
 * href='http://code.google.com/p/google-api-java-client/wiki/MediaDownload'>here</a>.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.9
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public class MediaHttpDownloader {

    /**
     * Download state associated with the Media HTTP downloader.
     */
    public enum DownloadState {
        /** The download process has not started yet. */
        NOT_STARTED,

        /** Set before the initiation request is sent. */
        INITIATION_STARTED,

        /** Set after the initiation request completes. */
        INITIATION_COMPLETE,

        /** Set after a media file chunk is downloaded. */
        DOWNLOAD_IN_PROGRESS,

        /** Set after the complete media file is successfully downloaded. */
        DOWNLOAD_COMPLETE
    }

    /**
     * Default maximum number of bytes that will be downloaded from the server in any single HTTP
     * request. Set to 32MB because that is the maximum App Engine request size.
     */
    public static final int MAXIMUM_CHUNK_SIZE = 32 * MediaHttpUploader.MB;

    /** The request factory for connections to the server. */
    private final HttpRequestFactory requestFactory;

    /** The transport to use for requests. */
    private final HttpTransport transport;

    /**
     * Determines whether the back off policy is enabled or disabled. If value is set to {@code false}
     * then server errors are not handled and the download process will fail if a server error is
     * encountered. Defaults to {@code true}.
     */
    private boolean backOffPolicyEnabled = true;

    /**
     * Determines whether direct media download is enabled or disabled. If value is set to
     * {@code true} then a direct download will be done where the whole media content is downloaded in
     * a single request. If value is set to {@code false} then the download uses the resumable media
     * download protocol to download in data chunks. Defaults to {@code false}.
     */
    private boolean directDownloadEnabled = false;

    /**
     * Progress listener to send progress notifications to or {@code null} for none.
     */
    private DownloaderProgressListener progressListener;

    /**
     * Maximum size of individual chunks that will get downloaded by single HTTP requests. The default
     * value is {@link #MAXIMUM_CHUNK_SIZE}.
     */
    private int chunkSize = MAXIMUM_CHUNK_SIZE;

    /**
     * The length of the HTTP media content or {@code 0} before it is initialized in
     * {@link #setMediaContentLength}.
     */
    private long mediaContentLength;

    /** The current state of the downloader. */
    private DownloadState downloadState = DownloadState.NOT_STARTED;

    /** The total number of bytes downloaded by this downloader. */
    private long bytesDownloaded;

    /**
     * The last byte position of the media file we want to download, default value is {@code -1}.
     *
     * <p>
     * If its value is {@code -1} it means there is no upper limit on the byte position.
     * </p>
     */
    private long lastBytePos = -1;

    /**
     * Construct the {@link MediaHttpDownloader}.
     *
     * @param transport The transport to use for requests
     * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
     *        {@code null} for none
     */
    public MediaHttpDownloader(HttpTransport transport,
                               HttpRequestInitializer httpRequestInitializer) {
        this.transport = Preconditions.checkNotNull(transport);
        this.requestFactory =
                httpRequestInitializer == null ? transport.createRequestFactory() : transport
                        .createRequestFactory(httpRequestInitializer);
    }


    /**
     * @author m0rganic
     *
     */
    public static class DownloadUrlResponse extends UriLocResponse {

        /**
         * @return the string representing the remote location of the file to download
         */
        public String getDownloadLoc() {
            return super.getBlobTemporaryUri();
        }

    }

    /** package-level for testing **/
    DownloadUrlResponse parse (JsonObjectParser parser, HttpResponse response) throws IOException {
        return parser.parseAndClose(response.getContent(), response.getContentCharset(), DownloadUrlResponse.class);
    }

    /**
     *
     * Executes a direct media download or a resumable media download.
     *
     * <p>
     * This method does not close the given output stream.
     * </p>
     *
     * <p>
     * This method is not reentrant. A new instance of {@link MediaHttpDownloader} must be
     * instantiated before download called be called again.
     * </p>
     *
     * @param initiationClientRequest request object used to request unique uri from kinvey
     * @param out output stream to dump bytes as they stream off the wire
     * @throws IOException
     */
    public void download(AbstractKinveyClientRequest initiationClientRequest, OutputStream out)
            throws IOException {
        Preconditions.checkArgument(downloadState == DownloadState.NOT_STARTED);
        updateStateAndNotifyListener(DownloadState.DOWNLOAD_IN_PROGRESS);

        // Make initial request to get the unique upload URL.
        DownloadUrlResponse initialResponse = executeDownloadInitiation(initiationClientRequest);
        GenericUrl downloadUrl;
        downloadUrl = new GenericUrl(initialResponse.getDownloadLoc());
        updateStateAndNotifyListener(DownloadState.INITIATION_COMPLETE);

        while (true) {
            HttpRequest currentRequest = requestFactory.buildGetRequest(downloadUrl);
            currentRequest.setSuppressUserAgentSuffix(true);
            long currentRequestLastBytePos = bytesDownloaded + chunkSize - 1;
            if (lastBytePos != -1) {
                // If last byte position has been specified use it iff it is smaller than the chunksize.
                currentRequestLastBytePos = Math.min(lastBytePos, currentRequestLastBytePos);
            }
            currentRequest.getHeaders().setRange(
                    "bytes=" + bytesDownloaded + "-" + currentRequestLastBytePos);

            if (backOffPolicyEnabled) {
                // Set ExponentialBackOffPolicy as the BackOffPolicy of the HTTP Request which will
                // retry the same request again if there is a server error.
                currentRequest.setBackOffPolicy(new ExponentialBackOffPolicy());
            }

            HttpResponse response = currentRequest.execute();
            if (response.getContent() != null)
                AbstractInputStreamContent.copy(response.getContent(), out);

            String contentRange = response.getHeaders().getContentRange();
            long nextByteIndex = getNextByteIndex(contentRange);
            setMediaContentLength(contentRange);

            if (mediaContentLength <= nextByteIndex) {
                // All required bytes have been downloaded from the server.
                bytesDownloaded = mediaContentLength;
                updateStateAndNotifyListener(DownloadState.DOWNLOAD_COMPLETE);
                return;
            }

            bytesDownloaded = nextByteIndex;
            updateStateAndNotifyListener(DownloadState.DOWNLOAD_IN_PROGRESS);
        }
    }


    /**
     *
     * @param initiationRequest
     * @return
     * @throws IOException
     */
    private DownloadUrlResponse executeDownloadInitiation(AbstractKinveyClientRequest initiationRequest) throws IOException {
        updateStateAndNotifyListener(DownloadState.INITIATION_STARTED);
        HttpResponse response = initiationRequest.executeUnparsed();
        JsonObjectParser jsonObjectParser = (JsonObjectParser) initiationRequest.getAbstractKinveyClient().getObjectParser();
        return parse(jsonObjectParser, response);
    }


    /**
     * Returns the next byte index identifying data that the server has not yet sent out, obtained
     * from the HTTP Content-Range header (E.g a header of "Content-Range: 0-55/1000" would cause 56
     * to be returned). <code>null</code> headers cause 0 to be returned.
     *
     * @param rangeHeader in the HTTP response
     * @return the byte index beginning where the server has yet to send out data
     */
    private long getNextByteIndex(String rangeHeader) {
        if (rangeHeader == null) {
            return 0L;
        }
        return Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('-') + 1,
                rangeHeader.indexOf('/'))) + 1;
    }

    /**
     * Sets the total number of bytes that have been downloaded of the media resource.
     *
     * <p>
     * If a download was aborted mid-way due to a connection failure then users can resume the
     * download from the point where it left off.
     * </p>
     *
     * <p>
     * Use {@link #setContentRange} if you need to specify both the bytes downloaded and the last byte
     * position.
     * </p>
     *
     * @param bytesDownloaded The total number of bytes downloaded
     */
    public MediaHttpDownloader setBytesDownloaded(long bytesDownloaded) {
        Preconditions.checkArgument(bytesDownloaded >= 0);
        this.bytesDownloaded = bytesDownloaded;
        return this;
    }

    /**
     * Sets the content range of the next download request. Eg: bytes=firstBytePos-lastBytePos.
     *
     * <p>
     * If a download was aborted mid-way due to a connection failure then users can resume the
     * download from the point where it left off.
     * </p>
     *
     * <p>
     * Use {@link #setBytesDownloaded} if you only need to specify the first byte position.
     * </p>
     *
     * @param firstBytePos The first byte position in the content range string
     * @param lastBytePos The last byte position in the content range string.
     * @since 1.13
     */
    public MediaHttpDownloader setContentRange(long firstBytePos, int lastBytePos) {
        Preconditions.checkArgument(lastBytePos >= firstBytePos);
        setBytesDownloaded(firstBytePos);
        this.lastBytePos = lastBytePos;
        return this;
    }

    /**
     * Sets the media content length from the HTTP Content-Range header (E.g a header of
     * "Content-Range: 0-55/1000" would cause 1000 to be set. <code>null</code> headers do not set
     * anything.
     *
     * @param rangeHeader in the HTTP response
     */
    private void setMediaContentLength(String rangeHeader) {
        if (rangeHeader == null) {
            return;
        }
        if (mediaContentLength == 0) {
            mediaContentLength = Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('/') + 1));
        }
    }

    /**
     * Returns whether direct media download is enabled or disabled. If value is set to {@code true}
     * then a direct download will be done where the whole media content is downloaded in a single
     * request. If value is set to {@code false} then the download uses the resumable media download
     * protocol to download in data chunks. Defaults to {@code false}.
     */
    public boolean isDirectDownloadEnabled() {
        return directDownloadEnabled;
    }

    /**
     * Returns whether direct media download is enabled or disabled. If value is set to {@code true}
     * then a direct download will be done where the whole media content is downloaded in a single
     * request. If value is set to {@code false} then the download uses the resumable media download
     * protocol to download in data chunks. Defaults to {@code false}.
     */
    public MediaHttpDownloader setDirectDownloadEnabled(boolean directDownloadEnabled) {
        this.directDownloadEnabled = directDownloadEnabled;
        return this;
    }

    /**
     * Sets the progress listener to send progress notifications to or {@code null} for none.
     */
    public MediaHttpDownloader setProgressListener(
            DownloaderProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    /**
     * Returns the progress listener to send progress notifications to or {@code null} for none.
     */
    public DownloaderProgressListener getProgressListener() {
        return progressListener;
    }

    /**
     * Sets whether the back off policy is enabled or disabled. If value is set to {@code false} then
     * server errors are not handled and the download process will fail if a server error is
     * encountered. Defaults to {@code true}.
     */
    public MediaHttpDownloader setBackOffPolicyEnabled(boolean backOffPolicyEnabled) {
        this.backOffPolicyEnabled = backOffPolicyEnabled;
        return this;
    }

    /**
     * Returns whether the back off policy is enabled or disabled. If value is set to {@code false}
     * then server errors are not handled and the download process will fail if a server error is
     * encountered. Defaults to {@code true}.
     */
    public boolean isBackOffPolicyEnabled() {
        return backOffPolicyEnabled;
    }

    /** Returns the transport to use for requests. */
    public HttpTransport getTransport() {
        return transport;
    }

    /**
     * Sets the maximum size of individual chunks that will get downloaded by single HTTP requests.
     * The default value is {@link #MAXIMUM_CHUNK_SIZE}.
     *
     * <p>
     * The maximum allowable value is {@link #MAXIMUM_CHUNK_SIZE}.
     * </p>
     */
    public MediaHttpDownloader setChunkSize(int chunkSize) {
        Preconditions.checkArgument(chunkSize > 0 && chunkSize <= MAXIMUM_CHUNK_SIZE);
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * Returns the maximum size of individual chunks that will get downloaded by single HTTP requests.
     * The default value is {@link #MAXIMUM_CHUNK_SIZE}.
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Gets the total number of bytes downloaded by this downloader.
     *
     * @return the number of bytes downloaded
     */
    public long getNumBytesDownloaded() {
        return bytesDownloaded;
    }

    /**
     * Gets the last byte position of the media file we want to download or {@code -1} if there is no
     * upper limit on the byte position.
     *
     * @return the last byte position
     * @since 1.13
     */
    public long getLastBytePosition() {
        return lastBytePos;
    }

    /**
     * Sets the download state and notifies the progress listener.
     *
     * @param downloadState value to set to
     */
    private void updateStateAndNotifyListener(DownloadState downloadState) throws IOException {
        this.downloadState = downloadState;
        if (progressListener != null) {
            progressListener.progressChanged(this);
        }
    }

    /**
     * Gets the current download state of the downloader.
     *
     * @return the download state
     */
    public DownloadState getDownloadState() {
        return downloadState;
    }

    /**
     * Gets the download progress denoting the percentage of bytes that have been downloaded,
     * represented between 0.0 (0%) and 1.0 (100%).
     *
     * @return the download progress
     */
    public double getProgress() {
        return mediaContentLength == 0 ? 0 : (double) bytesDownloaded / mediaContentLength;
    }
}
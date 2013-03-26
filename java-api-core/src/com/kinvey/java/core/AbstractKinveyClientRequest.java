/*
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
import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author m0rganic
 */
public abstract class AbstractKinveyClientRequest<T> extends GenericData {


    /**
     * Kinvey JSON client *
     */
    private final AbstractKinveyClient abstractKinveyClient;

    /**
     * HTTP method *
     */
    private final String requestMethod;

    /**
     * URI template of the path relative to the base url *
     */
    private final String uriTemplate;

    /**
     * http headers to be sent along with the request *
     */
    private HttpHeaders requestHeaders = new HttpHeaders();

    /**
     * http content or {@code null} if none is set *
     */
    private final HttpContent httpContent;

    /**
     * response headers of the last executed request, {@code null} before the request is made *
     */
    private HttpHeaders lastResponseHeaders;

    /**
     * response status code of the last executed request, {@code -1} before the request is made *
     */
    private int lastResponseCode = -1;

    /**
     * response status message of the last executed request, {@code null} before the request is made *
     */
    private String lastResponseMessage;

    /**
     * Response class to parse into *
     */
    private final Class<T> responseClass;

    /**
     * File uploader or {@code null} if none is set *
     */
    private MediaHttpUploader uploader;

    /**
     * File downloader or {@code null} if none is set *
     */
    private MediaHttpDownloader downloader;

    /**
     * {@link BackOffPolicy} to use for retries
     */
    private BackOffPolicy requestBackoffPolicy;

    @Key
    private String appKey;

    /**
     * @param abstractKinveyClient the abstract kinvey client
     * @param requestMethod the request method, PUT, GET, POST, or DELETE
     * @param uriTemplate valid uri template
     * @param httpContent object to send as the message body or {@code null} if none
     * @param responseClass expected type in the response of this request
     */
    protected AbstractKinveyClientRequest(AbstractKinveyClient abstractKinveyClient,
                                          String requestMethod, String uriTemplate, HttpContent httpContent,
                                          Class<T> responseClass) {
        Preconditions.checkNotNull(abstractKinveyClient, "abstractKinveyClient must not be null");
        Preconditions.checkNotNull(requestMethod, "requestMethod must not be null");
        this.abstractKinveyClient = abstractKinveyClient;
        this.requestMethod = requestMethod;
        this.uriTemplate = uriTemplate;
        this.responseClass = responseClass;
        this.httpContent = httpContent;
        this.requestBackoffPolicy = abstractKinveyClient.getBackoffPolicy();
    }

    /**
     * @return the abstractKinveyClient
     */
    public AbstractKinveyClient getAbstractKinveyClient() {
        return abstractKinveyClient;
    }

    /**
     * @return the requestMethod
     */
    public final String getRequestMethod() {
        return requestMethod;
    }

    /**
     * @return the uriTemplate
     */
    public final String getUriTemplate() {
        return uriTemplate;
    }

    /**
     * @return the httpHeaders
     */
    public final HttpHeaders getRequestHeaders() {
        return requestHeaders;
    }

    public AbstractKinveyClientRequest<T> setRequestHeaders(HttpHeaders headers) {
        this.requestHeaders = headers;
        return this;
    }

    /**
     * @return the httpContent
     */
    public final HttpContent getHttpContent() {
        return httpContent;
    }

    /**
     * @return the lastResponseHeaders
     */
    public final HttpHeaders getLastResponseHeaders() {
        return lastResponseHeaders;
    }

    /**
     * @return the lastResponseCode
     */
    public final int getLastResponseCode() {
        return lastResponseCode;
    }

    /**
     * @return the lastResponseMessage
     */
    public final String getLastResponseMessage() {
        return lastResponseMessage;
    }

    /**
     * @return the responseClass
     */
    public final Class<T> getResponseClass() {
        return responseClass;
    }

    /**
     *
     * @return the Backoff Policy
     */
    public BackOffPolicy getRequestBackoffPolicy() {
        return requestBackoffPolicy;
    }

    /**
     * Identical to calling {@code AbstractKinveyClientRequest.initializeMediaHttpUploader(content, null)}
     */
    protected void initializeMediaHttpUploader(AbstractInputStreamContent content) {
        initializeMediaHttpUploader(content, null);
    }

    /**
     * Sets up this request object to be used for uploading media.
     *
     * @param content data to be uploaded
     * @param progressListener an object to be notified of the different state changes as the upload progresses.
     *                         Optional {@code null} can be passed in.
     */
    protected void initializeMediaHttpUploader(AbstractInputStreamContent content, UploaderProgressListener progressListener) {
        HttpRequestFactory requestFactory = abstractKinveyClient.getRequestFactory();
        uploader = createMediaHttpUploader(content, requestFactory);
        uploader.setDirectUploadEnabled(true);
        uploader.setDisableGZipContent(true);
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

    /**
     * @return the uploader
     */
    public final MediaHttpUploader getUploader() {
        return uploader;
    }

    protected final void initializeMediaHttpDownloader(DownloaderProgressListener progressListener) {
        HttpRequestFactory requestFactory = abstractKinveyClient.getRequestFactory();
        downloader = new MediaHttpDownloader(requestFactory.getTransport(), requestFactory.getInitializer());
        downloader.setDirectDownloadEnabled(true);
        downloader.setProgressListener(progressListener);
    }

    /**
     * @return the downloader
     */
    public final MediaHttpDownloader getDownloader() {
        return downloader;
    }

    /**
     * @return
     */
    protected GenericUrl buildHttpRequestUrl() {
        return new GenericUrl(UriTemplate.expand(abstractKinveyClient.getBaseUrl(), uriTemplate, this, true));
    }

    /**
     * @return
     * @throws IOException
     */
    public HttpRequest buildHttpRequest() throws IOException {
        HttpRequest httpRequest = getAbstractKinveyClient()
                .getRequestFactory()
                .buildRequest(requestMethod, buildHttpRequestUrl(), httpContent);
        httpRequest.setParser(getAbstractKinveyClient().getObjectParser());
        httpRequest.setSuppressUserAgentSuffix(true);
        httpRequest.setRetryOnExecuteIOException(true);
        httpRequest.setBackOffPolicy(this.requestBackoffPolicy);
        // custom methods may use POST with no content but require a Content-Length header
        if (httpContent == null && (requestMethod.equals(HttpMethods.POST)
                || requestMethod.equals(HttpMethods.PUT))) {
            httpRequest.setContent(new EmptyContent());
        }
        httpRequest.getHeaders().putAll(requestHeaders);
        return httpRequest;
    }

    public HttpResponse executeUnparsed() throws IOException {
        return executeUnparsed(uploader != null);
    }

    /**
     * Executes the http request and returns the raw {@link HttpResponse}. If
     * throwExceptionOnError is {@code true} an {@link HttpResponseException} is
     * thrown, subclasses may override this method to customize the behavior.
     * <p>
     * Callers are responsible for disconnecting the HTTP response by calling
     * {@link HttpResponse#disconnect}. Example usage:
     * </p>
     *
     * <pre>
         HttpResponse response = request.executeUnparsed();
         try {
         // process response..
         } finally {
         response.disconnect();
         }
     * </pre>
     *
     * <p>
     * Subclasses may override by calling the super implementation.
     * </p>
     * @return the http response containing raw data and headers
     * @throws IOException
     */
    HttpResponse executeUnparsed(boolean upload) throws IOException {
        HttpResponse response;
        boolean throwExceptionOnError;
        if (!upload) {
            // normal request
            HttpRequest request = buildHttpRequest();
            throwExceptionOnError = request.getThrowExceptionOnExecuteError();
            request.setThrowExceptionOnExecuteError(false);
            request.setParser(getAbstractKinveyClient().getObjectParser());
            response = request.execute();

            // process response
            lastResponseCode = response.getStatusCode();
            lastResponseMessage = response.getStatusMessage();
            lastResponseHeaders = response.getHeaders();
            // process any errors
            if (throwExceptionOnError && !response.isSuccessStatusCode()) {
                throw newExceptionOnError(response);
            }
        } else {
            // execute upload procedure
            response = uploader.upload(this);
        }
        return response;
    }

    /**
     * Throws new {@link HttpResponseException} containing the response message.
     *
     * @param response object returned in the event of an error
     * @return and exception containing the error message returned by the server
     */
    protected IOException newExceptionOnError(HttpResponse response) {
        return new HttpResponseException(response);
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public T execute() throws IOException {
        HttpResponse response = executeUnparsed();

        // special class to handle void or empty responses
        if (Void.class.equals(responseClass) || response.getContent() == null) {
            response.ignore();
            return null;
        }
        return response.parseAs(responseClass);
    }

    /**
     * Sends the metadata request to the server and returns the metadata content input stream of
     * {@link HttpResponse}.
     * <p/>
     * <p>
     * Callers are responsible for closing the input stream after it is processed. Example sample:
     * </p>
     * <p/>
     * <pre>
     * InputStream is = request.executeAsInputStream();
     * try {
     * // Process input stream..
     * } finally {
     * is.close();
     * }
     * </pre>
     * <p/>
     * <p>
     * Subclasses may override by calling the super implementation.
     * </p>
     *
     * @return input stream of the response content
     */
    public InputStream executeAsInputStream() throws IOException {
        return executeUnparsed().getContent();
    }


    /**
     * Sends the metadata request to the server and writes the metadata content input stream of
     * {@link HttpResponse} into the given destination output stream.
     * <p/>
     * <p>
     * This method closes the content of the HTTP response from {@link HttpResponse#getContent()}.
     * </p>
     * <p/>
     * <p>
     * Subclasses may override by calling the super implementation.
     * </p>
     *
     * @param out destination output stream
     */
    public void executeAndDownloadTo(OutputStream out) throws IOException {
        if (downloader == null) {
            executeUnparsed().download(out);
        } else {
            downloader.download(this, out);
        }
    }


    public AbstractKinveyClientRequest setAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }
}

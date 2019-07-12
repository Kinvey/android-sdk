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

import java.io.*
import java.util.Locale

import com.google.api.client.http.*
import com.google.api.client.json.GenericJson
import com.google.api.client.util.Charsets
import com.google.api.client.util.GenericData
import com.google.api.client.util.Key
import com.google.common.base.Preconditions
import com.kinvey.java.AbstractClient
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger
import com.kinvey.java.auth.Credential
import com.kinvey.java.auth.KinveyAuthRequest
import com.kinvey.java.dto.BaseUser
import com.kinvey.java.store.UserStoreRequestManager

/**
 * @author m0rganic
 */
abstract class AbstractKinveyClientRequest<T>
/**
 * @param abstractKinveyClient the abstract kinvey client
 * @param requestMethod the request method, PUT, GET, POST, or DELETE
 * @param uriTemplate valid uri template
 * @param httpContent object to send as the message body or `null` if none
 * @param responseClass expected type in the response of this request
 */
protected constructor(
        /**
         * Kinvey JSON client *
         */
        private val client: AbstractClient<BaseUser>, private val hostName: String,
        /**
         * HTTP method *
         */
        /**
         * @return the requestMethod
         */
        val requestMethod: String, uriTemplate: String,
        /**
         * http content or `null` if none is set *
         */
        /**
         * @return the httpContent
         */
        val httpContent: HttpContent?,
        /**
         * Response class to parse into *
         */
        /**
         * @return the responseClass
         */
        @JvmField
        protected val responseClass: Class<T>?) : GenericData() {

    /**
     * URI template of the path relative to the base url *
     */
    /**
     * @return the uriTemplate
     */
    @JvmField
    protected var uriTemplate: String

    public fun getUriTemplate(): String {
        return uriTemplate
    }


    /**
     * http headers to be sent along with the request *
     */
    private var requestHeaders = HttpHeaders()

    /**
     * response headers of the last executed request, `null` before the request is made *
     */
    /**
     * @return the lastResponseHeaders
     */
    var lastResponseHeaders: HttpHeaders? = null
        private set

    /**
     * response status code of the last executed request, `-1` before the request is made *
     */
    /**
     * @return the lastResponseCode
     */
    var lastResponseCode = -1
        private set

    /**
     * response status message of the last executed request, `null` before the request is made *
     */
    /**
     * @return the lastResponseMessage
     */
    var lastResponseMessage: String? = null
        private set

    /**
     * NetworkFileManager downloader or `null` if none is set *
     */
    /**
     * @return the downloader
     */
    val downloader: MediaHttpDownloader? = null

    /**
     * [BackOffPolicy] to use for retries
     */
    /**
     *
     * @return the Backoff Policy
     */
    val requestBackoffPolicy: BackOffPolicy?

    /**
     * Does this request require the appkey/appsecret for authentication or does it require a user context
     */
    var isRequireAppCredentials = false

    /**
     * Should the request use the default template expansion for encoding the URL
     */
    private var templateExpand = true

    /**
     * Should the request intercept redirects and route them to an override
     */
    @JvmField
    protected var overrideRedirect = false

    public fun setOverrideRedirect(overrideRedirect: Boolean) {
        this.overrideRedirect = overrideRedirect
    }

    /**
     * Does this request require the client_id/appsecret for authentication
     */
    var isRequiredClientIdAuth = false

    @Key
    private var appKey: String? = null

    var callback: KinveyClientCallback<T>? = null

    /***
     * Used for MIC to indicate if a request has been repeated after getting a refresh token
     */
    private var hasRetryed = false


    /**
     * @return the abstractKinveyClient
     */
    open val abstractKinveyClient: AbstractKinveyClient
        get() = client

    val customerAppVersion: String?
        get() {
            val header = getRequestHeaders()["X-Kinvey-Client-App-Version"] ?: return null
            return header.toString()
        }

    val customRequestProperties: String?
        get() {
            val header = getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] ?: return null
            return header as String
        }

    /**
     * @param abstractKinveyClient the abstract kinvey client
     * @param requestMethod the request method, PUT, GET, POST, or DELETE
     * @param uriTemplate valid uri template
     * @param httpContent object to send as the message body or `null` if none
     * @param responseClass expected type in the response of this request
     */
    protected constructor(abstractKinveyClient: AbstractClient<BaseUser>,
                          requestMethod: String, uriTemplate: String, httpContent: HttpContent?,
                          responseClass: Class<T>?) : this(abstractKinveyClient, abstractKinveyClient.baseUrl, requestMethod, uriTemplate, httpContent, responseClass) {
    }

    init {
        Preconditions.checkNotNull(client, "abstractKinveyClient must not be null")
        Preconditions.checkNotNull(requestMethod, "requestMethod must not be null")
        this.uriTemplate = uriTemplate
        this.requestBackoffPolicy = client.backoffPolicy
    }

    /**
     * @return the httpHeaders
     */
    fun getRequestHeaders(): HttpHeaders {
        return requestHeaders
    }

    fun setRequestHeaders(headers: HttpHeaders): AbstractKinveyClientRequest<T> {
        this.requestHeaders = headers
        return this
    }

    /**
     * @return
     */
    protected fun buildHttpRequestUrl(): GenericUrl {

        var encodedURL = UriTemplate.expand(hostName, uriTemplate, this, true)
        if (!templateExpand) {
            encodedURL = encodedURL.replace("%3F", "?")
            encodedURL = encodedURL.replace("%3D", "=")
            encodedURL = encodedURL.replace("%26", "&")
        }
        return GenericUrl(encodedURL)
    }

    /**
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun buildHttpRequest(): HttpRequest {
        val httpRequest = abstractKinveyClient
                .requestFactory
                .buildRequest(requestMethod, buildHttpRequestUrl(), httpContent)
        httpRequest.parser = abstractKinveyClient.objectParser
        httpRequest.suppressUserAgentSuffix = true
        httpRequest.connectTimeout = client.requestTimeout
        httpRequest.readTimeout = client.requestTimeout
        //httpRequest.setRetryOnExecuteIOException(true);
        //httpRequest.setBackOffPolicy(this.requestBackoffPolicy);
        // custom methods may use POST with no content but require a Content-Length header
        if (httpContent == null && (requestMethod == HttpMethods.POST || requestMethod == HttpMethods.PUT)) {
            httpRequest.content = EmptyContent()
        }
        for ((key, value) in requestHeaders) {
            httpRequest.headers.set(key.toLowerCase(Locale.US), value)
        }
        if (httpRequest.headers.containsKey("x-kinvey-custom-request-properties")) {
            val customHeaders = httpRequest.headers["x-kinvey-custom-request-properties"] as String?
            if (customHeaders!!.toByteArray(charset("UTF-8")).size > 2000) {
                throw KinveyException("Cannot attach more than 2000 bytes of Custom Request Properties")
            }
        }
        return httpRequest
    }

    /**
     * Executes the http request and returns the raw [HttpResponse]. If
     * throwExceptionOnError is `true` an [HttpResponseException] is
     * thrown, subclasses may override this method to customize the behavior.
     *
     *
     * Callers are responsible for disconnecting the HTTP response by calling
     * [HttpResponse.disconnect]. Example usage:
     *
     *
     * <pre>
     * HttpResponse response = request.executeUnparsed();
     * try {
     * // process response..
     * } finally {
     * response.disconnect();
     * }
    </pre> *
     *
     *
     *
     * Subclasses may override by calling the super implementation.
     *
     * @return the http response containing raw data and headers
     * @throws IOException
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun executeUnparsed(upload: Boolean = false): HttpResponse {
        val response: HttpResponse
        val throwExceptionOnError: Boolean

        // normal request
        val request = buildHttpRequest()
        throwExceptionOnError = request.throwExceptionOnExecuteError
        request.throwExceptionOnExecuteError = false
        request.numberOfRetries = 3
        request.parser = abstractKinveyClient.objectParser

        if (overrideRedirect) {
            request.followRedirects = false
        }

        response = request.execute()
        Logger.INFO("Getting response for network request")

        lastResponseCode = response.getStatusCode()
        lastResponseMessage = response.getStatusMessage()
        lastResponseHeaders = response.getHeaders()

        if (lastResponseMessage != null && lastResponseMessage == LOCKED_DOWN) {
            this.client.performLockDown()
        }

        //process refresh token needed
        if (response.getStatusCode() == 401 && !hasRetryed) {
            //get the refresh token
            Logger.INFO("get the refresh token")
            val cred = client.store.load(client.activeUser.id)
            var refreshToken: String? = null

            if (cred != null) {
                refreshToken = cred.refreshToken
            }

            if (refreshToken != null) {
                hasRetryed = true
                val appKey = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).appKey
                val appSecret = (client.kinveyRequestInitializer as KinveyClientRequestInitializer).appSecret

                val builder: KinveyAuthRequest.Builder<BaseUser> = KinveyAuthRequest.Builder(client.requestFactory.transport,
                        client.jsonFactory, client.baseUrl, appKey, appSecret, null)

                val userStoreRequestManager = UserStoreRequestManager(client, builder)

                //use the refresh token for a new access token
                val result = userStoreRequestManager.useRefreshToken(refreshToken).execute()

                // soft logout the current user
                userStoreRequestManager.logoutSoft().execute()

                //login with the access token
                userStoreRequestManager.loginMobileIdentityBlocking(result!!["access_token"]!!.toString()).execute()

                //store the new refresh token
                val currentCred = client.store.load(client.activeUser.id)
                currentCred.refreshToken = result["refresh_token"]!!.toString()
                client.store.store(client.activeUser.id, currentCred)
                currentCred.initialize(this)
                return executeUnparsed()
            }

        }

        // process any other errors
        if (throwExceptionOnError && !response.isSuccessStatusCode() && response.getStatusCode() != 302) {
            throw newExceptionOnError(response)
        }

        Logger.INFO("Return response for network request")
        return response
    }

    /**
     * Throws new [HttpResponseException] containing the response message.
     *
     * @param response object returned in the event of an error
     * @return and exception containing the error message returned by the server
     */
    protected open fun newExceptionOnError(response: HttpResponse): IOException {
        return HttpResponseException(response)
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    open fun execute(): T? {
        Logger.INFO("Start execute for network request")
        val response = executeUnparsed()

        if (overrideRedirect) {
            Logger.INFO("overrideRedirect == true")
            return onRedirect(response.headers.location)
        }

        // special class to handle void or empty responses
        if (Void::class.java == responseClass || response.content == null) {
            response.ignore()
            return null
        }

        try {
            val statusCode = response.statusCode
            if (response.request.requestMethod == HttpMethods.HEAD || statusCode / 100 == 1
                    || statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT
                    || statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                response.ignore()
                return null

            } else {
                return abstractKinveyClient.objectParser.parseAndClose(response.content, Charsets.UTF_8, responseClass)
            }

        } catch (e: IllegalArgumentException) {
            Logger.ERROR("unable to parse response -> $e")
            throw KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString())

        } catch (ex: NullPointerException) {
            Logger.WARNING(ex.message)
            return null
        }

    }

    /**
     * Sends the metadata request to the server and returns the metadata content input stream of
     * [HttpResponse].
     *
     *
     *
     *
     * Callers are responsible for closing the input stream after it is processed. Example sample:
     *
     *
     *
     * <pre>
     * InputStream is = request.executeAsInputStream();
     * try {
     * // Process input stream..
     * } finally {
     * is.close();
     * }
    </pre> *
     *
     *
     *
     *
     * Subclasses may override by calling the super implementation.
     *
     *
     * @return input stream of the response content
     */
    @Throws(IOException::class)
    fun executeAsInputStream(): InputStream {
        return executeUnparsed().content
    }

    fun setAppKey(appKey: String): AbstractKinveyClientRequest<T> {
        this.appKey = appKey
        return this
    }

    fun setTemplateExpand(expand: Boolean) {
        this.templateExpand = expand
    }

    @Throws(IOException::class)
    open fun onRedirect(newLocation: String?): T? {
        Logger.ERROR("Override Redirect in response is expected, but not implemented!")
        return null
    }

    companion object {

        /**
         * The message received when a user has been locked down
         */
        private val LOCKED_DOWN = "UserLockedDown"
    }
}

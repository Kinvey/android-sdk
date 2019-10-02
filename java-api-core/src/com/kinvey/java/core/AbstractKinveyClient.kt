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

import com.google.api.client.http.BackOffPolicy
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.util.ObjectParser
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger.Companion.INFO
import com.kinvey.java.store.BaseFileStore
import com.kinvey.java.store.StoreType
import java.io.IOException

/**
 * @author m0rganic
 */
abstract class AbstractKinveyClient protected constructor(transport: HttpTransport?,
                                                          httpRequestInitializer: HttpRequestInitializer?, rootUrl: String?, servicePath: String?,
                                                          objectParser: JsonObjectParser?,
                                                          /** handle request init for auth headers and other standard headers across the service
                                                           *  @return the kinveyRequestInitializer
                                                           */
                                                          open var kinveyRequestInitializer: KinveyRequestInitializer?,
                                                          /** the http request backoff policy  */
                                                          open val backoffPolicy: BackOffPolicy?) {
    /** the noramlized root url for the service
     * Returns root url for this service with a trailing "/".
     * (e.g. http://baas.kinvey.com/)
     */
    val rootUrl: String?

    /** the normalized service path for the service
     *  @return the servicePath
     */
    val servicePath: String?

    /** the object parser or `null` if none is set
     *  @return object parser for this client
     */
    private var objectParser: ObjectParser? = null

    /** the http request factory.  */
    val requestFactory: HttpRequestFactory?

    /**
     * @param transport  HTTP transport
     * @param httpRequestInitializer the http request initializer
     * @param rootUrl the root url for this service
     * @param servicePath the service path
     * @param objectParser the object parser or `null` if none
     * @param requestPolicy the [BackOffPolicy] to use for HTTP requests
     */
    protected constructor(transport: HttpTransport, httpRequestInitializer: HttpRequestInitializer?, rootUrl: String, servicePath: String,
                          objectParser: JsonObjectParser, requestPolicy: BackOffPolicy)
            : this(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, null, requestPolicy) {
    }

    /**
     * Access to the BaseFileStore service where files of all sizes including images and videos can be uploaded and downloaded.
     */
    abstract fun getFileStore(storeType: StoreType?): BaseFileStore?

    /**
     *
     * @return the baseUrl appended to the servicePath
     */
    val baseUrl: String
        get() = rootUrl + servicePath

    abstract fun performLockDown()

    /* (non-Javadoc)
     * @see com.google.api.client.http.HttpRequestInitializer#initialize(com.google.api.client.http.HttpRequest)
     */
    @Throws(IOException::class)
    open fun initializeRequest(request: AbstractKinveyClientRequest<*>?) {
        kinveyRequestInitializer?.initialize(request!!)
    }

    /**
     * @return the objectParser
     */
    open fun getObjectParser(): ObjectParser? {
        return objectParser
    }

    /**
     * Constructs an [AbstractKinveyClient]
     */
    abstract class Builder @JvmOverloads constructor(
            /**
             * @return the transport
             */
            val transport: HttpTransport?, defaultRootUrl: String,
            defaultServicePath: String, httpRequestInitializer: HttpRequestInitializer?, kinveyRequestInitializer: KinveyClientRequestInitializer? = null) {

        var objectParser: JsonObjectParser? = null

        /**
         * @return the baseUrl
         */
        var baseUrl: String? = null

        /**
         * @return the serviceUrl
         */
        var servicePath: String? = null
            private set
        /**
         * @return the httpRequestInitializer
         */
        var httpRequestInitializer: HttpRequestInitializer?
            private set

        /**
         * @return the kinveyClientRequestInitializer
         */
        var kinveyClientRequestInitializer: KinveyClientRequestInitializer?

        /**
         *
         * @param requestBackoffPolicy The [BackOffPolicy] for the HTTP request
         * @return Current backoffpolicy
         */
        var requestBackoffPolicy: BackOffPolicy? = null

        abstract fun build(): AbstractKinveyClient?

        /**
         * @param baseUrl the baseUrl to set
         */
        open fun setBaseUrl(baseUrl: String): Builder {
            this.baseUrl = normalizeRootUrl(baseUrl)
            if (this.baseUrl?.toUpperCase()?.startsWith("HTTPS") == false) {
                val exceptionMessage: String = if (baseUrl.contains("://")) {
                    "Kinvey requires `https` as the protocol when setting a base URL, instead found: " + this.baseUrl?.substring(0, this.baseUrl?.indexOf("://") ?: 0) + " in baseURL: " + this.baseUrl
                } else {
                    "Kinvey requires `https` as the protocol when setting a base URL " + "in baseURL: " + this.baseUrl
                }
                throw KinveyException(exceptionMessage)
            }
            return this
        }

        /**
         * Set the JsonFactory, which will be used to create an object parser
         *
         * @param factory - the JSON factory for this client to use
         * @return the current client builder
         */
        open fun setJsonFactory(factory: JsonFactory?): Builder {
            objectParser = JsonObjectParser(factory)
            return this
        }

        /**
         * @param serviceUrl the serviceUrl to set
         */
        open fun setServiceUrl(serviceUrl: String): Builder {
            servicePath = normalizeServicePath(serviceUrl)
            return this
        }

        /**
         * @param httpRequestInitializer the httpRequestInitializer to set
         */
        open fun setHttpRequestInitializer(httpRequestInitializer: HttpRequestInitializer): Builder {
            this.httpRequestInitializer = httpRequestInitializer
            return this
        }

        /**
         * @param kinveyRequestInitializer the kinveyRequestInitializer to set
         */
        open fun setKinveyClientRequestInitializer(kinveyRequestInitializer: KinveyClientRequestInitializer?): Builder {
            kinveyClientRequestInitializer = kinveyRequestInitializer
            return this
        }

        init {
            setBaseUrl(defaultRootUrl)
            setServiceUrl(defaultServicePath)
            this.httpRequestInitializer = httpRequestInitializer
            kinveyClientRequestInitializer = kinveyRequestInitializer
        }
    }

    companion object {
        /** If the specified root URL does not end with a "/" then a "/" is added to the end.  */
        internal fun normalizeRootUrl(rootUrl: String?): String? {
            // Preconditions.checkNotNull(baseUrl, "root URL cannot be null.");
            var rootUrl = rootUrl
            if (rootUrl?.endsWith("/") == false) {
                rootUrl += "/"
            }
            return rootUrl
        }

        /**
         * If the specified service path does not end with a "/" then a "/" is added to the end. If the
         * specified service path begins with a "/" then the "/" is removed.
         */
        internal fun normalizeServicePath(servicePath: String?): String? {
            // Preconditions.checkNotNull(servicePath, "service path cannot be null");
            var servicePath = servicePath ?: ""

            if (servicePath.length == 1) {
                // Preconditions.checkArgument(
                // "/".equals(servicePath), "service path must equal \"/\" if it is of length 1.");
                servicePath = ""
            } else if (servicePath.isNotEmpty()) {
                if (!servicePath.endsWith("/")) {
                    servicePath += "/"
                }
                if (servicePath.startsWith("/")) {
                    servicePath = servicePath.substring(1)
                }
            }
            return servicePath
        }
    }

    init {
        this.rootUrl = normalizeRootUrl(rootUrl)
        this.servicePath = normalizeServicePath(servicePath)
        this.objectParser = objectParser
        requestFactory = if (httpRequestInitializer == null) transport?.createRequestFactory()
                         else transport?.createRequestFactory(httpRequestInitializer)
        INFO("Kinvey Client created, running version: " + KinveyHeaders.VERSION)
    }
}
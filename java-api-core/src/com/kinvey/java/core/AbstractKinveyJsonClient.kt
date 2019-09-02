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
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.JsonObjectParser

/**
 * @author m0rganic
 */
abstract class AbstractKinveyJsonClient : AbstractKinveyClient {

    val jsonFactory: JsonFactory
        get() = objectParser.jsonFactory

    /**
     * @param transport  HTTP transport
     * @param httpRequestInitializer the http request initializer
     * @param rootUrl the root url for this service
     * @param servicePath the service path
     * @param objectParser the object parser or `null` if none
     * @param requestPolicy the [BackOffPolicy] to use for HTTP requests
     */
    protected constructor(transport: HttpTransport,
                          httpRequestInitializer: HttpRequestInitializer, rootUrl: String, servicePath: String,
                          objectParser: JsonObjectParser, requestPolicy: BackOffPolicy)
            : super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, requestPolicy) {
    }

    /**
     * @param transport  HTTP transport
     * @param httpRequestInitializer the http request initializer
     * @param rootUrl the root url for this service
     * @param servicePath the service path
     * @param objectParser the object parser or `null` if none
     * @param kinveyRequestInitializer initializer to handle kinvey specific headers and authorization tokens
     * @param requestPolicy the [BackOffPolicy] to use for HTTP requests
     */
    protected constructor(transport: HttpTransport?,
                          httpRequestInitializer: HttpRequestInitializer?, rootUrl: String?, servicePath: String?,
                          objectParser: JsonObjectParser?, kinveyRequestInitializer: KinveyRequestInitializer?,
                          requestPolicy: BackOffPolicy?)
            : super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, requestPolicy)

    override fun getObjectParser(): JsonObjectParser {
        return super.getObjectParser() as JsonObjectParser
    }

    abstract class Builder : AbstractKinveyClient.Builder {

        /**
         * @param transport  HTTP transport
         * @param defaultRootUrl the root url for this service
         * @param defaultServicePath the service path
         * @param httpRequestInitializer the http request initializer
         */
        protected constructor(transport: HttpTransport?, defaultRootUrl: String,
                              defaultServicePath: String, httpRequestInitializer: HttpRequestInitializer?)
                : super(transport, defaultRootUrl, defaultServicePath, httpRequestInitializer)
        /**
         * @param transport  HTTP transport
         * @param defaultRootUrl the root url for this service
         * @param defaultServicePath the service path
         * @param httpRequestInitializer the http request initializer
         * @param kinveyRequestInitializer initializer to handle kinvey specific headers and authorization tokens
         */
        protected constructor(transport: HttpTransport, defaultRootUrl: String,
                              defaultServicePath: String, httpRequestInitializer: HttpRequestInitializer?, kinveyRequestInitializer: KinveyClientRequestInitializer)
                : super(transport, defaultRootUrl, defaultServicePath, httpRequestInitializer, kinveyRequestInitializer)

        /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#build()
     */
        abstract override fun build(): AbstractKinveyClient?

        /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#getObjectParser()
     */
        override fun getObjectParser(): JsonObjectParser {
            return super.getObjectParser() as JsonObjectParser
        }

        /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#setRootUrl(java.lang.String)
     */
        override fun setBaseUrl(baseUrl: String): Builder {
            return super.setBaseUrl(baseUrl) as Builder
        }

        /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#setServiceUrl(java.lang.String)
     */
        override fun setServiceUrl(serviceUrl: String): Builder {
            return super.setServiceUrl(serviceUrl) as Builder
        }

        /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#setHttpRequestInitializer(com.google.api.client.http.HttpRequestInitializer)
     */
        override fun setHttpRequestInitializer(
                httpRequestInitializer: HttpRequestInitializer): Builder {
            return super.setHttpRequestInitializer(httpRequestInitializer) as Builder
        }

        /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#setKinveyClientRequestInitializer(com.kinvey.java.core.KinveyRequestInitializer)
     */
        override fun setKinveyClientRequestInitializer(
                kinveyRequestInitializer: KinveyClientRequestInitializer): Builder {
            return super.setKinveyClientRequestInitializer(kinveyRequestInitializer) as Builder
        }
    }
}

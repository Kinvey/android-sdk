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
package com.kinvey.java.core;

import java.io.IOException;

import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.ObjectParser;
import com.kinvey.java.KinveyException;
import com.kinvey.java.Logger;
import com.kinvey.java.store.FileStore;

/**
 * @author m0rganic
 */
public abstract class AbstractKinveyClient {

    /** handle request init for auth headers and other standard headers across the service **/
    private final KinveyRequestInitializer kinveyRequestInitializer;

    /** the noramlized root url for the service **/
    private final String rootUrl;

    /** the normalized service path for the service **/
    private final String servicePath;

    /** the object parser or {@code null} if none is set **/
    private final ObjectParser objectParser;

    /** the http request factory. **/
    private final HttpRequestFactory httpRequestFactory;

    /** the http request backoff policy **/
    private final BackOffPolicy backoffPolicy;
    

    /**
     * @param transport  HTTP transport
     * @param httpRequestInitializer the http request initializer
     * @param rootUrl the root url for this service
     * @param servicePath the service path
     * @param objectParser the object parser or {@code null} if none
     * @param requestPolicy the {@link BackOffPolicy} to use for HTTP requests
     */
    protected AbstractKinveyClient(HttpTransport transport, HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath,
                                   JsonObjectParser objectParser, BackOffPolicy requestPolicy) {
        this(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, null, requestPolicy);
    }

    /**
     * @param transport  HTTP transport
     * @param httpRequestInitializer the http request initializer
     * @param rootUrl the root url for this service
     * @param servicePath the service path
     * @param objectParser the object parser or {@code null} if none
     * @param kinveyRequestInitializer the kinvey request initializer or {@code null} if none
     * @param requestPolicy the {@link BackOffPolicy} to use for HTTP requests
     */
    protected AbstractKinveyClient(HttpTransport transport,
                                   HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath,
                                   JsonObjectParser objectParser, KinveyRequestInitializer kinveyRequestInitializer,
                                   BackOffPolicy requestPolicy) {
        this.kinveyRequestInitializer = kinveyRequestInitializer;
        this.rootUrl = normalizeRootUrl(rootUrl);
        this.servicePath = normalizeServicePath(servicePath);
        this.objectParser = objectParser;
        this.httpRequestFactory =
                httpRequestInitializer == null ? transport.createRequestFactory() : transport
                        .createRequestFactory(httpRequestInitializer);
        this.backoffPolicy = requestPolicy;
        Logger.INFO("Kinvey Client created, running version: " + KinveyHeaders.VERSION);
    }


    /**
     * Access to the FileStore service where files of all sizes including images and videos can be uploaded and downloaded.
     */
    public abstract FileStore getFileStore();


    /**
     * Returns root url for this service with a trailing "/".
     * (e.g. http://baas.kinvey.com/)
     *
     **/
    public final String getRootUrl() {
        return rootUrl;
    }

    /**
     * @return the servicePath
     */
    public final String getServicePath() {
        return servicePath;
    }

    public final BackOffPolicy getBackoffPolicy() {
        return backoffPolicy;
    }

    /**
     *
     * @return the baseUrl appended to the servicePath
     */
    public final String getBaseUrl() {
        return rootUrl + servicePath;
    }

    /**
     * @return object parser for this client
     */
    public ObjectParser getObjectParser() {
        return objectParser;
    }

    public final HttpRequestFactory getRequestFactory() {
        return this.httpRequestFactory;
    }

    public abstract void performLockDown();

    /* (non-Javadoc)
     * @see com.google.api.client.http.HttpRequestInitializer#initialize(com.google.api.client.http.HttpRequest)
     */
    public void initializeRequest(AbstractKinveyClientRequest<?> request) throws IOException {
        if (getKinveyRequestInitializer() != null) {
            getKinveyRequestInitializer().initialize(request);
        }
    }


    /**
     * @return the kinveyRequestInitializer
     */
    public KinveyRequestInitializer getKinveyRequestInitializer() {
        return kinveyRequestInitializer;
    }

    /** If the specified root URL does not end with a "/" then a "/" is added to the end. */
    static String normalizeRootUrl(String rootUrl) {
//    Preconditions.checkNotNull(baseUrl, "root URL cannot be null.");
        if (!rootUrl.endsWith("/")) {
            rootUrl += "/";
        }
        return rootUrl;
    }


    /**
     * If the specified service path does not end with a "/" then a "/" is added to the end. If the
     * specified service path begins with a "/" then the "/" is removed.
     */
    static String normalizeServicePath(String servicePath) {
//    Preconditions.checkNotNull(servicePath, "service path cannot be null");
        if (servicePath.length() == 1) {
//      Preconditions.checkArgument(
//          "/".equals(servicePath), "service path must equal \"/\" if it is of length 1.");
            servicePath = "";
        } else if (servicePath.length() > 0) {
            if (!servicePath.endsWith("/")) {
                servicePath += "/";
            }
            if (servicePath.startsWith("/")) {
                servicePath = servicePath.substring(1);
            }
        }
        return servicePath;
    }


    /**
     * Constructs an {@link AbstractKinveyClient}
     */
    public abstract static class Builder {

        private final HttpTransport transport;
        private JsonObjectParser objectParser;
        private String baseUrl;
        private String servicePath;
        private HttpRequestInitializer httpRequestInitializer;
        private KinveyClientRequestInitializer kinveyRequestInitializer;
        private BackOffPolicy requestBackoffPolicy;


        /**
         * @param transport HTTP transport
         * @param defaultRootUrl root url
         * @param defaultServicePath service path
         * @param httpRequestInitializer http request initializer
         */
        public Builder(HttpTransport transport, String defaultRootUrl,
                       String defaultServicePath, HttpRequestInitializer httpRequestInitializer) {
            this(transport, defaultRootUrl, defaultServicePath, httpRequestInitializer, null);
        }

        /**
         * @param transport HTTP transport
         * @param defaultRootUrl root url
         * @param defaultServicePath service path
         * @param httpRequestInitializer request initializer
         * @param kinveyRequestInitializer kinvey request initializer
         */
        public Builder(HttpTransport transport, String defaultRootUrl,
                       String defaultServicePath, HttpRequestInitializer httpRequestInitializer, KinveyClientRequestInitializer kinveyRequestInitializer) {
            this.transport = transport;
            setBaseUrl(defaultRootUrl);
            setServiceUrl(defaultServicePath);
            this.httpRequestInitializer = httpRequestInitializer;
            this.kinveyRequestInitializer = kinveyRequestInitializer;
        }

        public abstract AbstractKinveyClient build();


        /**
         * @return the transport
         */
        public final HttpTransport getTransport() {
            return transport;
        }


        /**
         * @return the baseUrl
         */
        public final String getBaseUrl() {
            return baseUrl;
        }


        /**
         * @param baseUrl the baseUrl to set
         */
        public Builder setBaseUrl(String baseUrl) {
        	
            this.baseUrl = normalizeRootUrl(baseUrl);
            if (!this.baseUrl.toUpperCase().startsWith("HTTPS")){
            	throw new KinveyException("Kinvey requires `https` as the protocol when setting a base URL, instead found: " + this.baseUrl.substring(0, this.baseUrl.indexOf(":/")) + " in baseURL: " + this.baseUrl);
            }
            return this;
        }

        /**
         * Set the JsonFactory, which will be used to create an object parser
         *
         * @param factory - the JSON factory for this client to use
         * @return the current client builder
         */
        public Builder setJsonFactory(JsonFactory factory){
            this.objectParser = new JsonObjectParser(factory);
            return this;

        }


        /**
         * @return the serviceUrl
         */
        public final String getServicePath() {
            return servicePath;
        }


        /**
         * @param serviceUrl the serviceUrl to set
         */
        public Builder setServiceUrl(String serviceUrl) {
            this.servicePath = normalizeServicePath(serviceUrl);
            return this;
        }


        /**
         * @return the httpRequestInitializer
         */
        public final HttpRequestInitializer getHttpRequestInitializer() {
            return httpRequestInitializer;
        }


        /**
         * @param httpRequestInitializer the httpRequestInitializer to set
         */
        public Builder setHttpRequestInitializer(HttpRequestInitializer httpRequestInitializer) {
            this.httpRequestInitializer = httpRequestInitializer;
            return this;
        }


        /**
         * @return the objectParser
         */
        public ObjectParser getObjectParser() {
            return objectParser;
        }

        /**
         * @return the kinveyClientRequestInitializer
         */
        public final KinveyClientRequestInitializer getKinveyClientRequestInitializer() {
            return kinveyRequestInitializer;
        }

        /**
         *
         * @return Current backoffpolicy
         */
        public BackOffPolicy getRequestBackoffPolicy() {
            return requestBackoffPolicy;
        }

        /**
         *
         * @param requestBackoffPolicy The {@link BackOffPolicy} for the HTTP request
         */
        public void setRequestBackoffPolicy(BackOffPolicy requestBackoffPolicy) {
            this.requestBackoffPolicy = requestBackoffPolicy;
        }

        /**
         * @param kinveyRequestInitializer the kinveyRequestInitializer to set
         */
        public Builder setKinveyClientRequestInitializer(KinveyClientRequestInitializer kinveyRequestInitializer) {
            this.kinveyRequestInitializer = kinveyRequestInitializer;
            return this;
        }

    }

}

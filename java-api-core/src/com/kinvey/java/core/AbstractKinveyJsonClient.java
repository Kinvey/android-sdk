/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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

import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;

/**
 * @author m0rganic
 */
public abstract class AbstractKinveyJsonClient extends AbstractKinveyClient {

  /**
   * @param transport  HTTP transport
   * @param httpRequestInitializer the http request initializer
   * @param rootUrl the root url for this service
   * @param servicePath the service path
   * @param objectParser the object parser or {@code null} if none
   * @param requestPolicy the {@link BackOffPolicy} to use for HTTP requests
   */
  protected AbstractKinveyJsonClient(HttpTransport transport,
      HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath,
      JsonObjectParser objectParser, BackOffPolicy requestPolicy) {
    super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, requestPolicy);
  }
  
  /**
   * @param transport  HTTP transport
   * @param httpRequestInitializer the http request initializer
   * @param rootUrl the root url for this service
   * @param servicePath the service path
   * @param objectParser the object parser or {@code null} if none
   * @param kinveyRequestInitializer initializer to handle kinvey specific headers and authorization tokens
   * @param requestPolicy the {@link BackOffPolicy} to use for HTTP requests
   */
  protected AbstractKinveyJsonClient(HttpTransport transport,
      HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath,
      JsonObjectParser objectParser, KinveyRequestInitializer kinveyRequestInitializer,
      BackOffPolicy requestPolicy) {
    super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, requestPolicy);
  }


  @Override
  public JsonObjectParser getObjectParser() {
    return (JsonObjectParser) super.getObjectParser();
  }
  
  public JsonFactory getJsonFactory() {
    return getObjectParser().getJsonFactory();
  }
  
  
  public abstract static class Builder extends AbstractKinveyClient.Builder {

    /**
    * @param transport  HTTP transport
    * @param jsonFactory the json parser or {@code null} if none
    * @param defaultRootUrl the root url for this service
    * @param defaultServicePath the service path
    * @param httpRequestInitializer the http request initializer
    */
    protected Builder(HttpTransport transport, JsonFactory jsonFactory, String defaultRootUrl,
                    String defaultServicePath, HttpRequestInitializer httpRequestInitializer) {
      super(transport, jsonFactory, defaultRootUrl, defaultServicePath, httpRequestInitializer);
    }


    /**
     * @param transport  HTTP transport
     * @param jsonFactory the json parser or {@code null} if none
     * @param defaultRootUrl the root url for this service
     * @param defaultServicePath the service path
     * @param httpRequestInitializer the http request initializer
     * @param kinveyRequestInitializer initializer to handle kinvey specific headers and authorization tokens
     */
    protected Builder(HttpTransport transport, JsonFactory jsonFactory, String defaultRootUrl,
        String defaultServicePath, HttpRequestInitializer httpRequestInitializer, KinveyClientRequestInitializer kinveyRequestInitializer) {
      super(transport, jsonFactory, defaultRootUrl, defaultServicePath, httpRequestInitializer, kinveyRequestInitializer);
    }

    /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#build()
     */
    @Override
    public abstract AbstractKinveyClient build();

    /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#getObjectParser()
     */
    @Override
    public JsonObjectParser getObjectParser() {
      return (JsonObjectParser) super.getObjectParser();
    }
    
    /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#setRootUrl(java.lang.String)
     */
    @Override
    public Builder setBaseUrl(String baseUrl) {
      return (Builder) super.setBaseUrl(baseUrl);
    }

    /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#setServiceUrl(java.lang.String)
     */
    @Override
    public Builder setServiceUrl(String serviceUrl) {
      return (Builder)  super.setServiceUrl(serviceUrl);
    }

    /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#setHttpRequestInitializer(com.google.api.client.http.HttpRequestInitializer)
     */
    @Override
    public Builder setHttpRequestInitializer(
        HttpRequestInitializer httpRequestInitializer) {
      return (Builder)   super.setHttpRequestInitializer(httpRequestInitializer);
    }

    /* (non-Javadoc)
     * @see com.kinvey.java.core.AbstractKinveyClient.Builder#setKinveyClientRequestInitializer(com.kinvey.java.core.KinveyRequestInitializer)
     */
    @Override
    public Builder setKinveyClientRequestInitializer(
            KinveyClientRequestInitializer kinveyRequestInitializer) {
      return (Builder)  super.setKinveyClientRequestInitializer(kinveyRequestInitializer);
    }

    
  }




}

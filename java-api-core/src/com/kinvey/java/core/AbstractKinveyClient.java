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

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.ObjectParser;
import com.kinvey.java.File;

import java.io.IOException;

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


  /**
   * @param transport  HTTP transport
   * @param httpRequestInitializer the http request initializer
   * @param rootUrl the root url for this service
   * @param servicePath the service path
   * @param objectParser the object parser or {@code null} if none
   */
  protected AbstractKinveyClient(HttpTransport transport, HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath,
      JsonObjectParser objectParser) {
    this(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, null);
  }
  
  /**
   * @param transport  HTTP transport
   * @param httpRequestInitializer the http request initializer
   * @param rootUrl the root url for this service
   * @param servicePath the service path
   * @param objectParser the object parser or {@code null} if none
   * @param kinveyRequestInitializer the kinvey request initializer or {@code null} if none
   */
  protected AbstractKinveyClient(HttpTransport transport,
      HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath,
      JsonObjectParser objectParser, KinveyRequestInitializer kinveyRequestInitializer) {
    this.kinveyRequestInitializer = kinveyRequestInitializer;
    this.rootUrl = normalizeRootUrl(rootUrl);
    this.servicePath = normalizeServicePath(servicePath);
    this.objectParser = objectParser;
    this.httpRequestFactory =
        httpRequestInitializer == null ? transport.createRequestFactory() : transport
            .createRequestFactory(httpRequestInitializer);
  }


    /**
     * Access to the File service where files of all sizes including images and videos can be uploaded and downloaded.
     */
    public abstract File file();
  

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
    private final JsonObjectParser objectParser;
    private String baseUrl;
    private String servicePath;
    private HttpRequestInitializer httpRequestInitializer;
    private KinveyClientRequestInitializer kinveyRequestInitializer;
  
  
    /**
     * @param transport HTTP transport
     * @param jsonFactory json factory or {@code null} if none
     * @param defaultRootUrl root url 
     * @param defaultServicePath service path
     * @param httpRequestInitializer http request initializer
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory, String defaultRootUrl,
        String defaultServicePath, HttpRequestInitializer httpRequestInitializer) {
      this(transport, jsonFactory, defaultRootUrl, defaultServicePath, httpRequestInitializer, null);
    }
  
    /**
     * @param transport HTTP transport
     * @param jsonFactory json factory or {@code null} if none
     * @param defaultRootUrl root url 
     * @param defaultServicePath service path
     * @param httpRequestInitializer request initializer
     * @param kinveyRequestInitializer kinvey request initializer
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory, String defaultRootUrl,
                   String defaultServicePath, HttpRequestInitializer httpRequestInitializer, KinveyClientRequestInitializer kinveyRequestInitializer) {
      this.transport = transport;
      this.objectParser = new JsonObjectParser(jsonFactory);
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
     * @param kinveyRequestInitializer the kinveyRequestInitializer to set
     */
    public Builder setKinveyClientRequestInitializer(KinveyClientRequestInitializer kinveyRequestInitializer) {
      this.kinveyRequestInitializer = kinveyRequestInitializer;
      return this;
    }


  
  
  }
  
}

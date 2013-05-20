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

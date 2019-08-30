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

package com.kinvey.java.core;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.Json;
import com.kinvey.java.AbstractClient;

import java.util.UUID;

/**
 * @author m0rganic
 *
 */
public abstract class AbstractKinveyJsonClientRequest<T> extends AbstractKinveyClientRequest<T> {

  /** raw json data **/
  private final GenericJson jsonContent;
  private AsyncExecutor executor;

  public static final String TEMPID = "tempOfflineID_";

  /**
   * @param abstractKinveyJsonClient kinvey credential JSON client
   * @param requestMethod HTTP Method
   * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
   *        the base path from the base URL will be stripped out. The URI template can also be a
   *        full URL. URI template expansion is done using
   *        {@link UriTemplate#expand(String, String, Object, boolean)}
   * @param jsonContent POJO that can be serialized into JSON content or {@code null} for none
   * @param responseClass response class to parse into
   */
  protected AbstractKinveyJsonClientRequest(AbstractClient abstractKinveyJsonClient,
                                            String requestMethod, String uriTemplate, GenericJson jsonContent, Class<T> responseClass) {
    super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent == null
        ? null : new JsonHttpContent(abstractKinveyJsonClient.getJsonFactory(), jsonContent),
        responseClass);
    if (jsonContent != null) {
        super.getRequestHeaders().setContentType(Json.MEDIA_TYPE);
    }
    this.jsonContent = jsonContent;
  }


  /**
   * @return the jsonContent
   */
  public GenericJson getJsonContent() {
    return jsonContent;
  }
  

  @Override
  public AbstractKinveyJsonClient getAbstractKinveyClient() {
    return (AbstractKinveyJsonClient) super.getAbstractKinveyClient();
  }

  @Override
  protected KinveyJsonResponseException newExceptionOnError(HttpResponse response) {
    return KinveyJsonResponseException.from(getAbstractKinveyClient().getJsonFactory(), response);
  }

  public AsyncExecutor getExecutor() {
    return executor;
  }

  public void setExecutor(AsyncExecutor executor) {
    this.executor = executor;
  }

  public static String getUUID(){
    String id = UUID.randomUUID().toString();
    String ret = id.replace("-", "");
    return ret;

  }


}

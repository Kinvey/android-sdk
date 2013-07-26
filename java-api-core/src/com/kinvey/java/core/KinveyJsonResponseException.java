/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.core;

import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;

import java.io.IOException;

/**
 * @author m0rganic
 *
 */
public class KinveyJsonResponseException extends HttpResponseException {

  /** **/
  private static final long serialVersionUID = -5586707180518343613L;
  
  private final KinveyJsonError details;

    /**
     *
     * @param response raw http reponse
     * @param details detail message give by the response
     * @param message general message
     */
  private KinveyJsonResponseException(HttpResponse response, KinveyJsonError details, String message) {
    super(response, message);
    this.details = details;
  }

    /**
     * @param jsonFactory json factory to use while parsing the response
     * @param response raw http response
     * @return exception object built up from the raw http response
     */
  public static KinveyJsonResponseException from(JsonFactory jsonFactory, HttpResponse response) {
    KinveyJsonError details = null;
    try {
      if (!response.isSuccessStatusCode()
          && HttpMediaType.equalsIgnoreParameters(Json.MEDIA_TYPE, response.getContentType())
          && response.getContent() != null) {
        JsonParser parser = null;
        try {
          parser = jsonFactory.createJsonParser(response.getContent());
          details = KinveyJsonError.parse(jsonFactory, response);
        } catch (IOException exception) {
          // it would be bad to throw an exception while throwing an exception
          exception.printStackTrace();
        } finally {
          if (parser == null) {
            response.ignore();
          } else if (details == null) {
            parser.close();
          }
        }
      }
    } catch (IOException exception) {
      // it would be bad to throw an exception while throwing an exception
      exception.printStackTrace();
    }

    String detailMessage =
        (details == null ? "unknown" : String.format("%s%n%s", details.getError(),
            details.getDescription()));
    
    return new KinveyJsonResponseException(response, details, detailMessage);
  }

  /**
   * @return the details
   */
  public KinveyJsonError getDetails() {
    return details;
  }

}

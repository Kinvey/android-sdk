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

import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.kinvey.java.KinveyException;
import com.kinvey.java.query.KinveyClientErrorCode;

import java.io.IOException;

/**
 * @author m0rganic
 *
 */
public class KinveyJsonResponseException extends HttpResponseException {

  /** **/
  private static final long serialVersionUID = -5586707180518343613L;
  
  private final KinveyJsonError details;

  private final String message;

    /**
     *
     * @param response raw http reponse
     * @param details detail message give by the response
     * @param message general message
     */
  private KinveyJsonResponseException(HttpResponse response, KinveyJsonError details, String message) {
    super(response);
    this.details = details;
    this.message = message;
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
        } catch (Exception e) {
          throw new KinveyException(KinveyClientErrorCode.CantParseJson, e);
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
     // exception.printStackTrace();
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
    return this.details;
  }

    /**
     * @return the message
     */
    public String getMessage(){
        return this.message;
    }

}

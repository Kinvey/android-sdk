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

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

import java.io.IOException;

/**
 *
 *
 */
public class KinveyJsonError extends GenericData {
  
  /** **/
  @Key
  private String error;
  
  /** **/
  @Key
  private String description;
  
  /** **/
  @Key
  private String debug;
  
  /**
   * Parses the HttpResponse as a standard Kinvey error.
   * 
   * @param jsonFactory the json factory to use while parsing
   * @param response raw http response to parse
   * @return standard error object
   * @throws IOException error occurred during parse
   */
  public static KinveyJsonError parse(JsonFactory jsonFactory, HttpResponse response)
      throws IOException {
    return new JsonObjectParser(jsonFactory).parseAndClose(response.getContent(),
        response.getContentCharset(), KinveyJsonError.class);
  }

  /**
   * @return the error
   */
  public final String getError() {
    return error;
  }

  /**
   * @param error the error to set
   */
  public final void setError(String error) {
    this.error = error;
  }

  /**
   * @return the description
   */
  public final String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public final void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the debug
   */
  public final String getDebug() {
    return debug;
  }

  /**
   * @param debug the debug to set
   */
  public final void setDebug(String debug) {
    this.debug = debug;
  }
}

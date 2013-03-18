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

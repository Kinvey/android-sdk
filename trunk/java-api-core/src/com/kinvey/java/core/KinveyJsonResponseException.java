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

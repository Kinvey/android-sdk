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
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.Json;

/**
 * @author m0rganic
 *
 */
public abstract class AbstractKinveyJsonClientRequest<T> extends AbstractKinveyClientRequest<T> {

  /** raw json data **/
  private final Object jsonContent;
  
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
  protected AbstractKinveyJsonClientRequest(AbstractKinveyJsonClient abstractKinveyJsonClient,
      String requestMethod, String uriTemplate, Object jsonContent, Class<T> responseClass) {
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
  public Object getJsonContent() {
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
}

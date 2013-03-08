/*
 * Copyright (c) 2013 Kinvey Inc.
 *
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
package com.kinvey.java.auth;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Key;

import java.io.IOException;

/**
 * @author m0rganic
 * @since 2.0
 */
public class KinveyAuthResponse extends GenericJson {

    @Key("_id")
    private String userId;

    @Key("_kmd")
    private KinveyUserMetadata metadata;

    public KinveyAuthResponse() {}

    /**
     * Parses the HttpResponse as a standard Kinvey error.
     *
     * @param jsonFactory the json object parser
     * @param response the response to parse
     * @return a valid authorization response object
     * @throws IOException if an error occurs during parsing
     */
    private static KinveyAuthResponse parse(JsonFactory jsonFactory, HttpResponse response)
            throws IOException {
        return new JsonObjectParser(jsonFactory).parseAndClose(response.getContent(),
                response.getContentCharset(), KinveyAuthResponse.class);
    }

    public String getUserId() {
        return userId;
    }

    public KinveyUserMetadata getMetadata() {
        return metadata;
    }

    public final String getAuthToken() {
        return (metadata != null) ? metadata.authToken : null;
    }

    public static class KinveyUserMetadata extends GenericJson {

        @Key("lmt")
        private String lastModifiedTime;

        @Key("authtoken")
        private String authToken;

        public KinveyUserMetadata() {}
    }

}

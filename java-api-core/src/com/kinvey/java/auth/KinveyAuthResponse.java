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

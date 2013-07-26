/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android.offline;

import com.google.api.client.http.UriTemplate;
import com.kinvey.java.core.AbstractKinveyJsonClient;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;

/**
 *
 * @author edwardf
 * @since 2.4
 *
 */
public class OfflineClientRequest<T> extends AbstractKinveyJsonClientRequest<T> {
    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod            HTTP Method
     * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
     *                                 the base path from the base URL will be stripped out. The URI template can also be a
     *                                 full URL. URI template expansion is done using
     *                                 {@link com.google.api.client.http.UriTemplate#expand(String, String, Object, boolean)}
     * @param jsonContent              POJO that can be serialized into JSON content or {@code null} for none
     * @param responseClass            response class to parse into
     */
    protected OfflineClientRequest(AbstractKinveyJsonClient abstractKinveyJsonClient, String requestMethod, String uriTemplate, Object jsonContent, Class<T> responseClass) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, responseClass);
    }
}

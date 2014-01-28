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
package com.kinvey.java.model;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Generic blob service response object for upload and download url requests.
 *
 * @deprecated  with new file API
 */
public class UriLocResponse extends GenericJson {

    @Key("URI")
    private String blobTemporaryUri;

    public GenericUrl newGenericUrl() {
        return (blobTemporaryUri != null) ? new GenericUrl(blobTemporaryUri): null;
    }

    public String getBlobTemporaryUri() {
        return blobTemporaryUri;
    }
}

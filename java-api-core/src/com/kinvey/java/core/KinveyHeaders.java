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

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.Key;

/**
 * @author m0rganic
 * @since 2.0
 */
public class KinveyHeaders extends HttpHeaders {

    private String VERSION = "2.6.0";

    @Key("X-Kinvey-API-Version")
    private String kinveyApiVersion = "3";

    private String userAgent = "android-kinvey-http/"+ VERSION;

    public KinveyHeaders() {
        super();
        setUserAgent(userAgent);
    }
}

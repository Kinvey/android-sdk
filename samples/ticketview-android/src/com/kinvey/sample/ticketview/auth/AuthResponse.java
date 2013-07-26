/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.ticketview.auth;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * @author m0rganic
 * @since 2.0
 */
public class AuthResponse extends GenericJson {
    @Key("access_token")
    private String accessToken;

    @Key("refresh_token")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
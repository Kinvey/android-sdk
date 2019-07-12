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

package com.kinvey.java.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.Map;

import static com.kinvey.java.Constants.AUTH_TOKEN;
import static com.kinvey.java.model.KinveyMetaData.KMD;

/**
 * Created by Prots on 2/12/16.
 */
public class BaseUser extends GenericJson {

    @Key("_id")
    private String id;

    @Key("username")
    private String username;
    private String authToken;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setAuthTokenToKmd(String authToken) {
        if (get(KMD) != null) {
            Map<String, String> kmd = (Map<String, String>)get(KMD);
            if (kmd != null) {
                kmd.put(AUTH_TOKEN, authToken);
                put(KMD, kmd);
            }
        }
    }

    public String getAuthToken() {
        return authToken;
    }
}

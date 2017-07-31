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

package com.kinvey.java.store.requests.user;

import com.google.api.client.http.HttpContent;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.KinveyException;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.store.UserStoreRequestManager;

import java.io.IOException;

/**
 * Created by Prots on 2/12/16.
 */
public final class LoginToTempURL extends AbstractKinveyClientRequest<GenericJson> {

    private UserStoreRequestManager userStoreRequestManager;
    private String clientId;

    public LoginToTempURL(UserStoreRequestManager userStoreRequestManager, String clientId, String tempURL, HttpContent content) {
        super(userStoreRequestManager.getClient(), tempURL, "POST", "", content, GenericJson.class);
        this.userStoreRequestManager = userStoreRequestManager;
        this.clientId = clientId;
        this.setOverrideRedirect(true);
    }

    @Override
    public GenericJson onRedirect(String newLocation) throws IOException {

        int codeIndex = newLocation.indexOf("code=");
        if (codeIndex == -1) {
            throw new KinveyException("Redirect does not contain `code=`, was: " + newLocation);
        }

        String accesstoken = newLocation.substring(codeIndex + 5, newLocation.length());

        return userStoreRequestManager.getMICToken(accesstoken, clientId).execute();


    }
}

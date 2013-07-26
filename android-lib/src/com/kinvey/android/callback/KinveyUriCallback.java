/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android.callback;

import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.UriLocResponse;

/** Use this for async callbacks when requesting a URI
 *
 *
 * @author edwardf
 * @since 2.0
 *
 */
public interface KinveyUriCallback extends KinveyClientCallback<UriLocResponse> {

    @Override
    public void onSuccess(UriLocResponse result);

    @Override
    public void onFailure(Throwable error);
}

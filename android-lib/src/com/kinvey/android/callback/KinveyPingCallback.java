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

import java.util.List;

/**
 * This class provides callbacks for an asynchronous Ping
 *
 *
 * @author edwardf
 * @since 2.0
 */
public interface KinveyPingCallback extends KinveyClientCallback<Boolean>{

    @Override
    public void onSuccess(Boolean result);

    @Override
    public void onFailure(Throwable error);
}

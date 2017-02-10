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
package com.kinvey.android.sync;


import com.kinvey.java.core.KinveyClientCallback;

/**
 * This class provides callbacks from requests executed by the Sync API.
 *
 * @author edwardf
 */
public interface KinveySyncCallback<T> {

    void onSuccess(KinveyPushResponse kinveyPushResponse, KinveyPullResponse<T> kinveyPullResponse);

    /**
     * Used to indicate start of pull request by background service
     */
    void onPullStarted();

    /**
     * Used to indicate start of push request by background service
     */
    void onPushStarted();

    /**
     * Used to indicate successfull execution of pull request by background service
     */
    void onPullSuccess(KinveyPullResponse<T> kinveyPullResponse);

    /**
     * Used to indicate successfull execution of push request by background service
     */
    void onPushSuccess(KinveyPushResponse kinveyPushResponse);


    /**
     * Used to indicate the failed execution of a request by the background service.
     */

    void onFailure(Throwable t);
}
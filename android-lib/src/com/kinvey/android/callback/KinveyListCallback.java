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

/** Use this for async callbacks when retrieving multiple entities
 *
 * @author edwardf
 * @since 2.0
 */
public interface KinveyListCallback<T> extends KinveyClientCallback<T[]> {


    @Override
    public void onSuccess(T[] result);

    @Override
    public void onFailure(Throwable error);

}

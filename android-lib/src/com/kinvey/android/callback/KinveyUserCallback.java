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

import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;

/**
 * This callback is typed for a {@link com.kinvey.java.User} object, use it for operations on Users.
 *
 * @author edwardf
 * @since 2.0
 */
public interface KinveyUserCallback extends KinveyClientCallback<User> {


    @Override
    public void onSuccess(User result);

    @Override
    public void onFailure(Throwable error);

}

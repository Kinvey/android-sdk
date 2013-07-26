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
 * This callback is typed for an array of {@link com.kinvey.java.User} objects, use it for bulk operations on the User collection..
 *
 * @author edwardf
 * @since 2.0
 */
public interface KinveyUserListCallback extends KinveyClientCallback<User[]> {

    /**
     * Method invoked after a successful request against a set of Users
     *
     * @param result - the modified users
     */
    @Override
    public void onSuccess(User[] result);

    /**
     * Method invoked after a failed request against a set of Users
     *
     * @param error - details about the error
     */
    @Override
    public void onFailure(Throwable error);

}

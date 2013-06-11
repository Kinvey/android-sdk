/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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

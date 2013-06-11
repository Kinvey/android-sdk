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

import com.kinvey.java.core.KinveyClientCallback;

/**
 * This callback is used for User Management operations, such as sending emails or password reset forms.
 * <p>
 * This methods which use this callback do not provide any return values, instead they either "ran" or "didn't run".
 * </p>
 *
 *
 * @author mjsalinger
 * @since 2.0
 */
public interface KinveyUserManagementCallback extends KinveyClientCallback<Void> {

    /**
     * Method invoked when a user operation completes.
     *
     * @param result - typed to {@code Void} because there is no usable return value.
     */
    @Override
    public void onSuccess(Void result);

    /**
     * Method invoked when a user operation fails to complete.
     * @param error - details about the error.
     */
    @Override
    public void onFailure(Throwable error);
}

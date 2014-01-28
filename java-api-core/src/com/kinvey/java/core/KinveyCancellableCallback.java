/*
 * Copyright (c) 2014, Kinvey, Inc.
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
package com.kinvey.java.core;

/**
 * @author edwardf
 */
public interface KinveyCancellableCallback<T> extends KinveyClientCallback<T> {


    /**
     * Have this method return true if the pending request should be cancelled.
     * This return value of this method will be checked regularly while execution occurs, and before the callback is made.
     * <p/>
     * If a request has already been sent over the network, having this method return true will only result in callbacks being ignored.
     * <p/>
     * Once a request has been sent over the network this functionality will ONLY have a client-side effect.
     *
     *
     * @return true if request should be cancelled, false if it should not
     */
    public boolean isCancelled();


    /**
     * Called on the UI thread after a request has been cancelled.
     * <p/>
     * Depending on the reason for cancellation, this method can either do nothing, or update the UI.
     *
     */
    public void onCancelled();

}

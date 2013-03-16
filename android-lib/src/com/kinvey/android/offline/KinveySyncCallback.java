/*
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
package com.kinvey.android.offline;


/**
 * This class provides callbacks from requests executed by the Offline API.
 * <p>
 * See the {@class com.kinvey.android.offline.OfflineResponseInfo} class for details about how offline requests are managed internally.
 * </p>
 *
 * @author edwardf
 */
public interface KinveySyncCallback {

    /**
     * Used to indicate successful execution of a request by the background service.
     * @param responseInfo - Information about the request
     */
    public void onSuccess(OfflineResponseInfo responseInfo);

    /**
     * Used to indicate the failed execution of a request by the background service.
     * @param responseInfo - Information about the request and failure.
     */
    public void onFailure(OfflineResponseInfo responseInfo);













}

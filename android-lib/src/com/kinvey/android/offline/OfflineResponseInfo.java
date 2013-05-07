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
package com.kinvey.android.offline;

import java.io.Serializable;

/**
 * This class maintains information about a response from a request executed by the offline sync service.
 *
 * myResponse.getRequest().getHTTPVerb() represents the HTTP verb as a String ("GET", "PUT", "DELETE", "POST");
 * myResponse.getRequest().getEntityID() represents the id of the entity, which might be stored in the local store.
 * myResponse.getResponse() represents the response of the associated request.
 */
public class OfflineResponseInfo implements Serializable {

    private static final long serialVersionUID = -444939394072970523L;

    private OfflineRequestInfo request;
    private boolean success;
    private Object responseObject;
    private String collection;

    public OfflineResponseInfo(OfflineRequestInfo req, Object resp, boolean  success, String collection) {
        this.request = req;
        this.responseObject = resp;
        this.success = success;
        this.collection = collection;
    }

    /**
     * Get the RequestInfo which resulted in this response
     *
     * @return the request associated with thsi response
     */
    public OfflineRequestInfo getRequest() {
        return request;
    }

    public void setRequest(OfflineRequestInfo request) {
        this.request = request;
    }

    /**
     * Get the JSON response Object.
     *
     * @return the JSON response
     */
    public Object getResponse() {
        return responseObject;
    }

    public void setResponse(Object response) {
        this.responseObject = response;
    }

    /**
     * Indication of if this request was a success or failure
     * @return true if success, false if failed.
     */
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }
}
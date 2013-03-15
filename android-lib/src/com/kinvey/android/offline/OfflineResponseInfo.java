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
 * This public static class maintains information about the client Response.
 * <p>
 * See the above RequestInfo-- this class maintains the RequestInfo of the request which resulted in this response.
 * </p>
 * <p>
 * This class also maintains the response of the request.
 * </p>
 *
 * myResponse.getRequest().getHTTPVerb() represents the HTTP verb as a String ("GET", "PUT", "DELETE", "POST");
 * myResponse.getRequest().getEntityID() represents the id of the entity, which might be stored in the local store.
 * myResponse.getResponse() represents the response of the associated request.
 */
public class OfflineResponseInfo implements Serializable {

    private static final long serialVersionUID = -444939394072970523L;

    private OfflineRequestInfo request;
    private boolean success;
    private String response;

    public OfflineResponseInfo(OfflineRequestInfo req, String resp, boolean  success) {
        this.request = req;
        this.response = resp;
        this.success = success;
    }


    public OfflineRequestInfo getRequest() {
        return request;
    }

    public void setRequest(OfflineRequestInfo request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
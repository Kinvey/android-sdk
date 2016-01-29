/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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

    public OfflineResponseInfo(OfflineRequestInfo req, Object resp, boolean  success) {
        this.request = req;
        this.responseObject = resp;
        this.success = success;
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


}
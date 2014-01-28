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

import com.kinvey.java.Query;

import java.io.Serializable;

/**
 * This class is an abstraction of a REST request.
 * <p/>
 * An instance of this class stores the relationship between an Http Verb and and an associated entity's ID.
 * <p/>
 * myRequest.getHttpVerb() represents the HTTP verb as a String ("GET", "PUT", "DELETE", "POST");
 * <p/>
 * myRequest.getEntityID() represents the id of the entity, which might be stored in the local store.
 *
 * @author edwardf
 */
public class OfflineRequestInfo implements Serializable {

    private static final long serialVersionUID = -444939394072970523L;

    //The Http verb of the client request ("GET", "PUT", "DELETE", "POST", "QUERY");
    private String verb;

    //The id of the entity, or the query string
    private String id;


    public OfflineRequestInfo(String httpVerb, String entityID) {
        this.verb = httpVerb;
        this.id = entityID;
    }

    /**
     * Get the HTTP VERB used by this request.
     * @return the HTTP Verb used by this request
     */
    public String getHttpVerb() {
        return this.verb;
    }

    /**
     * Get the entity used by this request.
     * @return the _id of the entity affected by this request
     */
    public String getEntityID() {
        return this.id;
    }

}
/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
 * This public class maintains information about the client request.
 * <p/>
 * This stores the relationship between an Http Verb and and an associated entity's ID.
 * <p/>
 * myRequest.getHttpVerb() represents the HTTP verb as a String ("GET", "PUT", "DELETE", "POST");
 * myRequest.getEntityID() represents the id of the entity, which might be stored in the local store.
 */
public class OfflineRequestInfo implements Serializable {

    private static final long serialVersionUID = -444939394072970523L;

    //The Http verb of the client request ("GET", "PUT", "DELETE", "POST", "QUERY");
    private String verb;

    //The id of the entity, assuming it is in the store.
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
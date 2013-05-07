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

import com.kinvey.java.Query;

import java.io.Serializable;

/**
 * This public static class maintains information about the client request.
 * <p/>
 * This stores the relationship between an Http Verb and and an associated entity's ID.
 * <p/>
 * myRequest.getHttpVerb() represents the HTTP verb as a String ("GET", "PUT", "DELETE", "POST");
 * myRequest.getEntityID() represents the id of the entity, which might be stored in the local store.
 */
public class OfflineRequestInfo implements Serializable {

    private static final long serialVersionUID = -444939394072970523L;

    //The Http verb of the client request ("GET", "PUT", "DELETE", "POST");
    private String verb;

    //The id of the entity, assuming it is in the store.
    private String id;

    private Query query;

    public OfflineRequestInfo(String httpVerb, String entityID) {
        this.verb = httpVerb;
        this.id = entityID;
    }

    public OfflineRequestInfo(String httpVerb, Query q, String queryjson){
        this.verb = httpVerb;
        this.query = q;
        this.id = queryjson;
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


    public Query getQuery() {
        return query;
    }
}
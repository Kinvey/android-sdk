/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
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

package com.kinvey.java.sync.dto;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Key;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Prots on 2/24/16.
 */
public class SyncRequest extends GenericJson implements Serializable {

    private static final long serialVersionUID = -444939384072970223L;


    public enum HttpVerb {
        GET("GET"),
        PUT("PUT"),
        POST("POST"),
        DELETE("DELETE"),
        SAVE("SAVE"), // for backward compatibility with previous versions of keeping Sync requests
        QUERY("QUERY");

        private String query;

        HttpVerb(String query) {

            this.query = query;
        }

        public static HttpVerb fromString(String verb){
            if (verb != null) {
                for (HttpVerb v : HttpVerb.values()){
                    if (v.query.equalsIgnoreCase(verb)){
                        return v;
                    }
                }
            }
            return null;
        }
    }


    //The Http verb of the client request ("GET", "PUT", "DELETE", "POST", "QUERY");
    @Key("verb")
    private String verb;

    //The id of the entity, or the query string
    @Key("meta")
    protected SyncMetaData id;

    @Key("collection")
    protected String collectionName;

    @Key("url")
    private String url;


    public SyncRequest(){}

    public SyncRequest(HttpVerb httpVerb, SyncMetaData entityID, GenericUrl url, String collectionName) {
        this.verb = httpVerb.name();
        this.id = entityID;
        this.collectionName = collectionName;
        this.url = url.toString();
    }

    public SyncRequest(HttpVerb httpVerb,
                       String entityID,
                       String clientAppVersion,
                       String customProperties,
                       GenericUrl url,
                       String collectionName){
        this.verb = httpVerb.query;
        this.collectionName = collectionName;
        this.url = url.toString();
        this.id = new SyncMetaData(entityID, clientAppVersion, customProperties);
    }

    /**
     * Get the HTTP VERB used by this request.
     * @return the HTTP Verb used by this request
     */
    public HttpVerb getHttpVerb() {
        return HttpVerb.fromString(this.verb);
    }

    /**
     * Get the entity used by this request.
     * @return the _id of the entity affected by this request
     */
    public SyncMetaData getEntityID() {
        return this.id;
    }


    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getUrl() {
        return url;
    }

    /**
     * This class represents the uniqueness of an entity, containing the _id, customerAppVersion, and any CustomHeaders.
     *
     */
    public static class SyncMetaData extends GenericJson {

        @Key
        public String id;

        @Key
        public String customerVersion;

        @Key
        public String customheader;


        @Key
        public String data;

        @Key
        public boolean bunchData = false;

        public SyncMetaData(){}

        public SyncMetaData(String id){
            this.id = id;
        }

        public SyncMetaData(String id, String customerVersion, String customHeader) {
            this.id = id;
            this.customerVersion = customerVersion;
            this.customheader = customHeader;
        }

        public SyncMetaData(String id, String customerVersion, String customHeader, boolean bunchData) {
            this(id, customerVersion, customHeader);
            this.bunchData = bunchData;
        }

        public SyncMetaData(String id, AbstractKinveyJsonClientRequest req) {
            this.id = id;
            if (req != null){
                this.customerVersion = req.getCustomerAppVersion();
                this.customheader = req.getCustomRequestProperties();
            }

        }

        public SyncMetaData(GenericJson entity, AbstractKinveyJsonClientRequest req) {
            this.id = (String) entity.get("_id");
            if (req != null){
                this.customerVersion = req.getCustomerAppVersion();
                this.customheader = req.getCustomRequestProperties();
            }
        }

        public GenericJson getEntity() {
            GenericJson entity = null;
            try {
                JsonFactory factory = getFactory();
                if (factory != null && data != null) {
                    entity = factory.createJsonParser(data).parse(GenericJson.class);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return entity;
        }
    }
}

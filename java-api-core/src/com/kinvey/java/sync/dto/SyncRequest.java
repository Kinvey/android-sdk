package com.kinvey.java.sync.dto;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Value;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;

import org.apache.tools.ant.taskdefs.condition.Http;

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
        QUERY("QUERY");

        private String query;

        HttpVerb(String query) {

            this.query = query;
        }

        public static HttpVerb fromString(String verb){
            for (HttpVerb v : HttpVerb.values()){
                if (v.query.equals(verb)){
                    return v;
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
    private SyncMetaData id;

    @Key("collection")
    private String collectionName;

    @Key("url")
    private String url;


    public SyncRequest(){};

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

        public SyncMetaData(){}

        public SyncMetaData(String id){
            this.id = id;
        }

        public SyncMetaData(String id, String customerVersion, String customHeader){
            this.id = id;
            this.customerVersion = customerVersion;
            this.customheader = customHeader;
        }

        public SyncMetaData(String id, AbstractKinveyJsonClientRequest req){
            this.id = id;
            if (req != null){
                this.customerVersion = req.getCustomerAppVersion();
                this.customheader = req.getCustomRequestProperties();
            }

        }
        public SyncMetaData(GenericJson entity, AbstractKinveyJsonClientRequest req){
            this.id = (String) entity.get("_id");
            if (req != null){
                this.customerVersion = req.getCustomerAppVersion();
                this.customheader = req.getCustomRequestProperties();
            }
        }



    }

}

package com.kinvey.android.sync.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.gson.annotations.SerializedName;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;

import java.io.Serializable;

/**
 * Created by Prots on 2/24/16.
 */
public class SyncRequest extends GenericJson implements Serializable {

    private static final long serialVersionUID = -444939384072970223L;


    public static enum HttpVerb {
        @SerializedName("GET")
        GET,
        @SerializedName("PUT")
        PUT,
        @SerializedName("POST")
        POST,
        @SerializedName("DELETE")
        DELETE,
        @SerializedName("QUERY")
        QUERY
    }


    //The Http verb of the client request ("GET", "PUT", "DELETE", "POST", "QUERY");
    @Key("verb")
    private HttpVerb verb;

    //The id of the entity, or the query string
    @Key("meta")
    private SyncMetaData id;

    @Key("collectionName")
    private String collectionName;


    public SyncRequest(HttpVerb httpVerb, SyncMetaData entityID) {
        this.verb = httpVerb;
        this.id = entityID;
    }

    public SyncRequest(HttpVerb httpVerb, String entityID, String clientAppVersion, String customProperties){
        this.verb = httpVerb;
        this.id = new SyncMetaData(entityID, clientAppVersion, customProperties);
    }

    /**
     * Get the HTTP VERB used by this request.
     * @return the HTTP Verb used by this request
     */
    public HttpVerb getHttpVerb() {
        return this.verb;
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

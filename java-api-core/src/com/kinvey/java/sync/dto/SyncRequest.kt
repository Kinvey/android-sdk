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

package com.kinvey.java.sync.dto

import com.google.api.client.http.GenericUrl
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonFactory
import com.google.api.client.util.Key
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import java.io.IOException
import java.io.Serializable

/**
 * Created by Prots on 2/24/16.
 */
open class SyncRequest : GenericJson, Serializable {
    enum class HttpVerb(private val query: String) {
        GET("GET"), PUT("PUT"), POST("POST"), DELETE("DELETE"), SAVE("SAVE"), // for backward compatibility with previous versions of keeping Sync requests
        QUERY("QUERY");

        companion object {
            fun fromString(verb: String?): HttpVerb? {
                if (verb != null) {
                    for (v in values()) {
                        if (v.query.equals(verb, ignoreCase = true)) {
                            return v
                        }
                    }
                }
                return null
            }
        }

    }

    //The Http verb of the client request ("GET", "PUT", "DELETE", "POST", "QUERY");
    @Key("verb")
    private var verb: String? = null
    /**
     * Get the entity used by this request.
     * @return the _id of the entity affected by this request
     */
    //The id of the entity, or the query string
    @Key("meta")
    var entityID: SyncMetaData? = null
        protected set
    @Key("collection")
    var collectionName: String? = ""
    @Key("url")
    var url: String? = null
        private set

    constructor() {}
    constructor(httpVerb: HttpVerb?, entityID: SyncMetaData?, url: GenericUrl, collectionName: String?) {
        verb = httpVerb?.name
        this.entityID = entityID
        this.collectionName = collectionName
        this.url = url.toString()
    }

    constructor(httpVerb: HttpVerb?,
                entityID: String,
                clientAppVersion: String,
                customProperties: String,
                url: GenericUrl,
                collectionName: String?) {
        verb = httpVerb?.name
        this.collectionName = collectionName
        this.url = url.toString()
        this.entityID = SyncMetaData(entityID, clientAppVersion, customProperties)
    }

    /**
     * Get the HTTP VERB used by this request.
     * @return the HTTP Verb used by this request
     */
    val httpVerb: HttpVerb?
        get() = HttpVerb.fromString(verb)

    /**
     * This class represents the uniqueness of an entity, containing the _id, customerAppVersion, and any CustomHeaders.
     *
     */
    class SyncMetaData : GenericJson {
        @Key
        var id: String? = null
        @Key
        var customerVersion: String? = null
        @Key
        var customheader: String? = null
        @Key
        var data: String? = null
        @Key
        var bunchData = false

        constructor() {}
        constructor(id: String?) {
            this.id = id
        }

        constructor(id: String, customerVersion: String, customHeader: String) {
            this.id = id
            this.customerVersion = customerVersion
            customheader = customHeader
        }

        constructor(id: String, customerVersion: String, customHeader: String, bunchData: Boolean) : this(id, customerVersion, customHeader) {
            this.bunchData = bunchData
        }

        constructor(id: String?, req: AbstractKinveyJsonClientRequest<*>?) {
            this.id = id
            if (req != null) {
                customerVersion = req.customerAppVersion
                customheader = req.customRequestProperties
            }
        }

        constructor(entity: GenericJson, req: AbstractKinveyJsonClientRequest<*>?) {
            id = entity["_id"] as String
            if (req != null) {
                customerVersion = req.customerAppVersion
                customheader = req.customRequestProperties
            }
        }

        val entity: GenericJson?
            get() {
                var entity: GenericJson? = null
                try {
                    val factory: JsonFactory? = factory
                    if (factory != null && data != null) {
                        entity = factory.createJsonParser(data).parse<GenericJson>(GenericJson::class.java)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return entity
            }
    }

    companion object {
        private const val serialVersionUID = -444939384072970223L
    }
}
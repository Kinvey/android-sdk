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

package com.kinvey.java.linkedResources

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import java.io.*
import java.util.HashMap

/**
 * This class maintains metadata and java specific file access to a NetworkFileManager associated with an Entity through the `com.kinvey.java.network.LinkedNetworkManager` API.
 *
 * There are references to a `ByteArrayInputStream as well as a `ByteArrayOutputStream`, which can be used to stream to/from the file.
 *
 * NOTE:  It is the responsibility of the client application to close these streams appropriately after usage.
 *
 *
 * @author mjsalinger
 * @author edwardf
 * @since 2.0
 */
class LinkedFile(): GenericJson() {

    /**
     * Get the id of a Linked NetworkFileManager
     *
     * @return  the id
     */
    @Key("_id")
    var id: String? = null
    @Key("_filename")
    var fileName: String? = null

    var input: ByteArrayInputStream? = null
    var output: ByteArrayOutputStream? = null
    var isResolve = true
    var extras: HashMap<String, Any>? = null
        private set

    /**
     * Constructor for a LinkedFile, sets BOTH filename and id to be input
     *
     * @param id - the filename which is also used as the id
     */
    constructor(id: String): this() {
        this.id = id
        this.fileName = id
    }

    /**
     * Constructor for LinkedFile, allowing unique id and filename
     *
     *
     * @param id the id to use for the linked file
     * @param filename the filename of the linkedfile
     */
    constructor(id: String, filename: String): this() {
        this.id = id
        this.fileName = filename
    }

    /**
     * Add an extra property to this KinveyFile.  When the NetworkFileManager is uploaded through LinkedNetworkManager, any extra properties here
     * will be added to the [com.kinvey.java.model.FileMetaData] object created during the file upload.
     *
     * @param key the key to use for the extra associated with the [com.kinvey.java.model.FileMetaData]
     * @param value the value of the extra
     */
    fun addExtra(key: String, value: Any) {
        if (extras == null) { extras = HashMap() }
        extras?.let { map -> map[key] = value }
    }

    /**
     * Retrieve an extra property by key associated with this KinveyFile.
     *
     * @param key the key used to define the property
     * @return the value of the property, or null if it hasn't been set.
     */
    fun getExtra(key: String): Any? {
        return if (extras != null && extras!!.containsKey(key)) {
            extras!![key]
        } else null

    }

    fun hasExtras(): Boolean {
        return extras != null && extras!!.size > 0
    }
}

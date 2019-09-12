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

/**
 * Use this class as a base Entity instead of `com.google.api.client.json.GenericJson` when using the LinkedNetworkManager API.
 *
 *
 * This class maintains a Map of linked files, using the JSONKey of the field as the key and a `com.kinvey.java.LinkedResources.LinkedFile` as the value.
 *
 *
 *
 * The LinkedNetworkManager API uses this map to determine if there are any attachments to download.
 *
 *
 * @author mjsalinger
 * @since 2.0
 */
abstract class LinkedGenericJson : GenericJson() {

    val allFiles = mutableMapOf<String, LinkedFile?>()

    /**
     * General constructor, initializes map of LinkedFiles
     */
    fun putFile(key: String, file: LinkedFile) {
        allFiles[key] = file
    }

    fun putFile(key: String) {
        allFiles[key] = null
    }

    fun getFile(key: String): LinkedFile? {
        return allFiles[key]
    }
}

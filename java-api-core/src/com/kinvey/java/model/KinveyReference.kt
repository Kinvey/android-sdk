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

package com.kinvey.java.model

import com.google.api.client.json.GenericJson
import com.google.api.client.util.ArrayMap
import com.google.api.client.util.Key
import com.kinvey.java.Logger

/**
 * A KinveyReference allows for entities to include relational data from other collections and entities.
 *
 *
 * To use this class within an Entities' GenericJson implementation, just define a field's type as a KinveyReference.
 *
 *
 *
 * Then, when, making a GET call through the NetworkManager API various parameters can be used to specify how the backend should
 * handle these mappings.
 *
 *
 *
 * The 'resolve' attribute can be passed a list of Strings containing all the KinveyReference fields that should be mapped.
 * The appropriate field in the Response Object should also be a KinveyReference,
 * but the relational mapping can be followed by accessing the returnObject field declared below.
 *
 *
 *
 * 'resolve_depth' is an int argument for GET requests through the NetworkManager API, and will resolve all KinveyReferences up to N levels.
 * Keep in mind a KinveyReference.returnObject can contain other embedded KinveyReferences, and so on, allowing for multiple mappings
 * without explicitly declaring all fields.
 *
 *
 *
 * `retain_references` is also supported, and can be used to pull the contents of returnObject up to replace the KinveyReference.
 * This flag defaults to true, but can be explicitly set to false if an end user only cares about the 'returnObject' value.
 *
 *
 * @author edwardf
 * @since 2.0
 */
data class KinveyReference(
    @Key("_collection")
    var collection: String?,
    @Key("_id")
    var id: String?
) : GenericJson() {

    @Key("_type")
    var type: String? = "KinveyRef"

    val resolvedObject: GenericJson?
        get() {
            val direct = get("_obj") as ArrayMap<String, Any>? ?: return null
            val ret = GenericJson()
            ret.putAll(direct)
            return ret
        }

    fun <T : GenericJson> getTypedObject(clazz: Class<T>): T? {
        val direct = get("_obj") as ArrayMap<String, Any>? ?: return null
        var ret: T? = null
        try {
            ret = clazz.newInstance()
            ret?.putAll(direct)
        } catch (e: Exception) {
            Logger.ERROR("unable to instantiate class!")
            //e.printStackTrace();
        }
        return ret
    }

    companion object {
        @Deprecated("moved to {@link com.kinvey.java.User#USER_COLLECTION_NAME}")
        const val USER_COLLECTION = "user"
        const val RESOLVED_KEY = "_obj"
    }
}

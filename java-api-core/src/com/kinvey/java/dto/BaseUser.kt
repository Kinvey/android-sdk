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

package com.kinvey.java.dto

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key

import com.kinvey.java.Constants.AUTH_TOKEN
import com.kinvey.java.Constants._LLT
import com.kinvey.java.model.KinveyMetaData.Companion.KMD

/**
 * Created by Prots on 2/12/16.
 */
open class BaseUser : GenericJson() {
    @Key("_id")
    var id: String? = null
    @Key("username")
    var username: String? = null
    var authToken: String? = null

    fun setAuthTokenToKmd(authToken: String) {
        if (get(KMD) != null) {
            val kmd = get(KMD) as MutableMap<String, String>?
            if (kmd != null) {
                kmd.put(AUTH_TOKEN, authToken)
                put(KMD, kmd)
            }
        }
    }

    val lastLoginTime:() -> String? = {(get(KMD) as? MutableMap<*, *>)?.get(_LLT) as String?}

}

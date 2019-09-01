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
import com.google.api.client.util.Key

import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedHashMap

import com.kinvey.java.Query
import com.kinvey.java.core.AbstractKinveyJsonClient

/**
 * This class maintains information about Aggregation Requests
 *
 * @author mjsalinger
 * @since 2.0
 */
class AggregateEntity(
    fields: ArrayList<String>?,
    type: AggregateType,
    aggregateField: String?,
    query: Query?,
    client: AbstractKinveyJsonClient?
) : GenericJson() {
    @Key
    val key: Map<String, Boolean>?
    @Key
    val initial: HashMap<String, Any>?
    @Key
    var reduce: String? = null
    @Key
    var condition: LinkedHashMap<String, Any>? = null

    init {
        key = fields?.map { str -> str to true }?.toMap()
        reduce = ""
        condition = query?.queryFilterMap as LinkedHashMap<String, Any>? ?: LinkedHashMap()
        initial = HashMap()
        when (type) {
            AggregateType.COUNT -> {
                initial["_result"] = 0
                reduce = "function(doc,out){ out._result++;}"
            }
            AggregateType.SUM -> {
                initial["_result"] = 0
                reduce = "function(doc,out){ out._result= out._result + doc.$aggregateField;}"
            }
            AggregateType.MIN -> {
                initial["_result"] = "Infinity"
                reduce = "function(doc,out){ out._result = Math.min(out._result, doc.$aggregateField);}"
            }
            AggregateType.MAX -> {
                initial["_result"] = "-Infinity"
                reduce = "function(doc,out){ out._result = Math.max(out._result, doc.$aggregateField);}"
            }
            AggregateType.AVERAGE -> {
                initial["_result"] = 0
                reduce = "function(doc,out){ var count = (out._kcs_count == undefined) ? 0 : out._kcs_count; out._result =(out._result * count + doc.$aggregateField) / (count + 1); out._kcs_count = count+1;}"
            }
        }
    }
}

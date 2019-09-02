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

package com.kinvey.java.core

import com.google.api.client.http.HttpResponse
import com.google.api.client.http.UriTemplate
import com.google.api.client.http.json.JsonHttpContent
import com.google.api.client.json.GenericJson
import com.google.api.client.json.Json
import com.kinvey.java.AbstractClient

import java.util.UUID

/**
 * @author m0rganic
 */
abstract class AbstractKinveyJsonClientRequest<T>
/**
 * @param abstractKinveyJsonClient kinvey credential JSON client
 * @param requestMethod HTTP Method
 * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
 * the base path from the base URL will be stripped out. The URI template can also be a
 * full URL. URI template expansion is done using
 * [UriTemplate.expand]
 * @param jsonContent POJO that can be serialized into JSON content or `null` for none
 * @param responseClass response class to parse into
 */
protected constructor(abstractKinveyJsonClient: AbstractClient<*>,
                      requestMethod: String, uriTemplate: String,
     /** raw json data  */
     /**
      * @return the jsonContent
      */
    val jsonContent: GenericJson, responseClass: Class<T>?)
    : AbstractKinveyClientRequest<T>(abstractKinveyJsonClient, requestMethod, uriTemplate,
        if (jsonContent == null) null
        else JsonHttpContent(abstractKinveyJsonClient.jsonFactory, jsonContent), responseClass) {

    var executor: AsyncExecutor<*>? = null

    override val abstractKinveyClient: AbstractKinveyJsonClient
        get() = super.abstractKinveyClient as AbstractKinveyJsonClient

    init {
        if (jsonContent != null) {
            super.getRequestHeaders().contentType = Json.MEDIA_TYPE
        }
    }

    override fun newExceptionOnError(response: HttpResponse): KinveyJsonResponseException {
        return KinveyJsonResponseException.from(abstractKinveyClient.jsonFactory, response)
    }

    companion object {

        const val TEMPID = "tempOfflineID_"

        val uuid: String
            get() {
                val id = UUID.randomUUID().toString()
                return id.replace("-", "")
            }
    }
}

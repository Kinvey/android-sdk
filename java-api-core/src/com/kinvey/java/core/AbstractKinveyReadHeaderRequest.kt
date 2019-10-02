/*
 *  Copyright (c) 2018, Kinvey, Inc. All rights reserved.
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

import com.google.api.client.http.HttpMethods
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpStatusCodes
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.util.Charsets
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.KinveyException
import com.kinvey.java.Logger
import com.kinvey.java.model.AbstractKinveyHeadersResponse

import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.Locale

/**
 * Created by yuliya on 05/07/17.
 */

abstract class AbstractKinveyReadHeaderRequest<T : AbstractKinveyHeadersResponse>
/**
 * @param abstractKinveyJsonClient kinvey credential JSON client
 * @param requestMethod            HTTP Method
 * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
 * the base path from the base URL will be stripped out. The URI template can also be a
 * full URL.
 * @param jsonContent              POJO that can be serialized into JSON content or `null` for none
 */
protected constructor(abstractKinveyJsonClient: AbstractClient<*>, requestMethod: String,
                      uriTemplate: String, jsonContent: GenericJson?, responseClass: Class<T>)
    : AbstractKinveyJsonClientRequest<T>(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, responseClass) {

    @Throws(IOException::class)
    override fun execute(): T? {
        val response = executeUnparsed()
        if (overrideRedirect) {
            return onRedirect(response?.headers?.location ?: "")
        }
        // special class to handle void or empty responses
        if (response?.content == null) {
            response?.ignore()
            return null
        }
        try {
            var ret: T? = null
            val statusCode = response.statusCode
            if (response.request.requestMethod == HttpMethods.HEAD || statusCode / 100 == 1
                    || statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT
                    || statusCode == HttpStatusCodes.STATUS_CODE_NOT_MODIFIED) {
                response.ignore()
                return null

            } else {
                val jsonString = response.parseAsString()
                val objectParser = abstractKinveyClient.getObjectParser()
                ret = objectParser?.parseAndClose(ByteArrayInputStream(jsonString.toByteArray(Charsets.UTF_8)), Charsets.UTF_8, responseClass)
                ret?.run {
                    if (response.headers.containsKey(Constants.X_KINVEY_REQUEST_START)) {
                        this.lastRequestTime = response.headers.getHeaderStringValues(Constants.X_KINVEY_REQUEST_START)[0].toUpperCase(Locale.US)
                    } else if (response.headers.containsKey(Constants.X_KINVEY_REQUEST_START_CAMEL_CASE)) {
                        this.lastRequestTime = response.headers.getHeaderStringValues(Constants.X_KINVEY_REQUEST_START_CAMEL_CASE)[0].toUpperCase(Locale.US)
                    }
                }
                return ret
            }

        } catch (e: IllegalArgumentException) {
            Logger.ERROR("unable to parse response -> $e")
            throw KinveyException("Unable to parse the JSON in the response", "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)", e.toString())
        } catch (ex: NullPointerException) {
            return null
        }
    }
}

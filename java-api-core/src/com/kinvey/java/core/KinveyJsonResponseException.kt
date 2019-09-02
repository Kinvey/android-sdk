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

import com.google.api.client.http.HttpMediaType
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import com.google.api.client.json.Json
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.JsonParser
import com.google.common.io.Closer
import com.kinvey.java.KinveyException
import com.kinvey.java.store.file.FileUtils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * @author m0rganic
 */
class KinveyJsonResponseException
/**
 * @param response raw http response
 * @param details  detail message give by the response
 * @param message  general message
 */
private constructor(response: HttpResponse?, val details: KinveyJsonError?, override val message: String)
    : HttpResponseException(response) {

    companion object {

        private val serialVersionUID = -5586707180518343613L

        /**
         * @param jsonFactory json factory to use while parsing the response
         * @param response    raw http response
         * @return exception object built up from the raw http response
         */
        @JvmStatic
        fun from(jsonFactory: JsonFactory, response: HttpResponse): KinveyJsonResponseException {
            var kinveyJsonResponseException: KinveyJsonResponseException? = null
            var details: KinveyJsonError? = null
            try {
                if (!response.isSuccessStatusCode
                        && HttpMediaType.equalsIgnoreParameters(Json.MEDIA_TYPE, response.contentType)
                        && response.content != null) {
                    var parser: JsonParser? = null
                    val closer = Closer.create()
                    try {
                        val outputStream = closer.register(ByteArrayOutputStream())
                        FileUtils.copyStreams(response.content, outputStream)
                        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
                        parser = jsonFactory.createJsonParser(response.content)
                        details = KinveyJsonError.parse(jsonFactory, inputStream, response.contentCharset)
                    } catch (e: Exception) {
                        throw KinveyException("Unable to parse the JSON in the response",
                                "examine BL or DLC to ensure data format is correct. If the exception is caused by `key <somkey>`, then <somekey> might be a different type than is expected (int instead of of string)",
                                e.toString())
                    } finally {
                        if (parser == null) { response.ignore()
                        } else if (details == null) {
                            parser.close()
                        }
                        closer.close()
                    }
                }
            } catch (exception: IOException) {
                // it would be bad to throw an exception while throwing an exception
                exception.printStackTrace()
            }

            val detailMessage =
                    if (details == null) "unknown"
                    else String.format("%s%n%s", details.error, details.description)

            kinveyJsonResponseException = KinveyJsonResponseException(response, details, detailMessage)

            return kinveyJsonResponseException
        }
    }
}

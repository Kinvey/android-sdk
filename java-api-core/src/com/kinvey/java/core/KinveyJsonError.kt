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
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.util.GenericData
import com.google.api.client.util.Key

import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

/**
 *
 *
 */
class KinveyJsonError : GenericData() {

    /**  */
    /**
     * @return the error
     */
    /**
     * @param error the error to set
     */
    @Key
    var error: String? = null

    /**  */
    /**
     * @return the description
     */
    /**
     * @param description the description to set
     */
    @Key
    var description: String? = null

    /**  */
    /**
     * @return the debug
     */
    /**
     * @param debug the debug to set
     */
    @Key
    var debug: String? = null

    companion object {

        /**
         * Parses the HttpResponse as a standard Kinvey error.
         *
         * @param jsonFactory the json factory to use while parsing
         * @param response raw http response to parse
         * @return standard error object
         * @throws IOException error occurred during parse
         */
        @Throws(IOException::class)
        @JvmStatic
        fun parse(jsonFactory: JsonFactory, response: HttpResponse): KinveyJsonError {
            return JsonObjectParser(jsonFactory).parseAndClose(response.content,
                    response.contentCharset, KinveyJsonError::class.java)
        }

        /**
         * Parses the HttpResponse as a standard Kinvey error.
         *
         * @param jsonFactory the json factory to use while parsing
         * @param content
         * @param charset
         * @return standard error object
         * @throws IOException error occurred during parse
         */
        @Throws(IOException::class)
        @JvmStatic
        fun parse(jsonFactory: JsonFactory, content: InputStream, charset: Charset): KinveyJsonError {
            return JsonObjectParser(jsonFactory).parseAndClose(content, charset, KinveyJsonError::class.java)
        }
    }
}

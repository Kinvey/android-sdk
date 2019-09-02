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

package com.kinvey.java

import com.google.api.client.json.GenericJson
import com.google.api.client.util.GenericData
import com.google.api.client.util.Key
import com.google.common.base.Preconditions
import com.google.gson.Gson
import com.kinvey.java.core.AbstractKinveyJsonClientRequest

import java.io.IOException
import java.lang.reflect.Array

/**
 * Class for managing access to custom endpoints.
 *
 *
 * After defining a Custom Endpoint on a backend at Kinvey, this class can be used to execute remote commands.
 *
 *
 * @author edwardf
 * @since 2.0.2
 */
open class CustomEndpoints<I : GenericJson, O> {

    private var client: AbstractClient<*>? = null
    var currentResponseClass: Class<O>? = null

    private var clientAppVersion: String? = null

    private var customRequestProperties: GenericData? = GenericData()

    fun setClientAppVersion(appVersion: String) {
        this.clientAppVersion = appVersion
    }

    fun setClientAppVersion(major: Int, minor: Int, revision: Int) {
        setClientAppVersion("$major.$minor.$revision")
    }

    fun setCustomRequestProperties(customheaders: GenericJson) {
        this.customRequestProperties = customheaders
    }

    fun setCustomRequestProperty(key: String, value: Any) {
        if (this.customRequestProperties == null) {
            this.customRequestProperties = GenericJson()
        }
        this.customRequestProperties!![key] = value
    }

    fun clearCustomRequestProperties() {
        this.customRequestProperties = GenericJson()
    }


    /**
     * Create a new instance, should only be called by an [AbstractClient].
     * @param client - an active logged in Client
     */
    constructor(client: AbstractClient<*>) {
        this.client = client
        this.clientAppVersion = client.clientAppVersion
        this.customRequestProperties = client.customRequestProperties
    }

    /**
     * Create a new instance, should only be called by an [AbstractClient]
     *
     * @param responseClass the class of the response object
     * @param client - an active logged in client
     */
    constructor(responseClass: Class<O>, client: AbstractClient<*>) {
        this.client = client
        this.currentResponseClass = responseClass
        this.clientAppVersion = client.clientAppVersion
        this.customRequestProperties = client.customRequestProperties
    }

    /**
     * Execute a Custom Endpoint which returns a single JSON element
     *
     * @param endpoint - the name of the Custom Endpoint
     * @param input - any required input, can be `null`
     * @return a CustomCommand request ready to be executed.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun callEndpointBlocking(endpoint: String?, input: I?): CustomCommand< I, O> {
        Preconditions.checkNotNull(endpoint, "commandName must not be null")
        val command = CustomCommand(client as AbstractClient<*>, endpoint, input, currentResponseClass as Class<O>)
        client?.initializeRequest(command)
        return command
    }

    /**
     * Execute a Custom Endpoint which returns an array of JSON elements.
     *
     * @param endpoint - the name of the Custom Endpoint
     * @param input - any required input, can be `null`
     * @return a CustomCommand ready to be executed
     * @throws IOException
     */
    @Throws(IOException::class)
    fun callEndpointArrayBlocking(endpoint: String, input: I?): CustomCommandArray<I, O> {
        Preconditions.checkNotNull(endpoint, "commandName must not be null")
        val command = CustomCommandArray(client as AbstractClient<*>, endpoint, input, Array.newInstance(currentResponseClass, 0).javaClass)
        client?.initializeRequest(command)
        return command as CustomCommandArray<I, O>
    }

    /**
     * A JSON client request which executes against a custom endpoint returning a single JSON object.
     *
     */
    class CustomCommand<I : GenericJson, O> constructor(client: AbstractClient<*>, @Key private val endpoint: String?,
                                             args: I?, responseClass: Class<O>)
        : AbstractKinveyJsonClientRequest<O>(client, "POST", REST_PATH, args, responseClass) {

        init {
            this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = client.clientAppVersion
            if (client.customRequestProperties?.isEmpty() == false) {
                this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(client.customRequestProperties)
            }
        }

        companion object {
            private const val REST_PATH = "rpc/{appKey}/custom/{endpoint}"
        }
    }

    /**
     * A JSON client request which executes against a custom endpoint returning an array.
     *
     */
    class CustomCommandArray<I : GenericJson, O> constructor(client: AbstractClient<*>, @Key private val endpoint: String?, args: I?, responseClass: Class<O>)
        : AbstractKinveyJsonClientRequest<O>(client, "POST", REST_PATH, args, responseClass) {

        init {
            this.getRequestHeaders()["X-Kinvey-Client-App-Version"] = client.clientAppVersion
            if (client.customRequestProperties?.isEmpty() == false) {
                this.getRequestHeaders()["X-Kinvey-Custom-Request-Properties"] = Gson().toJson(client.customRequestProperties)
            }
        }

        companion object {
            private const val REST_PATH = "rpc/{appKey}/custom/{endpoint}"
        }
    }
}

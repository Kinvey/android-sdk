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
package com.kinvey.android

import com.google.api.client.json.GenericJson
import com.kinvey.android.callback.KinveyListCallback
import com.kinvey.java.AbstractClient
import com.kinvey.java.CustomEndpoints
import com.kinvey.java.core.KinveyClientCallback

import java.io.IOException
import java.util.Arrays

/**
 * Wraps the [com.kinvey.java.CustomEndpoints] public methods in asynchronous functionality using native Android AsyncTask.
 *
 *
 * NOTE:  It is the responsibility of the user to either use a [com.kinvey.android.callback.KinveyListCallback] or
 * a [com.kinvey.java.core.KinveyClientCallback] depending on what their Custom Endpoint returns.
 *
 *
 *
 * Sample usage:
 * <pre>
 * `mKinveyClient.customEndpoints().callEndpoint("myCustomCommand", null, new KinveyListCallback<GenericJson>() {
 * public void onSuccess(GenericJson[] result) {
 * results.setText(result[0].toString());
 * }
 * public void onFailure(Throwable error) {
 * results.setText("Uh oh -> " + error);
 * }
 * });
` *
</pre> *
 * The above sample assumes the Custom Endpoint takes no input `null` and returns an array of JSON objects.
 *
 *
 * @author edwardf
 * @since 2.0.2
 */
class AsyncCustomEndpoints<I : GenericJson, O> : CustomEndpoints<I, O> {

    /**
     * Constructor for this Asyncronous Custom Endpoint class
     *
     * @param client
     */
    @Deprecated("")
    constructor(client: AbstractClient<*>) : super(client)

    /**
     * Constructor for this Asyncronous Custom Endpoint class
     *
     * @param responseClass the class of the expected resposne object
     * @param client an active logged in client
     */
    constructor(responseClass: Class<O>, client: AbstractClient<*>) : super(responseClass, client)

    /**
     * Execute a Custom Endpoint which will return a single JSON object
     *
     * @param commandName - the name of the Command to execute
     * @param input - any required input, can be `null`
     * @param callback - get results of the command as a single JSON object
     */
    fun callEndpoint(commandName: String, input: I, callback: KinveyClientCallback<O>) {
        AsyncCommand(this, commandName, input, callback).execute()
    }

    /**
     * Execute a Custom Endpoint which will return an array of JSON objects.
     *
     * @param commandName - the name of the Command to execute
     * @param input - any required input, can be `null`
     * @param callback - get results of the command as an array of JSON objects.
     */
    fun callEndpoint(commandName: String, input: I?, callback: KinveyListCallback<O>) {
        AsyncCommandArray(this, commandName, input, callback).execute()
    }

    class AsyncCommand<I: GenericJson, O>(val endpoints: AsyncCustomEndpoints<I, O>,
        private val commandName: String, private val input: I, callback: KinveyClientCallback<O>) : AsyncClientRequest<O>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): O? {
            return endpoints.callEndpointBlocking(commandName, input).execute()
        }
    }

    class AsyncCommandArray<I: GenericJson, O>(val endpoints: AsyncCustomEndpoints<I, O>,
        private val commandName: String, private val input: I?, callback: KinveyListCallback<O>) : AsyncClientRequest<List<O>>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): List<O> {
            return Arrays.asList(endpoints.callEndpointArrayBlocking(commandName, input).execute()!!)
        }
    }
}

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
package com.kinvey.android;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.CustomEndpoints;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Wraps the {@link com.kinvey.java.CustomEndpoints} public methods in asynchronous functionality using native Android AsyncTask.
 * <p>
 *  NOTE:  It is the responsibility of the user to either use a {@link com.kinvey.android.callback.KinveyListCallback} or
 *  a {@link com.kinvey.java.core.KinveyClientCallback} depending on what their Custom Endpoint returns.
 * </p>
 * <p>
 * Sample usage:
 * <pre>
 * {@code
 *      mKinveyClient.customEndpoints().callEndpoint("myCustomCommand", null, new KinveyListCallback<GenericJson>() {
 *        public void onSuccess(GenericJson[] result) {
 *              results.setText(result[0].toString());
 *          }
 *        public void onFailure(Throwable error) {
 *              results.setText("Uh oh -> " + error);
 *          }
 *      });
 * }
 * </pre>
 *  The above sample assumes the Custom Endpoint takes no input {@code null} and returns an array of JSON objects.
 * </p>
 *
 * @author edwardf
 * @since 2.0.2
 */
public class AsyncCustomEndpoints<I extends GenericJson, O> extends CustomEndpoints<I, O> {

    /**
     * Constructor for this Asyncronous Custom Endpoint class
     *
     * @param client
     */
    @Deprecated
    public AsyncCustomEndpoints(AbstractClient client) {
        super(client);
    }

    /**
     * Constructor for this Asyncronous Custom Endpoint class
     *
     * @param responseClass the class of the expected resposne object
     * @param client an active logged in client
     */
    public AsyncCustomEndpoints(Class<O> responseClass, AbstractClient client){
        super(responseClass, client);
    }


    /**
     * Execute a Custom Endpoint which will return a single JSON object
     *
     * @param commandName - the name of the Command to execute
     * @param input - any required input, can be {@code null}
     * @param callback - get results of the command as a single JSON object
     */
    public void callEndpoint(String commandName, I input, KinveyClientCallback callback){
        new AsyncCommand(commandName, input, callback).execute();
    }

    /**
     * Execute a Custom Endpoint which will return an array of JSON objects.
     *
     * @param commandName - the name of the Command to execute
     * @param input - any required input, can be {@code null}
     * @param callback - get results of the command as an array of JSON objects.
     */
    public void callEndpoint(String commandName, I input, KinveyListCallback callback){
        new AsyncCommandArray(commandName, input, callback).execute();
    }






    private class AsyncCommand extends AsyncClientRequest<O> {

        private String commandName;
        private I input;

        public AsyncCommand(String commandName, I input, KinveyClientCallback callback) {
            super(callback);
            this.commandName = commandName;
            this.input = input;
        }

        @Override
        protected O executeAsync() throws IOException {
            return AsyncCustomEndpoints.this.callEndpointBlocking(commandName, input).execute();
        }

    }

    private class AsyncCommandArray extends AsyncClientRequest<List<O>> {

        private String commandName;
        private I input;

        public AsyncCommandArray(String commandName, I input, KinveyListCallback callback) {
            super(callback);
            this.commandName = commandName;
            this.input = input;
        }

        @Override
        protected List<O> executeAsync() throws IOException {
            return Arrays.asList(AsyncCustomEndpoints.this.callEndpointArrayBlocking(commandName, input).execute());
        }

    }}

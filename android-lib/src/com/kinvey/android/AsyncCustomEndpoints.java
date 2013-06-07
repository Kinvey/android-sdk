/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.android;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.CustomEndpoints;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;

/**
 * Wraps the {@link com.kinvey.java.CustomEndpoints} public methods in asynchronous functionality using native Android AsyncTask.
 * <p>
 *  NOTE:  It is the responsibility of the user to either use a {@code com.kinvey.android.callback.KinveyListCallback} or
 *  a {@code com.kinvey.android.callback.KinveyClientCallback} depending on what their Custom Endpoint returns.
 * </p>
 * <p>
 * Sample usage:
 * <pre>
 * {@code
 *    mKinveyClient.customEndpoints().callEndpoint("myCustomCommand", null, new KinveyListCallback<GenericJson>() {
        @Override
        public void onSuccess(GenericJson[] result) {
            results.setText(result[0].toString());
        }
        @Override
        public void onFailure(Throwable error) {
            results.setText("Uh oh -> " + error);
        }
    });
 * </pre>
 *  The above sample assumes the Custom Endpoint takes no input and returns an array of JSON objects.
 * </p>
 *
 * @author edwardf
 * @since 2.0.2
 */
public class AsyncCustomEndpoints extends CustomEndpoints {

    /**
     * Constructor for this Asyncronous Custom Endpoint class
     *
     * @param client
     */
    public AsyncCustomEndpoints(AbstractClient client) {
        super(client);
    }


    /**
     * Execute a Custom Endpoint which will return a single JSON object
     *
     * @param commandName - the name of the Command to execute
     * @param input - any required input, can be {@code null}
     * @param callback - get results of the command as a single JSON object
     */
    public void callEndpoint(String commandName, GenericJson input, KinveyClientCallback callback){
        new AsyncCommand(commandName, input, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Execute a Custom Endpoint which will return an array of JSON objects.
     *
     * @param commandName - the name of the Command to execute
     * @param input - any required input, can be {@code null}
     * @param callback - get results of the command as an array of JSON objects.
     */
    public void callEndpoint(String commandName, GenericJson input, KinveyListCallback callback){
        new AsyncCommandArray(commandName, input, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }




    private class AsyncCommand extends AsyncClientRequest<GenericJson> {

        private String commandName;
        private GenericJson input;

        public AsyncCommand(String commandName, GenericJson input, KinveyClientCallback callback) {
            super(callback);
            this.commandName = commandName;
            this.input = input;
        }

        @Override
        protected GenericJson executeAsync() throws IOException {
            return AsyncCustomEndpoints.this.callEndpointBlocking(commandName, input).execute();
        }

    }

    private class AsyncCommandArray extends AsyncClientRequest<GenericJson[]> {

        private String commandName;
        private GenericJson input;

        public AsyncCommandArray(String commandName, GenericJson input, KinveyListCallback callback) {
            super(callback);
            this.commandName = commandName;
            this.input = input;
        }

        @Override
        protected GenericJson[] executeAsync() throws IOException {
            return AsyncCustomEndpoints.this.callEndpointArrayBlocking(commandName, input).execute();
        }

    }}

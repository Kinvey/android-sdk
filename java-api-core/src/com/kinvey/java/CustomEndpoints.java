/*
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
package com.kinvey.java;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;

import java.io.IOException;

/**
 * Class for managing access to custom endpoints.
 * <p>
 * After defining a Custom Endpoint on a backend at Kinvey, this class can be used to execute remote commands.
 * </p>
 *
 * @author edwardf
 * @since 2.0.2
 */
public class CustomEndpoints {

    private AbstractClient client;

    /**
     * Create a new instance of this Custom Endpoints class
     * @param client - an active logged in Client
     */
    public CustomEndpoints(AbstractClient client){
        this.client = client;
    }

    /**
     * Execute a Custom Endpoint which returns a single JSON element
     *
     * @param endpoint - the name of the Custom Endpoint
     * @param input - any required input, can be {@code null}
     * @return a CustomCommand request ready to be executed.
     * @throws IOException
     */
    public customCommand callEndpointBlocking(String endpoint, GenericJson input) throws IOException{
        Preconditions.checkNotNull(endpoint, "commandName must not be null");
        if (input == null){
            input = new GenericJson();
        }
        customCommand command = new customCommand(endpoint, input,  GenericJson.class);
        client.initializeRequest(command);
        return command;
    }

    /**
     * Execute a Custom Endpoint which returns an array of JSON elements.
     *
     * @param endpoint - the name of the Custom Endpoint
     * @param input - any required input, can be {@code null}
     * @return a CustomCommand ready to be executed
     * @throws IOException
     */
    public customCommandArray callEndpointArrayBlocking(String endpoint, GenericJson input) throws IOException{
        Preconditions.checkNotNull(endpoint, "commandName must not be null");
        if (input == null){
            input = new GenericJson();
        }
        customCommandArray command = new customCommandArray(endpoint, input,  GenericJson[].class);
        client.initializeRequest(command);
        return command;
    }


    /**
     * A JSON client request which executes against a custom endpoint returning a single JSON object.
     *
     */
    protected class customCommand extends AbstractKinveyJsonClientRequest<GenericJson> {
        private static final String REST_PATH = "rpc/{appKey}/custom/{endpoint}";

        @Key
        private String endpoint;


        customCommand(String commandName, GenericJson args, Class responseClass) {
            super(client, "POST", REST_PATH, args, responseClass);
            this.endpoint = commandName;
        }


    }
    /**
     * A JSON client request which executes against a custom endpoint returning an array.
     *
     */
    protected class customCommandArray extends AbstractKinveyJsonClientRequest<GenericJson[]> {
        private static final String REST_PATH = "rpc/{appKey}/custom/{endpoint}";

        @Key
        private String endpoint;


        customCommandArray(String commandName, GenericJson args, Class responseClass) {
            super(client, "POST", REST_PATH, args, responseClass);
            this.endpoint = commandName;
        }


    }


}

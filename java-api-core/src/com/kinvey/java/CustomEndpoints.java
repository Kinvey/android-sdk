/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
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
     * Create a new instance, should only be called by an {@link AbstractClient}.
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
    public CustomCommand callEndpointBlocking(String endpoint, GenericJson input) throws IOException{
        Preconditions.checkNotNull(endpoint, "commandName must not be null");
        if (input == null){
            input = new GenericJson();
        }
        CustomCommand command = new CustomCommand(endpoint, input,  GenericJson.class);
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
    public CustomCommandArray callEndpointArrayBlocking(String endpoint, GenericJson input) throws IOException{
        Preconditions.checkNotNull(endpoint, "commandName must not be null");
        if (input == null){
            input = new GenericJson();
        }
        CustomCommandArray command = new CustomCommandArray(endpoint, input,  GenericJson[].class);
        client.initializeRequest(command);
        return command;
    }


    /**
     * A JSON client request which executes against a custom endpoint returning a single JSON object.
     *
     */
    protected class CustomCommand extends AbstractKinveyJsonClientRequest<GenericJson> {
        private static final String REST_PATH = "rpc/{appKey}/custom/{endpoint}";

        @Key
        private String endpoint;


        CustomCommand(String commandName, GenericJson args, Class responseClass) {
            super(client, "POST", REST_PATH, args, responseClass);
            this.endpoint = commandName;
        }


    }
    /**
     * A JSON client request which executes against a custom endpoint returning an array.
     *
     */
    protected class CustomCommandArray extends AbstractKinveyJsonClientRequest<GenericJson[]> {
        private static final String REST_PATH = "rpc/{appKey}/custom/{endpoint}";

        @Key
        private String endpoint;


        CustomCommandArray(String commandName, GenericJson args, Class responseClass) {
            super(client, "POST", REST_PATH, args, responseClass);
            this.endpoint = commandName;
        }


    }


}

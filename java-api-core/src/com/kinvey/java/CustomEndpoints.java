/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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

import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;

import java.io.IOException;
import java.lang.reflect.Array;

/**
 * Class for managing access to custom endpoints.
 * <p>
 * After defining a Custom Endpoint on a backend at Kinvey, this class can be used to execute remote commands.
 * </p>
 *
 * @author edwardf
 * @since 2.0.2
 */
public class CustomEndpoints<I, O> {

    private AbstractClient client;
    private Class<O> currentResponseClass;

    /**
     * Create a new instance, should only be called by an {@link AbstractClient}.
     * @param client - an active logged in Client
     */
    public CustomEndpoints(AbstractClient client){
        this.client = client;
    }

    /**
     * Create a new instance, should only be called by an {@link AbstractClient}
     *
     * @param responseClass the class of the response object
     * @param client - an active logged in client
     */
    public CustomEndpoints(Class<O> responseClass, AbstractClient client){
        this.client = client;
        this.currentResponseClass = responseClass;

    }

    /**
     * Execute a Custom Endpoint which returns a single JSON element
     *
     * @param endpoint - the name of the Custom Endpoint
     * @param input - any required input, can be {@code null}
     * @return a CustomCommand request ready to be executed.
     * @throws IOException
     */
    public CustomCommand callEndpointBlocking(String endpoint, I input) throws IOException{
        Preconditions.checkNotNull(endpoint, "commandName must not be null");
        CustomCommand command = new CustomCommand(endpoint, input,  currentResponseClass);
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
    public CustomCommandArray callEndpointArrayBlocking(String endpoint, I input) throws IOException{
        Preconditions.checkNotNull(endpoint, "commandName must not be null");
        CustomCommandArray command = new CustomCommandArray(endpoint, input,  Array.newInstance(currentResponseClass, 0).getClass());
        client.initializeRequest(command);
        return command;
    }

    public Class<O> getCurrentResponseClass(){
        return currentResponseClass;
    }

    /**
     * A JSON client request which executes against a custom endpoint returning a single JSON object.
     *
     */
    public class CustomCommand extends AbstractKinveyJsonClientRequest<O> {
        private static final String REST_PATH = "rpc/{appKey}/custom/{endpoint}";

        @Key
        private String endpoint;


        CustomCommand(String commandName, I args, Class<O> responseClass) {
            super(client, "POST", REST_PATH, args, responseClass);
            this.endpoint = commandName;
        }


    }
    /**
     * A JSON client request which executes against a custom endpoint returning an array.
     *
     */
    public class CustomCommandArray extends AbstractKinveyJsonClientRequest<O[]> {
        private static final String REST_PATH = "rpc/{appKey}/custom/{endpoint}";

        @Key
        private String endpoint;


        CustomCommandArray(String commandName, I args, Class responseClass) {
            super(client, "POST", REST_PATH, args, responseClass);
            this.endpoint = commandName;
        }


    }


}

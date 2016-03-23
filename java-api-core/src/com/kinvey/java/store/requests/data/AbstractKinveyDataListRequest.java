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

package com.kinvey.java.store.requests.data;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;

import java.util.List;

/**
 * Created by Prots on 2/8/16.
 */
public abstract class AbstractKinveyDataListRequest<T extends GenericJson> implements IRequest<List<T>> {
    public static class RequestConfig{

    }

    //configuration options for the request.
    //In strong typed languages, this can be a RequestConfig class that allows the developer to specify timeout, custom headers etc.
    protected RequestConfig requestConfig;

    //What collection we are operating on
    protected String collection;

    //The cache that backs the collection
    protected ICache<T> cache;

    //Network manager
    protected NetworkManager networkManager;


}

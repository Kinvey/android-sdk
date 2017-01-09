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

package com.kinvey.java.store.requests.data.read;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.requests.data.IRequest;

import java.io.IOException;
import java.util.List;

/**
 * Created by Prots on 12/30/16.
 */
public abstract class AbstractReduceFunctionRequest<T extends GenericJson> implements IRequest<List<T>> {
    protected final ICache<T> cache;
    private final ReadPolicy readPolicy;
    protected NetworkManager<T> networkManager;

    public AbstractReduceFunctionRequest(ICache<T> cache, ReadPolicy readPolicy, NetworkManager<T> networkManager) {

        this.cache = cache;
        this.readPolicy = readPolicy;
        this.networkManager = networkManager;
    }

    @Override
    public List<T> execute() throws IOException {
        List<T> ret = null;
        switch (readPolicy){
            case FORCE_LOCAL:
                ret = getCached();
                break;
            case FORCE_NETWORK:
            case BOTH:
                ret = getNetwork();
                break;
        }
        return ret;
    }

    @Override
    public void cancel() {

    }

    abstract protected List<T> getCached();
    abstract protected List<T> getNetwork() throws IOException;

}

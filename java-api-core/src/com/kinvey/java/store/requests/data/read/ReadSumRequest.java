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

import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.requests.data.AbstractKinveyDataRequest;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Prots on 12/29/16.
 */
public class ReadSumRequest<T> extends AbstractKinveyDataRequest {
    private final ICache cache;
    private final ReadPolicy readPolicy;
    private NetworkManager networkManager;
    private final Query query;
    private ArrayList<String> fields;
    private final String sumFiled;

    public ReadSumRequest(ICache cache, ReadPolicy readPolicy,
                          NetworkManager networkManager,
                          ArrayList<String> fields, String sumFiled, Query query) {
        this.cache = cache;
        this.readPolicy = readPolicy;
        this.networkManager = networkManager;
        this.fields = fields;
        this.sumFiled = sumFiled;
        this.query = query;
    }


    @Override
    public T execute() throws IOException {
        T ret = null;
        switch (readPolicy){
            case FORCE_LOCAL:
                ret = (T) cache.sum(sumFiled, query);
                break;
            case FORCE_NETWORK: // Logic for getting cached data implemented before running Request
            case BOTH:
                ret = (T) networkManager.sumBlocking(fields, sumFiled, query).execute();
                break;
        }
        return ret;
    }

    @Override
    public void cancel() {

    }
}

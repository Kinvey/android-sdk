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
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.requests.data.AbstractKinveyDataRequest;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Prots on 12/29/16.
 */
public class ReadSumRequest<T extends GenericJson> extends AbstractReduceFunctionRequest<Aggregation.Result> {
    private final Query query;
    private ArrayList<String> fields;
    private final String sumFiled;

    public ReadSumRequest(ICache cache, ReadPolicy readPolicy,
                          NetworkManager networkManager,
                          ArrayList<String> fields, String sumFiled, Query query) {
        super(cache, readPolicy, networkManager);
        this.fields = fields;
        this.sumFiled = sumFiled;
        this.query = query;
    }


    @Override
    public void cancel() {

    }

    @Override
    protected Double getCached() {
        return (Double) cache.sum(sumFiled, query);
    }

    @Override
    protected Aggregation.Result[] getNetwork() throws IOException {
        Aggregation.Result[] t =  networkManager.sumBlocking(fields, sumFiled, Aggregation.Result.class, query).execute();
        System.out.println(Arrays.toString(t));
        return t;
    }
}

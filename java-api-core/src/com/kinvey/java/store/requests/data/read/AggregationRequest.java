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
import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.AggregateEntity;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Prots on 12/29/16.
 */
public class AggregationRequest<T extends GenericJson> extends AbstractReduceFunctionRequest<Aggregation.Result> {
    private final Query query;
    private final AggregateEntity.AggregateType type;
    private ArrayList<String> fields;
    private final String field;

    public AggregationRequest(AggregateEntity.AggregateType type, ICache cache, ReadPolicy readPolicy,
                              NetworkManager networkManager,
                              ArrayList<String> fields, String field, Query query) {
        super(cache, readPolicy, networkManager);
        this.type = type;
        this.fields = fields;
        this.field = field;
        this.query = query;
    }


    @Override
    public void cancel() {

    }

    @Override
    protected List<Aggregation.Result> getCached() {
        switch (type) {
            case SUM:
                return cache.sum(fields, field, query);
            case MIN:
                return cache.min(fields, field, query);
            case MAX:
                return cache.max(fields, field, query);
            case AVERAGE:
                return cache.average(fields, field, query);
            case COUNT:
                return cache.count(fields, query);
            default:
                throw new KinveyException(type.name() + " doesn't supported. Supported types: SUM, MIN, MAX, AVERAGE, COUNT.");
        }

    }

    @Override
    protected List<Aggregation.Result> getNetwork() throws IOException {
        switch (type) {
            case SUM:
                return Arrays.asList(networkManager.sumBlocking(fields, field, Aggregation.Result.class, query).execute());
            case MIN:
                return Arrays.asList(networkManager.minBlocking(fields, field, Aggregation.Result.class, query).execute());
            case MAX:
                return Arrays.asList(networkManager.maxBlocking(fields, field, Aggregation.Result.class, query).execute());
            case AVERAGE:
                return Arrays.asList(networkManager.averageBlocking(fields, field, Aggregation.Result.class, query).execute());
            case COUNT:
                return Arrays.asList(networkManager.countBlocking(fields, Aggregation.Result.class, query).execute());
            default:
                throw new KinveyException(type.name() + " doesn't supported. Supported types: SUM, MIN, MAX, AVERAGE, COUNT.");
        }
    }
}

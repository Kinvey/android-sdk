package com.kinvey.java.store.requests.data;

import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.AggregateEntity;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by yuliya on 10/06/17.
 */

public class CalculationRequest extends AbstractCalculationRequest<Aggregation.Result> {
    private final Query query;
    private final AggregateEntity.AggregateType type;
    private ArrayList<String> fields;
    private final String field;

    public CalculationRequest(AggregateEntity.AggregateType type, ICache cache, ReadPolicy readPolicy,
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
            case COUNT:
                return cache.count(fields, query);
            case SUM:
                return cache.sum(fields, field, query);
            case MIN:
                return cache.min(fields, field, query);
            case MAX:
                return cache.max(fields, field, query);
            case AVERAGE:
                return cache.average(fields, field, query);
            default:
                throw new KinveyException(type.name() + " doesn't supported. Supported types: SUM, MIN, MAX, AVERAGE, COUNT.");
        }
    }

    @Override
    protected List<Aggregation.Result> getNetwork() throws IOException {
        switch (type) {
            case COUNT:
                return Arrays.asList(networkManager.countBlocking(fields, Aggregation.Result.class, query).execute());
            case SUM:
                return Arrays.asList(networkManager.sumBlocking(fields, field, Aggregation.Result.class, query).execute());
            case MIN:
                return Arrays.asList(networkManager.minBlocking(fields, field, Aggregation.Result.class, query).execute());
            case MAX:
                return Arrays.asList(networkManager.maxBlocking(fields, field, Aggregation.Result.class, query).execute());
            case AVERAGE:
                return Arrays.asList(networkManager.averageBlocking(fields, field, Aggregation.Result.class, query).execute());
            default:
                throw new KinveyException(type.name() + " doesn't supported. Supported types: SUM, MIN, MAX, AVERAGE, COUNT.");
        }
    }
}

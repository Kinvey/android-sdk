package com.kinvey.java.store.requests.data;

import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.AggregateType;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by yuliya on 10/06/17.
 */

public class AggregationRequest implements IRequest<Aggregation.Result[]> {
    private final Query query;
    private final AggregateType type;
    private final ICache<Aggregation.Result> cache;
    private ReadPolicy readPolicy;
    private final NetworkManager<Aggregation.Result> networkManager;
    private ArrayList<String> fields;
    private final String reduceField;

    public AggregationRequest(AggregateType type, ICache cache, ReadPolicy readPolicy,
                              NetworkManager networkManager,
                              ArrayList<String> fields, String reduceField, Query query) {
        this.type = type;
        this.cache = cache;
        this.readPolicy = readPolicy;
        this.networkManager = networkManager;
        this.fields = fields;
        this.reduceField = reduceField;
        this.query = query;
    }

    @Override
    public Aggregation.Result[] execute() throws IOException {
        Aggregation.Result[] ret = null;
        switch (readPolicy) {
            case FORCE_LOCAL:
                ret = getCached();
                break;
            case FORCE_NETWORK:
            case BOTH:
                ret = getNetwork();
                break;
            case NETWORK_OTHERWISE_LOCAL:
                try {
                    ret = getNetwork();
                } catch (Exception e) {
                    ret = getCached();
                }
        }
        return ret;
    }

    protected Aggregation.Result[] getCached() {
        return cache.group(type, fields, reduceField, query);
    }

    protected Aggregation.Result[] getNetwork() throws IOException {
        switch (type) {
            case COUNT:
                return networkManager.countBlocking(fields, Aggregation.Result[].class, query).execute();
            case SUM:
                return networkManager.sumBlocking(fields, reduceField, Aggregation.Result[].class, query).execute();
            case MIN:
                return networkManager.minBlocking(fields, reduceField, Aggregation.Result[].class, query).execute();
            case MAX:
                return networkManager.maxBlocking(fields, reduceField, Aggregation.Result[].class, query).execute();
            case AVERAGE:
                return networkManager.averageBlocking(fields, reduceField, Aggregation.Result[].class, query).execute();
            default:
                throw new KinveyException(type.name() + " doesn't supported. Supported types: SUM, MIN, MAX, AVERAGE, COUNT.");
        }
    }

    @Override
    public void cancel() {

    }
}

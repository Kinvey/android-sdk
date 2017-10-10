package com.kinvey.java.store.requests.data;

import com.kinvey.java.KinveyException;
import com.kinvey.java.Query;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.model.AggregateEntity;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.store.ReadPolicy;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by yuliya on 10/06/17.
 */

public class CalculationRequest implements IRequest<Aggregation.Result[]> {
    private final Query query;
    private final AggregateEntity.AggregateType type;
    private final ICache<Aggregation.Result> cache;
    private ReadPolicy readPolicy;
    private final NetworkManager<Aggregation.Result> networkManager;
    private ArrayList<String> fields;
    private final String field;

    public CalculationRequest(AggregateEntity.AggregateType type, ICache cache, ReadPolicy readPolicy,
                              NetworkManager networkManager,
                              ArrayList<String> fields, String field, Query query) {
        this.type = type;
        this.cache = cache;
        this.readPolicy = readPolicy;
        this.networkManager = networkManager;
        this.fields = fields;
        this.field = field;
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
        }
        return ret;
    }

    protected Aggregation.Result[] getCached() {
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

    protected Aggregation.Result[] getNetwork() throws IOException {
        switch (type) {
            case COUNT:
                return networkManager.countBlocking(fields, Aggregation.Result[].class, query).execute();
            case SUM:
                return networkManager.sumBlocking(fields, field, Aggregation.Result[].class, query).execute();
            case MIN:
                return networkManager.minBlocking(fields, field, Aggregation.Result[].class, query).execute();
            case MAX:
                return networkManager.maxBlocking(fields, field, Aggregation.Result[].class, query).execute();
            case AVERAGE:
                return networkManager.averageBlocking(fields, field, Aggregation.Result[].class, query).execute();
            default:
                throw new KinveyException(type.name() + " doesn't supported. Supported types: SUM, MIN, MAX, AVERAGE, COUNT.");
        }
    }

    @Override
    public void cancel() {

    }
}

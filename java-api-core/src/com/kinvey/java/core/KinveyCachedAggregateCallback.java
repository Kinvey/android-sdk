package com.kinvey.java.core;

import com.kinvey.java.model.Aggregation;

import java.util.List;

/**
 * Created by yuliya on 10/06/17.
 */

public abstract class KinveyCachedAggregateCallback implements KinveyClientCallback<List<Aggregation.Result>> {

    @Override
    public void onSuccess(List<Aggregation.Result> result) {
        Aggregation response = new Aggregation(result);
        onSuccess(response);
    }


    public abstract void onFailure(Throwable error);

    public abstract void onSuccess(Aggregation response);

}

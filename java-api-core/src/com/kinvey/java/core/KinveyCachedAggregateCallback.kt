package com.kinvey.java.core

import com.kinvey.java.model.Aggregation

/**
 * Created by yuliya on 10/06/17.
 */

abstract class KinveyCachedAggregateCallback : KinveyClientCallback<List<Aggregation.Result>> {

    override fun onSuccess(result: List<Aggregation.Result>) {
        val response = Aggregation(result)
        onSuccess(response)
    }

    abstract override fun onFailure(error: Throwable)

    abstract fun onSuccess(response: Aggregation)
}

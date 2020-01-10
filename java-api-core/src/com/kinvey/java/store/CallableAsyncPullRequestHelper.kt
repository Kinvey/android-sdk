package com.kinvey.java.store

import com.google.api.client.json.GenericJson
import com.kinvey.java.Query
import com.kinvey.java.network.NetworkManager

import java.util.concurrent.Callable

class CallableAsyncPullRequestHelper<T: GenericJson> internal constructor(private val pullRequest: NetworkManager.Get<T>?,
                                                                          private val query: Query?)
    : Callable<PullTaskResponse<T>> {

    @Throws(Exception::class)
    override fun call(): PullTaskResponse<T> {
        return PullTaskResponse(pullRequest?.execute(), query)
    }
}

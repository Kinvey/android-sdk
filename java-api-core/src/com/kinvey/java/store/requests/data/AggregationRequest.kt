package com.kinvey.java.store.requests.data

import com.kinvey.java.KinveyException
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.AggregateType
import com.kinvey.java.model.Aggregation
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy

import java.io.IOException
import java.util.ArrayList


/**
 * Created by yuliya on 10/06/17.
 */

class AggregationRequest<T: Aggregation.Result>(private val type: AggregateType, cache: ICache<T>?, private val readPolicy: ReadPolicy,
                         networkManager: NetworkManager<T>,
                         private val fields: ArrayList<String>, private val reduceField: String?, private val query: Query) : IRequest<Array<out Aggregation.Result>> {
    private val cache: ICache<out T>?
    private val networkManager: NetworkManager<out Aggregation.Result>

    protected val cached: Array<Aggregation.Result>?
        get() = cache?.group(type, fields, reduceField, query)

    protected val network: Array<out Aggregation.Result>?
        @Throws(IOException::class)
        get() {
            when (type) {
                AggregateType.COUNT -> return networkManager.countBlocking(fields, Array<Aggregation.Result>::class.java, query).execute()
                AggregateType.SUM -> return networkManager.sumBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query).execute()
                AggregateType.MIN -> return networkManager.minBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query).execute()
                AggregateType.MAX -> return networkManager.maxBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query).execute()
                AggregateType.AVERAGE -> return networkManager.averageBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query).execute()
                else -> throw KinveyException(type.name + " doesn't supported. Supported types: SUM, MIN, MAX, AVERAGE, COUNT.")
            }
        }

    init {
        this.cache = cache
        this.networkManager = networkManager
    }

    @Throws(IOException::class)
    override fun execute(): Array<out Aggregation.Result>? {
        var ret: Array<out Aggregation.Result>? = null
        when (readPolicy) {
            ReadPolicy.FORCE_LOCAL -> ret = cached
            ReadPolicy.FORCE_NETWORK, ReadPolicy.BOTH -> ret = network
            ReadPolicy.NETWORK_OTHERWISE_LOCAL -> {
                var networkException: IOException? = null
                try {
                    ret = network
                } catch (e: IOException) {
                    if (NetworkManager.checkNetworkRuntimeExceptions(e)) {
                        throw e
                    }
                    networkException = e
                }

                // if the network request fails, fetch data from local cache
                if (networkException != null) {
                    ret = cached
                }
            }
        }
        return ret
    }

    override fun cancel() {

    }
}

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

class AggregationRequest(private val type: AggregateType, private val cache: ICache<Aggregation.Result>?,
                         private val readPolicy: ReadPolicy?, private val networkManager: NetworkManager<Aggregation.Result>?,
                         private val fields: ArrayList<String>, private val reduceField: String?, private val query: Query) : IRequest<Array<Aggregation.Result>> {

    protected val cached: Array<Aggregation.Result>?
        get() = cache?.group(type, fields, reduceField ?: "", query)

    protected val network: Array<Aggregation.Result>?
        @Throws(IOException::class)
        get() {
            return when (type) {
                AggregateType.COUNT -> networkManager?.countBlocking(fields, Array<Aggregation.Result>::class.java, query)?.execute()
                AggregateType.SUM -> networkManager?.sumBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query)?.execute()
                AggregateType.MIN -> networkManager?.minBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query)?.execute()
                AggregateType.MAX -> networkManager?.maxBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query)?.execute()
                AggregateType.AVERAGE -> networkManager?.averageBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query)?.execute()
                else -> throw KinveyException(type.name + " doesn't supported. Supported types: SUM, MIN, MAX, AVERAGE, COUNT.")
            }
        }

    @Throws(IOException::class)
    override fun execute(): Array<Aggregation.Result> {
        var ret: Array<Aggregation.Result> = arrayOf()
        when (readPolicy) {
            ReadPolicy.FORCE_LOCAL -> ret = cached ?: arrayOf()
            ReadPolicy.FORCE_NETWORK, ReadPolicy.BOTH -> ret = network ?: arrayOf()
            ReadPolicy.NETWORK_OTHERWISE_LOCAL -> {
                var networkException: IOException? = null
                try {
                    ret = network ?: arrayOf()
                } catch (e: IOException) {
                    if (NetworkManager.checkNetworkRuntimeExceptions(e)) {
                        throw e
                    }
                    networkException = e
                }
                // if the network request fails, fetch data from local cache
                if (networkException != null) {
                    ret = cached ?: arrayOf()
                }
            }
        }
        return ret
    }

    override fun cancel() {}
}

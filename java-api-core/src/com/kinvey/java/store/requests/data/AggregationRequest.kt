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

open class AggregationRequest(private val type: AggregateType, private val cache: ICache<Aggregation.Result>?,
                         private val readPolicy: ReadPolicy?, private val networkManager: NetworkManager<Aggregation.Result>?,
                         private val fields: ArrayList<String>, private val reduceField: String?, private val query: Query) : IRequest<Array<Aggregation.Result>> {

    protected open val cached: Array<Aggregation.Result>?
        get() = groupCache()

    protected open val network: Array<Aggregation.Result>?
        @Throws(IOException::class)
        get() {
            return when (type) {
                AggregateType.COUNT -> countBlocking()
                AggregateType.SUM -> sumBlocking()
                AggregateType.MIN -> minBlocking()
                AggregateType.MAX -> maxBlocking()
                AggregateType.AVERAGE -> averageBlocking()
                else -> throw KinveyException(type.name + " doesn't supported. Supported types: SUM, MIN, MAX, AVERAGE, COUNT.")
            }
        }

    protected open fun groupCache(): Array<Aggregation.Result>? {
        return cache?.group(type, fields, reduceField ?: "", query)
    }

    protected open fun countBlocking(): Array<Aggregation.Result>? {
        return networkManager?.countBlocking(fields, Array<Aggregation.Result>::class.java, query)?.execute()
    }

    protected open fun sumBlocking(): Array<Aggregation.Result>? {
        return networkManager?.sumBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query)?.execute()
    }

    protected open fun minBlocking(): Array<Aggregation.Result>? {
        return networkManager?.minBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query)?.execute()
    }

    protected open fun maxBlocking(): Array<Aggregation.Result>? {
        return networkManager?.maxBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query)?.execute()
    }

    protected open fun averageBlocking(): Array<Aggregation.Result>? {
        return networkManager?.averageBlocking(fields, reduceField, Array<Aggregation.Result>::class.java, query)?.execute()
    }

    @Throws(IOException::class)
    override fun execute(): Array<Aggregation.Result> {
        var ret: Array<Aggregation.Result> = arrayOf()
        when (readPolicy) {
            ReadPolicy.FORCE_LOCAL -> ret = cached ?: arrayOf()
            ReadPolicy.FORCE_NETWORK, ReadPolicy.BOTH -> ret = network ?: arrayOf()
            ReadPolicy.NETWORK_OTHERWISE_LOCAL -> {
                ret = runNetworkOrLocal()
            }
        }
        return ret
    }

    @Throws(IOException::class)
    protected open fun runNetworkOrLocal(): Array<Aggregation.Result> {
        var networkException: IOException? = null
        var ret: Array<Aggregation.Result> = arrayOf()
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
        return ret
    }

    override fun cancel() {}
}

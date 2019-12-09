package com.kinvey.java.store.request

import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.AggregateType
import com.kinvey.java.model.Aggregation
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.requests.data.AggregationRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import java.lang.Exception
import java.net.UnknownHostException

class AggregationRequestTest : TestCase() {

    var spyNetworkManager: NetworkManager<Aggregation.Result>? = null
    var client: AbstractClient<*>? = null
    var cache: ICache<Aggregation.Result>? = null

    @Before
    override fun setUp() {
        client = Mockito.mock(AbstractClient::class.java)
        spyNetworkManager = Mockito.mock(NetworkManager::class.java) as NetworkManager<Aggregation.Result>?
        cache = Mockito.mock(ICache::class.java) as ICache<Aggregation.Result>
    }

    fun testForceLocal() {
        val aggregation = AggregateType.COUNT
        val fields = arrayListOf("username")
        val query = Query()
        val request = Mockito.spy(AggregationRequest(aggregation, cache, ReadPolicy.FORCE_LOCAL,
                spyNetworkManager, fields, null, query))
        PowerMockito.doReturn(arrayOf(Aggregation.Result())).`when`(request, "getCached")
        request.execute()
        PowerMockito.verifyPrivate(request, Mockito.times(1)).invoke("getCached")
    }

    fun testForceNetwork() {
        val aggregation = AggregateType.COUNT
        val fields = arrayListOf("username")
        val query = Query()
        val request = Mockito.spy(AggregationRequest(aggregation, cache, ReadPolicy.FORCE_NETWORK,
                spyNetworkManager, fields, null, query))
        PowerMockito.doReturn(arrayOf(Aggregation.Result())).`when`(request, "getNetwork")
        request.execute()
        PowerMockito.verifyPrivate(request, Mockito.times(1)).invoke("getNetwork")
    }

    fun testNetworkOtherwiseLocal() {
        val aggregation = AggregateType.COUNT
        val fields = arrayListOf("username")
        val query = Query()
        val request = Mockito.spy(AggregationRequest(aggregation, cache, ReadPolicy.NETWORK_OTHERWISE_LOCAL,
                spyNetworkManager, fields, null, query))
        PowerMockito.doReturn(arrayOf(Aggregation.Result())).`when`(request, "getNetwork")
        request.execute()
        PowerMockito.verifyPrivate(request, Mockito.times(1)).invoke("getNetwork")
    }

    fun testNetworkOtherwiseLocal_Error() {
        val aggregation = AggregateType.COUNT
        val fields = arrayListOf("username")
        val query = Query()
        val request = Mockito.spy(AggregationRequest(aggregation, cache, ReadPolicy.NETWORK_OTHERWISE_LOCAL,
                spyNetworkManager, fields, null, query))
        PowerMockito.doThrow(UnknownHostException("test exception")).`when`(request, "getNetwork")
        PowerMockito.doReturn(arrayOf(Aggregation.Result())).`when`(request, "getCached")
        try { request.execute() } catch (e: Exception) {}
        PowerMockito.verifyPrivate(request, Mockito.times(1)).invoke("getNetwork")
        PowerMockito.verifyPrivate(request, Mockito.times(1)).invoke("getCached")
    }
}
package com.kinvey.java.store.request.read

import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.query.MongoQueryFilter
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.request.Person
import com.kinvey.java.store.requests.data.read.ReadQueryRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import java.net.UnknownHostException

class ReadQueryRequestTest : TestCase() {

    var spyNetworkManager: NetworkManager<Person>? = null
    var client: AbstractClient<*>? = null
    var cache: ICache<Person>? = null

    @Before
    override fun setUp() {
        client = mock(AbstractClient::class.java)
        spyNetworkManager = mock(NetworkManager::class.java) as NetworkManager<Person>?
        cache = mock(ICache::class.java) as ICache<Person>
    }

    fun testForceLocal() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(ReadQueryRequest(cache, spyNetworkManager, ReadPolicy.FORCE_LOCAL, query))
        request.execute()
        verify(cache, times(1))?.get(any(Query::class.java))
    }

    fun testForceNetwork() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(ReadQueryRequest(cache, spyNetworkManager, ReadPolicy.FORCE_NETWORK, query))
        request.execute()
        verify(spyNetworkManager, times(1))?.getBlocking(any(Query::class.java))?.execute()
    }

    fun  testNetworkOtherwiseLocal() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(ReadQueryRequest(cache, spyNetworkManager, ReadPolicy.NETWORK_OTHERWISE_LOCAL, query))
        PowerMockito.doReturn(KinveyReadResponse("", listOf(Person()))).`when`(request, "getNetwork")
        request.execute()
        PowerMockito.verifyPrivate(request, times(1))?.invoke("getNetwork")
        verify(cache, times(1))?.save(anyList())
    }

    fun  testNetworkOtherwiseLocalError() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(ReadQueryRequest(cache, spyNetworkManager, ReadPolicy.NETWORK_OTHERWISE_LOCAL, query))
        PowerMockito.doThrow(UnknownHostException("test exception")).`when`(request, "getNetwork")
        try { request.execute() } catch (e: Exception) {}
        PowerMockito.verifyPrivate(request, times(1))?.invoke("getNetwork")
        verify(cache, times(1))?.get(any(Query::class.java))
    }
}
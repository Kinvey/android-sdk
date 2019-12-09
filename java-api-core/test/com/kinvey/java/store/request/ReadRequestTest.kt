package com.kinvey.java.store.request

import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.requests.data.ReadRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import java.net.SocketTimeoutException

class ReadRequestTest : TestCase() {
    val id = "testId"
    val query = Query()
    val maxValue = 10L
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
        val request = spy(ReadRequest(cache, query, ReadPolicy.FORCE_LOCAL, maxValue, spyNetworkManager))
        request.execute()
        verify(cache, times(1))?.get(any(Query::class.java))
    }

    fun testForceNetwork() {
        val request = spy(ReadRequest(cache, query, ReadPolicy.FORCE_NETWORK, maxValue, spyNetworkManager))
        PowerMockito.doReturn(null).`when`(request, "readItem", any(Query::class.java))
        request.execute()
        PowerMockito.verifyPrivate(request).invoke("readItem", any(Query::class.java))
    }

    fun testNetworkOtherwiseLocal() {
        val request = spy(ReadRequest(cache, query, ReadPolicy.NETWORK_OTHERWISE_LOCAL, maxValue, spyNetworkManager))
        PowerMockito.doReturn(null).`when`(request, "readItem", any(Query::class.java))
        request.execute()
        PowerMockito.verifyPrivate(request).invoke("readItem", any(Query::class.java))
    }

    fun testNetworkOtherwiseLocalError() {
        val request = spy(ReadRequest(cache, query, ReadPolicy.NETWORK_OTHERWISE_LOCAL, maxValue, spyNetworkManager))
        PowerMockito.doThrow(SocketTimeoutException("")).`when`(request, "readItem", any(Query::class.java))
        try { request.execute() } catch (e: Exception) {}
        PowerMockito.verifyPrivate(request).invoke("readItem", any(Query::class.java))
        verify(cache, times(1))?.get(any(Query::class.java))
    }
}
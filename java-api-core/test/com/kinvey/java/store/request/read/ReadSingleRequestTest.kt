package com.kinvey.java.store.request.read

import com.kinvey.java.AbstractClient
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.request.Person
import com.kinvey.java.store.requests.data.read.ReadSingleRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import java.net.UnknownHostException

class ReadSingleRequestTest : TestCase() {

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
        val id = "testId"
        val request = spy(ReadSingleRequest(cache, id, ReadPolicy.FORCE_LOCAL, spyNetworkManager))
        request.execute()
        verify(cache, Mockito.times(1))?.get(anyString())
    }

    fun testForceNetwork() {
        val id = "testId"
        val request = spy(ReadSingleRequest(cache, id, ReadPolicy.FORCE_NETWORK, spyNetworkManager))
        request.execute()
        verify(spyNetworkManager, times(1))?.getEntityBlocking(anyString())?.execute()
    }

    fun testNetworkOtherwiseLocal() {
        val id = "testId"
        val request = spy(ReadSingleRequest(cache, id, ReadPolicy.NETWORK_OTHERWISE_LOCAL, spyNetworkManager))
        PowerMockito.doReturn(Person()).`when`(request, "getNetwork")
        request.execute()
        PowerMockito.verifyPrivate(request, times(1))?.invoke("getNetwork")
        verify(cache, times(1))?.save(any(Person::class.java))
    }

    fun testNetworkOtherwiseLocalError() {
        val id = "testId"
        val request = spy(ReadSingleRequest(cache, id, ReadPolicy.NETWORK_OTHERWISE_LOCAL, spyNetworkManager))
        PowerMockito.doThrow(UnknownHostException("test exception")).`when`(request, "getNetwork")
        try { request.execute() } catch (e: Exception) {}
        PowerMockito.verifyPrivate(request, times(1))?.invoke("getNetwork")
        verify(cache, times(1))?.get(anyString())
    }
}
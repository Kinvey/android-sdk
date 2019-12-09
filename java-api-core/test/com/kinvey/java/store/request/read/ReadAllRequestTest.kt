package com.kinvey.java.store.request.read

import com.kinvey.java.AbstractClient
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.request.Person
import com.kinvey.java.store.requests.data.read.ReadAllRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import java.net.UnknownHostException

class ReadAllRequestTest : TestCase() {

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
        val request = spy(ReadAllRequest(cache, ReadPolicy.FORCE_LOCAL, spyNetworkManager))
        request.execute()
        verify(cache, times(1))?.get()
    }

    fun testForceNetwork() {
        val request = spy(ReadAllRequest(cache, ReadPolicy.FORCE_NETWORK, spyNetworkManager))
        request.execute()
        verify(spyNetworkManager, times(1))?.getBlocking()?.execute()
    }

    fun  testNetworkOtherwiseLocal() {
        val request = spy(ReadAllRequest(cache, ReadPolicy.NETWORK_OTHERWISE_LOCAL, spyNetworkManager))
        PowerMockito.doReturn(KinveyReadResponse("", listOf(Person()))).`when`(request, "getNetwork")
        request.execute()
        PowerMockito.verifyPrivate(request, times(1))?.invoke("getNetwork")
        verify(cache, times(1))?.save(anyList())
    }

    fun  testNetworkOtherwiseLocalError() {
        val request = spy(ReadAllRequest(cache, ReadPolicy.NETWORK_OTHERWISE_LOCAL, spyNetworkManager))
        PowerMockito.doThrow(UnknownHostException("test exception")).`when`(request, "getNetwork")
        try { request.execute() } catch (e: Exception) {}
        PowerMockito.verifyPrivate(request, times(1))?.invoke("getNetwork")
        verify(cache, times(1))?.get()
    }
}
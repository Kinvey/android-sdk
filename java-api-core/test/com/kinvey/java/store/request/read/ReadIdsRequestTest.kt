package com.kinvey.java.store.request.read

import com.kinvey.java.AbstractClient
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.request.Person
import com.kinvey.java.store.requests.data.read.ReadIdsRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import java.net.UnknownHostException

class ReadIdsRequestTest : TestCase() {

    var spyNetworkManager: NetworkManager<Person>? = null
    var client: AbstractClient<*>? = null
    var cache: ICache<Person>? = null

    @Before
    override fun setUp() {
        client = Mockito.mock(AbstractClient::class.java)
        spyNetworkManager = Mockito.mock(NetworkManager::class.java) as NetworkManager<Person>?
        cache = Mockito.mock(ICache::class.java) as ICache<Person>
    }

    fun testForceLocal() {
        val ids = listOf("id1", "id2")
        val request = Mockito.spy(ReadIdsRequest(cache, spyNetworkManager, ReadPolicy.FORCE_LOCAL, ids))
        request.execute()
        Mockito.verify(cache, Mockito.times(1))?.get(anyIterable())
    }

    fun testForceNetwork() {
        val ids = listOf("id1", "id2")
        val request = Mockito.spy(ReadIdsRequest(cache, spyNetworkManager, ReadPolicy.FORCE_NETWORK, ids))
        request.execute()
        Mockito.verify(spyNetworkManager, Mockito.times(1))?.getBlocking(any() as Array<String>?)?.execute()
    }

    fun  testNetworkOtherwiseLocal() {
        val ids = listOf("id1", "id2")
        val request = Mockito.spy(ReadIdsRequest(cache, spyNetworkManager, ReadPolicy.NETWORK_OTHERWISE_LOCAL, ids))
        PowerMockito.doReturn(KinveyReadResponse("", listOf(Person()))).`when`(request, "getNetwork")
        request.execute()
        PowerMockito.verifyPrivate(request, Mockito.times(1))?.invoke("getNetwork")
        Mockito.verify(cache, Mockito.times(1))?.save(Mockito.anyList())
    }

    fun  testNetworkOtherwiseLocalError() {
        val ids = listOf("id1", "id2")
        val request = Mockito.spy(ReadIdsRequest(cache, spyNetworkManager, ReadPolicy.NETWORK_OTHERWISE_LOCAL, ids))
        PowerMockito.doThrow(UnknownHostException("test exception")).`when`(request, "getNetwork")
        try { request.execute() } catch (e: Exception) {}
        PowerMockito.verifyPrivate(request, Mockito.times(1))?.invoke("getNetwork")
        Mockito.verify(cache, Mockito.times(1))?.get(anyIterable())
    }
}
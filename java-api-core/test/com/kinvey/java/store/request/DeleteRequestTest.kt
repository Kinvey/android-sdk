package com.kinvey.java.store.request

import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.requests.data.DeleteRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.Mockito.*

class DeleteRequestTest : TestCase() {
    val id = "testId"
    val query = Query()
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
        val request = spy(DeleteRequest(cache, id, WritePolicy.FORCE_LOCAL, spyNetworkManager, query))
        request.execute()
        verify(cache, times(1))?.delete(any(Query::class.java))
    }

    fun testForceNetwork() {
        val request = spy(DeleteRequest(cache, id, WritePolicy.FORCE_NETWORK, spyNetworkManager, query))
        request.execute()
        verify(spyNetworkManager, times(1))?.deleteBlocking(any(Query::class.java))
    }

    fun testNetworkOtherwiseLocal() {
        val request = spy(DeleteRequest(cache, id, WritePolicy.LOCAL_THEN_NETWORK, spyNetworkManager, query))
        request.execute()
        verify(cache, times(1))?.delete(any(Query::class.java))
        verify(spyNetworkManager, times(1))?.deleteBlocking(any(Query::class.java))
    }
}

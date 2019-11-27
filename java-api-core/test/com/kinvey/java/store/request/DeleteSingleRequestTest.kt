package com.kinvey.java.store.request

import com.kinvey.java.AbstractClient
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.requests.data.delete.DeleteSingleRequest
import com.kinvey.java.sync.SyncManager
import junit.framework.TestCase
import org.junit.Before
import org.mockito.Mockito.*
import java.io.IOException

class DeleteSingleRequestTest : TestCase() {

    var spyNetworkManager: NetworkManager<Person>? = null
    var syncManager: SyncManager? = null
    var client: AbstractClient<*>? = null
    var cache: ICache<Person>? = null

    @Before
    override fun setUp() {
        client = mock(AbstractClient::class.java)
        spyNetworkManager = mock(NetworkManager::class.java) as NetworkManager<Person>?
        syncManager = mock(SyncManager::class.java)
        cache = mock(ICache::class.java) as ICache<Person>
    }

    fun testForceNetwork() {
        val id = "testId"
        val request = spy(DeleteSingleRequest(cache, spyNetworkManager, WritePolicy.FORCE_NETWORK, id, syncManager))
        request.execute()
        verify(request, times(1)).deleteNetwork()
        verify(request, times(0)).deleteCached()
    }

    fun testLocalThenNetwork() {
        val id = "testId"
        val request = spy(DeleteSingleRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, id, syncManager))
        request.execute()
        verify(request, times(1)).deleteNetwork()
        verify(request, times(1)).deleteCached()
    }

    fun testForceLocal() {
        val id = "testId"
        val request = spy(DeleteSingleRequest(cache, spyNetworkManager, WritePolicy.FORCE_LOCAL, id, syncManager))
        request.execute()
        verify(request, times(1)).enqueueRequest(any(), any())
        verify(request, times(1)).deleteCached()
    }

    fun testLocalThenNetwork_EnqueueWhenError() {
        val id = "testId"
        val request = spy(DeleteSingleRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, id, syncManager))
        doThrow(IOException("throw on deleteNetwork")).`when`(request).deleteNetwork()
        try { request.execute() }
        catch (e: IOException) { print(e) }
        verify(request, times(1)).deleteNetwork()
        verify(request, times(1)).enqueueRequest(any(), any())
    }
}
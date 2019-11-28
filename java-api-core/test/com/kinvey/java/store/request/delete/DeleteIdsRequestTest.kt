package com.kinvey.java.store.request.delete

import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.request.Person
import com.kinvey.java.store.requests.data.delete.DeleteIdsRequest
import com.kinvey.java.sync.SyncManager
import junit.framework.TestCase
import org.junit.Before
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyCollection
import org.mockito.Mockito.*
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito

import java.io.IOException

class DeleteIdsRequestTest : TestCase() {

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
        val ids = listOf("id1", "id2")
        val request = spy(DeleteIdsRequest(cache, spyNetworkManager, WritePolicy.FORCE_NETWORK, ids, syncManager))
        request.execute()
        verify(spyNetworkManager, times(1))?.deleteBlocking(any(Query::class.java))
    }

    fun testLocalThenNetwork() {
        val ids = listOf("id1", "id2")
        val request = spy(DeleteIdsRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, ids, syncManager))
        request.execute()
        verify(spyNetworkManager, times(1))?.deleteBlocking(any(Query::class.java))
        verify(cache, times(1))?.delete(ids)
    }

    fun testForceLocal() {
        val ids = listOf("id1", "id2")
        val request = spy(DeleteIdsRequest(cache, spyNetworkManager, WritePolicy.FORCE_LOCAL, ids, syncManager))
        doNothing().`when`(syncManager)?.enqueueDeleteRequests(any(), any(NetworkManager::class.java), anyCollection())
        request.execute()
        verify(syncManager, times(1))?.enqueueDeleteRequests(any(), any(NetworkManager::class.java), anyCollection())
        verify(cache, times(1))?.delete(ids)
    }

    fun testLocalThenNetwork_EnqueueWhenError() {
        val ids = listOf("id1", "id2")
        val request = spy(DeleteIdsRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, ids, syncManager))
        PowerMockito.doThrow(IOException()).`when`(request, "deleteNetwork")
        doNothing().`when`(syncManager)?.enqueueDeleteRequests(any(), any(NetworkManager::class.java), anyCollection())
        try { request.execute() }
        catch (e: IOException) { print(e) }
        verify(syncManager, times(1))?.enqueueDeleteRequests(any(), any(NetworkManager::class.java), anyCollection())
    }
}
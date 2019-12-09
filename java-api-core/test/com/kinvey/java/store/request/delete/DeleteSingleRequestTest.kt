package com.kinvey.java.store.request.delete

import com.kinvey.java.AbstractClient
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.request.Person
import com.kinvey.java.store.requests.data.delete.DeleteSingleRequest
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
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
        verify(spyNetworkManager, times(1))?.deleteBlocking(any(String::class.java))
    }

    fun testLocalThenNetwork() {
        val id = "testId"
        val request = spy(DeleteSingleRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, id, syncManager))
        request.execute()
        verify(spyNetworkManager, times(1))?.deleteBlocking(any(String::class.java))
        verify(cache, times(1))?.delete(id)
    }

    fun testForceLocal() {
        val id = "testId"
        val request = spy(DeleteSingleRequest(cache, spyNetworkManager, WritePolicy.FORCE_LOCAL, id, syncManager))
        doNothing().`when`(syncManager)?.enqueueRequest(any(), any(NetworkManager::class.java), any(SyncRequest.HttpVerb::class.java), anyString())
        request.execute()
        verify(syncManager, times(1))?.enqueueRequest(any(), any(NetworkManager::class.java), any(SyncRequest.HttpVerb::class.java), anyString())
        verify(cache, times(1))?.delete(id)
    }

    fun testLocalThenNetwork_EnqueueWhenError() {
        val id = "testId"
        val request = spy(DeleteSingleRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, id, syncManager))
        PowerMockito.doThrow(IOException()).`when`(request, "deleteNetwork")
        doNothing().`when`(syncManager)?.enqueueRequest(any(), any(NetworkManager::class.java),
                any(SyncRequest.HttpVerb::class.java), anyString())
        try { request.execute() }
        catch (e: IOException) { print(e) }
        verify(syncManager, times(1))?.enqueueRequest(any(),
                any(NetworkManager::class.java), any(SyncRequest.HttpVerb::class.java), anyString())
    }
}
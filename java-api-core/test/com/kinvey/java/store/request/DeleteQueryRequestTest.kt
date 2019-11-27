package com.kinvey.java.store.request

import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.query.MongoQueryFilter
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.requests.data.delete.DeleteQueryRequest
import com.kinvey.java.sync.SyncManager
import junit.framework.TestCase
import org.junit.Before
import org.mockito.Mockito.*
import java.io.IOException

class DeleteQueryRequestTest : TestCase() {

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
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(DeleteQueryRequest(cache, spyNetworkManager, WritePolicy.FORCE_NETWORK, query, syncManager))
        request.execute()
        verify(request, times(1)).deleteNetwork()
        verify(request, times(0)).deleteCached()
    }

    fun testLocalThenNetwork() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(DeleteQueryRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, query, syncManager))
        request.execute()
        verify(request, times(1)).deleteNetwork()
        verify(request, times(1)).deleteCached()
    }

    fun testForceLocal() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(DeleteQueryRequest(cache, spyNetworkManager, WritePolicy.FORCE_LOCAL, query, syncManager))
        request.execute()
        verify(request, times(1)).enqueueRequest(any(), any())
        verify(request, times(1)).deleteCached()
    }

    fun testLocalThenNetwork_EnqueueWhenError() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(DeleteQueryRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, query, syncManager))
        doThrow(IOException("throw on deleteNetwork")).`when`(request).deleteNetwork()
        try { request.execute() }
        catch (e: IOException) { print(e) }
        verify(request, times(1)).deleteNetwork()
        verify(request, times(1)).enqueueRequest(any(), any())
    }
}
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
        verify(spyNetworkManager, times(1))?.deleteBlocking(any(Query::class.java))
        verify(request, times(1)).deleteNetwork()
    }

    fun testLocalThenNetwork() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(DeleteQueryRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, query, syncManager))
        request.execute()
        verify(spyNetworkManager, times(1))?.deleteBlocking(any(Query::class.java))
        verify(cache, times(1))?.delete(query)
        verify(request, times(1)).deleteNetwork()
        verify(request, times(1)).deleteCached()
    }

    fun testForceLocal() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(DeleteQueryRequest(cache, spyNetworkManager, WritePolicy.FORCE_LOCAL, query, syncManager))
        doReturn(Person.COLLECTION).`when`(spyNetworkManager)?.collectionName
        doReturn(listOf(Person())).`when`(cache)?.get(query)
        doNothing().`when`(syncManager)?.enqueueDeleteRequests(Person.COLLECTION, spyNetworkManager, listOf(Person()))
        request.execute()
        verify(syncManager, times(1))
                ?.enqueueDeleteRequests(anyString(), any(NetworkManager::class.java) as NetworkManager<Person>?, anyList<Person>())
        verify(cache, times(1))?.delete(query)
        verify(request, times(1)).enqueueRequest(any(), any())
        verify(request, times(1)).deleteCached()
    }

    fun testLocalThenNetwork_EnqueueWhenError() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(DeleteQueryRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, query, syncManager))
        doThrow(IOException("throw on deleteNetwork")).`when`(request).deleteNetwork()
        doReturn(Person.COLLECTION).`when`(spyNetworkManager)?.collectionName
        doReturn(listOf(Person())).`when`(cache)?.get(query)
        doNothing().`when`(syncManager)?.enqueueDeleteRequests(any(), any(NetworkManager::class.java), anyCollection())
        try { request.execute() }
        catch (e: IOException) { print(e) }
        verify(syncManager, times(1))
              ?.enqueueDeleteRequests(anyString(), any(NetworkManager::class.java) as NetworkManager<Person>?, anyList<Person>())
        verify(request, times(1)).deleteNetwork()
        verify(request, times(1)).enqueueRequest(any(), any())
    }
}
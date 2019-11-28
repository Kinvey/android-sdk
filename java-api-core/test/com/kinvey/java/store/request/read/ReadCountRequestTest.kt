package com.kinvey.java.store.request.read

import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.KinveyCountResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.query.MongoQueryFilter
import com.kinvey.java.store.ReadPolicy
import com.kinvey.java.store.request.Person
import com.kinvey.java.store.requests.data.read.ReadCountRequest
import com.kinvey.java.sync.SyncManager
import junit.framework.TestCase
import org.junit.Before
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import java.net.UnknownHostException

class ReadCountRequestTest : TestCase() {

    var spyNetworkManager: NetworkManager<Person>? = null
    var client: AbstractClient<*>? = null
    var cache: ICache<Person>? = null
    var syncManager: SyncManager? = null

    @Before
    override fun setUp() {
        client = mock(AbstractClient::class.java)
        spyNetworkManager = mock(NetworkManager::class.java) as NetworkManager<Person>?
        cache = mock(ICache::class.java) as ICache<Person>
        syncManager = mock(SyncManager::class.java)
    }

    fun testForceLocal() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(ReadCountRequest(cache, spyNetworkManager, ReadPolicy.FORCE_LOCAL, query, syncManager))
        PowerMockito.doReturn(100).`when`(request, "countCached")
        request.execute()
        PowerMockito.verifyPrivate(request, times(1))?.invoke("countCached")
    }

    fun testForceNetwork() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(ReadCountRequest(cache, spyNetworkManager, ReadPolicy.FORCE_NETWORK, query, syncManager))
        PowerMockito.doReturn(KinveyCountResponse(100)).`when`(request, "countNetwork")
        request.execute()
        PowerMockito.verifyPrivate(request, times(1))?.invoke("countNetwork")
    }

    fun  testNetworkOtherwiseLocal() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(ReadCountRequest(cache, spyNetworkManager, ReadPolicy.NETWORK_OTHERWISE_LOCAL, query, syncManager))
        PowerMockito.doReturn(KinveyCountResponse(100)).`when`(request, "countNetwork")
        PowerMockito.doReturn(100).`when`(request, "countCached")
        request.execute()
        PowerMockito.verifyPrivate(request, times(1))?.invoke("countNetwork")
        PowerMockito.verifyPrivate(request, times(0))?.invoke("countCached")
    }

    fun  testNetworkOtherwiseLocalError() {
        val query = Query(MongoQueryFilter.MongoQueryFilterBuilder())
        val request = spy(ReadCountRequest(cache, spyNetworkManager, ReadPolicy.NETWORK_OTHERWISE_LOCAL, query, syncManager))
        PowerMockito.doReturn(KinveyCountResponse(100)).`when`(request, "countNetwork")
        PowerMockito.doReturn(100).`when`(request, "countCached")
        PowerMockito.doThrow(UnknownHostException("test exception")).`when`(request, "countNetwork")
        request.execute()
        PowerMockito.verifyPrivate(request, times(1))?.invoke("countNetwork")
        PowerMockito.verifyPrivate(request, times(1))?.invoke("countCached")
    }
}
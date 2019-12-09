package com.kinvey.java.store.request.save

import com.kinvey.java.AbstractClient
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.request.Person
import com.kinvey.java.store.requests.data.save.SaveListRequest
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import java.io.IOException

class SaveListRequestTest : TestCase() {

    lateinit var spyNetworkManager: NetworkManager<Person>
    lateinit var syncManager: SyncManager
    lateinit var client: AbstractClient<*>
    lateinit var cache: ICache<Person>

    @Before
    override fun setUp() {
        client = mock(AbstractClient::class.java)
        spyNetworkManager = mock(NetworkManager::class.java) as NetworkManager<Person>
        syncManager = mock(SyncManager::class.java)
        cache = mock(ICache::class.java) as ICache<Person>
    }

    fun testForceLocal() {
        val ids = (1..5).map { Person("user $it") }
        val request =
        spy(SaveListRequest(cache, spyNetworkManager, WritePolicy.FORCE_LOCAL, ids, syncManager))
        request.execute()
        verify(cache, times(1))?.save(anyList())
        verify(syncManager, times(1))?.enqueueSaveRequests(anyString(), any(NetworkManager::class.java), anyList())
    }

    fun testForceNetwork() {
        val ids = (1..5).map { Person(username = "user $it") }
        val request =
                spy(SaveListRequest(cache, spyNetworkManager, WritePolicy.FORCE_NETWORK, ids, syncManager))
        PowerMockito.doThrow(IOException("")).`when`(spyNetworkManager, "saveBlocking", any())
        try { request.execute() } catch (e: IOException) { }
        verify(spyNetworkManager, times(5)).saveBlocking(any())
    }

    fun testNetworkOtherwiseLocal() {
        val ids = (1..5).map { Person(username = "user $it") }
        val request =
                spy(SaveListRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, ids, syncManager))
        request.execute()
        verify(cache, times(2)).save(anyList())
        verify(spyNetworkManager, times(5)).saveBlocking(any())
    }

    fun testNetworkOtherwiseLocalError() {
        val ids = (1..5).map { Person(username = "user $it") }
        val request =
                spy(SaveListRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, ids, syncManager))
        PowerMockito.doThrow(IOException("")).`when`(spyNetworkManager, "saveBlocking", any())
        try { request.execute() } catch (e: IOException) { }
        verify(cache, times(2)).save(anyList())
        verify(spyNetworkManager, times(5)).saveBlocking(any())
        verify(syncManager, times(5))
                ?.enqueueRequest(any(), any(NetworkManager::class.java),
                        any(SyncRequest.HttpVerb::class.java), any())
    }
}
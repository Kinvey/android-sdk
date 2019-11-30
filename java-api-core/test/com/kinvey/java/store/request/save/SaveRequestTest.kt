package com.kinvey.java.store.request.save

import com.kinvey.java.AbstractClient
import com.kinvey.java.cache.ICache
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.request.Person
import com.kinvey.java.store.requests.data.save.SaveRequest
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import java.io.IOException

class SaveRequestTest : TestCase() {

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
        val item = Person(username = "user")
        val request =
        spy(SaveRequest(cache, spyNetworkManager, WritePolicy.FORCE_LOCAL, item, syncManager))
        request.execute()
        verify(cache, times(1))?.save(any(Person::class.java))
        verify(syncManager, times(1))
                ?.enqueueRequest(any(), any(NetworkManager::class.java), any(SyncRequest.HttpVerb::class.java), any())
    }

    fun testForceNetwork() {
        val item = Person(username = "user")
        val request =
        spy(SaveRequest(cache, spyNetworkManager, WritePolicy.FORCE_NETWORK, item, syncManager))
        PowerMockito.doThrow(IOException("")).`when`(spyNetworkManager, "saveBlocking", any())
        try { request.execute() } catch (e: IOException) { }
        verify(spyNetworkManager, times(1)).saveBlocking(any())
    }

    fun testNetworkOtherwiseLocal() {
        val item = Person(username = "user")
        val request =
        spy(SaveRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, item, syncManager))
        PowerMockito.doReturn(Person()).`when`(request, "saveItem", any(Person::class.java))
        request.execute()
        verify(cache, times(2))?.save(any(Person::class.java))
        PowerMockito.verifyPrivate(request, times(1))
                .invoke("saveItem", any(Person::class.java))
    }

    fun testNetworkOtherwiseLocalError() {
        val item = Person(username = "user")
        val request =
                spy(SaveRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, item, syncManager))
        PowerMockito.doThrow(IOException()).`when`(request, "saveItem", any(Person::class.java))
        try { request.execute() } catch(e: IOException) {}
        verify(cache, times(1))?.save(any(Person::class.java))
        PowerMockito.verifyPrivate(request, times(1))
                .invoke("saveItem", any(Person::class.java))
        verify(syncManager, times(1))?.enqueueRequest(any(), any(NetworkManager::class.java) as NetworkManager<Person>?,
                any(SyncRequest.HttpVerb::class.java), any())
    }
}
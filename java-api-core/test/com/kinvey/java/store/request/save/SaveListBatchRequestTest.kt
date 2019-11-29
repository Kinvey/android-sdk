package com.kinvey.java.store.request.save

import com.kinvey.java.AbstractClient
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.KinveyBatchInsertError
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.WritePolicy
import com.kinvey.java.store.request.Person
import com.kinvey.java.store.requests.data.save.SaveListBatchRequest
import com.kinvey.java.sync.SyncManager
import junit.framework.TestCase
import org.junit.Before
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito

class SaveListBatchRequestTest : TestCase() {

    lateinit var spyNetworkManager: NetworkManager<Person>
    lateinit var syncManager: SyncManager
    lateinit var client: AbstractClient<*>
    lateinit var cache: ICache<Person>

    @Before
    override fun setUp() {
        client = Mockito.mock(AbstractClient::class.java)
        spyNetworkManager = Mockito.mock(NetworkManager::class.java) as NetworkManager<Person>
        syncManager = Mockito.mock(SyncManager::class.java)
        cache = Mockito.mock(ICache::class.java) as ICache<Person>
    }

    fun testForceLocal() {
        val ids = listOf(Person())
        val request =
                Mockito.spy(SaveListBatchRequest(cache, spyNetworkManager, WritePolicy.FORCE_LOCAL, ids, syncManager))
        request.execute()
        verify(cache, times(1))?.save(anyIterable())
        verify(syncManager, times(1))
             ?.enqueueSaveRequests(anyString(), any(NetworkManager::class.java), anyList())
    }

    fun testForceNetwork() {
        val ids = listOf(Person())
        val request =
                Mockito.spy(SaveListBatchRequest(cache, spyNetworkManager, WritePolicy.FORCE_NETWORK, ids, syncManager))
        request.execute()
        verify(spyNetworkManager, times(1))?.saveBatchBlocking(anyList())
        PowerMockito.verifyPrivate(request, times(1))
                ?.invoke("postSaveBatchRequest", listOf(Person()),
                        listOf(Person()), listOf(KinveyBatchInsertError()), false)
    }

    fun testForceNetwork_MultipleRequests() {
        val ids = (1..200).map { Person("user $it") }
        val request =
                Mockito.spy(SaveListBatchRequest(cache, spyNetworkManager, WritePolicy.FORCE_NETWORK, ids, syncManager))
        request.execute()
        verify(spyNetworkManager, times(2))?.saveBatchBlocking(anyList())
        PowerMockito.verifyPrivate(request, times(2))
                ?.invoke("postSaveBatchRequest", mutableListOf(Person()),
                        mutableListOf(Person()), mutableListOf(KinveyBatchInsertError()), false)
    }

    fun testLocalThenNetwork() {
        val ids = mutableListOf(Person())
        val request =
            Mockito.spy(SaveListBatchRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, ids, syncManager))
        request.execute()
        //PowerMockito.verifyPrivate(request, times(1))?.invoke("doPushRequest")
        verify(cache, times(2))?.save(anyList())
        PowerMockito.verifyPrivate(request, times(1))
                ?.invoke("postSaveBatchRequest", mutableListOf(Person()),
                        mutableListOf(Person()), mutableListOf(KinveyBatchInsertError()), false)
    }

    fun testLocalThenNetwork_MultipleRequests() {
        val ids = (1..200).map { Person("user $it") }
        val request =
                Mockito.spy(SaveListBatchRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, ids, syncManager))
        request.execute()
        //PowerMockito.verifyPrivate(request, times(1))?.invoke("doPushRequest")
        verify(cache, times(2))?.save(anyList())
        PowerMockito.verifyPrivate(request, times(2))
                ?.invoke("postSaveBatchRequest", mutableListOf(Person()),
                        mutableListOf(Person()), mutableListOf(KinveyBatchInsertError()), false)
    }
}
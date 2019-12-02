package com.kinvey.java.store.request.save

import com.kinvey.java.AbstractClient
import com.kinvey.java.cache.ICache
import com.kinvey.java.model.KinveyBatchInsertError
import com.kinvey.java.model.KinveySaveBatchResponse
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
import java.io.IOException

class SaveListBatchRequestTest : TestCase() {

    private val MAX_POST_ITEMS = 100

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
        val ids = (1..5).map { Person("user $it") }
        val request =
                Mockito.spy(SaveListBatchRequest(cache, spyNetworkManager, WritePolicy.FORCE_LOCAL, ids, syncManager))
        request.execute()
        verify(cache, times(1))?.save(anyIterable())
        verify(syncManager, times(1))
             ?.enqueueSaveRequests(anyString(), any(NetworkManager::class.java), anyList())
    }

    fun testForceNetwork() {
        val ids = (1..5).map { Person("user $it") }
        val request =
                Mockito.spy(SaveListBatchRequest(cache, spyNetworkManager, WritePolicy.FORCE_NETWORK, ids, syncManager))
        PowerMockito.doReturn(listOf(Person())).`when`(request, "runSaveItemsRequest", ids, false)
        request.execute()
        PowerMockito.verifyPrivate(request, times(1))
                ?.invoke("runSaveItemsRequest", ids, false)
    }

    fun testForceNetwork_MultipleRequests() {
        val ids = (1..200).map { Person("user $it") }
        val request =
                Mockito.spy(SaveListBatchRequest(cache, spyNetworkManager, WritePolicy.FORCE_NETWORK, ids, syncManager))
        PowerMockito.doReturn(listOf(Person())).`when`(request, "runSaveItemsRequest", ids, false)
        request.execute()
        PowerMockito.verifyPrivate(request, times(1))
                ?.invoke("runSaveItemsRequest", ids, false)
    }

    fun testLocalThenNetwork() {
        val ids = (1..5).map { Person("user $it") }
        val request =
            Mockito.spy(SaveListBatchRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, ids, syncManager))
        PowerMockito.doReturn(ids).`when`(cache, "save", ids)
        PowerMockito.doReturn(KinveySaveBatchResponse(listOf(Person()), null)).`when`(request, "postSaveBatchRequest", ids,
                listOf<Person>(), listOf<KinveyBatchInsertError>(), true)
        request.execute()
        verify(cache, times(2))?.save(anyList())
        PowerMockito.verifyPrivate(request, times(1))
                ?.invoke("postSaveBatchRequest", ids,
                listOf<Person>(), listOf<KinveyBatchInsertError>(), true)
    }

    fun testLocalThenNetwork_MultipleRequests() {

        val ids = (1..200).map { Person("user $it") }
        val chunks = ids.chunked(MAX_POST_ITEMS).toTypedArray() // should be 2 chunks for post items

        val request =
                Mockito.spy(SaveListBatchRequest(cache, spyNetworkManager, WritePolicy.LOCAL_THEN_NETWORK, ids, syncManager))
        PowerMockito.doReturn(ids).`when`(cache, "save", ids)
        PowerMockito.doReturn(KinveySaveBatchResponse(listOf<Person>(), listOf())).`when`(request, "postSaveBatchRequest", chunks[0],
                listOf<Person>(), listOf<KinveyBatchInsertError>(), true)
        PowerMockito.doReturn(KinveySaveBatchResponse(listOf<Person>(), listOf())).`when`(request, "postSaveBatchRequest", chunks[1],
                listOf<Person>(), listOf<KinveyBatchInsertError>(), true)

        request.execute()

        verify(cache, times(2))?.save(anyList())

        // test of saving first chunk
        PowerMockito.verifyPrivate(request, times(1))
                ?.invoke("postSaveBatchRequest", chunks[0],
                listOf<Person>(), listOf<KinveyBatchInsertError>(), true)
        // test of saving second chunk
        PowerMockito.verifyPrivate(request, times(1))
                ?.invoke("postSaveBatchRequest", chunks[1],
                        listOf<Person>(), listOf<KinveyBatchInsertError>(), true)
    }
}
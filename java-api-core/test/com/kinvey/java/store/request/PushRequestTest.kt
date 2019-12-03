package com.kinvey.java.store.request

import com.kinvey.java.AbstractClient
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.store.requests.data.PushRequest
import com.kinvey.java.sync.SyncManager
import com.kinvey.java.sync.dto.SyncItem
import com.kinvey.java.sync.dto.SyncRequest
import junit.framework.TestCase
import org.junit.Before
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito

class PushRequestTest : TestCase() {
    val personId1 = "testId1"
    val personId2 = "testId2"

    val query = Query()
    var spyNetworkManager: NetworkManager<Person>? = null
    var client: AbstractClient<*>? = null
    var syncManager: SyncManager? = null
    var cache: ICache<Person>? = null

    @Before
    override fun setUp() {
        syncManager = mock(SyncManager::class.java)
        client = mock(AbstractClient::class.java)
        spyNetworkManager = mock(NetworkManager::class.java) as NetworkManager<Person>?
        cache = mock(ICache::class.java) as ICache<Person>
    }

    fun testPushPutRequest() {
        val syncItem1 = SyncItem(SyncRequest.HttpVerb.PUT, SyncRequest.SyncMetaData(personId1, null, null), Person.COLLECTION)
        val syncItem2 = SyncItem(SyncRequest.HttpVerb.PUT, SyncRequest.SyncMetaData(personId2, null, null), Person.COLLECTION)
        val saveItems = listOf(syncItem1, syncItem2)
        val person1 = Person(id = personId1)
        val person2 = Person(id = personId2)
        val personItems = listOf(person1, person2)

        val request = spy(PushRequest(Person.COLLECTION, cache as ICache<Person>,
                spyNetworkManager as NetworkManager<Person>, client))
        request.syncManager = syncManager

        PowerMockito.doReturn(saveItems).`when`(syncManager, "popSingleItemQueue", Person.COLLECTION)
        PowerMockito.doReturn(person1).`when`(cache, "get", personId1)
        PowerMockito.doReturn(person2).`when`(cache, "get", personId2)

        request.execute()

        verify(cache, times(2))?.get(ArgumentMatchers.anyString())
        verify(syncManager, times(2))?.executeRequest(any(AbstractClient::class.java), any())
        verify(syncManager, times(2))?.deleteCachedItem(any())
    }

    fun testPushPostRequest() {
        val syncItem1 = SyncItem(SyncRequest.HttpVerb.POST, SyncRequest.SyncMetaData(personId1, null, null), Person.COLLECTION)
        val syncItem2 = SyncItem(SyncRequest.HttpVerb.POST, SyncRequest.SyncMetaData(personId2, null, null), Person.COLLECTION)
        val saveItems = listOf(syncItem1, syncItem2)
        val person1 = Person(id = personId1)
        val person2 = Person(id = personId2)
        val personItems = listOf(person1, person2)

        val request = spy(PushRequest(Person.COLLECTION, cache as ICache<Person>,
                spyNetworkManager as NetworkManager<Person>, client))
        request.syncManager = syncManager

        PowerMockito.doReturn(saveItems).`when`(syncManager, "popSingleItemQueue", Person.COLLECTION)
        PowerMockito.doReturn(person1).`when`(cache, "get", personId1)
        PowerMockito.doReturn(person2).`when`(cache, "get", personId2)

        request.execute()

        verify(cache, times(4))?.get(ArgumentMatchers.anyString())
        verify(syncManager, times(2))?.executeRequest(any(AbstractClient::class.java), any())
        verify(syncManager, times(2))?.deleteCachedItem(any())
    }
}
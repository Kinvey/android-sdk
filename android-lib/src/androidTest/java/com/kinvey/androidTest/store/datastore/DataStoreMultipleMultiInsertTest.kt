package com.kinvey.androidTest.store.datastore

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.store.DataStore
import com.kinvey.androidTest.model.Person
import com.kinvey.androidTest.network.MockMultiInsertNetworkManager
import com.kinvey.java.store.StoreType
import org.junit.Assert

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore

@RunWith(AndroidJUnit4::class)
@SmallTest
class DataStoreMultipleMultiInsertTestKotlin : BaseDataStoreTest() {

    @Test
    @Throws(InterruptedException::class)
    fun testSaveMultipleBatchRequests() {
        print("should send multiple multi-insert POST requests")
        val netManager = MockMultiInsertNetworkManager(Person.COLLECTION, Person::class.java, client)
        val store = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.NETWORK, netManager)
        clearBackend(store)
        client.syncManager.clear(Person.COLLECTION)

        val itemsList = createPersonsList(200, false)

        val saveCallback = saveList(store, itemsList)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)

        Assert.assertEquals(netManager.multiPostCount, 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveMultipleBatchRequestsIfWasErrors() {
        print("should send multiple multi-insert POST requests, if was errors then should return empty array and array of errors")
        val netManager = MockMultiInsertNetworkManager(Person.COLLECTION, Person::class.java, client)
        val store = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.NETWORK, netManager)
        clearBackend(store)
        client.syncManager.clear(Person.COLLECTION)
        val itemsList = createPersonsListErr(200, 50, 50)
        val saveCallback = saveList(store, itemsList)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)

        Assert.assertEquals(netManager.multiPostCount, 3)
    }
}

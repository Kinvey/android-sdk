package com.kinvey.androidTest.store.datastore

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.store.DataStore
import com.kinvey.androidTest.model.Person
import com.kinvey.androidTest.network.MockMultiInsertNetworkManager
import com.kinvey.java.store.StoreType

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class DataStoreMultiInsertItemsOrderTestKotlin : BaseDataStoreTest() {

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListCheckItemsOrderNetwork() {
        val personList = createCombineList()
        val personStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)
        client.syncManager.clear(Person.COLLECTION)

        testSaveListCheckItemsOrder(personStore, personList)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListCombineWithIdAndWithoutIdSync() {
        val personList = createCombineList()
        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(syncStore)
        client.syncManager.clear(Person.COLLECTION)

        testSaveListCheckItemsOrder(syncStore, personList)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListCombineWithIdAndWithoutIdAuto() {
        val personList = createCombineList()
        val mockNetManager = MockMultiInsertNetworkManager(Person.COLLECTION, Person::class.java, client)
        val autoStore = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.AUTO, mockNetManager)
        clearBackend(autoStore)
        client.syncManager.clear(Person.COLLECTION)
        mockNetManager.clear()

        testSaveListCheckItemsOrder(autoStore, personList)
    }

    private fun testSaveListCheckItemsOrder(store: DataStore<Person>, personList: List<Person>) {

        val saveCallback = saveList(store, personList)

        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonListIfSameOrder(personList, saveCallback.result))

        val findCallback = find(store, LONG_TIMEOUT)
        assertNotNull(findCallback.result)

        assertTrue(checkPersonIfSameObjects(personList, findCallback.result?.result!!))
    }
}

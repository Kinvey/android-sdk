package com.kinvey.androidTest.store.datastore

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.store.DataStore
import com.kinvey.androidTest.model.Person
import com.kinvey.java.store.StoreType

import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class DataStoreMultiInsertOfflinePushTest : BaseDataStoreTest() {

    @Test
    @Throws(InterruptedException::class)
    fun testPushItemWithConnectivityErrorAuto() {
        print("should push item with connectivity error")

        val person = Person(TEST_USERNAME)
        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(autoStore)
        client.syncManager.clear(Person.COLLECTION)

        mockInvalidConnection()
        val saveResult = save(autoStore, person)
        val pushResult = push(autoStore, LONG_TIMEOUT)
        cancelMockInvalidConnection()
        val syncItems = pendingSyncEntities(Person.COLLECTION)

        assertNotNull(syncItems)
        assertEquals(1, syncItems?.count())

        assertNull(saveResult.error)
        assertNotNull(pushResult.error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushItemCheckSyncStoreIfConnectivityErrorAuto() {
        print("should push item with connectivity error and store item in local cache")

        val person = Person(TEST_USERNAME)
        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)

        clearBackend(syncStore)
        clearBackend(autoStore)
        client.syncManager.clear(Person.COLLECTION)

        mockInvalidConnection()
        val saveResult = save(autoStore, person)
        val pushResult = push(autoStore, LONG_TIMEOUT)
        cancelMockInvalidConnection()

        val findResult = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findResult.result)
        assertNull(findResult.error)
        assertEquals(1, findResult.result?.result?.count())

        val syncItems = pendingSyncEntities(Person.COLLECTION)
        assertNotNull(syncItems)
        assertEquals(1, syncItems?.count())
    
        assertNull(saveResult.error)
        assertNotNull(pushResult.error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveItemWithConnectivityErrorAuto() {
        print("should save item in local cache")

        val person = Person(TEST_USERNAME)
        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(autoStore)
        client.syncManager.clear(Person.COLLECTION)

        mockInvalidConnection()
        val saveResult = save(autoStore, person)
        cancelMockInvalidConnection()

        val syncItems = pendingSyncEntities(Person.COLLECTION)
        assertNotNull(syncItems)
        assertEquals(1, syncItems?.count())

        assertNotNull(saveResult.result)
        assertNull(saveResult.error)
    }
}

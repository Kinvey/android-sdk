package com.kinvey.androidTest.store.datastore

import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.store.DataStore
import com.kinvey.androidTest.model.Person
import com.kinvey.java.store.StoreType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertNotNull

@RunWith(AndroidJUnit4::class)
@LargeTest
class DataStoreMultiInsertOfflinePushTestKotlin : BaseDataStoreTest() {

    @Test
    @Throws(InterruptedException::class)
    fun testPushItemWithConnectivityErrorAuto() {
        print("should push with connectivity error")

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
        assertNotNull(saveResult.error)
        assertNotNull(pushResult.error)
    }
}

package com.kinvey.androidTest.store.datastore

import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.android.store.DataStore
import com.kinvey.androidTest.model.EntitySet
import com.kinvey.androidTest.model.Person
import com.kinvey.androidTest.network.MockMultiInsertNetworkManager
import com.kinvey.java.KinveySaveBatchException
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.dto.SyncRequest

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

import java.util.ArrayList

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

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

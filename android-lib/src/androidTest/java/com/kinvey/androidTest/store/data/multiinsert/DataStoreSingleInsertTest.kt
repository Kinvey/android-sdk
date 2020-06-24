package com.kinvey.androidTest.store.data.multiinsert

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
import com.kinvey.java.AbstractClient.Companion.kinveyApiVersion
import com.kinvey.java.Constants._ID


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
class DataStoreSingleInsertTest : BaseDataStoreMultiInsertTest() {

    // CREATE METHOD TESTS
    @Test
    @Throws(InterruptedException::class)
    fun createSingleInsertNetwork() {
        client.enableDebugLogging()
        createSingleInsert(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun createSingleInsertAuto() {
        createSingleInsert(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun createSingleInsertSync() {
        createSingleInsert(StoreType.SYNC)
    }

    @Throws(InterruptedException::class)
    fun createSingleInsert(storeType: StoreType) {
        val person = createPerson(TEST_USERNAME)

        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, storeType, client)
        clearBackend(personStore)
        client.syncManager.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = create(personStore, person)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)

        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(person.username, findCallback.result!!.result!![0].username)
        val personListSecond = ArrayList<Person>()
        personListSecond.addAll(findCallback.result?.result!!)
        personListSecond.add(Person())
        val saveCallbackSecond = createList(personStore, personListSecond)
        assertNull(saveCallbackSecond.error)
        assertNotNull(saveCallbackSecond.result)
        if (storeType != StoreType.SYNC) {
            assertNotNull(saveCallbackSecond.result?.errors)
            assertEquals(1, saveCallbackSecond.result?.errors?.size)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun createWithId() {
        val person = createPerson(TEST_USERNAME)
        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)
        client.syncManager.clear(MULTI_INSERT_COLLECTION)
        person[_ID] = "123"
        val saveCallback =  create(personStore, person)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(person.username, findCallback.result?.result!![0].username)
        assertEquals(person.id, findCallback.result?.result!![0].id)
        assertEquals("123", findCallback.result?.result!![0].id)
    }

    @Test
    @Throws(InterruptedException::class)
    fun <T : GenericJson> testCreateApiV6() {
        val person = createPerson(TEST_USERNAME)
        kinveyApiVersion = "6"
        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(personStore)
        client.syncManager.clear(MULTI_INSERT_COLLECTION)
        val saveCallback = create(personStore, person)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        clearBackend(personStore)
        kinveyApiVersion = "5"
    }
   
}

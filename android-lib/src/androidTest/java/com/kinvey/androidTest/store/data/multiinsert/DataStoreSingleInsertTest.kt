package com.kinvey.androidTest.store.data.multiinsert

import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.json.GenericJson
import com.kinvey.android.store.DataStore
import com.kinvey.androidTest.model.EntitySet
import com.kinvey.androidTest.model.Person
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.store.StoreType
import com.kinvey.java.AbstractClient.Companion.kinveyApiVersion
import com.kinvey.java.Constants._ID
import org.junit.Assert


import org.junit.Test
import org.junit.runner.RunWith

import java.util.ArrayList

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Ignore

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
        assertEquals(person.username, saveCallback.result!!.username)

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

    @Test
    @Throws(InterruptedException::class)
    fun testCreateWithoutId() {
        val netStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(netStore)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = create(netStore, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val resultPerson = saveCallback.result!!
        assertNotNull(resultPerson.id)
        assertEquals(person.username, resultPerson.username)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCreateListReturnErrorForInvalidCredentialsNetwork() {
        val store = DataStore.collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        val entity = EntitySet()
        entity.description = "entity 1"
        val defaultKinveyListCallback = create(store, entity)
        assertNotNull(defaultKinveyListCallback.error)
        assertEquals(defaultKinveyListCallback.error?.javaClass, KinveyJsonResponseException::class.java)
    }


    @Test
    @Throws(InterruptedException::class)
    fun testTempIdSync() {
        testTempId(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testTempIdCache() {
        testTempId(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testTempIdAuto() {
        testTempId(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testTempIdNetwork() {
        testTempId(StoreType.NETWORK)
    }

    @Throws(InterruptedException::class)
    private fun testTempId(storeType: StoreType) {
        val person = createPerson(TEST_USERNAME)
        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, storeType, client)
        clearBackend(personStore)
        client.syncManager.clear(MULTI_INSERT_COLLECTION)
        val saveCallback = create(personStore, person)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        if (storeType == StoreType.SYNC) {
            Assert.assertTrue(saveCallback.result!!.id?.startsWith("temp")!!)
            val pushCallback = push(personStore, DEFAULT_TIMEOUT)
            assertNull(pushCallback.error)
            assertNotNull(pushCallback.result)
        } else {
            Assert.assertTrue(!saveCallback.result!!.id?.startsWith("temp")!!)
        }
        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertNotNull(findCallback.result!!.result)
        if (storeType == StoreType.SYNC) {
            Assert.assertTrue(saveCallback.result!!.id?.startsWith("temp")!!)
        } else {
            Assert.assertTrue(!saveCallback.result!!.id?.startsWith("temp")!!)
        }

    }

    @Test
    @Throws(InterruptedException::class)
    fun testErrorMessageIfSameIdExistsNetwork() {
        testErrorMessageIfSameIdExists(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testErrorMessageIfSameIdExistsAuto() {
        testErrorMessageIfSameIdExists(StoreType.AUTO)
    }

    @Test
    @Ignore("Should work after fixing: https://kinvey.atlassian.net/browse/KDEV-781")
    @Throws(InterruptedException::class)
    fun testErrorMessageIfSameIdExistsSync() {
        testErrorMessageIfSameIdExists(StoreType.SYNC)
    }

    @Throws(InterruptedException::class)
    fun testErrorMessageIfSameIdExists(storeType: StoreType) {
        val personList = createPersonsList(true)
        val netStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, storeType, client)
        clearBackend(netStore)
        var saveCallback = createList(netStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        Assert.assertTrue(checkPersonIfSameObjects(personList, saveCallback.result?.entities, false))
        saveCallback = createList(netStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.errors)
        assertNotNull(saveCallback.result?.errors?.get(0)?.description)
        assertNotNull(saveCallback.result?.errors?.get(0)?.debug)
    }
}

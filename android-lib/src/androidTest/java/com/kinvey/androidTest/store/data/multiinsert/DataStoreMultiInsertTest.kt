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
class DataStoreMultiInsertTest : BaseDataStoreMultiInsertTest() {

    // CREATE METHOD TESTS
    @Test
    @Throws(InterruptedException::class)
    fun createMultiInsertListNetwork() {
        createMultiInsertList(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun createMultiInsertListAuto() {
        createMultiInsertList(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun createMultiInsertListSync() {
        createMultiInsertList(StoreType.SYNC)
    }

    @Throws(InterruptedException::class)
    fun createMultiInsertList(storeType: StoreType) {
        val personList = createPersonsList(false)

        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, storeType, client)
        clearBackend(personStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = createList(personStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)

        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result?.result))
        val personListSecond = ArrayList<Person>()
        personListSecond.addAll(findCallback.result?.result!!)
        personListSecond.add(Person())
        val saveCallbackSecond = createList(personStore, personListSecond)
        assertNull(saveCallbackSecond.error)
        assertNotNull(saveCallbackSecond.result)
        if (!storeType.equals(StoreType.SYNC)) {
            assertNotNull(saveCallbackSecond.result?.errors)
            assertEquals(saveCallbackSecond.result?.errors?.size, 5)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun createMultiListWithId() {
        val personList = createPersonsList(true)

        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = createList(personStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)

        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result?.result))
        val personListSecond = ArrayList<Person>()
        personListSecond.addAll(findCallback.result?.result!!)
        personListSecond.add(Person())
        val saveCallbackSecond = createList(personStore, personListSecond)
        assertNull(saveCallbackSecond.error)
        assertNotNull(saveCallbackSecond.result)
        assertNotNull(saveCallbackSecond.result?.errors)
        assertEquals(saveCallbackSecond.result?.errors?.size, 5)
    }

    @Throws(InterruptedException::class)
    fun testCreateListReturnErrorForInvalidCredentialsNetwork() {
        val entityList = createEntityList(2)
        val store = DataStore.collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.NETWORK, client)
        clearBackend(store)
        val defaultKinveyListCallback = createList(store, entityList)
        assertNotNull(defaultKinveyListCallback.error)
        assertEquals(defaultKinveyListCallback.error?.javaClass, KinveyJsonResponseException::class.java)
    }

    @Throws(InterruptedException::class)
    fun testCreateListCombineWithIdAndWithoutIdSync() {
        val personList = createCombineList()

        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)

        clearBackend(syncStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = createList(syncStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result?.entities, false))

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        assertEquals(syncItems!!.size.toLong(), personList.size.toLong())
        assertEquals(syncItems[0].requestMethod, SyncRequest.HttpVerb.POST)
        assertEquals(syncItems[1].requestMethod, SyncRequest.HttpVerb.PUT)
        assertEquals(syncItems[2].requestMethod, SyncRequest.HttpVerb.POST)
        assertEquals(syncItems[3].requestMethod, SyncRequest.HttpVerb.PUT)

        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCreateListCombineWithIdAndWithoutIdAuto() {
        val personList = createCombineList()

        val mockNetManager = MockMultiInsertNetworkManager(MULTI_INSERT_COLLECTION, Person::class.java, client as Client)
        val autoStore = DataStore(MULTI_INSERT_COLLECTION, Person::class.java, client, StoreType.AUTO, mockNetManager)
        val syncStore = DataStore(MULTI_INSERT_COLLECTION, Person::class.java, client, StoreType.SYNC, mockNetManager)
        val netStore = DataStore(MULTI_INSERT_COLLECTION, Person::class.java, client, StoreType.NETWORK, mockNetManager)

        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        mockNetManager.clear()
        val saveCallback = createList(autoStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result?.entities))
        assertTrue(mockNetManager.useMultiInsertSave)

        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result))

        val findCallbackNet = find(netStore, LONG_TIMEOUT)
        assertNotNull(findCallbackNet.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackNet.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCreateListReturnErrorForEmptyListSync() {
        val list = ArrayList<Person>()
        testCreateEmptyList(list, Person::class.java, MULTI_INSERT_COLLECTION, StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun <T : GenericJson> testCreateApiV6() {
        val personList = createPersonsList(false)
        kinveyApiVersion = "6"
        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(personStore)
        client.syncManager.clear(MULTI_INSERT_COLLECTION)
        val saveCallback = createList(personStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        clearBackend(personStore)
        kinveyApiVersion = "5"
    }

    // SAVE METHOD TESTS

    // NETWORK STORE
    @Test
    @Throws(InterruptedException::class)
    fun testSaveWithoutIdNetwork() {
        print("should send POST with a single item with no _id")
        // create an item that has no _id property
        // call save() with it
        // find using network store

        val netStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(netStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(netStore, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val resultPerson = saveCallback.result as Person
        assertNotNull(resultPerson.id)
        assertEquals(person.username, resultPerson.username)

        val findCallback = find(netStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        val list = findCallback.result?.result
        assertEquals(1, list?.count())
        val findPerson = list?.get(0)
        assertNotNull(findPerson)
        assertNotNull(findPerson?.id)
        assertEquals(person.username, findPerson?.username)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveWithIdNetwork() {
        print("should send PUT with a sngle item with _id")
        // create an item with _id property
        // call save() with it
        // find using network store

        val netStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(netStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val id = "123456"
        val person = createPerson(TEST_USERNAME)
        person.id = id

        val saveCallback = save(netStore, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val resultPerson = saveCallback.result as Person
        assertNotNull(resultPerson)
        assertEquals(id, resultPerson.id)

        val findCallback = find(netStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        val list = findCallback.result?.result
        assertEquals(1, list?.count())
        val findPerson = list?.get(0)
        assertNotNull(findPerson)
        assertEquals(id, findPerson?.id)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListWithoutIdNetwork() {
        print("should send POST multi-insert request for array of items with no _id")
        // create an array with a few items that have no _id property
        // save() with the array as param
        // find using network store

        val personList = createPersonsList(false)

        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(personStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result))

        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListWithIdNetwork() {
        print("should sent PUT requests for an array of items with _id")
        // create an array with a few items that have _id property
        // save() with the array as param
        // find using network store

        val personList = createPersonsList(true)

        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(personStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result))

        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListCombineWithIdAndWithoutIdNetwork() {
        print("should combine POST and PUT requests for items with and without _id")
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no _id, _id]
        // save() using the array
        // find using network store

        val personList = createCombineList()

        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(personStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result))

        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListReturnErrorForEmptyListNetwork() {
        print("should return an error for an empty array")
        // create an empty array
        // save() using the array
        val list = ArrayList<Person>()
        testSaveEmptyList(list, Person::class.java, MULTI_INSERT_COLLECTION, StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListReturnErrorForInvalidCredentialsNetwork() {
        print("should return an error when all items fail with multi-insert for invalid credentials")
        // create an array with a few items that have no _id property
        // set a collection permission to deny creating items
        // save() using the array from above
        val entityList = createEntityList(2)

        val store = DataStore.collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.NETWORK, client)
        clearBackend(store)

        val defaultKinveyListCallback = saveList(store, entityList)
        assertNotNull(defaultKinveyListCallback.error)
        assertEquals(defaultKinveyListCallback.error?.javaClass, KinveyJsonResponseException::class.java)
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testSaveListReturnErrorArrayForAllItemsFailNetwork() {
        print("should return an array of errors for all items failing for different reasons")
        // create an array containing two items failing for different reasons
        // save using the array above
        val personList = createErrList()
        val checkIndexes = intArrayOf(0, 1)

        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)

        val saveCallback = saveList(personStore, personList)
        assertNotNull(saveCallback.error)
        if (saveCallback.error is KinveySaveBatchException) {
            val resultEntities = (saveCallback.error as KinveySaveBatchException).entities as List<Person>?
            val errorsList = (saveCallback.error as KinveySaveBatchException).errors
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexes, true))
            val errMessages = arrayOf(ERR_GEOLOC_MSG, ERR_GEOLOC_MSG)
            assertTrue(checkBatchResponseErrors(errorsList, checkIndexes, true, errMessages))
        }
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testSaveListReturnErrorArrayForSomeItemsFailNetwork() {
        print("should return an entities and errors when some requests fail and some succeed")
        // create an array of items with no _id and the second of them should have invalid _geoloc params
        // save using the array above
        // find using network store

        val personList = createErrList1()
        val checkIndexesSuccess = intArrayOf(0)
        val checkIndexesErr = intArrayOf(1)

        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)

        val saveCallback = saveList(personStore, personList)
        assertNotNull(saveCallback.error)
        if (saveCallback.error is KinveySaveBatchException) {
            val resultEntities = (saveCallback.error as KinveySaveBatchException).entities as List<Person>?
            val errorsList = (saveCallback.error as KinveySaveBatchException).errors
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesSuccess, false))
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesErr, true))
            val errMessages = arrayOf(ERR_GEOLOC_MSG, ERR_GEOLOC_MSG)
            assertTrue(checkBatchResponseErrors(errorsList, checkIndexesErr, true, errMessages))
        }

        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkIfPersonItemsAtRightIndex(personList, findCallback.result?.result, checkIndexesSuccess, false))
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testSaveListReturnPutFailuresAtMatchingIndexNetwork() {
        print("should return PUT failures at the matching index")
        // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
        // save using the array above
        // find using network store
        val personList = createErrListGeoloc()
        val checkIndexesSuccess = intArrayOf(1, 3)
        val checkIndexesErr = intArrayOf(0, 2)

        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)

        val saveCallback = saveList(personStore, personList)
        assertNotNull(saveCallback.error)
        if (saveCallback.error is KinveySaveBatchException) {
            val resultEntities = (saveCallback.error as KinveySaveBatchException).entities as List<Person>?
            val errorsList = (saveCallback.error as KinveySaveBatchException).errors
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesSuccess, false))
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesErr, true))
            val errMessages = arrayOf(ERR_GEOLOC_MSG, ERR_GEOLOC_MSG)
            assertTrue(checkBatchResponseErrors(errorsList, checkIndexesErr, true, errMessages))
        }

        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkIfPersonItemsAtRightIndex(personList, findCallback.result?.result, checkIndexesSuccess, false))
    }

    // SYNC STORE

    @Test
    @Throws(InterruptedException::class)
    fun testSaveWithoutIdSync() {
        print("should send POST with a single item with no _id")
        // create an item that has no _id property
        // call save() with it
        // pendingSyncEntities()
        // find() using sync store

        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(syncStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val person = createPerson(TEST_USERNAME)

        val saveCallback = save(syncStore, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val resultPerson = saveCallback.result as Person
        assertNotNull(resultPerson.id)
        assertEquals(person.username, resultPerson.username)

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        assertEquals(syncItems?.count(), 1)
        assertEquals(syncItems?.get(0)?.requestMethod, SyncRequest.HttpVerb.POST)

        val findCallback = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        val list = findCallback.result?.result
        assertEquals(1, list?.count())
        val findPerson = list?.get(0)
        assertNotNull(findPerson)
        assertNotNull(findPerson?.id)
        assertEquals(person.username, findPerson?.username)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveWithIdSync() {
        print("should send PUT with a single item with _id")
        // create an item with _id property
        // call save() with it
        // pendingSyncEntities()
        // find() using syncstore

        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(syncStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val id = "123456"
        val person = createPerson(TEST_USERNAME)
        person.id = id

        val saveCallback = save(syncStore, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val resultPerson = saveCallback.result as Person
        assertNotNull(resultPerson)
        assertEquals(id, resultPerson.id)

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        assertEquals(syncItems?.count(), 1)
        assertEquals(syncItems?.get(0)?.requestMethod, SyncRequest.HttpVerb.PUT)

        val findCallback = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        val list = findCallback.result?.result
        assertEquals(1, list?.count())
        val findPerson = list?.get(0)
        assertNotNull(findPerson)
        assertEquals(id, findPerson?.id)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListWithoutIdSync() {
        print("should send save an array of items with no _id")
        // create an array with a few items that have no _id property
        // save() with the array as param
        // pendingSyncEntities()
        // find() using syncstore

        val personList = createPersonsList(false)

        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)

        clearBackend(syncStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(syncStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result, false))

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        assertTrue(checkSyncItems<GenericJson>(syncItems, personList.size, SyncRequest.HttpVerb.POST))

        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListWithIdSync() {
        print("should save an array of items with _id")
        // create an array with a few items that have _id property
        // save() with the array as param
        // pendingSyncEntities()
        // find() using syncstore

        val personList = createPersonsList(true)

        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)

        clearBackend(syncStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(syncStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result, false))

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        assertTrue(checkSyncItems<GenericJson>(syncItems, personList.size, SyncRequest.HttpVerb.PUT))

        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListCombineWithIdAndWithoutIdSync() {
        print("should save and array of items with and without _id")
        // create an array that has 2 items with _id and 2 without
        // save() using the array, should return the items from step 1
        // pendingSyncEntities(), should return the items with PUT and POST operations respectively
        // find() using syncstore, should return the items from step 1 with _ids of the items that were assigned
        val personList = createCombineList()

        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)

        clearBackend(syncStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(syncStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result, false))

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        assertEquals(syncItems!!.size.toLong(), personList.size.toLong())
        assertEquals(syncItems[0].requestMethod, SyncRequest.HttpVerb.POST)
        assertEquals(syncItems[1].requestMethod, SyncRequest.HttpVerb.PUT)
        assertEquals(syncItems[2].requestMethod, SyncRequest.HttpVerb.POST)
        assertEquals(syncItems[3].requestMethod, SyncRequest.HttpVerb.PUT)

        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListReturnErrorForEmptyListSync() {
        print("should return an error for an empty array")
        // create an empty array
        // save() using the array
        val list = ArrayList<Person>()
        testSaveEmptyList(list, Person::class.java, MULTI_INSERT_COLLECTION, StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushItemsListWithoutIdSync() {
        print("should use multi insert for multiple items without _id")
        // create an array of items without _id
        // save using the array above
        // push(), should return an array of the objects pushed with the operation performed
        // pendingSyncEntities(), should return an empty array
        // find using syncstore, should return the items with ect and lmt
        val personsList = createPersonsList(false)

        val storeSync = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallbackSecond = saveList(storeSync, personsList)
        assertNull(saveCallbackSecond.error)
        assertNotNull(saveCallbackSecond.result)
        assertTrue(checkPersonIfSameObjects(personsList, saveCallbackSecond.result, false))

        val pushCallback = push(storeSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertTrue(syncItems.isNullOrEmpty())

        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushItemsListCombineWithIdAndWithoutIdSync() {
        print("should combine POST and PUT requests for items with and without _id")
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push(), should return and array of the items pushed with the respective operation - POST for no _id and PUT for _id
        // pendingSyncEntities(), should return an empty array
        // find using syncstore, should return the items with ect and lmt
        val personsList = createCombineList()

        val storeSync = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallbackSecond = saveList(storeSync, personsList)
        assertNull(saveCallbackSecond.error)
        assertNotNull(saveCallbackSecond.result)
        assertTrue(checkPersonIfSameObjects(personsList, saveCallbackSecond.result, false))

        val pushCallback = push(storeSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertTrue(syncItems.isNullOrEmpty())

        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushItemsListCombineWithIdAndWithoutIdMockedSync() {
        print("should combine POST and PUT requests for items with and without _id - mocked")
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array
        // push(), should use a multi-insert POST request and separate PUT requests for the items with _id. Should return and array of the items pushed with the respective operation - POST for no _id and PUT for _id
        // pendingSyncEntities(), should return an empty array
        // find using syncstore, should return the items with ect and lmt
        val personsList = createCombineList()

        val mockNetManager = MockMultiInsertNetworkManager(MULTI_INSERT_COLLECTION, Person::class.java, client as Client)
        val storeSync = DataStore(MULTI_INSERT_COLLECTION, Person::class.java, client, StoreType.SYNC, mockNetManager)
        clearBackend(storeSync)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        saveList(storeSync, personsList)

        val pushCallback = push(storeSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)
        assertTrue(mockNetManager.useMultiInsertSave)

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertTrue(syncItems == null || syncItems.isEmpty())

        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result?.result, true))
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testPushItemsListReturnsErrorForEachItemSync() {
        print("should return the failure reason in the result for each pushed item even if it is the same")
        // create an array of items without _id and set the collection permission for create to never
        // save() using the array
        // push(), should return the items from step 1 each with a POST operation and an error property with value: the invalid credentials error
        // pendingSyncEntities(), should return all items from step 1
        // find using syncstore, should return the items from step 1

        val entitySetList = createEntityList(2)
        val itemsErrorIndexes = intArrayOf(0, 1)

        val storeSync = DataStore.collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        client?.syncManager?.clear(EntitySet.COLLECTION)

        val saveListCallback = saveList(storeSync, entitySetList)
        assertNotNull(saveListCallback.result)
        assertTrue(checkIfItemsAtRightIndex(entitySetList, saveListCallback.result,
                itemsErrorIndexes, EntitySet.DESCRIPTION_KEY, false, false))

        val pushCallback = push(storeSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.error)
        if (pushCallback.error is KinveySaveBatchException) {
            val entityList = (pushCallback.error as KinveySaveBatchException).entities as List<EntitySet>?
            val errorsList = (pushCallback.error as KinveySaveBatchException).errors
            assertTrue(checkIfItemsAtRightIndex(entitySetList, entityList,
                    itemsErrorIndexes, EntitySet.DESCRIPTION_KEY, true, false))
            val errMessages = arrayOf(ERR_PERMISSION_MSG, ERR_PERMISSION_MSG)
            assertTrue(checkBatchResponseErrors(errorsList, itemsErrorIndexes, true, errMessages))
        }

        val syncItems = pendingSyncEntities(EntitySet.COLLECTION)
        assertNotNull(syncItems)
        assertEquals(syncItems?.count(), entitySetList.count())

        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        checkIfSameObjects(entitySetList, findCallback.result?.result, EntitySet.DESCRIPTION_KEY, false)
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testPushItemsListReturnsErrorsForEachItemErrorSync() {
        print("should return the failure reason in the result for each pushed item when they are different")
        // create an array with 3 items without _id, two of which should be invalid for different reasons
        // save using the array above
        // push(), should return the items with respective POST and PUT operations and items which are set to fail should have their distinct errors
        // pendingSyncEntities(), should return the failing items from step 1
        // find using syncstore, should return the items from step 1 - the successfull one with lmt and ect
        val personsList = createPushErrList()
        val successItemsIdx = intArrayOf(2)

        val storeSync = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        saveList(storeSync, personsList)

        val pushCallback = push(storeSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(1, pushCallback.result?.successCount)

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        assertTrue(checkSyncItems<GenericJson>(syncItems, 2, SyncRequest.HttpVerb.POST))

        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkIfPersonItemsAtRightIndex(personsList, findCallback.result?.result, successItemsIdx, false))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushShouldUseMultiInsertAfterSaveSync() {
        print("should use multi-insert even if the items have not been created in an array")
        // save an item without _id
        // save another item without _id
        // push(), should use a multi-insert POST request and return the items with POST operations
        // pendingSyncEntities(), should return an empty array
        // find using syncstore, should return all items from step 1
        testPushMultiInsertSupport(StoreType.SYNC)
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testSyncItemsListSync() {
        print("test sync items list")
        // create an array of 3 items, the second of which has invalid _geoloc parameters
        // save()
        // Sync(), Should return error for not pushed item
        // pendingSyncEntities(), should return the item with invalid params
        // find() using networkstore, should return the valid items
        // find using syncstore, should return all items including the invalid one
        testSyncItemsList(false, StoreType.SYNC)
    }

    // AUTO STORE

    @Test
    @Throws(InterruptedException::class)
    fun testSaveWithoutIdAuto() {
        print("should send POST with a single item with no _id")
        // create an item that has no _id property
        // call save() with it
        // call find() with sync store
        // find using network store

        val autoStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        val netStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val person = createPerson(TEST_USERNAME)

        val saveCallback = save(autoStore, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val resultPerson = saveCallback.result as Person
        assertNotNull(resultPerson.id)
        assertEquals(person.username, resultPerson.username)

        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        val listSync = findCallbackSync.result?.result
        assertEquals(1, listSync?.count())
        val findPersonSync = listSync?.get(0)
        assertNotNull(findPersonSync)
        assertNotNull(findPersonSync?.id)
        assertEquals(person.username, findPersonSync?.username)

        val findCallbackNet = find(netStore, LONG_TIMEOUT)
        assertNotNull(findCallbackNet.result)
        val listNet = findCallbackNet.result?.result
        assertEquals(1, listNet?.count())
        val findPersonNet = listNet?.get(0)
        assertNotNull(findPersonNet)
        assertNotNull(findPersonNet?.id)
        assertEquals(person.username, findPersonNet?.username)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveWithIdAuto() {
        print("should send PUT with a single item with _id")
        // create an item with _id property
        // call save() with it
        // call find() with syncstore
        // find using networkstore

        val autoStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        val netStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val id = "123456"
        val person = createPerson(TEST_USERNAME)
        person.id = id

        val saveCallback = save(autoStore, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val resultPerson = saveCallback.result as Person
        assertNotNull(resultPerson)
        assertEquals(id, resultPerson.id)

        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        val listSync = findCallbackSync.result?.result
        assertEquals(1, listSync?.count())
        val findPersonSync = listSync?.get(0)
        assertNotNull(findPersonSync)
        assertEquals(id, findPersonSync?.id)

        val findCallbackNet = find(netStore, LONG_TIMEOUT)
        assertNotNull(findCallbackNet.result)
        val listNet = findCallbackNet.result?.result
        assertEquals(1, listNet?.count())
        val findPersonNet = listNet?.get(0)
        assertNotNull(findPersonNet)
        assertEquals(id, findPersonNet?.id)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveWithConnectivityErrorAuto() {
        print("should send with connectivity error")
        // create an item that has no _id property with invalid _geoloc params
        // call save() with it -mock it for connectivity error
        // call find() with syncstore, should return the item from step 1
        // call pendingSyncEntities(), should return the item from step 1 with POST operation

        val person = Person(TEST_USERNAME)
        person.geoloc = ERR_GEOLOC

        val autoStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        mockInvalidConnection()
        save(autoStore, person)
        cancelMockInvalidConnection()

        val findCallback = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        val list = findCallback.result?.result
        assertEquals(1, list?.count())
        assertNotNull(list?.get(0)?.id)

        val pendingList = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(pendingList)
        assertEquals(1, pendingList?.count())
        val item = pendingList?.get(0)
        assertNotNull(item)
        assertEquals(SyncRequest.HttpVerb.POST, item?.requestMethod)
    }

    @Throws(InterruptedException::class)
    fun testSaveLocallyIfNetworkErrorAuto() {
        print("should save the item with _id locally if network connectivity issue")
        //create an item with _id
        //call save with it - mock the request to return connectivity error, should return connectivity error
        //find using syncstore, should return the item from step 1
        //pendingSyncEntities(), should return the item from step 1 with PUT operation with the specified _id

        val testId = "TEST_ID_123"
        val person = Person(testId, TEST_USERNAME)

        val autoStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        mockInvalidConnection()
        save(autoStore, person)
        cancelMockInvalidConnection()

        val findCallback = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        val list = findCallback.result?.result
        assertEquals(1, list?.count())
        assertEquals(testId, list?.get(0)?.id)

        val pendingList = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(pendingList)
        assertEquals(1, pendingList?.count())
        val item = pendingList?.get(0)
        assertNotNull(item)
        assertEquals(testId, item?.entityID?.id)
        assertEquals(SyncRequest.HttpVerb.PUT, item?.requestMethod)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListWithoutIdAuto() {
        print("should send save an array of items with no _id")
        // create an array with a few items that have no _id property
        // save() with the array as param
        // find() with syncstore
        // find() using networkstore

        val personList = createPersonsList(false)

        val autoStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        val netStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(autoStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result))

        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result))

        val findCallbackNet = find(netStore, LONG_TIMEOUT)
        assertNotNull(findCallbackNet.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackNet.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListWithIdAuto() {
        print("should save an array of items with _id")
        // create an array with a few items that have _id property
        // save() with the array as param
        // find() using networkstore

        val personList = createPersonsList(true)

        val autoStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        val netStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoStore)
        clearBackend(netStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(autoStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result))

        val findCallbackNet = find(netStore, LONG_TIMEOUT)
        assertNotNull(findCallbackNet.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackNet.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListCombineWithIdAndWithoutIdAuto() {
        print("should combine POST and PUT requests for items with and without _id")
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array, should send POST multi-insert and PUT and return the items in their order from the original array, this one should be mocked to trace the requests.
        // find() with syncstore, should return the items along with metadata from the backend - ect, lmt
        // find() using networkstore, should return the items form step 1

        val personList = createCombineList()

        val mockNetManager = MockMultiInsertNetworkManager(MULTI_INSERT_COLLECTION, Person::class.java, client as Client)
        val autoStore = DataStore(MULTI_INSERT_COLLECTION, Person::class.java, client, StoreType.AUTO, mockNetManager)
        val syncStore = DataStore(MULTI_INSERT_COLLECTION, Person::class.java, client, StoreType.SYNC, mockNetManager)
        val netStore = DataStore(MULTI_INSERT_COLLECTION, Person::class.java, client, StoreType.NETWORK, mockNetManager)

        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        mockNetManager.clear()
        val saveCallback = saveList(autoStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result))
        assertTrue(mockNetManager.useMultiInsertSave)

        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result))

        val findCallbackNet = find(netStore, LONG_TIMEOUT)
        assertNotNull(findCallbackNet.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackNet.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListReturnErrorForEmptyListAuto() {
        print("should return an error for an empty array")
        // create an empty array
        // save() using the array
        // pendingSyncEntities()
        val list = ArrayList<Person>()
        testSaveEmptyList(list, Person::class.java, MULTI_INSERT_COLLECTION, StoreType.AUTO)

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        val pendingCount = syncItems?.size ?: 0
        //assertNotNull(syncItems);
        assertEquals(pendingCount.toLong(), list.size.toLong())
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testSaveListReturnErrorForInvalidCredentialsAuto() {
        print("should return an error when all items fail with multi-insert for invalid credentials")
        // create an array with a few items that have no _id property
        // set a collection permission to deny creating items
        // save() using the array from above
        // pendingSyncEntities()
        // find() using syncstore

        val entityList = createEntityList(2)

        val autoStore = DataStore.collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.SYNC, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        client?.syncManager?.clear(EntitySet.COLLECTION)

        val defaultKinveyListCallback = saveList(autoStore, entityList)
        assertNotNull(defaultKinveyListCallback.error)
        assertEquals(defaultKinveyListCallback.error?.javaClass, KinveyJsonResponseException::class.java)

        val syncItems = pendingSyncEntities(EntitySet.COLLECTION)
        assertNotNull(syncItems)
        assertTrue(checkSyncItems<GenericJson>(syncItems, entityList.size, SyncRequest.HttpVerb.POST))

        val findCallback = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkIfSameObjects(entityList, findCallback.result?.result, EntitySet.DESCRIPTION_KEY, false))
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testSaveListReturnErrorArrayForAllItemsFailAuto() {
        print("should return an array of errors for all items failing for different reasons")
        // create an array containing two items failing for different reasons
        // save using the array above
        // find using syncstore
        // pendingSyncEntities()
        val personList = createErrList()
        val checkIndexes = intArrayOf(0, 1)

        val autoStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(autoStore, personList)
        assertNotNull(saveCallback.error)
        if (saveCallback.error is KinveySaveBatchException) {
            val resultEntities = (saveCallback.error as KinveySaveBatchException).entities as List<Person>?
            val errorsList = (saveCallback.error as KinveySaveBatchException).errors
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexes, true))
            val errMessages = arrayOf(ERR_GEOLOC_MSG, ERR_GEOLOC_MSG)
            assertTrue(checkBatchResponseErrors(errorsList, checkIndexes, true, errMessages))
        }

        val findCallback = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result?.result))

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        assertTrue(checkSyncItems<GenericJson>(syncItems, personList.size, SyncRequest.HttpVerb.POST))
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testSaveListReturnErrorArrayForSomeItemsFailAuto() {
        print("should return an entities and errors when some requests fail and some succeed")
        // create an array of items with no _id and the second of them should have invalid _geoloc params
        // save using the array above, should return an array of entities where the second one is null, and an array of errors where the index of the one entry is 1
        // pendingSyncEntities(), should return the items that failed with their respective operations
        // find() using syncstore, should return all items from step 1 the successful items with valid ect and lmt properties and the failed one with local:true property
        // find() using networkstore, should return the successful items
        val personList = createErrList1()
        val checkIndexesSuccess = intArrayOf(0)
        val checkIndexesErr = intArrayOf(1)

        val autoStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        val netStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(autoStore, personList)
        assertNotNull(saveCallback.error)
        if (saveCallback.error is KinveySaveBatchException) {
            val resultEntities = (saveCallback.error as KinveySaveBatchException).entities as List<Person>?
            val errorsList = (saveCallback.error as KinveySaveBatchException).errors
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesSuccess, false))
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesErr, true))
            val errMessages = arrayOf(ERR_GEOLOC_MSG)
            assertTrue(checkBatchResponseErrors(errorsList, checkIndexesErr, true, errMessages))
        }

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        assertTrue(checkSyncItems<GenericJson>(syncItems, 1, SyncRequest.HttpVerb.POST))

        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result))

        val findCallbackNet = find(netStore, LONG_TIMEOUT)
        assertNotNull(findCallbackNet.result)
        assertTrue(checkIfPersonItemsAtRightIndex(personList, findCallbackNet.result?.result, checkIndexesSuccess, false))
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testSaveListReturnPutFailuresAtMatchingIndexAuto() {
        print("should return PUT failures at the matching index")
        // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
        // save using the array above
        // pendingSyncEntities()
        // find() using syncstore

        val personList = createErrListGeoloc()
        val checkIndexesSuccess = intArrayOf(1, 3)
        val checkIndexesErr = intArrayOf(0, 2)

        val autoStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        val saveCallback = saveList(autoStore, personList)
        assertNotNull(saveCallback.error)
        if (saveCallback.error is KinveySaveBatchException) {
            val resultEntities = (saveCallback.error as KinveySaveBatchException).entities as List<Person>?
            val errorsList = (saveCallback.error as KinveySaveBatchException).errors
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesSuccess, false))
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesErr, true))
            val errMessages = arrayOf(ERR_GEOLOC_MSG, ERR_GEOLOC_MSG)
            assertTrue(checkBatchResponseErrors(errorsList, checkIndexesErr, true, errMessages))
        }

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        syncItems?.let { sItems ->
            assertEquals(sItems[0].requestMethod, SyncRequest.HttpVerb.POST)
            assertEquals(sItems[1].requestMethod, SyncRequest.HttpVerb.PUT)
        }
        val findCallbackSync = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result as List<Person>?))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushItemsListWithoutIdAuto() {
        print("should use multi insert for multiple items without _id")
        // create an array of items without _id
        // save using the array above : mock to return connection error
        // push() : should return an array of the objects pushed with the operation performed
        // pendingSyncEntities() : should return empty array
        // find() using networkstore : should return the items from step 1
        val personsList = createPersonsList(false)

        val storeNet = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeNet)
        clearBackend(storeAuto)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        mockInvalidConnection()
        saveList(storeAuto, personsList)
        cancelMockInvalidConnection()

        val pushCallback = push(storeAuto, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(pushCallback.result?.successCount, personsList.count())

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertTrue(syncItems.isNullOrEmpty())

        val findCallback = find(storeNet, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushItemsListCombineWithIdAndWithoutIdAuto() {
        print("should combine POST and PUT requests for items with and without _id")
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array, mock to return connection error
        // push(), should use a multi-insert POST request and separate PUT requests for the items with _id. Should return and array of the items pushed with the respective operation - POST for no _id and PUT for _id
        // pendingSyncEntities(), should return empty array
        // find() using networkstore, should return the items from step 1
        val personsList = createCombineList()

        val autoSync = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        val netSync = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoSync)
        clearBackend(netSync)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        mockInvalidConnection()
        saveList(autoSync, personsList)
        cancelMockInvalidConnection()

        val pushCallback = push(autoSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertTrue(syncItems.isNullOrEmpty())

        val findCallback = find(netSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result?.result))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushItemsListCombineWithIdAndWithoutIdMockedAuto() {
        print("should combine POST and PUT requests for items with and without _id - mocked")
        // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no_id, _id]
        // save() using the array, mock to return connection error
        // push(), should use a multi-insert POST request and separate PUT requests for the items with _id. Should return and array of the items pushed with the respective operation - POST for no _id and PUT for _id
        // pendingSyncEntities(), should return empty array
        // find() using networkstore, should return the items from step 1
        val personsList = createCombineList()

        val mockNetManager = MockMultiInsertNetworkManager(MULTI_INSERT_COLLECTION, Person::class.java, client as Client)
        val autoSync = DataStore(MULTI_INSERT_COLLECTION, Person::class.java, client, StoreType.AUTO, mockNetManager)
        val netSync = DataStore(MULTI_INSERT_COLLECTION, Person::class.java, client, StoreType.NETWORK, mockNetManager)

        clearBackend(autoSync)
        clearBackend(netSync)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        mockInvalidConnection()
        saveList(autoSync, personsList)
        cancelMockInvalidConnection()

        val pushCallback = push(autoSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)
        assertTrue(mockNetManager.useMultiInsertSave)

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertTrue(syncItems.isNullOrEmpty())

        val findCallback = find(netSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result?.result, true))
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testPushItemsListReturnsErrorForEachItemAuto() {
        print("should return the failure reason in the result for each pushed item even if it is the same")
        // create an array of items without _id and set the collection permission for create to never
        // save() using the array
        // push(), should return the items from step 1 each with a POST operation and an error property with value: the invalid credentials error
        // pendingSyncEntities(), should return all items from step 1

        val entitySetList = createEntityList(2)
        val itemsErrorIndexes = intArrayOf(0, 1)

        val autoSync = DataStore.collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.AUTO, client)
        clearBackend(autoSync)
        client?.syncManager?.clear(EntitySet.COLLECTION)

        saveList(autoSync, entitySetList)

        val pushCallback = push(autoSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.error)
        if (pushCallback.error is KinveySaveBatchException) {
            val entityList = (pushCallback.error as KinveySaveBatchException).entities
            val errorsList = (pushCallback.error as KinveySaveBatchException).errors
            assertTrue(checkIfItemsAtRightIndex(entitySetList, entityList as List<GenericJson>,
                    itemsErrorIndexes, EntitySet.DESCRIPTION_KEY, true, false))
            val errMessages = arrayOf(ERR_PERMISSION_MSG, ERR_PERMISSION_MSG)
            assertTrue(checkBatchResponseErrors(errorsList, itemsErrorIndexes, true, errMessages))
        }

        val syncItems = pendingSyncEntities(EntitySet.COLLECTION)
        assertNotNull(syncItems)
        assertEquals(syncItems?.count(), entitySetList.count())
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testPushItemsListReturnsErrorsForEachItemErrorAuto() {
        print("should return the failure reason in the result for each pushed item when they are different")
        // create an array with 3 items without _id, two of which should be invalid for different reasons
        // save using the array above, mock to return connection error
        // push(), should return the items with respective POST and PUT operations and items which are set to fail should have their distinct errors
        // pendingSyncEntities(), should return the failing items

        val personsList = createPushErrList()

        val autoSync = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(autoSync)
        client?.syncManager?.clear(MULTI_INSERT_COLLECTION)

        mockInvalidConnection()
        saveList(autoSync, personsList)
        cancelMockInvalidConnection()

        val pushCallback = push(autoSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)

        val syncItems = pendingSyncEntities(MULTI_INSERT_COLLECTION)
        assertNotNull(syncItems)
        assertTrue(checkSyncItems<GenericJson>(syncItems, 2, SyncRequest.HttpVerb.PUT))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushShouldUseMultiInsertAfterSaveAuto() {
        print("should use multi-insert even if the items have not been created in an array")
        // save an item without _id
        // save another item without _id, mock to return connection error
        // push(), should use a multi-insert POST request and return the items with POST operations
        // pendingSyncEntities(), should return empty array
        // find() using networkstore, should return all items from step 1
        testPushMultiInsertSupport(StoreType.AUTO)
    }

    @Test
    @Ignore("Currently fail because of KCS bug: https://kinvey.atlassian.net/browse/BACK-3959")
    @Throws(InterruptedException::class)
    fun testSyncItemsListAuto() {
        print("test sync items list")
        // create an array of 3 items, the second of which has invalid _geoloc parameters
        // save() mocking connectivity error
        // Sync(), Should return error for not pushed item
        // pendingSyncEntities(), should return the item with invalid params
        // find() using networkstore, should return the valid items
        // find using syncstore, should return all items including the invalid one
        testSyncItemsList(true, StoreType.AUTO)
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
        val personList = createPersonsList(false)
        val personStore = DataStore.collection(MULTI_INSERT_COLLECTION, Person::class.java, storeType, client)
        clearBackend(personStore)
        client.syncManager.clear(MULTI_INSERT_COLLECTION)
        val saveCallback = createList(personStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        if (storeType == StoreType.SYNC) {
            saveCallback.result!!.entities?.forEach {
                assertTrue(it.id?.startsWith("temp")!!)
            }
            val pushCallback = push(personStore, DEFAULT_TIMEOUT)
            assertNull(pushCallback.error)
            assertNotNull(pushCallback.result)
        } else {
            saveCallback.result!!.entities?.forEach {
                assertTrue(!it.id?.startsWith("temp")!!)
            }
        }
        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertNotNull(findCallback.result!!.result)
        findCallback.result!!.result?.forEach {
            assertTrue(!it.id?.contains("temp")!!)
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
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result?.entities, false))
        saveCallback = createList(netStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.errors)
        assertNotNull(saveCallback.result?.errors?.get(0)?.description)
        assertNotNull(saveCallback.result?.errors?.get(0)?.debug)
    }

}

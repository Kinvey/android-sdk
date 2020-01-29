package com.kinvey.androidTest.store.datastore

import android.content.Context
import android.os.Message
import android.util.Log
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.async.AsyncPullRequest
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.callback.*
import com.kinvey.androidTest.model.*
import com.kinvey.androidTest.model.Person.Companion.LONG_NAME
import com.kinvey.androidTest.util.RealmCacheManagerUtil
import com.kinvey.androidTest.util.TableNameManagerUtil
import com.kinvey.java.Constants
import com.kinvey.java.Query
import com.kinvey.java.cache.ICache
import com.kinvey.java.cache.KinveyCachedClientCallback
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.model.KinveyPullResponse
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.query.AbstractQuery.SortOrder
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.dto.SyncRequest.HttpVerb
import io.realm.DynamicRealm
import io.realm.RealmObjectSchema
import io.realm.RealmSchema
import org.junit.After
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@SmallTest
class DataStoreTest() : BaseDataStoreTest() {

    @Test
    @Throws(InterruptedException::class)
    fun testSaveSync() {
        testSave(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveCache() {
        testSave(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveAuto() {
        testSave(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveNetwork() {
        testSave(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUpdateSync() {
        testUpdate(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUpdateCache() {
        testUpdate(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUpdateAuto() {
        testUpdate(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUpdateNetwork() {
        testUpdate(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUpdateSyncPush() {
        // Setup
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(store)
        client?.syncManager?.clear(COLLECTION)

        // Save an entity locally
        val person = createPerson(TEST_USERNAME)
        val callback = save(store, person)
        assertNotNull(callback)
        assertNotNull(callback.result)

        // Record the temporary Realm-generated ID
        val updatedPerson = callback.result
        val tempID = updatedPerson?.id

        // Push the local entity to the backend
        val pushCallback = push(store, 60)
        assertNotNull(pushCallback)

        // Find the item locally and verify that the permanent ID is in place
        val findCallback = find(store, 60)
        assertNotNull(findCallback)
        assertNotNull(findCallback.result)
        val readResponse = findCallback.result
        assertNotNull(readResponse)
        val people = readResponse?.result
        assertNotNull(people)
        assertEquals(1, people?.size)
        val permID = people?.get(0)?.id
        assertNotNull(permID)
        assertNotEquals(tempID, permID)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveItemLongCollectionNameNetwork() {
        val store = collection(Person.LONG_NAME, Person::class.java, StoreType.NETWORK, client)
        client?.syncManager?.clear(Person.LONG_NAME)
        val callback = save(store, createPerson(TEST_USERNAME))
        assertNotNull(callback.result)
        assertNotNull(callback.result?.username)
        assertNull(callback.error)
        assertTrue(callback.result?.username == TEST_USERNAME)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDateObject() {
        val store = collection(DateExample.COLLECTION, DateExample::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(DateExample.COLLECTION)
        val date = Date()
        val obj = DateExample("first", date)
        val startTime = date.time
        val timeFromEntity = obj.date?.time
        val callback = saveDate(store, obj)
        assertNotNull(callback.result)
        var query = client?.query()
        query = query?.equals(FIELD, "first")
        val kinveyListCallback = findDate(store, query, DEFAULT_TIMEOUT)
        deleteDate(store, query)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(startTime == timeFromEntity)
        assertTrue(kinveyListCallback.result?.result?.get(0)?.date?.time == startTime)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUpdatePersonArray() {
        val store = collection(PersonArray.COLLECTION, PersonArray::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(PersonArray.COLLECTION)
        val person = PersonArray()
        val callback = savePersonArray(store, person)
        assertNotNull(callback.result)
        val callbackFind = store.find()
        assertTrue(callbackFind?.result?.size ?: 0 > 0)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveItemLongCollectionNameLocally() {
        val store = collection(LONG_NAME, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(LONG_NAME)
        val callback = save(store, createPerson(TEST_USERNAME))
        assertNotNull(callback.result)
        assertNotNull(callback.result?.username)
        assertNull(callback.error)
        assertTrue(callback.result?.username == TEST_USERNAME)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun test56SymbolsInTableName() {
        val store = collection(LONG_NAME, Person56::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        client?.syncManager?.clear(LONG_NAME)
        val result = store.save(Person56())
        assertNotNull(result)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testALotSymbolsInListName() {
        val store = collection(LONG_NAME, PersonLongListName::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        client?.syncManager?.clear(LONG_NAME)
        val person = PersonLongListName()
        val list: MutableList<String> = ArrayList()
        list.add("Test1")
        list.add("Test2")
        person.list = list
        var result = store.save(person)
        assertNotNull(result)
        result = store.find(client?.query()?.equals(ID, result?.id))?.result?.get(0)
        assertNotNull(result)
        assertEquals(1, store.delete(result?.id ?: ""))
    }

    @Test
    fun testOver63SymbolsInListName() {
        try {
            collection(LONG_NAME, PersonOver63CharsInFieldName::class.java, StoreType.SYNC, client)
            assertFalse(true)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("limited to max 63 characters") == true)
        }
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCollectionWithLongClassName() {
        val store = collection(
                "LongClassNameLongClassNameLongClassNameLongClassNameLongClassName",
                LongClassNameLongClassNameLongClassNameLongClassNameLongClassName::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        client?.syncManager?.clear(Person.LONG_NAME)
        val result = store.save(LongClassNameLongClassNameLongClassNameLongClassNameLongClassName())
        assertNotNull(result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByIdSync() {
        testFindById(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByIdCache() {
        testFindById(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByIdAuto() {
        testFindById(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByIdNetwork() {
        testFindById(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByIdWithCacheCallback() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.CACHE.ttl)?.clear()
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        val personId = saveCallback.result?.id
        val findCallback = find(store, personId, LONG_TIMEOUT, object : KinveyCachedClientCallback<Person> {
            override fun onSuccess(result: Person?) {
                Log.d("testFindById: username ", result?.username)
            }
            override fun onFailure(error: Throwable?) {
                Log.d("testFindById: ", error?.message)
            }
        })
        assertNotNull(findCallback.result)
        assertNull(saveCallback.error)
        assertEquals(findCallback.result?.id, personId)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByIdForAutoType() {
        val store = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.AUTO.ttl)?.clear()
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        val personId = saveCallback.result?.id
        val findCallback = find(store, personId, LONG_TIMEOUT, null)
        assertNotNull(findCallback.result)
        assertNull(saveCallback.error)
        assertEquals(findCallback.result?.id, personId)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindPersonsAutoType() {
        val store = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.AUTO.ttl)?.clear()
        clearBackend(store)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val personSecond = createPerson(TEST_USERNAME_2)
        val saveSecondCallback = save(store, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val findCallback = find(store, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.result?.size, 2)
        clearBackend(store)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindPersonsCorrectDataAutoType() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val findCallbackAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 2)
        val findCallbackSync = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertEquals(findCallbackSync.result?.result?.size, 2)
        createAndSavePerson(storeNetwork, TEST_TEMP_USERNAME)
        val findCallbackAutoSec = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAutoSec.result)
        assertEquals(findCallbackAutoSec.result?.result?.size, 3)
        val findCallbackSyncSec = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallbackSyncSec.result)
        assertEquals(findCallbackSyncSec.result?.result?.size, 3)
        clearBackend(storeNetwork)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByQuerySync() {
        testFindByQuery(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByQueryCache() {
        testFindByQuery(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByQueryAuto() {
        testFindByQuery(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByQueryNetwork() {
        testFindByQuery(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindDataByQueryAuto() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        client?.syncManager?.clear(COLLECTION)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeNetwork, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 2
        val saveThirdCallback = save(storeNetwork, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        var query = client?.query()
        query = query?.equals("weight", 2)
        val findCallbackAuto = find(storeAuto, query, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 2)
        val findCallbackSync = find(storeSync, query, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertEquals(findCallbackSync.result?.result?.size, 2)
        clearBackend(storeNetwork)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindSortedDataDescendingAuto() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeNetwork)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        person.weight = 1
        val saveCallback = save(storeNetwork, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeNetwork, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 3
        val saveThirdCallback = save(storeNetwork, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        var query = client?.query()
        query = query?.addSort("weight", SortOrder.DESC)
        val findCallbackAuto = find(storeAuto, query, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 3)
        assertTrue(findCallbackAuto.result?.result?.get(0)?.weight == 3L && findCallbackAuto.result?.result?.get(2)?.weight == 1L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindSortedDataAscendingAuto() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeNetwork)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        person.weight = 1
        val saveCallback = save(storeNetwork, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeNetwork, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 3
        val saveThirdCallback = save(storeNetwork, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        var query = client?.query()
        query = query?.addSort("weight", SortOrder.ASC)
        val findCallbackAuto = find(storeAuto, query, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 3)
        assertTrue(findCallbackAuto.result?.result?.get(0)?.weight == 1L && findCallbackAuto.result?.result?.get(2)?.weight == 3L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteFromCacheAfterBackend() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(storeAuto, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        createAndSavePerson(storeAuto, TEST_USERNAME_2)
        val userId = saveCallback.result?.id
        val deleteCallback = delete(storeAuto, userId, DEFAULT_TIMEOUT)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        val findCallbackAuto = find(storeAuto, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 1)
        assertEquals(findCallbackAuto.result?.result?.get(0)?.username, TEST_USERNAME_2)
        val findCallbackSync = find(storeSync, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertEquals(findCallbackSync.result?.result?.size, 1)
        assertEquals(findCallbackSync.result?.result?.get(0)?.username, TEST_USERNAME_2)
        clearBackend(storeNetwork)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindDataSkipLimitAuto() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        client?.syncManager?.clear(COLLECTION)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        createAndSavePerson(storeNetwork, TEST_TEMP_USERNAME)
        val query = client?.query()
        query?.setSkip(1)?.setLimit(1)
        val findCallbackAuto = find(storeAuto, query, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 1)
        assertEquals(findCallbackAuto.result?.result?.get(0)?.username, TEST_USERNAME_2)
        val findCallbackSync = find(storeSync, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertEquals(findCallbackSync.result?.result?.size, 1)
        assertEquals(findCallbackSync.result?.result?.get(0)?.username, TEST_USERNAME_2)
        clearBackend(storeNetwork)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindDataInvalidQueryAuto() { //FIND
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeAuto)
        val query = client?.query()
        query?.setQueryString("{{test")
        val findCallbackAuto = find(storeAuto, query, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackAuto.error)
        assertNull(findCallbackAuto.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLocalDataNoConnectionAuto() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val findCallbackAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 2)
        createAndSavePerson(storeNetwork, TEST_TEMP_USERNAME)
        mockInvalidConnection()
        val findCallbackSecondAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackSecondAuto.result)
        assertEquals(findCallbackSecondAuto.result?.result?.size, 2)
        cancelMockInvalidConnection()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSortedDataNoConnectionAuto() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        person.weight = 1
        val saveCallback = save(storeNetwork, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeNetwork, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 3
        val saveThirdCallback = save(storeNetwork, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        val findCallbackAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 3)
        mockInvalidConnection()
        var query = client?.query()
        query = query?.addSort("weight", SortOrder.ASC)
        val findCallbackAutoSecond = find(storeAuto, query, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackAutoSecond.result)
        assertEquals(findCallbackAutoSecond.result?.result?.size, 3)
        assertTrue(findCallbackAutoSecond.result?.result?.get(0)?.weight == 1L
                && findCallbackAutoSecond.result?.result?.get(2)?.weight == 3L)
        cancelMockInvalidConnection()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDataNoAccessForCollection() { //FIND
        val storeAuto = collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.AUTO, client)
        val findCallbackAuto = findEntitySet(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAuto.error)
        assertEquals(findCallbackAuto.error?.javaClass, KinveyJsonResponseException::class.java)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDataWithLimitAndSkipNoConnectionAuto() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        createAndSavePerson(storeNetwork, USERNAME)
        val findCallbackAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 3)
        mockInvalidConnection()
        val query = client?.query()
        query?.setSkip(1)?.setLimit(1)
        val findCallbackAutoSecond = find(storeAuto, query, DEFAULT_TIMEOUT)
        assertNotNull(findCallbackAutoSecond.result)
        assertEquals(findCallbackAutoSecond.result?.result?.size, 1)
        assertEquals(findCallbackAutoSecond.result?.result?.get(0)?.username, TEST_USERNAME_2)
        cancelMockInvalidConnection()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLocalDataNoConnectionAutoEliminated() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val findCallbackAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 2)
        createAndSavePerson(storeNetwork, TEST_TEMP_USERNAME)
        mockInvalidConnection()
        val findCallbackSecondAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackSecondAuto.result)
        assertEquals(findCallbackSecondAuto.result?.result?.size, 2)
        cancelMockInvalidConnection()
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        val findCallbackAutoThird = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAutoThird.result)
        assertEquals(findCallbackAutoThird.result?.result?.size, 4)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveNoAccess() { //SAVE
        val storeAuto = collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.AUTO, client)
        val defaultKinveyEntityCallback = saveEntitySet(storeAuto, EntitySet())
        assertNotNull(defaultKinveyEntityCallback.error)
        //assertEquals(defaultKinveyEntityCallback.error.getClass(), KinveyJsonResponseException.class);
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveConnectErrorAuto() { //SAVE
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeAuto)
        clearBackend(storeSync)
        mockInvalidConnection()
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(storeAuto, person)
        assertNotNull(saveCallback.result)
        val findCallback = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
        cancelMockInvalidConnection()
        val syncItems = pendingSyncEntities(COLLECTION)
        assertTrue(syncItems?.size == 1)
        assertEquals(syncItems?.get(0)?.requestMethod, HttpVerb.POST)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQueriedDataNoConnectionAuto() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeAuto, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 2
        val saveThirdCallback = save(storeAuto, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        val findCallbackAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 3)
        var query = client?.query()
        query = query?.equals("weight", 2)
        mockInvalidConnection()
        val findCallbackAutoQuery = find(storeAuto, query, LONG_TIMEOUT)
        assertNotNull(findCallbackAutoQuery.result)
        assertEquals(findCallbackAutoQuery.result?.result?.size, 2)
        cancelMockInvalidConnection()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeltasetAuto() { //FIND
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        client?.syncManager?.clear(COLLECTION)
        storeAuto.isDeltaSetCachingEnabled = true
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val findCallbackAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 2)
        createAndSavePerson(storeNetwork, TEST_TEMP_USERNAME)
        val findCallbackSecondAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackSecondAuto.result)
        assertEquals(findCallbackSecondAuto.result?.result?.size, 3)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        val findCallbackThirdAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackThirdAuto.result)
        assertEquals(findCallbackThirdAuto.result?.result?.size, 4)
        val findCallbackFourthAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackFourthAuto.result)
        assertEquals(findCallbackFourthAuto.result?.result?.size, 4)
        val findCallbackSync = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertEquals(findCallbackSync.result?.result?.size, 4)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRemoveIdBothStoreAuto() { //REMOVEBYID
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        val saveSecondCallback = save(storeAuto, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val userId = saveSecondCallback.result?.id
        val deleteCallback = delete(storeNetwork, userId, DEFAULT_TIMEOUT)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        val deleteCallbackAuto = delete(storeAuto, userId, DEFAULT_TIMEOUT)
        assertNotNull(deleteCallbackAuto.error)
        val findCallbackAuto = find(storeSync, userId, DEFAULT_TIMEOUT, null)
        assertNotNull(findCallbackAuto.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRemoveByNotExistingIdAuto() { //REMOVEBYID
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val deleteCallback = delete(storeAuto, "notexist", DEFAULT_TIMEOUT)
        assertNotNull(deleteCallback.error)
        assertNull(deleteCallback.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRemoveByIdAuto() { //REMOVEBYID
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        val saveSecondCallback = save(storeAuto, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val id = saveSecondCallback.result?.id
        val deleteCallback = delete(storeAuto, id, DEFAULT_TIMEOUT)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        val findCallbackAuto = find(storeNetwork, id, DEFAULT_TIMEOUT, null)
        assertNull(findCallbackAuto.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRemoveByIdErrorAuto() { //REMOVEBYID
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(storeAuto, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val id = saveCallback.result?.id
        mockInvalidConnection()
        val deleteCallback = delete(storeAuto, id, DEFAULT_TIMEOUT)
        assertNotNull(deleteCallback.error)
        assertNull(deleteCallback.result)
        val syncItems = pendingSyncEntities(COLLECTION)
        assertNotNull(syncItems)
        assertTrue(syncItems?.size == 1)
        assertEquals(syncItems?.get(0)?.requestMethod, HttpVerb.DELETE)
        val findCallback = find(storeSync, id, DEFAULT_TIMEOUT, null)
        assertNotNull(findCallback.result)
        cancelMockInvalidConnection()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRemoveQuerySyncQueueAuto() { //REMOVE
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeAuto, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 2
        val saveThirdCallback = save(storeAuto, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        var query = client?.query()
        query = query?.equals("weight", 2)
        mockInvalidConnection()
        val deleteCallback = delete(storeAuto, query)
        assertNotNull(deleteCallback.error)
        assertNull(deleteCallback.result)
        val syncItems = pendingSyncEntities(COLLECTION)
        assertNotNull(syncItems)
        assertTrue(syncItems?.size == 2)
        cancelMockInvalidConnection()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRemoveBothStoreAuto() { //REMOVE
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeAuto, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val userId = saveSecondCallback.result?.id
        var query = client?.query()
        query = query?.equals("weight", 2)
        val deleteCallback = delete(storeNetwork, query)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        val deleteCallbackAuto = delete(storeAuto, query)
        assertNotNull(deleteCallbackAuto.result)
        assertTrue(deleteCallbackAuto.result == 0)
        val findCallbackAuto = find(storeSync, userId, DEFAULT_TIMEOUT, null)
        assertNull(findCallbackAuto.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRemoveQueryAuto() { //REMOVE
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeAuto, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 2
        val saveThirdCallback = save(storeAuto, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        var query = client?.query()
        query = query?.equals("weight", 2)
        val deleteCallback = delete(storeAuto, query)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        val findCallback = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.result?.size, 1)
        val findCallbackSync = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertEquals(findCallbackSync.result?.result?.size, 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRemoveQueryNoMatchedAuto() { //REMOVE
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        var query = client?.query()
        query = query?.equals("weight", 3)
        val deleteCallback = delete(storeAuto, query)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        assertNotNull(deleteCallback.result == 0)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRemoveBothQueryAuto() { //REMOVE
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeAuto, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val userId = saveSecondCallback.result?.id
        var query = client?.query()
        query = query?.equals("weight", 2)
        val deleteCallback = delete(storeSync, query)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        val deleteCallbackAuto = delete(storeAuto, query)
        assertNull(deleteCallbackAuto.error)
        assertNotNull(deleteCallbackAuto.result)
        val findCallbackAuto = find(storeAuto, userId, DEFAULT_TIMEOUT, null)
        assertNull(findCallbackAuto.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testRemoveDataInvalidQueryAuto() { //REMOVE
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeAuto)
        val query = client?.query()
        query?.setQueryString("{{test")
        val deleteCallback = delete(storeAuto, query)
        assertNotNull(deleteCallback.error)
        assertNull(deleteCallback.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveWithoutIdAuto() { //SAVE
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeAuto)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        val findCallback = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
        assertTrue(findCallback.result?.result?.get(0)?.id != null)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveWithIdAuto() { //SAVE
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeAuto)
        val id = "123456"
        val person = createPerson(TEST_USERNAME)
        person.id = id
        val saveCallback = save(storeAuto, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertEquals(saveCallback.result?.id, id)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveUpdateAuto() { //SAVE
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeAuto)
        val id = "123456"
        val person = createPerson(TEST_USERNAME)
        person.id = id
        val saveCallback = save(storeAuto, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertEquals(saveCallback.result?.id, id)
        person.height = 2f
        val saveCallbackSecond = save(storeAuto, person)
        assertNotNull(saveCallbackSecond.result)
        assertNull(saveCallbackSecond.error)
        val findCallback = find(storeNetwork, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPendingSyncAuto() { //PENDINGSYNCCOUNT
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        createAndSavePerson(storeSync, TEST_USERNAME)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        assertNotNull(pendingSyncEntities(COLLECTION))
        assertEquals(pendingSyncEntities(COLLECTION)?.size, 3)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPendingSyncItemsAuto() { //PENDINGSYNCENTITIES
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        createAndSavePerson(storeSync, TEST_USERNAME)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        assertNotNull(pendingSyncEntities(COLLECTION))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testClearSyncAuto() { //CLEARSYNC
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        createAndSavePerson(storeSync, TEST_USERNAME)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        storeAuto.clear()
        assertNull(pendingSyncEntities(COLLECTION))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testClearQuerySyncAuto() { //CLEARSYNC
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        createAndSavePerson(storeSync, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeSync, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 2
        val saveThirdCallback = save(storeSync, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        var query = client?.query()
        query = query?.equals("weight", 2) as Query
        storeAuto.clear(query)
        assertNotNull(pendingSyncEntities(COLLECTION))
        assertEquals(pendingSyncEntities(COLLECTION)?.size, 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testClearAllQueueAuto() { //CLEAR
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        createAndSavePerson(storeAuto, TEST_USERNAME_2)
        storeAuto.clear()
        assertNull(pendingSyncEntities(COLLECTION))
        val findCallbackSync = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertTrue(findCallbackSync.result?.result?.size == 0)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testClearQueryQueueAuto() { //CLEAR
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeAuto, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 2
        val saveThirdCallback = save(storeAuto, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        val findCallbackSync = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        val resultList = findCallbackSync.result?.result
        resultList?.forEach {person ->
            person.height = 3f
            val save = save(storeSync, person)
            assertNotNull(save.result)
            assertNull(save.error)
        }
        var query = client?.query()
        query = query?.equals("weight", 2) as Query
        storeAuto.clear(query)
        assertTrue(pendingSyncEntities(COLLECTION) != null)
        assertTrue(pendingSyncEntities(COLLECTION)?.size == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testClearQueryAuto() { //CLEAR
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeAuto, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 2
        val saveThirdCallback = save(storeAuto, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        var query = client?.query()
        query = query?.equals("weight", 2) as Query
        storeAuto.clear(query)
        val findCallbackNetwork = find(storeNetwork, LONG_TIMEOUT)
        assertNotNull(findCallbackNetwork.result)
        assertEquals(findCallbackNetwork.result?.result?.size, 3)
        val findCallbackSync = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertEquals(findCallbackSync.result?.result?.size, 1)
        assertTrue(findCallbackSync.result?.result?.get(0)?.weight != 2L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testClearAuto() { //CLEAR
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeAuto, TEST_USERNAME)
        createAndSavePerson(storeAuto, TEST_USERNAME_2)
        storeAuto.clear()
        val findCallbackNetwork = find(storeNetwork, LONG_TIMEOUT)
        assertNotNull(findCallbackNetwork.result)
        assertEquals(findCallbackNetwork.result?.result?.size, 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncAuto() { //SYNC
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeSync, TEST_USERNAME)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val syncCallback = sync(storeAuto, DEFAULT_TIMEOUT)
        assertNotNull(syncCallback.kinveyPushResponse)
        assertEquals(syncCallback.kinveyPushResponse?.successCount, 3)
        assertNotNull(syncCallback.kinveyPullResponse)
        assertEquals(syncCallback.kinveyPullResponse?.count, 5)
        val findCallbackSync = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallbackSync.result)
        assertEquals(findCallbackSync.result?.result?.size, 5)
        val findCallbackNetwork = find(storeNetwork, LONG_TIMEOUT)
        assertNotNull(findCallbackNetwork.result)
        assertEquals(findCallbackNetwork.result?.result?.size, 5)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncQueryAuto() { //SYNC
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeSync, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeSync, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 2
        val saveThirdCallback = save(storeSync, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        val personFouth = createPerson(TEST_TEMP_USERNAME)
        personFouth.weight = 2
        val saveFouthCallback = save(storeNetwork, personFouth)
        assertNotNull(saveFouthCallback.result)
        assertNull(saveFouthCallback.error)
        var query = Query()
        query = query.equals("weight", 2)
        val syncCallback = sync(storeAuto, query, DEFAULT_TIMEOUT)
        assertNotNull(syncCallback.kinveyPushResponse)
        assertEquals(syncCallback.kinveyPushResponse?.successCount, 3)
        assertNotNull(syncCallback.kinveyPullResponse)
        assertEquals(syncCallback.kinveyPullResponse?.count, 3)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncNoConnection() { //SYNC
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeSync, TEST_USERNAME)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        mockInvalidConnection()
        val syncCallback = sync(storeAuto, DEFAULT_TIMEOUT)
        assertNotNull(syncCallback.kinveyPushResponse)
        assertEquals(syncCallback.kinveyPushResponse?.successCount, 2)
        assertNull(syncCallback.kinveyPullResponse)
        assertNotNull(syncCallback.error)
        cancelMockInvalidConnection()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullAfterDeleteAuto() { //PULL
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(storeNetwork, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val userId = saveCallback.result?.id
        val personSecond = createPerson(TEST_USERNAME_2)
        val saveCallbackSecond = save(storeNetwork, personSecond)
        assertNotNull(saveCallbackSecond.result)
        assertNull(saveCallbackSecond.error)
        val userIdSecond = saveCallbackSecond.result?.id
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val pullCallback = pull(storeAuto, null)
        assertNotNull(pullCallback.result)
        assertEquals(pullCallback.result?.count, 4)
        val deleteCallback = delete(storeNetwork, userId, DEFAULT_TIMEOUT)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        val deleteCallbackSecond = delete(storeNetwork, userIdSecond, DEFAULT_TIMEOUT)
        assertNull(deleteCallbackSecond.error)
        assertNotNull(deleteCallbackSecond.result)
        val pullCallbackSecond = pull(storeAuto, null)
        assertNotNull(pullCallbackSecond.result)
        assertEquals(pullCallbackSecond.result?.count, 2)
        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.result?.size, 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullBeforePushAuto() { //PULL
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeSync, TEST_USERNAME)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        val pullCallback = pull(storeAuto, null)
        assertNull(pullCallback.result)
        assertNotNull(pullCallback.error)
        assertTrue(pullCallback.error?.message?.contains("You must push all pending sync items before new data is pulled") == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullAuto() { //PULL
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val pullCallback = pull(storeAuto, null)
        assertNotNull(pullCallback.result)
        assertEquals(pullCallback.result?.count, 2)
        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.result?.size, 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullNoConnectionAuto() { //PULL
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        mockInvalidConnection()
        val pullCallback = pull(storeAuto, null)
        assertNull(pullCallback.result)
        assertNotNull(pullCallback.error)
        assertEquals(pullCallback.error?.javaClass, UnknownHostException::class.java)
        cancelMockInvalidConnection()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullAfterDeleteAndUpdateAuto() { //PULL

        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        var countUpdatedItems = 0
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(storeNetwork, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val userId = saveCallback.result?.id
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val pullCallback = pull(storeAuto, null)
        assertNotNull(pullCallback.result)
        assertEquals(pullCallback.result?.count, 3)
        val kinveyReadCallback = find(storeSync, LONG_TIMEOUT)
        val personFirst = kinveyReadCallback.result?.result?.get(1)
        personFirst?.weight = 15
        val saveFirst = save(storeNetwork, personFirst!!)
        assertNotNull(saveFirst.result)
        assertNull(saveFirst.error)
        val deleteCallback = delete(storeNetwork, userId, DEFAULT_TIMEOUT)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        val pullCallbackSecond = pull(storeAuto, null)
        assertNotNull(pullCallbackSecond.result)
        assertEquals(pullCallbackSecond.result?.count, 2)
        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.result?.size, 2)
        findCallback.result?.result?.forEach { personFollowing ->
            if (personFollowing.weight == 15L) {
                countUpdatedItems++
            }
        }
        assertTrue(countUpdatedItems == 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullUpdateAuto() { //PULL

        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        var countUpdatedItems = 0
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        createAndSavePerson(storeNetwork, TEST_TEMP_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        val pullCallback = pull(storeAuto, null)
        assertNotNull(pullCallback.result)
        assertEquals(pullCallback.result?.count, 4)
        val kinveyReadCallback = find(storeSync, LONG_TIMEOUT)
        val personFirst = kinveyReadCallback.result?.result?.get(0)
        personFirst?.weight = 15
        val personSecond = kinveyReadCallback.result?.result?.get(1)
        personSecond?.weight = 15
        val saveFirst = save(storeNetwork, personFirst!!)
        assertNotNull(saveFirst.result)
        assertNull(saveFirst.error)
        val saveSecond = save(storeNetwork, personSecond!!)
        assertNotNull(saveSecond.result)
        assertNull(saveSecond.error)
        val pullCallbackSecond = pull(storeAuto, null)
        assertNotNull(pullCallbackSecond.result)
        assertEquals(pullCallbackSecond.result?.count, 4)
        val kinveyReadCallbackSecond = find(storeSync, LONG_TIMEOUT)
        kinveyReadCallbackSecond.result?.result?.forEach { person ->
            if (person.weight == 15L) { countUpdatedItems++ }
        }
        assertTrue(countUpdatedItems == 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullAutopaginationAuto() { //PULL
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        createAndSavePerson(storeNetwork, TEST_TEMP_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        val pullCallback = pull(storeAuto, null, 2)
        assertNotNull(pullCallback.result)
        assertEquals(pullCallback.result?.count, 4)
        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.result?.size, 4)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullQueryAuto() { //PULL
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeNetwork, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        val personThird = createPerson(TEST_TEMP_USERNAME)
        personThird.weight = 2
        val saveThirdCallback = save(storeNetwork, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        var query = Query()
        query = query.equals("weight", 2)
        val pullCallback = pull(storeAuto, query)
        assertNotNull(pullCallback.result)
        assertEquals(pullCallback.result?.count, 2)
        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.result?.size, 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullNoAccessForCollection() { //PULL
        val storeAuto = collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.AUTO, client)
        val pullCallback = pullEntitySet(storeAuto, null)
        assertNotNull(pullCallback.error)
        assertEquals(pullCallback.error?.javaClass, KinveyJsonResponseException::class.java)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushRecreateAuto() { //PUSH
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeSync, TEST_USERNAME)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        val pushCallback = push(storeAuto, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(pushCallback.result!!.successCount, 2)
        val kinveyReadCallback = find(storeSync, LONG_TIMEOUT)
        var userId: String? = null
        kinveyReadCallback.result?.result?.forEach { personFollowing ->
            if (personFollowing.username == TEST_USERNAME) {
                personFollowing.weight = 15
                userId = personFollowing.id
                val saveCallbackSecond = save(storeSync, personFollowing)
                assertNotNull(saveCallbackSecond.result)
                assertNull(saveCallbackSecond.error)
            }
        }
        val deleteCallback = delete(storeNetwork, userId, DEFAULT_TIMEOUT)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        val pushCallbackSecond = push(storeAuto, LONG_TIMEOUT)
        assertNotNull(pushCallbackSecond.result)
        assertEquals(pushCallbackSecond.result?.successCount, 1)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushAuto() { //PUSH
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeSync, TEST_USERNAME)
        createAndSavePerson(storeSync, TEST_USERNAME_2)
        val pushCallback = push(storeAuto, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(pushCallback.result?.successCount, 2)
        val findCallback = find(storeNetwork, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.result?.size, 2)
        assertEquals(pendingSyncEntities(COLLECTION), null)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushUpdateAuto() { //PUSH
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val pullCallback = pull(storeAuto, null)
        assertNotNull(pullCallback.result)
        assertEquals(pullCallback.result?.count, 2)
        val kinveyReadCallback = find(storeSync, LONG_TIMEOUT)
        val personFirst = kinveyReadCallback.result?.result?.get(0)
        personFirst?.weight = 15
        val saveCallback = save(storeSync, personFirst!!)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val pushCallback = push(storeAuto, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(pushCallback.result?.successCount, 1)
        val findCallback = find(storeNetwork, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.result?.size, 2)
        assertEquals(pendingSyncEntities(COLLECTION), null)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushDeleteAuto() { //PUSH
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(storeNetwork, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val userId = saveCallback.result?.id
        val personSecond = createPerson(TEST_USERNAME_2)
        val saveCallbackSecond = save(storeNetwork, personSecond)
        assertNotNull(saveCallbackSecond.result)
        assertNull(saveCallbackSecond.error)
        val userIdSecond = saveCallbackSecond.result?.id
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        val pullCallback = pull(storeAuto, null)
        assertNotNull(pullCallback.result)
        assertEquals(pullCallback.result?.count, 3)
        val deleteCallback = delete(storeSync, userId, DEFAULT_TIMEOUT)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        val deleteCallbackSecond = delete(storeSync, userIdSecond, DEFAULT_TIMEOUT)
        assertNull(deleteCallbackSecond.error)
        assertNotNull(deleteCallbackSecond.result)
        val pushCallback = push(storeAuto, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(pushCallback.result?.successCount, 2)
        val findCallback = find(storeNetwork, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.result?.size, 1)
        assertEquals(pendingSyncEntities(COLLECTION), null)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindIdItemAuto() { //FINDBYID
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(storeAuto, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        createAndSavePerson(storeAuto, TEST_USERNAME_2)
        val userId = saveCallback.result?.id
        val findCallbackAuto = find(storeAuto, userId, DEFAULT_TIMEOUT, null)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.username, TEST_USERNAME)
        val findCallbackAutoSecond = find(storeSync, userId, DEFAULT_TIMEOUT, null)
        assertNotNull(findCallbackAutoSecond.result)
        assertEquals(findCallbackAutoSecond.result?.username, TEST_USERNAME)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindIdDeleteItemAuto() { //FINDBYID
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(storeAuto, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        createAndSavePerson(storeAuto, TEST_USERNAME_2)
        val userId = saveCallback.result?.id
        val findCallback = find(storeSync, userId, DEFAULT_TIMEOUT, null)
        assertNotNull(findCallback.result)
        assertEquals(findCallback.result?.username, TEST_USERNAME)
        val deleteCallbackSecond = delete(storeAuto, userId, DEFAULT_TIMEOUT)
        assertNull(deleteCallbackSecond.error)
        assertNotNull(deleteCallbackSecond.result)
        val findCallbackAuto = find(storeAuto, userId, DEFAULT_TIMEOUT, null)
        assertNull(findCallbackAuto.result)
        val findCallbackSync = find(storeSync, userId, DEFAULT_TIMEOUT, null)
        assertNull(findCallbackSync.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindWithoutSpecifyingIdAuto() { //FINDBYID
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val userId = "test"
        val findCallbackAuto = find(storeAuto, userId, DEFAULT_TIMEOUT, null)
        assertNull(findCallbackAuto.result)
        assertNotNull(findCallbackAuto.error)
        assertTrue(findCallbackAuto.error?.message?.contains("EntityNotFound") == true)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindIdNoAccessForCollection() { //FINDBYID
        val storeAuto = collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.AUTO, client)
        val findCallbackAuto = findIdEntitySet(storeAuto, "testId", LONG_TIMEOUT)
        assertNotNull(findCallbackAuto.error)
        assertEquals(findCallbackAuto.error?.javaClass, KinveyJsonResponseException::class.java)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testLocalDataByIdNoConnectionAuto() { //FINDBYID
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(storeNetwork, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val userId = saveCallback.result?.id
        val findCallbackAuto = find(storeAuto, userId, DEFAULT_TIMEOUT, null)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.username, TEST_USERNAME)
        mockInvalidConnection()
        val findCallbackAutoSecond = find(storeAuto, userId, DEFAULT_TIMEOUT, null)
        assertNotNull(findCallbackAutoSecond.result)
        assertEquals(findCallbackAutoSecond.result?.username, TEST_USERNAME)
        cancelMockInvalidConnection()
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCountAllItemsAuto() { //COUNT
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeNetwork)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        assertTrue(storeAuto.count() == 2)
        createAndSavePerson(storeNetwork, TEST_TEMP_USERNAME)
        assertTrue(storeAuto.count() == 3)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCountAllQueriedItemsAuto() { //COUNT
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeNetwork)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        val personSecond = createPerson(TEST_USERNAME_2)
        personSecond.weight = 2
        val saveSecondCallback = save(storeAuto, personSecond)
        assertNotNull(saveSecondCallback.result)
        assertNull(saveSecondCallback.error)
        var query = client?.query()
        query = query?.equals("weight", 2) as Query
        assertTrue(storeAuto.count(null, query) == 1)
        val personThird = createPerson(TEST_USERNAME_2)
        personThird.weight = 2
        val saveThirdCallback = save(storeAuto, personThird)
        assertNotNull(saveThirdCallback.result)
        assertNull(saveThirdCallback.error)
        assertTrue(storeAuto.count() == 3)
        assertTrue(storeAuto.count(null, query) == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCountLocallyStoredNoConnectionAuto() { //COUNT
        val storeNetwork = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val storeSync = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeNetwork)
        clearBackend(storeSync)
        client?.syncManager?.clear(COLLECTION)
        createAndSavePerson(storeNetwork, TEST_USERNAME)
        createAndSavePerson(storeNetwork, TEST_USERNAME_2)
        val findCallbackAuto = find(storeAuto, LONG_TIMEOUT)
        assertNotNull(findCallbackAuto.result)
        assertEquals(findCallbackAuto.result?.result?.size, 2)
        createAndSavePerson(storeNetwork, TEST_TEMP_USERNAME)
        mockInvalidConnection()
        assertTrue(storeAuto.count() == 2)
        cancelMockInvalidConnection()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCountNoAccessForCollection() { //COUNT
        val storeAuto = collection(EntitySet.COLLECTION, EntitySet::class.java, StoreType.AUTO, client)
        var exception: Exception? = null
        try {
            storeAuto.count()
        } catch (e: Exception) {
            exception = e
        }
        assertNotNull(exception)
        assertEquals(exception?.javaClass, KinveyJsonResponseException::class.java)
    }

    @Test
    fun testMongoQueryStringBuilder() {
        // Arrange
        val store = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        var myQuery = client?.query()
        var expectedMongoQuery: String
        var mongoQuery: String?

        // Act
        // Assert

        // Test field string value
        myQuery = client?.query()
        myQuery?.equals("testString", "a test")
        expectedMongoQuery = "{\"testString\":\"a test\"}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test field boolean value
        myQuery = client?.query()
        myQuery?.equals("testbool", true)
        expectedMongoQuery = "{\"testbool\":true}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test field int value
        myQuery = client?.query()
        myQuery?.equals("testint", 33)
        expectedMongoQuery = "{\"testint\":33}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)
        val ttl = 120
        myQuery = client?.query()
        myQuery?.equals("ttl_in_seconds", ttl)
        expectedMongoQuery = "{\"ttl_in_seconds\":120}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test field long value
        myQuery = client?.query()
        myQuery?.equals("testlong", 34L)
        expectedMongoQuery = "{\"testlong\":34}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test field double value
        myQuery = client?.query()
        myQuery?.equals("testdouble", 34.0)
        expectedMongoQuery = "{\"testdouble\":34.0}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test field float value
        myQuery = client?.query()
        myQuery?.equals("testfloat", 34.0f)
        expectedMongoQuery = "{\"testfloat\":34.0}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test field null value
        myQuery = client?.query()
        myQuery?.equals("testnull", null)
        expectedMongoQuery = "{\"testnull\":null}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test $ne operator
        myQuery = client?.query()
        myQuery?.notEqual("age", "100500")
        expectedMongoQuery = "{\"age\":{\"\$ne\":\"100500\"}}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test $in operator - string
        myQuery = client?.query()
        myQuery?.`in`("testIn", arrayOf("1", "2", "3"))
        expectedMongoQuery = "{\"testIn\":{\"\$in\":[\"1\",\"2\",\"3\"]}}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test $in operator - bool
        myQuery = client?.query()
        myQuery?.`in`("testIn", arrayOf(true, false, true))
        expectedMongoQuery = "{\"testIn\":{\"\$in\":[true,false,true]}}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test $in operator - int
        myQuery = client?.query()
        myQuery?.`in`("testIn", arrayOf(1, 2, 3))
        expectedMongoQuery = "{\"testIn\":{\"\$in\":[1,2,3]}}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test $in operator - long
        myQuery = client?.query()
        myQuery?.`in`("testIn", arrayOf(1L, 2L, 3L))
        expectedMongoQuery = "{\"testIn\":{\"\$in\":[1,2,3]}}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test $in operator - float
        myQuery = client?.query()
        myQuery?.`in`("testIn", arrayOf(1.0f, 2.0f, 3.0f))
        expectedMongoQuery = "{\"testIn\":{\"\$in\":[1.0,2.0,3.0]}}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // Test $in operator - double
        myQuery = client?.query()
        myQuery?.`in`("testIn", arrayOf(1.1, 2.2, 3.3))
        expectedMongoQuery = "{\"testIn\":{\"\$in\":[1.1,2.2,3.3]}}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // $and query with 2 string values
        myQuery = client?.query()
        myQuery?.equals("testStr1", "test 1")?.and(client?.query()?.equals("testStr2", "test 2"))
        expectedMongoQuery = "{\"\$and\":[{\"testStr1\":\"test 1\"},{\"testStr2\":\"test 2\"}]}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // $and query with 2 boolean values
        myQuery = client?.query()
        myQuery?.equals("testBool1", true)?.and(client?.query()?.equals("testBool2", false))
        expectedMongoQuery = "{\"\$and\":[{\"testBool1\":true},{\"testBool2\":false}]}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // $and query with 2 int values
        myQuery = client?.query()
        myQuery?.equals("testInt1", 33)?.and(client?.query()?.equals("testInt2", 23))
        expectedMongoQuery = "{\"\$and\":[{\"testInt1\":33},{\"testInt2\":23}]}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // $and query with null value
        myQuery = client?.query()
        val hospitalCode = "H1"
        myQuery?.equals("hospitalCode", hospitalCode)?.and(client?.query()?.equals("archived", null))
        expectedMongoQuery = "{\"\$and\":[{\"hospitalCode\":\"H1\"},{\"archived\":null}]}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // $and query with null value and boolean
        myQuery = client?.query()
        val isHospital = false
        myQuery?.equals("isHospital", isHospital)?.and(client?.query()?.equals("archived", null))
        expectedMongoQuery = "{\"\$and\":[{\"isHospital\":false},{\"archived\":null}]}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)

        // implicit $and equals query
        myQuery = client?.query()
        myQuery?.equals("city", "Boston")
        myQuery?.equals("age", "21")
        expectedMongoQuery = "{\"city\":\"Boston\",\"age\":\"21\"}"
        mongoQuery = myQuery?.getQueryFilterJson(client?.jsonFactory)
        assertEquals(expectedMongoQuery, mongoQuery)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindCountSync() {
        testFindCount(StoreType.SYNC, false)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindCountCache() {
        testFindCount(StoreType.CACHE, false)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindCountCachedCallbackCache() {
        testFindCount(StoreType.CACHE, true)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindCountNetwork() {
        testFindCount(StoreType.NETWORK, false)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testFindCountAuto() {
        testFindCount(StoreType.AUTO, false)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteSync() {
        testDelete(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteCache() {
        testDelete(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteAuto() {
        testDelete(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteNetwork() {
        testDelete(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteNullIdSync() {
        testDeleteNullId(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteNullIdCache() {
        testDeleteNullId(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteNullIdAuto() {
        testDeleteNullId(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteNullIdNetwork() {
        testDeleteNullId(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteArraySync() {
        testDeleteArray(StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteArrayCache() {
        testDeleteArray(StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteArrayAuto() {
        testDeleteArray(StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteArrayNetwork() {
        testDeleteArray(StoreType.NETWORK)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPurge() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        save(store, createPerson(TEST_USERNAME))
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 1L)
        var purgeCallback = purge(null, store)
        assertNull(purgeCallback.error)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        store.clear()
        save(store, createPerson(TEST_USERNAME))
        save(store, createPerson(TEST_USERNAME_2))
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 2L)
        purgeCallback = purge(Query().equals("username", TEST_USERNAME), store)
        assertNull(purgeCallback.error)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 1L)
        purgeCallback = purge(Query(), store)
        assertNull(purgeCallback.error)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPurgeInvalidDataStoreType() {
        val store = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        client?.syncManager?.clear(COLLECTION)
        save(store, createPerson(TEST_USERNAME))
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        val purgeCallback = purge(null, store)
        assertNotNull(purgeCallback.error)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSync() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        save(store, createPerson(TEST_USERNAME))
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 1L)
        val syncCallback = sync(store, 120)
        assertNull(syncCallback.error)
        assertNotNull(syncCallback.kinveyPushResponse)
        assertNotNull(syncCallback.kinveyPullResponse)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncInvalidDataStoreType() {
        val store = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        client?.syncManager?.clear(COLLECTION)
        save(store, createPerson(TEST_USERNAME))
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        val syncCallback = sync(store, 120)
        assertNotNull(syncCallback.error)
        assertNull(syncCallback.kinveyPushResponse)
        assertNull(syncCallback.kinveyPullResponse)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncTimeoutError() {
        val changeTimeout = ChangeTimeout()
        val initializer = HttpRequestInitializer { request -> changeTimeout.initialize(request) }
        client = Builder<User>(client?.context)
                .setHttpRequestInitializer(initializer)
                .build() as Client<User>
        client?.syncManager?.clear(COLLECTION)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = createPerson(TEST_USERNAME)
        save(store, person)
        val syncCallback = sync(store, 120)
        assertNotNull(syncCallback.error)
        assertTrue(syncCallback.error?.javaClass == SocketTimeoutException::class.java)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncNoCompletionHandler() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        save(store, createPerson(TEST_USERNAME))
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 1L)
        val syncCallback = sync(store, DEFAULT_TIMEOUT)
        assertFalse(syncCallback.error == null
                && syncCallback.kinveyPullResponse == null
                && syncCallback.kinveyPushResponse == null)
        assertNotNull(syncCallback.kinveyPushResponse)
        assertNotNull(syncCallback.kinveyPullResponse)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPush() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        save(store, person)
        Log.d("DataStoreTest", "id: " + person.id)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 1L)
        val pushCallback = push(store, 120)
        assertNull(pushCallback.error)
        assertTrue(pushCallback.result?.listOfExceptions?.size == 0)
        assertNotNull(pushCallback.result)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushBatching() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        val LIMIT = 25
        (0 until LIMIT).forEach { i ->
            val person = createPerson(TEST_USERNAME)
            save(store, person)
            Log.d("DataStoreTest", "id: " + person.id)
            assertTrue(client?.syncManager?.getCount(COLLECTION) == i + 1.toLong())
        }
        val pushCallback = push(store, 120)
        assertNull(pushCallback.error)
        assertTrue(pushCallback.result?.listOfExceptions?.size == 0)
        assertNotNull(pushCallback.result)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPushBlocking() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        save(store, person)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 1L)
        store.pushBlocking()
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncBlocking() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        cleanBackendDataStore(store)
        client?.syncManager?.clear(COLLECTION)
        var person = createPerson(TEST_USERNAME)
        save(store, person)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 1L)
        store.pushBlocking()
        person = createPerson(TEST_USERNAME_2)
        save(store, person)
        store.syncBlocking(Query())
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        val countCallback = findCount(store, DEFAULT_TIMEOUT, null)
        assertTrue(countCallback.result == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncBlocking2() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        cleanBackendDataStore(store)
        client?.syncManager?.clear(COLLECTION)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        var person = createPerson(TEST_USERNAME)
        save(store, person)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 1L)
        person.age = "237 y.o."
        save(store, person)
        val countAfterSave = client?.syncManager?.getCount(COLLECTION)
        assertTrue(countAfterSave == 1L)
        person = createPerson(TEST_USERNAME_2)
        save(store, person)
        val countAfter2ndSave = client?.syncManager?.getCount(COLLECTION)
        assertTrue(countAfter2ndSave == 2L)
        store.syncBlocking(Query())
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        val countCallback = findCount(store, DEFAULT_TIMEOUT, null)
        assertTrue(countCallback.result == 2)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushInvalidDataStoreType() {
        val store = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        client?.syncManager?.clear(COLLECTION)
        save(store, createPerson(TEST_USERNAME))
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        val pushCallback = push(store, 120)
        assertTrue(pushCallback.error != null || pushCallback.result?.listOfExceptions != null)
        assertNull(pushCallback.result)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPushNoCompletionHandler() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        save(store, createPerson(TEST_USERNAME))
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 1L)
        val pushCallback = push(store, DEFAULT_TIMEOUT)
        assertFalse(pushCallback.error == null && pushCallback.result == null)
        assertNull(pushCallback.error)
        assertTrue(pushCallback.result?.listOfExceptions?.size == 0)
        assertNotNull(pushCallback.result)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testGettingItemsByIds() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val cacheManager = client?.cacheManager
        val cache = cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)
        cache?.clear()
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.clear()
        val ids = (0..99).map { i ->
            val person = createPerson("Test$i")
            val id = i.toString()
            person.id = id
            save(store, person)
            id
        }

        val cachedObjects = client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.get(ids)
        assertEquals(100, cachedObjects?.size)
        cachedObjects?.indices?.forEach { i ->
            val res = cachedObjects[i]
            assertEquals(res.id, i.toString())
        }
        assertTrue(true)
        client?.syncManager?.clear(COLLECTION)
    }

    /**
     * Test checks that if you have some not correct value type in item's field at server,
     * you will have exception in KinveyPullResponse#getListOfExceptions after #pull.
     */
    @Test
    @Throws(InterruptedException::class)
    fun testPullNotCorrectItem() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(Person.COLLECTION_WITH_EXCEPTION, Person::class.java, StoreType.SYNC, client)
        val pullCallback: CustomKinveyPullCallback = testManager.pullCustom(store, null)
        assertTrue(pullCallback.result?.listOfExceptions?.size == 1)
        assertTrue(pullCallback.result?.count == 4)
        testManager.cleanBackendDataStore(store)
    }

    /**
     * Check that your collection has public permission console.kinvey.com
     * Collections / Collection Name / Settings / Permissions - Public
     */
    @Test
    @Throws(InterruptedException::class)
    fun testPull() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        client?.syncManager?.clear(COLLECTION)
        cleanBackendDataStore(store)

        // uploading 3 person to backend
        val persons = ArrayList<Person>()
        val victor = createPerson("Victor_" + UUID.randomUUID().toString())
        val hugo = createPerson("Hugo_" + UUID.randomUUID().toString())
        val barros = createPerson("Barros_" + UUID.randomUUID().toString())
        save(store, victor)
        save(store, hugo)
        save(store, barros)
        var pushCallback = push(store, 120)
        assertNull(pushCallback.error)
        assertNotNull(pushCallback.result)

        //cleaning cache store
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.CACHE.ttl)?.clear()

        //test pulling all data from backend
        var pullCallback = pull(store, null)
        assertNull(pullCallback.error)
        assertNotNull(pullCallback.result)
        assertTrue(pullCallback.result?.count == 3)
        assertTrue(pullCallback.result?.count?.toLong() == getCacheSize(StoreType.CACHE))

        //cleaning cache store
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.CACHE.ttl)?.clear()

        //test pull only 1 item by query
        var query = client?.query()
        query = query?.equals(USERNAME, victor.username)
        pullCallback = pull(store, query)
        assertNull(pullCallback.error)
        assertNotNull(pullCallback.result)
        assertTrue(pullCallback.result?.count == 1)
        assertTrue(pullCallback.result?.count?.toLong() == getCacheSize(StoreType.SYNC))
        cleanBackendDataStore(store)

        //creating 1 entity and uploading to backend
        save(store, hugo)
        pushCallback = push(store, 120)
        assertNull(pushCallback.error)
        assertNotNull(pushCallback.result)

        //cleaning cache store
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.clear()

        //test pulling not existing data from backend
        query = client?.query()
        query = query?.equals(USERNAME, victor.username)
        pullCallback = pull(store, query)
        assertNull(pullCallback.error)
        assertNotNull(pullCallback.result)
        assertTrue(pullCallback.result?.count == 0)
        assertTrue(pullCallback.result?.count?.toLong() == getCacheSize(StoreType.SYNC))
        cleanBackendDataStore(store)

        //creating 1 entity and uploading to backend
        save(store, victor)
        pushCallback = push(store, 120)
        assertNull(pushCallback.error)
        assertNotNull(pushCallback.result)

        //cleaning cache store
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.clear()

        //test pulling 1 entity if only 1 entity exist at backend
        query = client?.query()
        query = query?.equals(USERNAME, victor.username)
        pullCallback = pull(store, query)
        assertNull(pullCallback.error)
        assertNotNull(pullCallback.result)
        assertTrue(pullCallback.result?.count == 1)
        assertTrue(pullCallback.result?.count?.toLong() == getCacheSize(StoreType.CACHE))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullPendingSyncItems() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        save(store, createPerson("TestPullPendingSyncItems"))
        val pullCallback = pull(store, null)
        assertNull(pullCallback.result)
        assertNotNull(pullCallback.error)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSkipLimitInPullBlocking() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        client?.syncManager?.clear(COLLECTION)
        cleanBackendDataStore(store)

        // Arrange
        val persons = ArrayList<Person>()
        val alvin = createPerson("Alvin")
        val simon = createPerson("Simon")
        val theodore = createPerson("Theodore")
        val anna = createPerson("Anna")
        val kate = createPerson("Kate")
        save(store, alvin)
        save(store, simon)
        save(store, theodore)
        save(store, anna)
        save(store, kate)
        val cacheSizeBefore = getCacheSize(StoreType.CACHE)
        assertTrue(cacheSizeBefore == 5L)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.CACHE.ttl)?.clear()
        val cacheSizeBetween = getCacheSize(StoreType.CACHE)
        assertTrue(cacheSizeBetween == 0L)
        val query = client?.query()?.addSort(SORT_FIELD, SortOrder.ASC)
        (0..4).forEach { i ->
            query?.setLimit(1)
            query?.setSkip(i)
            assertEquals(1, store.pullBlocking(query).count.toLong())
            assertEquals(i + 1.toLong(), getCacheSize(StoreType.CACHE))
        }
        assertEquals(5, getCacheSize(StoreType.CACHE))
        (0..4).forEach { i ->
            query?.setLimit(1)
            query?.setSkip(i)
            assertEquals(1, store.pullBlocking(query).count.toLong())
            assertEquals(5, getCacheSize(StoreType.CACHE))
        }
        assertEquals(5, getCacheSize(StoreType.CACHE))
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSkipLimitInPullAsync() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        client?.syncManager?.clear(COLLECTION)
        cleanBackendDataStore(store)

        // Arrange
        val persons = ArrayList<Person>()
        val alvin = createPerson("Alvin")
        val simon = createPerson("Simon")
        val theodore = createPerson("Theodore")
        val anna = createPerson("Anna")
        val kate = createPerson("Kate")
        save(store, alvin)
        save(store, simon)
        save(store, theodore)
        save(store, anna)
        save(store, kate)
        val cacheSizeBefore = getCacheSize(StoreType.CACHE)
        assertTrue(cacheSizeBefore == 5L)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.CACHE.ttl)?.clear()
        val cacheSizeBetween = getCacheSize(StoreType.CACHE)
        assertTrue(cacheSizeBetween == 0L)
        var resultCount = 0
        val query = client?.query()?.addSort(ID, SortOrder.ASC)
        (0..4).forEach { i ->
            query?.setLimit(1)
            query?.setSkip(i)
            resultCount = pull(store, query).result?.count ?: 0
            assertEquals(1, resultCount.toLong())
            assertEquals(i + 1.toLong(), getCacheSize(StoreType.CACHE))
        }
        assertEquals(5, getCacheSize(StoreType.CACHE))
        (0..4).forEach { i ->
            query?.setLimit(1)
            query?.setSkip(i)
            resultCount = pull(store, query).result?.count ?: 0
            assertEquals(1, resultCount.toLong())
            assertEquals(5, getCacheSize(StoreType.CACHE))
        }
        assertEquals(5, getCacheSize(StoreType.CACHE))
    }

    /**
     * Check that SDK pulls the items in correct order from the server
     * if skip limit is used in pull/sync query.
     */
    @Test
    @Throws(InterruptedException::class)
    fun testPullOrderWithSkipLimitQuery() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        cleanBackendDataStore(store)
        (0..4).forEach { i ->
            save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i))
        }
        sync(store, DEFAULT_TIMEOUT)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.clear()
        val query = client?.query()?.addSort(ID, SortOrder.ASC)
        query?.setLimit(1)
        (0..4).forEach { i ->
            query?.setSkip(i)
            assertEquals(1, pull(store, query).result?.count)
            assertEquals(i + 1.toLong(), getCacheSize(StoreType.SYNC))
        }
        assertEquals(5L, getCacheSize(StoreType.SYNC))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullOrderWithSkipLimitQueryWithCachedItemsBeforeTestSortById() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        var pullResponse: DefaultKinveyPullCallback
        (0..9).forEach {
            cleanBackendDataStore(store)
            (0..4).forEach { i ->
                save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i))
            }
            sync(store, DEFAULT_TIMEOUT)
            val query = client?.query()
            query?.setLimit(1)?.addSort(ID, SortOrder.ASC)
            (0..4).forEach { i ->
                query?.setSkip(i)
                pullResponse = pull(store, query)
                assertNotNull(pullResponse)
                assertTrue(pullResponse.result?.count == 1)
                assertEquals(5, getCacheSize(StoreType.SYNC))
            }
            assertEquals(5, getCacheSize(StoreType.SYNC))
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullOrderWithSkipLimitQueryWithCachedItemsBeforeTest() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        var pullResponse: KinveyPullResponse?
        (0..9).forEach { j ->
            cleanBackendDataStore(store)
            (0..4).forEach { i ->
                save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i))
            }
            sync(store, DEFAULT_TIMEOUT)
            val query = client?.query()
            query?.setLimit(1)?.addSort(ID, SortOrder.ASC)
            (0..4).forEach { i ->
                query?.setSkip(i)
                pullResponse = pull(store, query).result
                assertNotNull(pullResponse)
                assertTrue(pullResponse?.count == 1)
                assertEquals(5, getCacheSize(StoreType.SYNC))
            }
            assertEquals(5, getCacheSize(StoreType.SYNC))
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullOrderWithSkipLimitQueryWithCachedItemsBeforeTestWithAutoPagination() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        var pullResponse: KinveyPullResponse?
        (0..9).forEach { j ->
            cleanBackendDataStore(store)
            (0..4).forEach { i ->
                save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i))
            }
            sync(store, DEFAULT_TIMEOUT)
            val query = client?.query()
            pullResponse = pull(store, query, 1).result
            assertNotNull(pullResponse)
            assertEquals(5, getCacheSize(StoreType.SYNC))
        }
    }

    /**
     * Check that SDK finds the items in the cache in correct order
     * if skip limit is used in find method.
     */
    @Test
    @Throws(InterruptedException::class)
    fun testFindInCacheOrderWithSkipLimitQuery() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        (0..4).forEach { i ->
            save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i))
        }
        val query = client?.query()
        var findCallback: DefaultKinveyReadCallback
        (0..4).forEach { i ->
            query?.setLimit(1)
            query?.setSkip(i)
            findCallback = find(store, query, DEFAULT_TIMEOUT)
            assertTrue(findCallback.result?.result?.size == 1)
            assertEquals(TEST_USERNAME + Constants.UNDERSCORE + i, findCallback.result?.result?.get(0)?.username)
        }
        assertEquals(5, getCacheSize(StoreType.SYNC))
    }

    /**
     * Check that SDK removes the correct items from cache in pull/sync process
     * if skip limit is used in pull/sync query.
     */
    @Test
    @Throws(InterruptedException::class)
    fun testSyncUpdateCacheInCorrectWay() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        cleanBackendDataStore(store)
        (0..4).forEach { i ->
            save(store, createPerson(TEST_USERNAME + Constants.UNDERSCORE + i))
        }
        sync(store, DEFAULT_TIMEOUT)
        var findResult = find(store, client?.query(), DEFAULT_TIMEOUT).result?.result
        assertEquals(5, findResult?.size)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.clear()
        var pullResults: List<Person?>
        var resultCount: Int
        var query = client?.query()?.addSort(ID, SortOrder.ASC)
        query?.setLimit(1)
        (0..4).forEach { i ->
            query?.setSkip(i)
            resultCount = pull(store, query).result?.count ?: 0
            assertNotNull(resultCount)
        }
        assertEquals(5, getCacheSize(StoreType.SYNC))
        findResult = find(store, client?.query(), DEFAULT_TIMEOUT).result?.result
        assertEquals(5, findResult?.size)
        assertEquals(5, getCacheSize(StoreType.SYNC))
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.clear()
        query = client?.query()?.addSort(ID, SortOrder.ASC)
        val limit = 2
        var skip = 0
        query?.setLimit(limit)
        (0..4).forEach { i ->
            query?.setSkip(skip)
            skip += limit
            resultCount = pull(store, query).result?.count ?: 0
            assertNotNull(resultCount)
        }
        assertEquals(5, getCacheSize(StoreType.SYNC))
        findResult = find(store, client?.query(), DEFAULT_TIMEOUT).result?.result
        assertEquals(5, findResult?.size)
        assertEquals(5, getCacheSize(StoreType.SYNC))
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSkipLimitInSyncBlocking() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        client?.syncManager?.clear(COLLECTION)
        cleanBackendDataStore(store)

        // Arrange
        val persons = ArrayList<Person>()
        val alvin = createPerson("Alvin")
        val simon = createPerson("Simon")
        val theodore = createPerson("Theodore")
        val anna = createPerson("Anna")
        val kate = createPerson("Kate")
        save(store, alvin)
        save(store, simon)
        save(store, theodore)
        save(store, anna)
        save(store, kate)
        val cacheSizeBefore = getCacheSize(StoreType.CACHE)
        assertTrue(cacheSizeBefore == 5L)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.CACHE.ttl)?.clear()
        val cacheSizeBetween = getCacheSize(StoreType.CACHE)
        assertTrue(cacheSizeBetween == 0L)
        val query = client?.query()?.addSort(SORT_FIELD, SortOrder.ASC)
        (0..4).forEach { i ->
            query?.setLimit(1)
            query?.setSkip(i)
            store.syncBlocking(query)
            assertEquals(i + 1.toLong(), getCacheSize(StoreType.CACHE))
        }
        assertEquals(5, getCacheSize(StoreType.CACHE))
        (0..4).forEach { i ->
            query?.setLimit(1)
            query?.setSkip(i)
            store.syncBlocking(query)
            assertEquals(5, getCacheSize(StoreType.CACHE))
        }
        assertEquals(5, getCacheSize(StoreType.CACHE))
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSkipLimitInSyncAsync() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        client?.syncManager?.clear(COLLECTION)
        cleanBackendDataStore(store)

        // Arrange
        val persons = ArrayList<Person>()
        val alvin = createPerson("Alvin")
        val simon = createPerson("Simon")
        val theodore = createPerson("Theodore")
        val anna = createPerson("Anna")
        val kate = createPerson("Kate")
        save(store, alvin)
        save(store, simon)
        save(store, theodore)
        save(store, anna)
        save(store, kate)
        val cacheSizeBefore = getCacheSize(StoreType.CACHE)
        assertTrue(cacheSizeBefore == 5L)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.CACHE.ttl)?.clear()
        val cacheSizeBetween = getCacheSize(StoreType.CACHE)
        assertTrue(cacheSizeBetween == 0L)
        val query = client?.query()?.addSort(SORT_FIELD, SortOrder.ASC) as Query
        (0..4).forEach { i ->
            query?.setLimit(1)
            query?.setSkip(i)
            sync(store, query, 120)
            assertEquals(i + 1.toLong(), getCacheSize(StoreType.CACHE))
        }
        assertEquals(5, getCacheSize(StoreType.CACHE))
        (0..4).forEach { i ->
            query.setLimit(1)
            query.setSkip(i)
            sync(store, query, 120)
            assertEquals(5, getCacheSize(StoreType.CACHE))
        }
        assertEquals(5, getCacheSize(StoreType.CACHE))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPullInvalidDataStoreType() {
        val store = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        client?.syncManager?.clear(COLLECTION)
        val pullCallback = pull(store, null)
        assertNull(pullCallback.result)
        assertNotNull(pullCallback.error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testExpiredTTL() {
        StoreType.SYNC.ttl = 1
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        val saveCallback = save(store, createPerson(TEST_USERNAME))
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val userId = saveCallback.result?.id
        assertNotNull(userId)
        Thread.sleep(1000)
        var query = client?.query()
        query = query?.equals(ID, userId)
        val findCallback = find(store, query, 120)
        assertNull(findCallback.error)
        assertNotNull(findCallback.result)
        assertTrue(findCallback.result?.result?.size == 0)
        StoreType.SYNC.ttl = Long.MAX_VALUE
    }

    /**
     * Check that your collection has public permission console.kinvey.com
     * Collections / Collection Name / Settings / Permissions - Public
     */
    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testSaveAndFind10SkipLimit() {
        assertNotNull(client?.activeUser)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        cleanBackendDataStore(store)
        sync(store, 120)
        val user = client?.activeUser

        (0..9).forEach { i ->
            val person = createPerson("Person_$i")
            val saveCallback = save(store, person)
            assertNull(saveCallback.error)
            assertNotNull(saveCallback.result)
        }

        var skip = 0
        val limit = 2
        var kinveyListCallback: DefaultKinveyReadCallback
        var query = client?.query()?.addSort(SORT_FIELD, SortOrder.ASC)
        (0..4).forEach { i ->
            query?.setSkip(skip)
            query?.setLimit(limit)
            kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
            assertNull(kinveyListCallback.error)
            assertNotNull(kinveyListCallback.result)
            val resultList = kinveyListCallback.result?.result
            assertEquals(resultList?.get(0)?.username, "Person_$skip")
            assertEquals(resultList?.get(1)?.username, "Person_" + (skip + 1))
            skip += limit
        }

        query = client?.query()
        query?.setLimit(5)
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
        var resultList = kinveyListCallback.result?.result
        var size = resultList?.size ?: 0
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(size == 5)
        assertEquals(resultList?.get(0)?.username, "Person_0")
        assertEquals(resultList?.get(size - 1)?.username, "Person_4")

        query = client?.query()
        query?.setSkip(5)
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        resultList = kinveyListCallback.result?.result
        size = resultList?.size ?: 0
        assertTrue(size == 5)
        assertEquals(resultList?.get(0)?.username, "Person_5")
        assertEquals(resultList?.get(size - 1)?.username, "Person_9")

        query = client?.query()
        query?.setLimit(6)
        query?.setSkip(6)
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        resultList = kinveyListCallback.result?.result
        size = resultList?.size ?: 0
        assertTrue(size == 4)
        assertEquals(resultList?.get(0)?.username, "Person_6")
        assertEquals(resultList?.get(size - 1)?.username, "Person_9")

        query = client?.query()
        query?.setSkip(10)
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        resultList = kinveyListCallback.result?.result
        size = resultList?.size ?: 0
        assertTrue(size == 0)

        query = client?.query()
        query?.setSkip(11)
        kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        resultList = kinveyListCallback.result?.result
        size = resultList?.size ?: 0
        assertTrue(size == 0)

        val pushCallback = push(store, DEFAULT_TIMEOUT)
        assertNull(pushCallback.error)
        assertNotNull(pushCallback.result)
        assertTrue(pushCallback.result?.successCount == 10)
        skip = 0
        (0..4).forEach { i ->
            query = client?.query()
            query?.equals("_acl.creator", user?.id)
            query?.setSkip(skip)
            query?.setLimit(limit)
            query?.addSort("username", SortOrder.ASC)
            val pullCallback = pull(store, query)
            assertNull(pullCallback.error)
            assertNotNull(pullCallback.result)
            assertTrue(pullCallback.result?.count == limit)
            skip += limit
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSyncCount() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        assertTrue(store.syncCount() == 1L)
        sync(store, DEFAULT_TIMEOUT)
        assertTrue(store.syncCount() == 0L)
        delete(store, saveCallback.result?.id, DEFAULT_TIMEOUT)
        assertTrue(store.syncCount() == 1L)
        sync(store, DEFAULT_TIMEOUT)
        assertTrue(store.syncCount() == 0L)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSaveKmd() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, 60L)?.clear()
        clearBackend(collection(COLLECTION, Person::class.java, StoreType.NETWORK, client))
        store.clear()
        val person = createPerson(TEST_TEMP_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback)
        sync(store, DEFAULT_TIMEOUT)
        val findCallback = find(store, DEFAULT_TIMEOUT)
        assertNotNull(findCallback)
        assertNotNull(findCallback.result)
        val people = findCallback.result?.result
        assertNotNull(people)
        assertEquals(1, people?.size)
        val savedPerson = people?.get(0)
        assertNotNull(savedPerson?.get(KMD))
        assertNotNull((savedPerson?.get(KMD) as GenericJson)[LMT])
        delete(store, savedPerson.id, DEFAULT_TIMEOUT)
        push(store, DEFAULT_TIMEOUT)
        client?.syncManager?.clear(COLLECTION)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUpdateLmt() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        store.clear()
        clearBackend(collection(COLLECTION, Person::class.java, StoreType.NETWORK, client))
        val person = createPerson(TEST_TEMP_USERNAME)
        val savedPerson = store.save(person)
        sync(store, DEFAULT_TIMEOUT)
        var findCallback = find(store, DEFAULT_TIMEOUT)
        assertNotNull(findCallback)
        assertNotNull(findCallback.result)
        val people = findCallback.result?.result
        assertEquals(1, people?.size)
        var syncedPerson = people?.get(0)
        assertNotNull(syncedPerson?.get(KMD))
        val savedLmd = (syncedPerson?.get(KMD) as GenericJson)[LMT] as String
        assertNotNull(savedLmd)
        syncedPerson.username = TEST_TEMP_USERNAME + "_Change"
        syncedPerson = store.save(syncedPerson)
        sync(store, DEFAULT_TIMEOUT)
        findCallback = find(store, DEFAULT_TIMEOUT)
        assertNotNull(findCallback.result?.result)
        val updatedSyncedPerson = findCallback.result?.result?.get(0)
        assertNotNull(updatedSyncedPerson?.get(KMD))
        val updatedLmd = (updatedSyncedPerson?.get(KMD) as GenericJson)[LMT] as String
        assertNotNull(updatedLmd)
        assertNotEquals(savedLmd, updatedLmd)
        delete(store, updatedSyncedPerson.id, DEFAULT_TIMEOUT)
        push(store, DEFAULT_TIMEOUT)
        client?.syncManager?.clear(COLLECTION)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveList() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val persons = ArrayList<Person>()
        persons.add(Person(TEST_USERNAME))
        val saveCallback = testManager.saveCustomList(store, persons)
        assertNotNull(saveCallback.getResult())
        val pushCallback = testManager.push(store)
        assertNotNull(pushCallback.result)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListWithStoreTypeCache() {
        client?.enableDebugLogging()
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        val persons = ArrayList<Person>()
        persons.add(Person(TEST_USERNAME))
        persons.add(Person(TEST_USERNAME_2))
        persons.add(Person(TEST_TEMP_USERNAME))
        val saveCallback = testManager.saveCustomList(store, persons)
        assertNotNull(saveCallback.getResult())
        assertEquals(3, saveCallback.getResult()?.size)
        val cachedItems = client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.CACHE.ttl)?.get()
        assertEquals(3, cachedItems?.size)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testClear() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        var saveCallback = testManager.save(store, Person(TEST_USERNAME))
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)
        store.clear()
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        saveCallback = testManager.save(store, Person(TEST_USERNAME))
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)
    }

    @Test
    fun testNumberThreadPoolForNotMultithreading() {
        if (client?.isClientRequestMultithreading == false) {
            assertTrue(client?.numberThreadPool == 1)
        }
    }

    @Test
    fun testNumberThreadPoolForMultithreading() {
        if (client?.isClientRequestMultithreading == true) {
            assertTrue(client?.numberThreadPool ?: 0 >= 1)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListWithStoreTypeAuto() {
        client?.enableDebugLogging()
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val persons = ArrayList<Person>()
        persons.add(Person(TEST_USERNAME))
        persons.add(Person(TEST_USERNAME_2))
        persons.add(Person(TEST_TEMP_USERNAME))
        val saveCallback = testManager.saveCustomList(store, persons)
        assertNotNull(saveCallback.getResult())
        assertEquals(3, saveCallback.getResult()?.size)
        val cachedItems = client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.AUTO.ttl)?.get()
        assertEquals(3, cachedItems?.size)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testQueryClear() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        var saveCallback = testManager.save(store, Person(TEST_USERNAME))
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)
        saveCallback = testManager.save(store, Person(TEST_USERNAME_2))
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 2L)
        assertTrue(store.count() == 2)
        store.clear(client?.query()?.equals("username", TEST_USERNAME))
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)
        val deletePerson = Person(TEST_USERNAME)
        saveCallback = testManager.save(store, deletePerson)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 2L)
        assertTrue(store.count() == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testQueryPurge() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        var saveCallback = testManager.save(store, Person(TEST_USERNAME))
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)
        saveCallback = testManager.save(store, Person(TEST_USERNAME_2))
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 2L)
        assertTrue(store.count() == 2)
        store.purge(client?.query()!!.equals("username", TEST_USERNAME))
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 2)
        store.purge(client?.query()!!)
        assertTrue(store.syncCount() == 0L)
        val deletePerson = Person(TEST_USERNAME)
        saveCallback = testManager.save(store, deletePerson)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 3)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSelfReferenceClass() {
        val testManager = TestManager<SelfReferencePerson>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, SelfReferencePerson::class.java, StoreType.SYNC, client)
        assertNotNull(store)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSelfReferenceClassWithData() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        val testManager = TestManager<SelfReferencePerson>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, SelfReferencePerson::class.java, StoreType.SYNC, client)
        val person1 = SelfReferencePerson("person1")
        val person2 = SelfReferencePerson("person2")
        val person3 = SelfReferencePerson("person3")
        val person4 = SelfReferencePerson("person4")
        val person5 = SelfReferencePerson("person5")
        person4.person = person5
        person3.person = person4
        person2.person = person3
        person1.person = person2
        val callback = testManager.saveCustom(store, person1)
        assertNotNull(callback.result)
        assertNull(callback.error)
        val listCallback = testManager.findCustom(store, client?.query())
        assertNotNull(listCallback.result)
        assertNull(listCallback.error)
        val person = listCallback.result?.result?.get(0)
        assertTrue(person?.username == "person1")
        assertTrue(person?.person?.username == "person2")
        assertTrue(person?.person?.person?.username == "person3")
        assertTrue(person?.person?.person?.person?.username == "person4")
        assertTrue(person?.person?.person?.person?.person?.username == "person5")
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQueryInSelfReferenceClass() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        val testManager = TestManager<SelfReferencePerson>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, SelfReferencePerson::class.java, StoreType.SYNC, client)
        (0..9).forEach { i ->
            val person1 = SelfReferencePerson("person1")
            val person2 = SelfReferencePerson("person2")
            val person3 = SelfReferencePerson("person3")
            val person4 = SelfReferencePerson("person4")
            val person5 = SelfReferencePerson("person5")
            person4.person = person5
            person3.person = person4
            person2.person = person3
            person1.person = person2
            val callback = testManager.saveCustom(store, person1)
            assertNotNull(callback.result)
            assertNull(callback.error)
        }
        val query = client?.query()?.`in`("selfReferencePerson.selfReferencePerson.username", arrayOf("person3"))
        val listCallback = testManager.findCustom(store, query)
        assertNotNull(listCallback.result)
        assertNull(listCallback.error)
        assertTrue(listCallback.result?.result?.size == 10)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSelfReferenceClassInList() {
        val testManager = TestManager<PersonList>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonList::class.java, StoreType.SYNC, client)
        assertNotNull(store)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSelfReferenceClassInListWithData() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        val testManager = TestManager<PersonList>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonList::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        val person1 = PersonList("person1")
        val person2 = PersonList("person2")
        val person3 = PersonList("person3")
        val person4 = PersonList("person4")
        val person5 = PersonList("person5")
        val person6 = PersonList("person6")
        val person7 = PersonList("person7")
        val person8 = PersonList("person8")
        val person9 = PersonList("person9")
        val list: MutableList<PersonList> = ArrayList()
        list.add(person6)
        list.add(person7)
        list.add(person8)
        list.add(person9)
        person2.list = list
        val list2: MutableList<PersonList> = ArrayList()
        list2.add(person2)
        list2.add(person3)
        list2.add(person4)
        list2.add(person5)
        person1.list = list2
        val callback = testManager.saveCustom(store, person1)
        assertNotNull(callback.result)
        assertNull(callback.error)
        val listCallback = testManager.findCustom(store, client?.query())
        assertNotNull(listCallback.result)
        assertNull(listCallback.error)
        val person = listCallback.result?.result?.get(0)
        assertEquals(person?.username, "person1")
        assertEquals(person?.list?.get(1)?.username, "person3")
        assertEquals(person?.list?.get(0)?.username, "person2")
        assertEquals(person?.list?.get(0)?.list?.get(0)?.username, "person6")
        val deleteCallback = testManager.deleteCustom(store, client?.query())
        assertNotNull(deleteCallback.result)
        assertNull(deleteCallback.error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testQueryInSelfReferenceClassInList() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        val testManager = TestManager<PersonList>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonList::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        (0..4).forEach { i ->
            val person1 = PersonList("person1_$i")
            val person2 = PersonList("person2_$i")
            val person3 = PersonList("person3_$i")
            val person4 = PersonList("person4_$i")
            val person5 = PersonList("person5_$i")
            val person6 = PersonList("person6_$i")
            val person7 = PersonList("person7_$i")
            val person8 = PersonList("person8_$i")
            val person9 = PersonList("person9_$i")
            val list: MutableList<PersonList> = ArrayList()
            list.add(person6)
            list.add(person7)
            list.add(person8)
            list.add(person9)
            person2.list = list
            val list2: MutableList<PersonList> = ArrayList()
            list2.add(person2)
            list2.add(person3)
            list2.add(person4)
            list2.add(person5)
            person1.list = list2
            val callback: CustomKinveyClientCallback<PersonList> = testManager.saveCustom(store, person1)
            assertNotNull(callback.result)
            assertNull(callback.error)
        }
        var query = client?.query()?.`in`("username", arrayOf("person1_0"))
        var listCallback = testManager.findCustom(store, query)
        assertNotNull(listCallback.result)
        assertTrue(listCallback.result?.result?.size == 1)
        assertNull(listCallback.error)
        val person = listCallback.result?.result?.get(0)
        assertEquals(person?.username, "person1_0")

        query = client?.query()?.`in`("list.username", arrayOf("person2_0"))
        listCallback = testManager.findCustom(store, query)
        assertNotNull(listCallback.result)
        assertTrue(listCallback.result?.result?.size == 1)
        assertEquals(listCallback.result?.result?.get(0)?.username, "person1_0")
        assertNull(listCallback.error)

        query = client?.query()?.`in`("list.list.username", arrayOf("person6_1"))
        listCallback = testManager.findCustom(store, query)
        assertNotNull(listCallback.result)
        assertTrue(listCallback.result?.result?.size == 1)
        assertEquals(listCallback.result?.result?.get(0)?.username, "person1_1")
        assertNull(listCallback.error)

        query = client?.query()?.`in`("list.list.username", arrayOf("person6_1", "person6_2"))
        listCallback = testManager.findCustom(store, query)
        assertNotNull(listCallback.result)
        assertTrue(listCallback.result?.result?.size == 2)
        assertEquals(listCallback.result?.result?.get(0)?.username, "person1_1")
        assertEquals(listCallback.result?.result?.get(1)?.username, "person1_2")
        assertNull(listCallback.error)

        query = client?.query()?.equals("list.list.username", "person6_1")
        listCallback = testManager.findCustom(store, query)
        assertNotNull(listCallback.result)
        assertTrue(listCallback.result?.result?.size == 1)
        assertEquals(listCallback.result?.result?.get(0)?.username, "person1_1")
        assertNull(listCallback.error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSelfReferenceClassInClassWithList() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        val testManager = TestManager<PersonList>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonList::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        val person1 = PersonList("person1")
        val person2 = PersonList("person2")
        val person3 = PersonList("person3")
        val person4 = PersonList("person4")
        val person5 = PersonList("person5")
        val person6 = PersonList("person6")
        val list: MutableList<PersonList> = ArrayList()
        list.add(person3)
        list.add(person4)
        list.add(person5)
        list.add(person6)
        person2.list = list
        person1.personList = person2

        val callback = testManager.saveCustom(store, person1)
        assertNotNull(callback.result)
        assertNull(callback.error)

        val listCallback = testManager.findCustom(store, client?.query())
        assertNotNull(listCallback.result)
        assertNull(listCallback.error)

        val person = listCallback.result?.result?.get(0)
        assertEquals(person?.username, "person1")
        assertEquals(person?.personList?.username, "person2")
        assertEquals(person?.personList?.list?.get(0)?.username, "person3")

        val deleteCallback = testManager.deleteCustom(store, client?.query())
        assertNotNull(deleteCallback.result)
        assertNull(deleteCallback.error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSelfReferenceBookAuthorBook() {
        val testManager = TestManager<PersonWithAddress>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonWithAddress::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        val person = PersonWithAddress("person")
        val address = Address("test_address")
        val person2 = PersonWithAddress("person_2")
        address.person = person2
        person.address = address

        val callback = testManager.saveCustom(store, person)
        assertNotNull(callback.result)
        assertNull(callback.error)

        val deleteCallback = testManager.deleteCustom(store, client?.query())
        assertNotNull(deleteCallback.result)
        assertNull(deleteCallback.error)
    }

    //Check person_person_list and person_list_person initialization
    @Test
    @Throws(InterruptedException::class)
    fun testInitializationPersonWithPersonAndList() {
        val testManager = TestManager<PersonWithPersonAndList>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonWithPersonAndList::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        val person = PersonWithPersonAndList("person")
        person.person = PersonWithPersonAndList("person_1")
        val list: MutableList<PersonWithPersonAndList> = ArrayList()
        list.add(PersonWithPersonAndList("person_2"))
        person.list = list
        val callback = testManager.saveCustom(store, person)
        assertNotNull(callback.result)
        assertNull(callback.error)
        val realm = RealmCacheManagerUtil.getRealm(client)
        try {
            realm?.beginTransaction()
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(COLLECTION, realm))?.count(), 1L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "__kmd", realm))?.count(), 1L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "__acl", realm))?.count(), 1L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_person", realm))?.count(), 1L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_list", realm))?.count(), 1L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_list", realm) + "_person", realm))?.count(), 0L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_person", realm) + "_list", realm))?.count(), 0L)
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }
    }

    //Check possibility delete self-reference object
    @Test
    @Throws(InterruptedException::class)
    fun testDeletePersonWithPersonAndList() {
        val testManager = TestManager<PersonWithPersonAndList>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonWithPersonAndList::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        val person = PersonWithPersonAndList("person")
        person.person = PersonWithPersonAndList("person_1")
        val list: MutableList<PersonWithPersonAndList> = ArrayList()
        list.add(PersonWithPersonAndList("person_2"))
        person.list = list

        val callback = testManager.saveCustom(store, person)
        assertNotNull(callback.result)
        assertNull(callback.error)

        val deleteCallback = testManager.deleteCustom(store, client?.query())
        assertNotNull(deleteCallback.result)
        assertNull(deleteCallback.error)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteSelfReferenceManyObject() {
        val testManager = TestManager<PersonWithPersonAndList>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonWithPersonAndList::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        val person = PersonWithPersonAndList("person")
        val person1 = PersonWithPersonAndList("person1")
        val person2 = PersonWithPersonAndList("person2")
        val person3 = PersonWithPersonAndList("person3")
        val person4 = PersonWithPersonAndList("person4")
        val person5 = PersonWithPersonAndList("person5")
        val person6 = PersonWithPersonAndList("person6")
        val person7 = PersonWithPersonAndList("person7")
        val person8 = PersonWithPersonAndList("person8")
        person7.person = person8
        person6.person = person7
        person5.person = person6
        person4.person = person5
        person3.person = person4
        person2.person = person3
        person1.person = person2
        person.person = person1
        val list: MutableList<PersonWithPersonAndList> = ArrayList()
        list.add(PersonWithPersonAndList("person_2"))
        person.list = list

        val callback = testManager.saveCustom(store, person)
        assertNotNull(callback.result)
        assertNull(callback.error)

        val deleteCallback = testManager.deleteCustom(store, client?.query())
        assertNotNull(deleteCallback.result)
        assertNull(deleteCallback.error)
    }

    @Test //Model: PersonRoomAddressPerson - Room - Address - PersonRoomAddressPerson
    @Throws(InterruptedException::class)
    fun testSelfReferencePersonRoomAddressPerson() {
        val testManager = TestManager<PersonRoomAddressPerson>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonRoomAddressPerson::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        val person = PersonRoomAddressPerson()
        person.name = "person_1"
        val room = Room()
        room.name = "room_name"
        val address = RoomAddress()
        address.name = "address_name"
        val person2 = PersonRoomAddressPerson()
        person2.name = "person_2"
        address.person = person2
        room.roomAddress = address
        person.room = room

        val callback = testManager.saveCustom(store, person)
        assertNotNull(callback.result)
        assertNull(callback.error)
        val result = callback.result
        assertEquals("person_1", result?.name)
        assertNotNull(result?.room)
        assertEquals("room_name", result?.room?.name)
        assertNotNull(result?.room?.roomAddress)
        assertEquals("address_name", result?.room?.roomAddress?.name)
        assertNotNull(result?.room?.roomAddress?.person)
        assertEquals("person_2", result?.room?.roomAddress?.person?.name)
    }

    @Test //Model: PersonRoomPerson - RoomPerson - PersonRoomPerson
    @Throws(InterruptedException::class)
    fun testSelfReferencePersonRoomPerson() {
        val testManager = TestManager<PersonRoomPerson>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonRoomPerson::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        val person = PersonRoomPerson()
        val personForList = PersonRoomPerson()
        val personForList2 = PersonRoomPerson()
        person.name = "person_1"
        personForList.name = "personForList_1"
        personForList2.name = "personForList_2"
        val room = RoomPerson()
        room.name = "room_name"
        val person2 = PersonRoomPerson()
        person2.name = "person_2"
        val personList: MutableList<PersonRoomPerson> = ArrayList()
        personList.add(personForList)
        personList.add(personForList2)
        person.personList = personList
        person2.personList = personList
        room.person = person2
        person.room = room

        val callback = testManager.saveCustom(store, person)
        assertNotNull(callback.result)
        assertNull(callback.error)
        val result = callback.result
        assertEquals("person_1", result?.name)
        assertEquals("personForList_1", result?.personList?.get(0)?.name)
        assertEquals("personForList_2", result?.personList?.get(1)?.name)
        assertNotNull(result?.room)
        assertEquals("room_name", result?.room?.name)
        assertNotNull(result?.room?.person)
        assertEquals("person_2", result?.room?.person?.name)
        assertEquals("personForList_1", result?.room?.person?.personList?.get(0)?.name)
        assertEquals("personForList_2", result?.room?.person?.personList?.get(1)?.name)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSelfReferenceClassComplex() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        val testManager = TestManager<PersonList>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, PersonList::class.java, StoreType.SYNC, client)
        assertNotNull(store)
        val person1 = PersonList("person1")
        val person2 = PersonList("person2")
        val person3 = PersonList("person3")
        val person4 = PersonList("person4")
        val person5 = PersonList("person5")
        val person6 = PersonList("person6")
        val person7 = PersonList("person7")
        val person8 = PersonList("person8")
        val person9 = PersonList("person9")
        val person10 = PersonList("person10")
        val list: MutableList<PersonList> = ArrayList()
        list.add(person3)
        list.add(person4)
        list.add(person5)
        list.add(person6)
        person2.list = list
        person8.personList = person7
        person9.personList = person8
        person10.personList = person9
        person2.personList = person10
        person1.personList = person2

        val callback = testManager.saveCustom(store, person1)
        assertNotNull(callback.result)
        assertNull(callback.error)

        val listCallback = testManager.findCustom(store, client?.query())
        assertNotNull(listCallback.result)
        assertNull(listCallback.error)

        val person = listCallback.result?.result?.get(0)
        assertEquals(person?.username, "person1")
        assertEquals(person?.personList?.username, "person2")
        assertEquals(person?.personList?.list?.get(0)?.username, "person3")
        assertEquals(person?.personList?.list?.get(1)?.username, "person4")
        assertEquals(person?.personList?.list?.get(2)?.username, "person5")
        assertEquals(person?.personList?.list?.get(3)?.username, "person6")
        assertEquals(person?.personList?.personList?.username, "person10")
        assertEquals(person?.personList?.personList?.personList?.username, "person9")
        assertEquals(person?.personList?.personList?.personList?.personList?.username, "person8")
        assertEquals(person?.personList?.personList?.personList?.personList?.personList?.username, "person7")
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDeleteInternalTables() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        val person = Person(TEST_USERNAME)
        person.author = Author("author_name")

        val saveCallback = testManager.save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)
        var realm = RealmCacheManagerUtil.getRealm(client)
        var resSize: Int
        try {
            realm?.beginTransaction()
            resSize = realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_author", realm))?.findAll()?.size ?: 0
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }
        assertEquals(1, resSize) // check that item in sub table was created

        testManager.delete(store, saveCallback.result?.id)
        realm = RealmCacheManagerUtil.getRealm(client)
        try {
            realm?.beginTransaction()
            resSize = realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_author", realm))?.findAll()?.size ?: 0
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }
        assertEquals(0, resSize) // check that item in sub table was deleted after call 'clear'
        assertTrue(store.count() == 0)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testClearInternalTables() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        val person = Person(TEST_USERNAME)
        person.author = Author("author_name")
        val saveCallback = testManager.save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)
        var realm = RealmCacheManagerUtil.getRealm(client)
        var resSize: Int
        try {
            realm?.beginTransaction()
            resSize = realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_author", realm))?.findAll()?.size ?: 0
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }
        assertEquals(1, resSize) // check that item in sub table was created

        store.clear()
        realm = RealmCacheManagerUtil.getRealm(client)
        try {
            realm?.beginTransaction()
            resSize = realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_author", realm))?.findAll()?.size ?: 0
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }
        assertEquals(0, resSize) // check that item in sub table was deleted after call 'clear'
        assertTrue(store.count() == 0)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testClearCollectionIfModelClassChanged() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        var store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        var person = Person(TEST_USERNAME)

        var saveCallback = testManager.save(store, person)
        assertNotNull(saveCallback?.result)
        assertNull(saveCallback?.error)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)

        var mockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mockContext).build()
        store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)
        var realm = RealmCacheManagerUtil.getRealm(client)
        try {
            realm?.beginTransaction()
            RealmCacheManagerUtil.setTableHash(client, COLLECTION, "hashTest", realm)
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }

        mockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mockContext).build()
        store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        person = Person(TEST_USERNAME)

        saveCallback = testManager.save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)
        var isOneTable = false

        realm = RealmCacheManagerUtil.getRealm(client)
        try {
            realm?.beginTransaction()
            isOneTable = isCollectionHasOneTable(COLLECTION, realm)
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }
        assertTrue(isOneTable)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testGrowCollectionExponentially() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager.pullCustom(store, client?.query())
        testManager.cleanBackendDataStore(store)
        testManager.push(store)
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        (0..2).forEach { i ->
            val person = Person(TEST_USERNAME + i)
            person.author = Author("Author_$i")
            val saveCallback = testManager.save(store, person)
            assertNotNull(saveCallback.result)
            assertNull(saveCallback.error)
            assertTrue(store.syncCount() == i + 1.toLong())
            assertTrue(store.count() == i + 1)
        }
        testManager.push(store)
        var realm = RealmCacheManagerUtil.getRealm(client)
        try {
            realm?.beginTransaction()
            checkInternalTablesHasItems(3, COLLECTION, realm)
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }
        testManager.pullCustom(store, client?.query())
        testManager.pullCustom(store, client?.query())
        testManager.pullCustom(store, client?.query())
        realm = RealmCacheManagerUtil.getRealm(client)
        try {
            realm?.beginTransaction()
            checkInternalTablesHasItems(3, COLLECTION, realm)
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSaveItemToInternalTable() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        val person = Person(TEST_USERNAME)
        person.author = Author("Author_")

        val saveCallback = testManager.save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertTrue(store.syncCount() == 1L)
        assertTrue(store.count() == 1)
        val realm = RealmCacheManagerUtil.getRealm(client)
        try {
            realm?.beginTransaction()
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(COLLECTION, realm))?.count(), 1L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "__kmd", realm))?.count(), 1L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "__acl", realm))?.count(), 1L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_author", realm))?.count(), 1L)
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_author", realm) + "__kmd", realm))
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_author", realm) + "__acl", realm))
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testInitializationInternalTable() {
        val testManager = TestManager<Person>()
        testManager.login(TestManager.USERNAME, TestManager.PASSWORD, client)
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertTrue(store.syncCount() == 0L)
        assertTrue(store.count() == 0)
        val realm = RealmCacheManagerUtil.getRealm(client)
        try {
            realm?.beginTransaction()
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(COLLECTION, realm))?.count() ?: 0L, 0L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "__kmd", realm))?.count(), 0L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "__acl", realm))?.count(), 0L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_author", realm))?.count(), 0L)
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_author", realm) + "__kmd", realm))
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(COLLECTION, realm) + "_author", realm) + "__acl", realm))
            assertEquals(realm?.where(TableNameManagerUtil.getShortName("sync", realm))?.count(), 0L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("sync", realm) + "_meta", realm))?.count(), 0L)
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("sync", realm) + "_meta", realm) + "__kmd", realm))
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("sync", realm) + "_meta", realm) + "__acl", realm))
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("sync", realm) + "__kmd", realm))?.count(), 0L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("sync", realm) + "__acl", realm))?.count(), 0L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName("syncitems", realm))?.count(), 0L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("syncitems", realm) + "_meta", realm))?.count(), 0L)
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("syncitems", realm) + "_meta", realm) + "__kmd", realm))
            assertNull(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("syncitems", realm) + "_meta", realm) + "__acl", realm))
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("syncitems", realm) + "__kmd", realm))?.count(), 0L)
            assertEquals(realm?.where(TableNameManagerUtil.getShortName(TableNameManagerUtil.getShortName("syncitems", realm) + "__acl", realm))?.count(), 0L)
            realm?.commitTransaction()
        } finally {
            realm?.close()
        }
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testHashCode() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        cleanBackendDataStore(store)
        store.syncBlocking(null)
        val person = createPerson(TEST_USERNAME)
        person.author = Author("author")
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        store.syncBlocking(null)
        val personList = store.find()?.result
        val person1 = personList?.get(0)
        val hashcode = person1.hashCode()
        assertNotNull(hashcode)
        val theSamePerson = store.find(client?.query()?.equals("username", TEST_USERNAME))?.result?.get(0)
        assertEquals(hashcode.toLong(), theSamePerson.hashCode().toLong())
        save(store, createPerson(TEST_USERNAME + 2))
        val theSamePerson2 = store.find(client?.query()?.equals("author.name", "author"))?.result?.get(0)
        assertEquals(hashcode.toLong(), theSamePerson2.hashCode().toLong())
    }

    @Test
    @Ignore //ignore while test app won't support delta cache
    @Throws(InterruptedException::class, IOException::class)
    fun testDeltaCache() {
        client?.isUseDeltaCache = true
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertTrue(store.isDeltaSetCachingEnabled)
        client?.syncManager?.clear(COLLECTION)
        clearBackend(store)
        val person = Person()
        person.username = "name"
        person.set("CustomField", "CustomValue")

        store.save(person)
        store.syncBlocking(null)
        val person2 = Person()
        person2.username = "changed name 1"
        person2.set("CustomField", "CustomValueChanged ")
        store.save(person2)
        val person3 = Person()
        person3.username = "changed name 2"
        person3.set("CustomField", "CustomValueChanged 2")
        store.save(person3)

        val callback = sync(store, DEFAULT_TIMEOUT)
        assertNull(callback.error)
        assertNotNull(callback.kinveyPushResponse)
        assertNotNull(callback.kinveyPullResponse)

        val personList = store.find()?.result
        val person1 = personList?.get(0)
        assertNotNull(person1)
        assertNotNull(person1?.username)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDeltaCacheAfterDataStoreInitialization() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        store.isDeltaSetCachingEnabled = true
        assertTrue(store.isDeltaSetCachingEnabled)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUpdateInternalObject() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val person = Person()
        person.username = "person_name"
        val author = Author("author_name")
        person.author = author
        store.save(person)
        val newPerson = store.find(client?.query()?.equals("username", "person_name"))?.result?.get(0)
        val updatedAuthor = Author("updated_author_name")
        newPerson?.author = updatedAuthor
        newPerson?.username = "updated_person_name"
        store.save(newPerson as Person)
        val updatedPerson = store.find(client?.query()?.equals("username", "updated_person_name"))?.result?.get(0)
        assertNotNull(updatedPerson)
        assertEquals("updated_person_name", updatedPerson?.username)
        assertEquals("updated_author_name", updatedPerson?.author?.name)
        updatedPerson?.author = null
        store.save(updatedPerson as Person)
        val updatedPersonWithoutAuthor = store.find(client?.query()?.equals("username", "updated_person_name"))?.result?.get(0)
        assertNotNull(updatedPersonWithoutAuthor)
        assertEquals("updated_person_name", updatedPersonWithoutAuthor?.username)
        assertNull(updatedPersonWithoutAuthor?.author)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUpdateInternalList() {
        val store = collection(Person.COLLECTION, PersonList::class.java, StoreType.SYNC, client)
        val person = PersonList()
        person.username = "person_name"
        var list: MutableList<PersonList> = ArrayList()
        list.add(PersonList("person_name_in_list_1"))
        person.list = list
        store.save(person)
        val newPerson = store.find(client?.query()?.equals("username", "person_name"))?.result?.get(0)
        list = ArrayList()
        list.add(PersonList("person_name_in_list_2"))
        newPerson?.list = list
        newPerson?.username = "updated_person_name"
        store.save(newPerson as PersonList)
        val updatedPerson = store.find(client?.query()?.equals("username", "updated_person_name"))?.result?.get(0)
        assertNotNull(updatedPerson)
        assertEquals("updated_person_name", updatedPerson?.username)
        assertEquals("person_name_in_list_2", updatedPerson?.list?.get(0)?.username)
        (updatedPerson?.list as MutableList?)?.clear()
        updatedPerson?.list = updatedPerson?.list
        store.save(updatedPerson as PersonList)
        val updatedPersonWithoutList = store.find(client?.query()?.equals("username", "updated_person_name"))?.result?.get(0)
        assertNotNull(updatedPersonWithoutList)
        assertEquals("updated_person_name", updatedPersonWithoutList?.username)
        assertEquals(0, updatedPersonWithoutList?.list?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCreateUpdateDeleteSync() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        clearBackend(collection(COLLECTION, Person::class.java, StoreType.NETWORK, client))
        store.clear()
        var person = createPerson(TEST_USERNAME)
        var callback = save(store, person)
        assertNotNull(callback.result)
        assertNotNull(callback.result?.username)
        sync(store, DEFAULT_TIMEOUT)
        person = find(store, DEFAULT_TIMEOUT).result?.result?.get(0) as Person
        person.username = TEST_USERNAME_2
        callback = save(store, person)
        assertNotNull(callback.result)
        assertNotNull(callback.result?.username)
        assertEquals(TEST_USERNAME_2, callback.result?.username)
        val deleteCallback = delete(store, callback.result?.id, DEFAULT_TIMEOUT)
        assertNotNull(deleteCallback.result)
        sync(store, DEFAULT_TIMEOUT)
        val findQuery = client?.query()?.equals(Constants._ID, callback.result?.id)
        val findByIdResult = find(store, findQuery, DEFAULT_TIMEOUT).result?.result?.size
        assertEquals(0, findByIdResult)
        assertEquals(0L, client?.syncManager?.getCount(COLLECTION))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testCreateDeleteSync() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val callback = save(store, person)
        assertNotNull(callback.result)
        assertNotNull(callback.result?.username)
        val deleteCallback = delete(store, callback.result?.id, DEFAULT_TIMEOUT)
        assertNotNull(deleteCallback.result)
        sync(store, DEFAULT_TIMEOUT)
        assertEquals(0, find(store, client?.query()?.equals(Constants._ID, callback.result?.id), DEFAULT_TIMEOUT).result?.result?.size)
        assertEquals(0L, client?.syncManager?.getCount(COLLECTION))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testAsyncPullRequestConstructors() {
        val latch = CountDownLatch(1)
        val looperThread = LooperThread(Runnable {
            val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
            val pullRequest = AsyncPullRequest(store, Query(), null)
            assertNotNull(pullRequest)
            latch.countDown()
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByIds() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        store.clear()
        client?.syncManager?.clear(COLLECTION)
        val ids: MutableList<String> = ArrayList()
        var saveCallback = save(store, createPerson(TEST_USERNAME))
        assertNotNull(saveCallback.result?.id)
        ids.add(saveCallback.result?.id ?: "")
        saveCallback = save(store, createPerson(TEST_USERNAME_2))
        assertNotNull(saveCallback.result?.id)
        ids.add(saveCallback.result?.id ?: "")
        val kinveyListCallback = find(store, ids, DEFAULT_TIMEOUT, null)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertEquals(2, kinveyListCallback.result?.result?.size)
        store.clear()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByIdsCached() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        val testManager = TestManager<Person>()
        testManager.cleanBackend(store, StoreType.CACHE)
        val ids: MutableList<String> = ArrayList()

        var saveCallback = save(store, createPerson(TEST_USERNAME))
        assertNotNull(saveCallback.result?.id)
        ids.add(saveCallback.result?.id ?: "")

        saveCallback = save(store, createPerson(TEST_USERNAME_2))
        assertNotNull(saveCallback.result?.id)
        ids.add(saveCallback.result?.id ?: "")

        val cachedCallback = CustomKinveyCachedCallback<KinveyReadResponse<Person>?>()
        val kinveyListCallback = find(store, ids, DEFAULT_TIMEOUT, cachedCallback)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertEquals(2, kinveyListCallback.result?.result?.size)
        assertNotNull(cachedCallback.result)
        assertNull(cachedCallback.error)
        assertNotNull(cachedCallback.result)
        assertNotNull(cachedCallback.result?.result)
        assertEquals(2, cachedCallback.result?.result?.size)
        testManager.cleanBackend(store, StoreType.CACHE)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testFindByIdsAuto() {
        val store = collection(COLLECTION, Person::class.java, StoreType.AUTO, client)
        val testManager = TestManager<Person>()
        testManager.cleanBackend(store, StoreType.AUTO)
        val ids: MutableList<String> = ArrayList()

        var saveCallback = save(store, createPerson(TEST_USERNAME))
        assertNotNull(saveCallback.result?.id)
        ids.add(saveCallback.result?.id ?: "")

        saveCallback = save(store, createPerson(TEST_USERNAME_2))
        assertNotNull(saveCallback.result?.id)
        ids.add(saveCallback.result?.id ?: "")

        val kinveyListCallback = find(store, ids, DEFAULT_TIMEOUT, null)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertEquals(2, kinveyListCallback.result?.result?.size)
        testManager.cleanBackend(store, StoreType.AUTO)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCount() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        store.clear()
        client?.syncManager?.clear(COLLECTION)

        var saveCallback = save(store, createPerson(TEST_USERNAME))
        assertNotNull(saveCallback.result?.id)

        saveCallback = save(store, createPerson(TEST_USERNAME_2))
        assertNotNull(saveCallback.result?.id)

        val count = findCount(store, DEFAULT_TIMEOUT, null).result
        assertEquals(2, count)
        store.clear()
    }

    @Test
    fun testQuery() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertNotNull(store.query())
    }
}
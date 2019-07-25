package com.kinvey.androidTest.store.data

import android.os.Message
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4

import com.google.api.client.json.GenericJson
import com.kinvey.android.Client
import com.kinvey.android.callback.KinveyDeleteCallback
import com.kinvey.android.callback.KinveyReadCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.UserStore
import com.kinvey.android.sync.KinveyPushCallback
import com.kinvey.android.sync.KinveyPushResponse
import com.kinvey.android.sync.KinveySyncCallback
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.model.EntitySet
import com.kinvey.androidTest.model.Person
import com.kinvey.androidTest.network.MockMultiInsertNetworkManager
import com.kinvey.java.AbstractClient
import com.kinvey.java.Constants
import com.kinvey.java.KinveySaveBatchException
import com.kinvey.java.Logger
import com.kinvey.java.Query
import com.kinvey.java.core.AbstractKinveyClient
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.model.KinveyBatchInsertError
import com.kinvey.java.model.KinveyPullResponse
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.dto.SyncItem
import com.kinvey.java.sync.dto.SyncRequest

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

import java.io.IOException
import java.lang.reflect.Field
import java.util.ArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
@SmallTest
class DataStoreMultiInsertTest {

    private var client: Client<User>? = null

    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Client.Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        AbstractClient.KINVEY_API_VERSION = "5"
        val latch = CountDownLatch(1)
        var looperThread: LooperThread? = null
        if (client?.isUserLoggedIn == false) {
            looperThread = LooperThread(Runnable {
                try {
                    UserStore.login(TestManager.USERNAME, TestManager.PASSWORD, client as Client<User>, object : KinveyClientCallback<User> {
                        override fun onSuccess(result: User) {
                            assertNotNull(result)
                            latch.countDown()
                        }

                        override fun onFailure(error: Throwable) {
                            assertNull(error)
                            latch.countDown()
                        }
                    })
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            })
            looperThread.start()
        } else {
            latch.countDown()
        }
        latch.await()
        looperThread?.mHandler?.sendMessage(Message())
    }

    private class DefaultKinveyClientCallback<T : GenericJson> (private val latch: CountDownLatch) : KinveyClientCallback<T> {
        var result: T? = null
        var error: Throwable? = null

        override fun onSuccess(result: T) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    private class DefaultKinveyClientListCallback<T : GenericJson> (private val latch: CountDownLatch) : KinveyClientCallback<List<T>> {
        var result: List<T>? = null
        var error: Throwable? = null

        override fun onSuccess(result: List<T>) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    private class DefaultKinveyDeleteCallback (private val latch: CountDownLatch) : KinveyDeleteCallback {
        var result: Int? = null
        var error: Throwable? = null

        override fun onSuccess(result: Int?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    private class DefaultKinveyReadCallback<T : GenericJson> (private val latch: CountDownLatch) : KinveyReadCallback<T> {
        var result: KinveyReadResponse<T>? = null
        var error: Throwable? = null

        override fun onSuccess(result: KinveyReadResponse<T>) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    private class DefaultKinveyPushCallback (private val latch: CountDownLatch) : KinveyPushCallback {
        var result: KinveyPushResponse? = null
        var error: Throwable? = null

        override fun onSuccess(result: KinveyPushResponse) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable) {
            this.error = error
            finish()
        }

        override fun onProgress(current: Long, all: Long) {

        }

        internal fun finish() {
            latch.countDown()
        }
    }

    private class DefaultKinveySyncCallback (private val latch: CountDownLatch) : KinveySyncCallback {
        var kinveyPushResponse: KinveyPushResponse? = null
        var kinveyPullResponse: KinveyPullResponse? = null
        var error: Throwable? = null

        override fun onSuccess(kinveyPushResponse: KinveyPushResponse, kinveyPullResponse: KinveyPullResponse) {
            this.kinveyPushResponse = kinveyPushResponse
            this.kinveyPullResponse = kinveyPullResponse
            finish()
        }

        override fun onPullStarted() {

        }

        override fun onPushStarted() {

        }

        override fun onPullSuccess(kinveyPullResponse: KinveyPullResponse) {
            this.kinveyPullResponse = kinveyPullResponse
        }

        override fun onPushSuccess(kinveyPushResponse: KinveyPushResponse) {
            this.kinveyPushResponse = kinveyPushResponse
        }

        override fun onFailure(error: Throwable) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    private fun createPerson(name: String): Person {
        return Person(name)
    }

    @Throws(InterruptedException::class)
    private fun <T : GenericJson> delete(store: DataStore<T>, query: Query): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store.delete(query, callback) })
        looperThread.start()
        latch.await(120, TimeUnit.SECONDS)
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun <T : GenericJson> find(store: DataStore<T>, seconds: Int): DefaultKinveyReadCallback<T> {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyReadCallback<T>(latch)
        val looperThread = LooperThread(Runnable { store.find(callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun sync(store: DataStore<Person>, seconds: Int): DefaultKinveySyncCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveySyncCallback(latch)
        val looperThread = LooperThread(Runnable { store.sync(callback) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun <T : GenericJson> clearBackend(store: DataStore<T>) {
        var query = client?.query()
        query = query?.notEqual("age", "100500")
        query?.let { delete(store, query) }
    }

    private fun pendingSyncEntities(collectionName: String): List<SyncItem>? {
        return client?.syncManager?.popSingleItemQueue(collectionName)
    }

    private fun print(msg: String) {
        Logger.INFO(msg)
        println(msg)
    }

    @Throws(InterruptedException::class)
    private fun <T : GenericJson> saveList(store: DataStore<T>, persons: List<T>): DefaultKinveyClientListCallback<T> {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientListCallback<T>(latch)
        val looperThread = LooperThread(Runnable {
            try {
                store.save(persons, callback)
            } catch (e: Exception) {
                callback.onFailure(e)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun <T : GenericJson> save(store: DataStore<T>, item: T): DefaultKinveyClientCallback<T> {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback<T>(latch)
        val looperThread = LooperThread(Runnable { store.save(item, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    private fun <T : GenericJson> push(store: DataStore<T>, seconds: Int): DefaultKinveyPushCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyPushCallback(latch)
        val looperThread = LooperThread(Runnable { store.push(callback) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    private fun mockInvalidConnection() {
        var field: Field? = null
        try {
            field = AbstractKinveyClient::class.java.getDeclaredField("rootUrl")
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
        assert(field != null)
        field?.isAccessible = true
        try {
            field?.set(client, "https://bmock.kinvey.com/")
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    private fun cancelMockInvalidConnection() {
        var field: Field? = null
        try {
            field = AbstractKinveyClient::class.java.getDeclaredField("rootUrl")
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
        assert(field != null)
        field?.isAccessible = true
        try {
            field?.set(client, AbstractClient.DEFAULT_BASE_URL)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    private fun isBackendItem(item: GenericJson?): Boolean {
        return item?.containsKey(Constants._KMD) == true && item.containsKey(Constants._ACL)
    }

    // create an array with a few items that have _id property or have not.
    private fun createPersonsList(count: Int, withId: Boolean): List<Person> {
        val items = ArrayList<Person>()
        for (i in 0 until count) {
            val person = createPerson(TEST_USERNAME + i.toString())
            if (withId) {
                val id = "123456$i"
                person.id = id
            }
            items.add(person)
        }
        return items
    }

    private fun createPersonsList(withId: Boolean): List<Person> {
        return createPersonsList(MAX_PERSONS_COUNT, withId)
    }

    // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no _id, _id]
    private fun createCombineList(): List<Person> {
        val items = ArrayList<Person>()
        val person1 = Person(TEST_USERNAME + 1.toString())
        items.add(person1)
        val person2 = Person("76575", TEST_USERNAME + 2.toString())
        items.add(person2)
        val person3 = Person(TEST_USERNAME + 3.toString())
        items.add(person3)
        val person4 = Person("53521", TEST_USERNAME + 4.toString())
        items.add(person4)
        return items
    }

    // create an array containing two items failing for different reasons
    private fun createPushErrList(): List<Person> {
        val items = ArrayList<Person>()
        val errStr = ERR_GEOLOC
        val person1 = Person(TEST_USERNAME + 1.toString())
        person1.geoloc = errStr
        items.add(person1)
        val person2 = Person(TEST_USERNAME + 2.toString())
        person2.geoloc = errStr
        items.add(person2)
        val person3 = Person(TEST_USERNAME + 3.toString())
        items.add(person3)
        return items
    }

    // create an array containing two items failing for different reasons
    private fun createErrList(): List<Person> {
        val items = ArrayList<Person>()
        val errStr = ERR_GEOLOC
        val person1 = Person(TEST_USERNAME + 1.toString())
        person1.geoloc = errStr
        items.add(person1)
        val person2 = Person(TEST_USERNAME + 2.toString())
        person2.geoloc = errStr
        items.add(person2)
        return items
    }

    // create an array of items with no _id and the second of them should have invalid _geoloc params
    private fun createErrList1(): List<Person> {
        val items = ArrayList<Person>()
        val person1 = Person(TEST_USERNAME + 1.toString())
        items.add(person1)
        val person2 = Person(TEST_USERNAME + 2.toString())
        person2.geoloc = ERR_GEOLOC
        items.add(person2)
        return items
    }

    // create an array of items with no _id and the second of them should have invalid _geoloc params
    private fun createErrList2(): List<Person> {
        val items = ArrayList<Person>()
        val person1 = Person(TEST_USERNAME + 1.toString())
        items.add(person1)
        val person2 = Person(TEST_USERNAME + 2.toString())
        person2.geoloc = ERR_GEOLOC
        items.add(person2)
        val person3 = Person(TEST_USERNAME + 3.toString())
        items.add(person3)
        return items
    }

    // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
    private fun createErrListGeoloc(): List<Person> {
        val items = ArrayList<Person>()
        val errStr = ERR_GEOLOC
        val person1 = Person(TEST_USERNAME + 1.toString())
        person1.geoloc = errStr
        items.add(person1)
        val person2 = Person("76575", TEST_USERNAME + 2.toString())
        items.add(person2)
        val person3 = Person("343275", TEST_USERNAME + 3.toString())
        person1.geoloc = errStr
        items.add(person3)
        val person4 = Person(TEST_USERNAME + 4.toString())
        items.add(person4)
        return items
    }

    private fun createEntityList(itemsCount: Int): List<EntitySet> {
        val items = ArrayList<EntitySet>()
        var entity: EntitySet? = null
        for (i in 0 until itemsCount) {
            entity = EntitySet()
            entity.description = "entity #$i"
            items.add(entity)
        }
        return items
    }

    private fun <T : GenericJson> getResultCheckFields(resultList: List<T?>?, checkField: String): List<String> {
        return resultList?.map { item ->
            item?.get(checkField).toString()
        } ?: mutableListOf()
    }

    @Throws(AssertionError::class)
    private fun <T : GenericJson> checkIfItemsAtRightIndex(srcList: List<T>, resultList: List<T?>?,
                                                           checkIndexes: IntArray, checkField: String, checkErr: Boolean, checkIsBackendItem: Boolean): Boolean {

        assertNotNull(srcList)
        assertNotNull(resultList)

        var result = true
        var srcItem: GenericJson
        var item: GenericJson?
        val resultFields = getResultCheckFields(resultList, checkField)
        for ((curIdx, idx) in checkIndexes.withIndex()) {
            item = resultList?.get(curIdx)
            if (checkErr) {
                result = result and (item == null)
            } else {
                srcItem = srcList[idx]
                result = result and (item != null)
                if (checkIsBackendItem) {
                    result = result and isBackendItem(item)
                }
                val srcField = srcItem.get(checkField).toString()
                result = result and resultFields.contains(srcField)
            }
        }
        return result
    }

    @Throws(AssertionError::class)
    private fun <T : GenericJson> checkIfSameObjects(srcList: List<T>, resultList: List<T?>?, checkField: String, checkIsBackendItem: Boolean): Boolean {

        assertNotNull(srcList)
        assertNotNull(resultList)

        var result = true
        var srcItem: GenericJson?
        var resultItem: GenericJson?
        val resultFields = getResultCheckFields(resultList, checkField)
        for (i in srcList.indices) {
            srcItem = srcList[i]
            resultItem = resultList?.get(i)
            if (checkIsBackendItem) {
                result = result and isBackendItem(resultItem)
            }
            val srcField = srcItem[checkField].toString()
            result = result and resultFields.contains(srcField)
        }
        return result
    }

    @Throws(AssertionError::class)
    private fun checkBatchResponseErrors(errors: List<KinveyBatchInsertError>?, checkIndexes: IntArray, checkErrMsg: Boolean = false, errMessages: Array<String>? = null): Boolean {
        assertNotNull(errors)
        assertNotNull(checkIndexes)
        assertEquals(errors?.count(), checkIndexes.count())
        if (checkErrMsg) {
            assertNotNull(errMessages)
            assertEquals(checkIndexes.count(), errMessages?.count())
        }
        var result = true
        var err: KinveyBatchInsertError?
        for ((curIdx, idx) in checkIndexes.withIndex()) {
            err = errors?.get(curIdx)
            result = result and (idx == err?.index)
            if (checkErrMsg) {
                val msg = errMessages?.get(curIdx) ?: ""
                result = result and (err?.errorMessage?.contains(msg) == true)
            }
        }
        return result
    }

    @Throws(AssertionError::class)
    private fun <T : GenericJson> checkSyncItems(list: List<SyncItem>?, checkCount: Int, requestMethod: SyncRequest.HttpVerb): Boolean {
        assertEquals(list?.count(), checkCount)
        var result = false
        list?.let { items ->
            result = true
            items.onEach { item -> result = result and (requestMethod == item.requestMethod) }
        }
        return result
    }

    @Throws(AssertionError::class)
    private fun checkPersonIfSameObjects(srcList: List<Person>, resultList: List<Person>?, checkIsBackendItem: Boolean): Boolean {
        return checkIfSameObjects(srcList, resultList, Person.USERNAME_KEY, checkIsBackendItem)
    }

    @Throws(AssertionError::class)
    private fun checkPersonIfSameObjects(srcList: List<Person>, resultList: List<Person>?): Boolean {
        return checkIfSameObjects(srcList, resultList, Person.USERNAME_KEY, true)
    }

    private fun checkIfPersonItemsAtRightIndex(srcList: List<Person>, resultList: List<Person>?, checkIndexes: IntArray, checkErr: Boolean): Boolean {
        return checkIfItemsAtRightIndex(srcList, resultList, checkIndexes, Person.USERNAME_KEY, checkErr, true)
    }

    @Throws(InterruptedException::class)
    private fun <T : GenericJson> testSaveEmptyList(list: List<T>, cls: Class<T>, collection: String, storeType: StoreType) {
        val personStore = DataStore.collection(collection, cls, storeType, client)
        clearBackend(personStore)
        client?.syncManager?.clear(Person.COLLECTION)

        val saveCallback = saveList(personStore, list)
        assertNull(saveCallback.result)
        val error = saveCallback.error
        assertNotNull(error)
        assertEquals(error?.javaClass, IllegalStateException::class.java)
        assertTrue(error?.message?.contains(ERR_EMPTY_LIST_MSG) == true)
    }

    // save an item without _id
    // save another item without _id
    // push(), should use a multi-insert POST request and return the items with POST operations
    // pendingSyncEntities(), should return an empty array
    // find using syncstore, should return all items from step 1
    @Throws(InterruptedException::class)
    private fun testPushMultiInsertSupport(storeType: StoreType) {

        val personsList = createPersonsList(2, false)

        val netManager = MockMultiInsertNetworkManager(Person.COLLECTION, Person::class.java, client)
        val personStore = DataStore(Person.COLLECTION, Person::class.java, client, storeType, netManager)
        val storeSync = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.SYNC, netManager)

        clearBackend(personStore)
        clearBackend(storeSync)
        client?.syncManager?.clear(Person.COLLECTION)

        for (item in personsList) {
            save(storeSync, item)
        }

        netManager.clear()
        val pushCallback = push(personStore, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(pushCallback.result?.successCount, personsList.count())
        assertTrue(netManager.useMultiInsertSave())

        val syncItems = pendingSyncEntities(Person.COLLECTION)
        assertTrue(syncItems == null || syncItems.isEmpty())

        val findCallback = find(storeSync, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personsList, findCallback.result?.result))
    }

    // create an array of 3 items, the second of which has invalid _geoloc parameters
    // save()
    // Sync(), Should return error for not pushed item
    // pendingSyncEntities(), should return the item with invalid params
    // find() using networkstore, should return the valid items
    // find using syncstore, should return all items including the invalid one
    @Throws(InterruptedException::class)
    private fun testSyncItemsList(mockConnectionErr: Boolean, storeType: StoreType) {

        val personList = createErrList2()
        val checkIndexesSuccess = intArrayOf(0, 2)
        val checkIndexesErr = intArrayOf(1)
        val personStoreCurrent = DataStore.collection(Person.COLLECTION, Person::class.java, storeType, client)
        val personStoreNet = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val personStoreSync = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)

        clearBackend(personStoreCurrent)
        clearBackend(personStoreNet)
        clearBackend(personStoreSync)
        client?.syncManager?.clear(Person.COLLECTION)

        if (mockConnectionErr) {
            mockInvalidConnection()
        }
        saveList(personStoreSync, personList)
        if (mockConnectionErr) {
            cancelMockInvalidConnection()
        }

        val syncCallback = sync(personStoreCurrent, LONG_TIMEOUT)
        assertNotNull(syncCallback.error)
        if (syncCallback.error is KinveySaveBatchException) {
            val resultEntities = (syncCallback.error as KinveySaveBatchException).entities as List<Person>?
            val errorsList = (syncCallback.error as KinveySaveBatchException).errors
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesSuccess, false))
            assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesErr, true))
            val errMessages = arrayOf(ERR_GEOLOC_MSG)
            assertTrue(checkBatchResponseErrors(errorsList, checkIndexesErr, true, errMessages))
        }

        val syncItems = pendingSyncEntities(Person.COLLECTION)
        assertNotNull(syncItems)
        assertEquals(syncItems!!.size.toLong(), 1)

        val findCallbackNet = find(personStoreNet, LONG_TIMEOUT)
        val findCallbackSync = find(personStoreSync, LONG_TIMEOUT)

        assertNotNull(findCallbackNet.result)
        assertTrue(checkIfPersonItemsAtRightIndex(personList, findCallbackNet.result?.result, checkIndexesSuccess, false))

        assertNotNull(findCallbackSync.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result))
    }

    // NETWORK STORE

    @Test
    @Throws(InterruptedException::class)
    fun testSaveWithoutIdNetwork() {
        print("should send POST with a single item with no _id")
        // create an item that has no _id property
        // call save() with it
        // find using network store

        val netStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(netStore)
        client?.syncManager?.clear(Person.COLLECTION)

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

        val netStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(netStore)
        client?.syncManager?.clear(Person.COLLECTION)

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

        val personStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)
        client?.syncManager?.clear(Person.COLLECTION)

        val saveCallback = saveList(personStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result))

        val findCallback = find(personStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, findCallback.result?.getResult()))
    }

    @Test
    @Throws(InterruptedException::class)
    fun testSaveListWithIdNetwork() {
        print("should sent PUT requests for an array of items with _id")
        // create an array with a few items that have _id property
        // save() with the array as param
        // find using network store

        val personList = createPersonsList(true)

        val personStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)
        client?.syncManager?.clear(Person.COLLECTION)

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

        val personStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(personStore)
        client?.syncManager?.clear(Person.COLLECTION)

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
        testSaveEmptyList(list, Person::class.java, Person.COLLECTION, StoreType.NETWORK)
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

        val personStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
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

        val personStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
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

        val personStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
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

        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(syncStore)
        client?.syncManager?.clear(Person.COLLECTION)

        val person = createPerson(TEST_USERNAME)

        val saveCallback = save(syncStore, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val resultPerson = saveCallback.result as Person
        assertNotNull(resultPerson.id)
        assertEquals(person.username, resultPerson.username)

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(syncStore)
        client?.syncManager?.clear(Person.COLLECTION)

        val id = "123456"
        val person = createPerson(TEST_USERNAME)
        person.id = id

        val saveCallback = save(syncStore, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        val resultPerson = saveCallback.result as Person
        assertNotNull(resultPerson)
        assertEquals(id, resultPerson.id)

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)

        clearBackend(syncStore)
        client?.syncManager?.clear(Person.COLLECTION)

        val saveCallback = saveList(syncStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result, false))

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)

        clearBackend(syncStore)
        client?.syncManager?.clear(Person.COLLECTION)

        val saveCallback = saveList(syncStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result, false))

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)

        clearBackend(syncStore)
        client?.syncManager?.clear(Person.COLLECTION)

        val saveCallback = saveList(syncStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result, false))

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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
        testSaveEmptyList(list, Person::class.java, Person.COLLECTION, StoreType.SYNC)
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

        val storeSync = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        client?.syncManager?.clear(Person.COLLECTION)

        val saveCallbackSecond = saveList(storeSync, personsList)
        assertNull(saveCallbackSecond.error)
        assertNotNull(saveCallbackSecond.result)
        assertTrue(checkPersonIfSameObjects(personsList, saveCallbackSecond.result, false))

        val pushCallback = push(storeSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val storeSync = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        client?.syncManager?.clear(Person.COLLECTION)

        val saveCallbackSecond = saveList(storeSync, personsList)
        assertNull(saveCallbackSecond.error)
        assertNotNull(saveCallbackSecond.result)
        assertTrue(checkPersonIfSameObjects(personsList, saveCallbackSecond.result, false))

        val pushCallback = push(storeSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val mockNetManager = MockMultiInsertNetworkManager(Person.COLLECTION, Person::class.java, client)
        val storeSync = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.SYNC, mockNetManager)
        clearBackend(storeSync)
        client?.syncManager?.clear(Person.COLLECTION)

        saveList(storeSync, personsList)

        val pushCallback = push(storeSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)
        assertTrue(mockNetManager.useMultiInsertSave())

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val storeSync = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(storeSync)
        client?.syncManager?.clear(Person.COLLECTION)

        saveList(storeSync, personsList)

        val pushCallback = push(storeSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(1, pushCallback.result?.successCount)

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val netStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(Person.COLLECTION)

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

        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val netStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(Person.COLLECTION)

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

        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        client?.syncManager?.clear(Person.COLLECTION)

        mockInvalidConnection()
        save(autoStore, person)
        cancelMockInvalidConnection()

        val findCallback = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        val list = findCallback.result?.result
        assertEquals(1, list?.count())
        assertNotNull(list?.get(0)?.id)

        val pendingList = pendingSyncEntities(Person.COLLECTION)
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

        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        client?.syncManager?.clear(Person.COLLECTION)

        mockInvalidConnection()
        save(autoStore, person)
        cancelMockInvalidConnection()

        val findCallback = find(syncStore, LONG_TIMEOUT)
        assertNotNull(findCallback.result)
        val list = findCallback.result?.result
        assertEquals(1, list?.count())
        assertEquals(testId, list?.get(0)?.id)

        val pendingList = pendingSyncEntities(Person.COLLECTION)
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

        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val netStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(Person.COLLECTION)

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

        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val netStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoStore)
        clearBackend(netStore)
        client?.syncManager?.clear(Person.COLLECTION)

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

        val mockNetManager = MockMultiInsertNetworkManager(Person.COLLECTION, Person::class.java, client)
        val autoStore = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.AUTO, mockNetManager)
        val syncStore = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.SYNC, mockNetManager)
        val netStore = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.NETWORK, mockNetManager)

        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(Person.COLLECTION)

        mockNetManager.clear()
        val saveCallback = saveList(autoStore, personList)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result)
        assertTrue(checkPersonIfSameObjects(personList, saveCallback.result))
        assertTrue(mockNetManager.useMultiInsertSave())

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
        testSaveEmptyList(list, Person::class.java, Person.COLLECTION, StoreType.AUTO)

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        client?.syncManager?.clear(Person.COLLECTION)

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

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        val netStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        clearBackend(netStore)
        client?.syncManager?.clear(Person.COLLECTION)

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

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val autoStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val syncStore = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.SYNC, client)
        clearBackend(autoStore)
        clearBackend(syncStore)
        client?.syncManager?.clear(Person.COLLECTION)

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

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val storeNet = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        val storeAuto = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(storeNet)
        clearBackend(storeAuto)
        client?.syncManager?.clear(Person.COLLECTION)

        mockInvalidConnection()
        saveList(storeAuto, personsList)
        cancelMockInvalidConnection()

        val pushCallback = push(storeAuto, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(pushCallback.result?.successCount, personsList.count())

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val autoSync = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        val netSync = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.NETWORK, client)
        clearBackend(autoSync)
        clearBackend(netSync)
        client?.syncManager?.clear(Person.COLLECTION)

        mockInvalidConnection()
        saveList(autoSync, personsList)
        cancelMockInvalidConnection()

        val pushCallback = push(autoSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val mockNetManager = MockMultiInsertNetworkManager(Person.COLLECTION, Person::class.java, client)
        val autoSync = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.AUTO, mockNetManager)
        val netSync = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.NETWORK, mockNetManager)

        clearBackend(autoSync)
        clearBackend(netSync)
        client?.syncManager?.clear(Person.COLLECTION)

        mockInvalidConnection()
        saveList(autoSync, personsList)
        cancelMockInvalidConnection()

        val pushCallback = push(autoSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)
        assertTrue(mockNetManager.useMultiInsertSave())

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

        val autoSync = DataStore.collection(Person.COLLECTION, Person::class.java, StoreType.AUTO, client)
        clearBackend(autoSync)
        client?.syncManager?.clear(Person.COLLECTION)

        mockInvalidConnection()
        saveList(autoSync, personsList)
        cancelMockInvalidConnection()

        val pushCallback = push(autoSync, LONG_TIMEOUT)
        assertNotNull(pushCallback.result)
        assertEquals(personsList.count(), pushCallback.result?.successCount)

        val syncItems = pendingSyncEntities(Person.COLLECTION)
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

    companion object {

        private const val TEST_USERNAME = "Test_UserName"
        private const val DEFAULT_TIMEOUT = 60
        private const val MAX_PERSONS_COUNT = 5
        private const val LONG_TIMEOUT = 6 * DEFAULT_TIMEOUT

        private const val ERR_GEOLOC = "#!@%^&*())_+?{}"
        private const val ERR_GEOLOC_MSG = "The value specified for one of the request parameters is out of range"
        private const val ERR_PERMISSION_MSG = "The credentials used to authenticate this request are not authorized to run this operation"
        private const val ERR_EMPTY_LIST_MSG = "Entity list cannot be empty"

    }
}

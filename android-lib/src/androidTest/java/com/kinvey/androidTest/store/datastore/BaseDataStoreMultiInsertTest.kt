package com.kinvey.androidTest.store.datastore

import android.os.Message
import androidx.test.platform.app.InstrumentationRegistry
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
import com.kinvey.java.*
import com.kinvey.java.AbstractClient.Companion.kinveyApiVersion
import com.kinvey.java.core.AbstractKinveyClient
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.model.KinveyBatchInsertError
import com.kinvey.java.model.KinveyPullResponse
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.dto.SyncItem
import com.kinvey.java.sync.dto.SyncRequest
import org.junit.Assert
import org.junit.Before
import java.io.IOException
import java.lang.reflect.Field
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

open class BaseDataStoreMultiInsertTest {

    protected lateinit var client: Client<User>

    companion object {
        const val TEST_USERNAME = "Test_UserName"
        const val DEFAULT_TIMEOUT = 60
        const val MAX_PERSONS_COUNT = 5
        const val LONG_TIMEOUT = 6 * DEFAULT_TIMEOUT

        const val ERR_GEOLOC = "#!@%^&*())_+?{}"
        const val ERR_GEOLOC_MSG = "The value specified for one of the request parameters is out of range"
        const val ERR_PERMISSION_MSG = "The credentials used to authenticate this request are not authorized to run this operation"
        const val ERR_EMPTY_LIST_MSG = "Entity list cannot be empty"
    }

    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        kinveyApiVersion = "5"
        client = Client.Builder<User>(mMockContext).build()
        client.enableDebugLogging()
        kinveyApiVersion = "5"
        val latch = CountDownLatch(1)
        var looperThread: LooperThread? = null
        if (!client.isUserLoggedIn) {
            looperThread = LooperThread(Runnable {
                try {
                    UserStore.login(TestManager.USERNAME, TestManager.PASSWORD, client as Client<User>, object : KinveyClientCallback<User> {
                        override fun onSuccess(result: User?) {
                            Assert.assertNotNull(result)
                            latch.countDown()
                        }

                        override fun onFailure(error: Throwable?) {
                            Assert.assertNull(error)
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

    protected class DefaultKinveyClientCallback<T : GenericJson> (private val latch: CountDownLatch) : KinveyClientCallback<T?> {
        var result: T? = null
        var error: Throwable? = null

        override fun onSuccess(result: T?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    protected class DefaultKinveyClientListCallback<T : GenericJson> (private val latch: CountDownLatch) : KinveyClientCallback<List<T>> {
        var result: List<T>? = null
        var error: Throwable? = null

        override fun onSuccess(result: List<T>?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    protected class DefaultKinveyDeleteCallback (private val latch: CountDownLatch) : KinveyDeleteCallback {
        var result: Int? = null
        var error: Throwable? = null

        override fun onSuccess(result: Int?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    protected class DefaultKinveyReadCallback<T : GenericJson> (private val latch: CountDownLatch) : KinveyReadCallback<T> {
        var result: KinveyReadResponse<T>? = null
        var error: Throwable? = null

        override fun onSuccess(result: KinveyReadResponse<T>?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    protected class DefaultKinveyPushCallback (private val latch: CountDownLatch) : KinveyPushCallback {
        var result: KinveyPushResponse? = null
        var error: Throwable? = null

        override fun onSuccess(result: KinveyPushResponse?) {
            this.result = result
            finish()
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        override fun onProgress(current: Long, all: Long) {

        }

        internal fun finish() {
            latch.countDown()
        }
    }

    protected class DefaultKinveySyncCallback (private val latch: CountDownLatch) : KinveySyncCallback {
        var kinveyPushResponse: KinveyPushResponse? = null
        var kinveyPullResponse: KinveyPullResponse? = null
        var error: Throwable? = null

        override fun onSuccess(kinveyPushResponse: KinveyPushResponse?, kinveyPullResponse: KinveyPullResponse?) {
            this.kinveyPushResponse = kinveyPushResponse
            this.kinveyPullResponse = kinveyPullResponse
            finish()
        }

        override fun onPullStarted() {

        }

        override fun onPushStarted() {

        }

        override fun onPullSuccess(kinveyPullResponse: KinveyPullResponse?) {
            this.kinveyPullResponse = kinveyPullResponse
        }

        override fun onPushSuccess(kinveyPushResponse: KinveyPushResponse?) {
            this.kinveyPushResponse = kinveyPushResponse
        }

        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }

        internal fun finish() {
            latch.countDown()
        }
    }

    fun createPerson(name: String): Person {
        return Person(name)
    }

    @Throws(InterruptedException::class)
    protected fun <T : GenericJson> delete(store: DataStore<T>, query: Query): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store.delete(query, callback) })
        looperThread.start()
        latch.await(120, TimeUnit.SECONDS)
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    protected fun <T : GenericJson> find(store: DataStore<T>, seconds: Int): DefaultKinveyReadCallback<T> {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyReadCallback<T>(latch)
        val looperThread = LooperThread(Runnable { store.find(callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    protected fun sync(store: DataStore<Person>, seconds: Int): DefaultKinveySyncCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveySyncCallback(latch)
        val looperThread = LooperThread(Runnable { store.sync(callback) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun <T : GenericJson> clearBackend(store: DataStore<T>) {
        var query = client?.query()
        query = query?.notEqual("age", "100500")
        query?.let { delete(store, query) }
    }

    fun pendingSyncEntities(collectionName: String): List<SyncItem>? {
        return client?.syncManager?.popSingleItemQueue(collectionName)
    }

    fun print(msg: String) {
        Logger.INFO(msg)
        println(msg)
    }

    @Throws(InterruptedException::class)
    protected fun <T : GenericJson> saveList(store: DataStore<T>, items: List<T>): DefaultKinveyClientListCallback<T> {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientListCallback<T>(latch)
        val looperThread = LooperThread(Runnable {
            try {
                store.save(items, callback)
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
    protected fun <T : GenericJson> save(store: DataStore<T>, item: T): DefaultKinveyClientCallback<T> {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback<T>(latch)
        val looperThread = LooperThread(Runnable { store.save(item, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    protected fun <T : GenericJson> push(store: DataStore<T>, seconds: Int): DefaultKinveyPushCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyPushCallback(latch)
        val looperThread = LooperThread(Runnable { store.push(callback) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler.sendMessage(Message())
        return callback
    }

    fun mockInvalidConnection() {
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

    fun cancelMockInvalidConnection() {
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

    fun isBackendItem(item: GenericJson?): Boolean {
        return item?.containsKey(Constants._KMD) == true && item.containsKey(Constants._ACL)
    }

    // create an array with a few items that have _id property or have not.
    fun createPersonsList(count: Int, error: Boolean = false, withId: Boolean = false): List<Person> {
        return 1.rangeTo(count).map { i ->
            val person = createPerson(TEST_USERNAME + i.toString())
            if (withId) {
                val id = "123456$i"
                person.id = id
            }
            if (error) {
                person.geoloc = ERR_GEOLOC
            }
            person
        }
    }

    // create an array with a few items that have _id property or have not.
    fun createPersonsListErr(count: Int, errCount: Int, errPos: Int = count): List<Person> {
        val items = createPersonsList(count, error = false, withId = false)
        val itemsErr = createPersonsList(errCount, error = true, withId = false)
        (items as MutableList).addAll(errPos, itemsErr)
        return items
    }

    fun createPersonsList(withId: Boolean): List<Person> {
        return createPersonsList(MAX_PERSONS_COUNT, error = false, withId = withId)
    }

    fun createEntityList(itemsCount: Int): List<EntitySet> {
        return 1.rangeTo(itemsCount).map { i ->
            val entity = EntitySet()
            entity.description = "entity #$i"
            entity
        }
    }

    // create an array that has 2 items with _id and 2 without in the following order - [no _id, _id, no _id, _id]
    fun createCombineList(): List<Person> {
        val items = ArrayList<Person>()
        val person1 = Person("$TEST_USERNAME${1}")
        items.add(person1)
        val person2 = Person("76575", "$TEST_USERNAME${2}")
        items.add(person2)
        val person3 = Person("$TEST_USERNAME${3}")
        items.add(person3)
        val person4 = Person("53521", "$TEST_USERNAME${4}")
        items.add(person4)
        return items
    }

    // create an array containing two items failing for different reasons
    fun createPushErrList(): List<Person> {
        val items = ArrayList<Person>()
        val errStr = ERR_GEOLOC
        val person1 = Person("$TEST_USERNAME${1}")
        person1.geoloc = errStr
        items.add(person1)
        val person2 = Person("$TEST_USERNAME${2}")
        person2.geoloc = errStr
        items.add(person2)
        val person3 = Person("$TEST_USERNAME${3}")
        items.add(person3)
        return items
    }

    // create an array containing two items failing for different reasons
    fun createErrList(): List<Person> {
        val items = ArrayList<Person>()
        val errStr = ERR_GEOLOC
        val person1 = Person("$TEST_USERNAME${1}")
        person1.geoloc = errStr
        items.add(person1)
        val person2 = Person("$TEST_USERNAME${2}")
        person2.geoloc = errStr
        items.add(person2)
        return items
    }

    // create an array of items with no _id and the second of them should have invalid _geoloc params
    fun createErrList1(): List<Person> {
        val items = ArrayList<Person>()
        val person1 = Person("$TEST_USERNAME${1}")
        items.add(person1)
        val person2 = Person("$TEST_USERNAME${2}")
        person2.geoloc = ERR_GEOLOC
        items.add(person2)
        return items
    }

    // create an array of items with no _id and the second of them should have invalid _geoloc params
    fun createErrList2(): List<Person> {
        val items = ArrayList<Person>()
        val person1 = Person("$TEST_USERNAME${1}")
        items.add(person1)
        val person2 = Person("$TEST_USERNAME${2}")
        person2.geoloc = ERR_GEOLOC
        items.add(person2)
        val person3 = Person("$TEST_USERNAME${3}")
        items.add(person3)
        return items
    }

    // create an array  - [{no_id, invalid_geoloc},{_id}, {_id, invalid_geoloc}, {no_id}]
    fun createErrListGeoloc(): List<Person> {
        val items = ArrayList<Person>()
        val errStr = ERR_GEOLOC
        val person1 = Person("$TEST_USERNAME${1}")
        person1.geoloc = errStr
        items.add(person1)
        val person2 = Person("76575", "$TEST_USERNAME${2}")
        items.add(person2)
        val person3 = Person("343275", "$TEST_USERNAME${3}")
        person1.geoloc = errStr
        items.add(person3)
        val person4 = Person("$TEST_USERNAME${4}")
        items.add(person4)
        return items
    }

    fun <T : GenericJson> getResultCheckFields(resultList: List<T?>?, checkField: String): List<String> {
        return resultList?.map { item ->
            item?.get(checkField).toString()
        } ?: mutableListOf()
    }

    @Throws(AssertionError::class)
    fun <T : GenericJson> checkIfItemsAtRightIndex(srcList: List<T>, resultList: List<T?>?,
                                                   checkIndexes: IntArray, checkField: String,
                                                   checkErr: Boolean, checkIsBackendItem: Boolean): Boolean {
        Assert.assertNotNull(srcList)
        Assert.assertNotNull(resultList)

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
    fun <T : GenericJson> checkIfSameObjects(srcList: List<T>, resultList: List<T?>?,
                                             checkField: String, checkIsBackendItem: Boolean): Boolean {

        Assert.assertNotNull(srcList)
        Assert.assertNotNull(resultList)

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
    fun checkBatchResponseErrors(errors: List<KinveyBatchInsertError>?, checkIndexes: IntArray,
                                 checkErrMsg: Boolean = false, errMessages: Array<String>? = null): Boolean {
        Assert.assertNotNull(errors)
        Assert.assertNotNull(checkIndexes)
        Assert.assertEquals(errors?.count(), checkIndexes.count())
        if (checkErrMsg) {
            Assert.assertNotNull(errMessages)
            Assert.assertEquals(checkIndexes.count(), errMessages?.count())
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
    fun <T : GenericJson> checkSyncItems(list: List<SyncItem>?, checkCount: Int,
                                                 requestMethod: SyncRequest.HttpVerb): Boolean {
        Assert.assertEquals(list?.count(), checkCount)
        var result = false
        list?.let { items ->
            result = true
            items.onEach { item -> result = result and (requestMethod == item.requestMethod) }
        }
        return result
    }

    @Throws(AssertionError::class)
    fun checkPersonIfSameObjects(srcList: List<Person>, resultList: List<Person>?, checkIsBackendItem: Boolean): Boolean {
        return checkIfSameObjects(srcList, resultList, Person.USERNAME_KEY, checkIsBackendItem)
    }

    @Throws(AssertionError::class)
    fun checkPersonIfSameObjects(srcList: List<Person>, resultList: List<Person>?): Boolean {
        return checkIfSameObjects(srcList, resultList, Person.USERNAME_KEY, true)
    }

    fun checkIfPersonItemsAtRightIndex(srcList: List<Person>, resultList: List<Person>?,
                                       checkIndexes: IntArray, checkErr: Boolean): Boolean {
        return checkIfItemsAtRightIndex(srcList, resultList, checkIndexes, Person.USERNAME_KEY, checkErr, true)
    }

    @Throws(AssertionError::class)
    fun checkPersonListIfSameOrder(personList: List<Person>, resultPersonList: List<Person>?): Boolean {
        Assert.assertTrue(personList.count() == resultPersonList?.count())
        var result: Boolean? = false
        resultPersonList?.let { list ->
            result = personList.zip(list) { p, r ->  p.username == r.username}.min()
        }
        return result ?: false
    }

    @Throws(InterruptedException::class)
    fun <T : GenericJson> testSaveEmptyList(list: List<T>, cls: Class<T>, collection: String, storeType: StoreType) {
        val personStore = DataStore.collection(collection, cls, storeType, client)
        clearBackend(personStore)
        client?.syncManager?.clear(Person.COLLECTION)

        val saveCallback = saveList(personStore, list)
        Assert.assertNull(saveCallback.result)
        val error = saveCallback.error
        Assert.assertNotNull(error)
        Assert.assertEquals(error?.javaClass, IllegalStateException::class.java)
        Assert.assertTrue(error?.message?.contains(ERR_EMPTY_LIST_MSG) == true)
    }

    // save an item without _id
    // save another item without _id
    // push(), should use a multi-insert POST request and return the items with POST operations
    // pendingSyncEntities(), should return an empty array
    // find using syncstore, should return all items from step 1
    @Throws(InterruptedException::class)
    protected fun testPushMultiInsertSupport(storeType: StoreType) {

        val personsList = createPersonsList(2, false)

        val netManager = MockMultiInsertNetworkManager(Person.COLLECTION, Person::class.java, client as Client)
        val personStore = DataStore(Person.COLLECTION, Person::class.java, client, storeType, netManager)
        val storeSync = DataStore(Person.COLLECTION, Person::class.java, client, StoreType.SYNC, netManager)

        clearBackend(personStore)
        clearBackend(storeSync)
        client?.syncManager?.clear(Person.COLLECTION)

        personsList.onEach { item -> save(storeSync, item) }

        netManager.clear()
        val pushCallback = push(personStore, LONG_TIMEOUT)
        Assert.assertNotNull(pushCallback.result)
        Assert.assertEquals(pushCallback.result?.successCount, personsList.count())
        Assert.assertTrue(netManager.useMultiInsertSave)

        val syncItems = pendingSyncEntities(Person.COLLECTION)
        Assert.assertTrue(syncItems == null || syncItems.isEmpty())

        val findCallback = find(storeSync, LONG_TIMEOUT)
        Assert.assertNotNull(findCallback.result)
        Assert.assertTrue(checkPersonIfSameObjects(personsList, findCallback.result?.result))
    }

    // create an array of 3 items, the second of which has invalid _geoloc parameters
    // save()
    // Sync(), Should return error for not pushed item
    // pendingSyncEntities(), should return the item with invalid params
    // find() using networkstore, should return the valid items
    // find using syncstore, should return all items including the invalid one
    @Throws(InterruptedException::class)
    protected fun testSyncItemsList(mockConnectionErr: Boolean, storeType: StoreType) {

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
        Assert.assertNotNull(syncCallback.error)
        if (syncCallback.error is KinveySaveBatchException) {
            val resultEntities = (syncCallback.error as KinveySaveBatchException).entities as List<Person>?
            val errorsList = (syncCallback.error as KinveySaveBatchException).errors
            Assert.assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesSuccess, false))
            Assert.assertTrue(checkIfPersonItemsAtRightIndex(personList, resultEntities, checkIndexesErr, true))
            val errMessages = arrayOf(ERR_GEOLOC_MSG)
            Assert.assertTrue(checkBatchResponseErrors(errorsList, checkIndexesErr, true, errMessages))
        }

        val syncItems = pendingSyncEntities(Person.COLLECTION)
        Assert.assertNotNull(syncItems)
        Assert.assertEquals(syncItems?.count(), 1)

        val findCallbackNet = find(personStoreNet, LONG_TIMEOUT)
        val findCallbackSync = find(personStoreSync, LONG_TIMEOUT)

        Assert.assertNotNull(findCallbackNet.result)
        Assert.assertTrue(checkIfPersonItemsAtRightIndex(personList, findCallbackNet.result?.result, checkIndexesSuccess, false))

        Assert.assertNotNull(findCallbackSync.result)
        Assert.assertTrue(checkPersonIfSameObjects(personList, findCallbackSync.result?.result))
    }
}

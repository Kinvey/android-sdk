package com.kinvey.androidTest.store.datastore

import android.content.Context
import android.os.Message
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.callback.KinveyCountCallback
import com.kinvey.android.callback.KinveyDeleteCallback
import com.kinvey.android.callback.KinveyPurgeCallback
import com.kinvey.android.callback.KinveyReadCallback
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.android.store.UserStore.Companion.login
import com.kinvey.android.sync.KinveyPullCallback
import com.kinvey.android.sync.KinveyPushCallback
import com.kinvey.android.sync.KinveyPushResponse
import com.kinvey.android.sync.KinveySyncCallback
import com.kinvey.androidTest.LooperThread
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.model.*
import com.kinvey.androidTest.util.TableNameManagerUtil
import com.kinvey.java.AbstractClient
import com.kinvey.java.AbstractClient.Companion.kinveyApiVersion
import com.kinvey.java.Constants
import com.kinvey.java.Query
import com.kinvey.java.cache.KinveyCachedClientCallback
import com.kinvey.java.core.AbstractKinveyClient
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.model.KinveyPullResponse
import com.kinvey.java.model.KinveyReadResponse
import com.kinvey.java.store.StoreType
import com.kinvey.java.sync.dto.SyncItem
import io.realm.DynamicRealm
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.net.SocketTimeoutException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

open class BaseDataStoreTest {

    var client: Client<User>? = null

    @Before
    @Throws(InterruptedException::class, IOException::class)
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        kinveyApiVersion = "4"
        client = Builder<User>(mMockContext).build()
        client?.enableDebugLogging()
        kinveyApiVersion = "4"
        val latch = CountDownLatch(1)
        var looperThread: LooperThread? = null
        if (client?.isUserLoggedIn == false) {
            looperThread = LooperThread(Runnable {
                try {
                    login<User>(TestManager.USERNAME, TestManager.PASSWORD, client as AbstractClient<User>, object : KinveyClientCallback<User> {
                        override fun onSuccess(result: User?) {
                            assertNotNull(result)
                            latch.countDown()
                        }

                        override fun onFailure(error: Throwable?) {
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

    class DefaultKinveyClientCallback (private val latch: CountDownLatch) : KinveyClientCallback<Person> {
        var result: Person? = null
        var error: Throwable? = null
        override fun onSuccess(result: Person?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyEntityCallback (private val latch: CountDownLatch) : KinveyClientCallback<EntitySet> {
        var result: EntitySet? = null
        var error: Throwable? = null
        override fun onSuccess(result: EntitySet?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyPurgeCallback (private val latch: CountDownLatch) : KinveyPurgeCallback {
        var error: Throwable? = null
        override fun onSuccess(result: Void?) {
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyClientArrayCallback (private val latch: CountDownLatch) : KinveyClientCallback<PersonArray> {
        var result: PersonArray? = null
        var error: Throwable? = null
        override fun onSuccess(result: PersonArray?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveySyncCallback (private val latch: CountDownLatch) : KinveySyncCallback {
        var kinveyPushResponse: KinveyPushResponse? = null
        var kinveyPullResponse: KinveyPullResponse? = null
        var error: Throwable? = null
        override fun onSuccess(kinveyPushResponse: KinveyPushResponse?, kinveyPullResponse: KinveyPullResponse?) {
            this.kinveyPushResponse = kinveyPushResponse
            this.kinveyPullResponse = kinveyPullResponse
            finish()
        }
        override fun onPullStarted() {}
        override fun onPushStarted() {}
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
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyPushCallback internal constructor(private val latch: CountDownLatch) : KinveyPushCallback {
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
        override fun onProgress(current: Long, all: Long) {}
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyPullCallback internal constructor(private val latch: CountDownLatch) : KinveyPullCallback {
        var result: KinveyPullResponse? = null
        var error: Throwable? = null
        override fun onSuccess(result: KinveyPullResponse?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyCountCallback(private val latch: CountDownLatch) : KinveyCountCallback {
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
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyCachedCallback<T>() : KinveyCachedClientCallback<Int> {
        var latch: CountDownLatch? = null
        var result: Int? = null
        var error: Throwable? = null
        constructor(latch: CountDownLatch?): this() {
            this.latch = latch
        }
        override fun onSuccess(result: Int?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch?.countDown()
        }
    }

    class CustomKinveyCachedCallback<T>() : KinveyCachedClientCallback<T> {
        var latch: CountDownLatch? = null
        var result: T? = null
        var error: Throwable? = null
        constructor(latch: CountDownLatch?): this() {
            this.latch = latch
        }
        override fun onSuccess(result: T?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch?.countDown()
        }
    }

    class DefaultKinveyDeleteCallback(private val latch: CountDownLatch) : KinveyDeleteCallback {
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
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyReadCallback(private val latch: CountDownLatch) : KinveyReadCallback<Person> {
        var result: KinveyReadResponse<Person>? = null
        var error: Throwable? = null
        override fun onSuccess(result: KinveyReadResponse<Person>?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyReadEntitySetCallback(private val latch: CountDownLatch) : KinveyReadCallback<EntitySet> {
        var result: KinveyReadResponse<EntitySet>? = null
        var error: Throwable? = null
        override fun onSuccess(result: KinveyReadResponse<EntitySet>?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyClientEntitySetCallback(private val latch: CountDownLatch) : KinveyClientCallback<EntitySet> {
        var result: EntitySet? = null
        var error: Throwable? = null
        override fun onSuccess(result: EntitySet?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyReadDateCallback(private val latch: CountDownLatch) : KinveyReadCallback<DateExample> {
        var result: KinveyReadResponse<DateExample>? = null
        var error: Throwable? = null
        override fun onSuccess(result: KinveyReadResponse<DateExample>?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    class DefaultKinveyDateCallback(private val latch: CountDownLatch) : KinveyClientCallback<DateExample> {
        var result: DateExample? = null
        var error: Throwable? = null
        override fun onSuccess(result: DateExample?) {
            this.result = result
            finish()
        }
        override fun onFailure(error: Throwable?) {
            this.error = error
            finish()
        }
        fun finish() {
            latch.countDown()
        }
    }

    class ChangeTimeout : HttpRequestInitializer {
        @Throws(SocketTimeoutException::class)
        override fun initialize(request: HttpRequest) {
            throw SocketTimeoutException()
        }
    }

    fun createPerson(name: String): Person {
        return Person(name)
    }

    @Throws(InterruptedException::class)
    fun save(store: DataStore<Person>, person: Person): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { store.save(person, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun testSave(storeType: StoreType) {
        val store = collection(COLLECTION, Person::class.java, storeType, client)
        client?.syncManager?.clear(COLLECTION)
        val callback = save(store, createPerson(TEST_USERNAME))
        assertNotNull(callback.result)
        assertNotNull(callback.result?.username)
        assertNull(callback.error)
        assertTrue(callback.result?.username == TEST_USERNAME)
    }

    @Throws(InterruptedException::class)
    fun testUpdate(storeType: StoreType) {
        val store = collection(COLLECTION, Person::class.java, storeType, client)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        var callback = save(store, person)
        assertNotNull(callback.result)
        assertNotNull(callback.result?.username)
        assertNull(callback.error)
        assertTrue(callback.result?.username == TEST_USERNAME)
        person.username = TEST_USERNAME_2
        callback = save(store, person)
        assertNotNull(callback.result)
        assertNotNull(callback.result?.username)
        assertNull(callback.error)
        assertFalse(callback.result?.username == TEST_USERNAME)
    }

    @Throws(InterruptedException::class)
    fun saveDate(store: DataStore<DateExample>, `object`: DateExample): DefaultKinveyDateCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDateCallback(latch)
        val looperThread = LooperThread(Runnable { store.save(`object`, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun saveEntitySet(store: DataStore<EntitySet>, `object`: EntitySet): DefaultKinveyEntityCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyEntityCallback(latch)
        val looperThread = LooperThread(Runnable { store.save(`object`, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun findDate(store: DataStore<DateExample>, query: Query?, seconds: Int): DefaultKinveyReadDateCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyReadDateCallback(latch)
        val looperThread = LooperThread(Runnable { store.find(query!!, callback, null) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun deleteDate(store: DataStore<DateExample>, query: Query?): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store.delete(query!!, callback) })
        looperThread.start()
        latch.await(120, TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun savePersonArray(store: DataStore<PersonArray>, person: PersonArray): DefaultKinveyClientArrayCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientArrayCallback(latch)
        val looperThread = LooperThread(Runnable { store.save(person, callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun find(store: DataStore<Person>, id: String?, seconds: Int, cachedClientCallback: KinveyCachedClientCallback<Person>?): DefaultKinveyClientCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientCallback(latch)
        val looperThread = LooperThread(Runnable { store.find(id!!, callback, cachedClientCallback) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun testFindById(storeType: StoreType) {
        val store = collection(COLLECTION, Person::class.java, storeType, client)
        if (storeType !== StoreType.NETWORK) {
            cleanBackendDataStore(store)
        }
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        val personId = saveCallback.result?.id
        val findCallback = find(store, personId, DEFAULT_TIMEOUT, null)
        assertNotNull(findCallback.result)
        assertNull(saveCallback.error)
        assertEquals(findCallback.result?.id, personId)
    }

    @Throws(InterruptedException::class)
    fun find(store: DataStore<Person>, query: Query?, seconds: Int): DefaultKinveyReadCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyReadCallback(latch)
        val looperThread = LooperThread(Runnable { store.find(query!!, callback, null) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun find(store: DataStore<Person>, seconds: Int): DefaultKinveyReadCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyReadCallback(latch)
        val looperThread = LooperThread(Runnable { store.find(callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun testFindByQuery(storeType: StoreType) {
        val store = collection(COLLECTION, Person::class.java, storeType, client)
        clearBackend(store)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        val userId = saveCallback?.result?.id
        var query = client?.query()
        query = query?.equals(ID, userId)
        val kinveyListCallback = find(store, query, DEFAULT_TIMEOUT)
        assertNull(kinveyListCallback.error)
        assertNotNull(kinveyListCallback.result)
        assertTrue(kinveyListCallback.result?.result?.size ?: 0 > 0)
        delete(store, query)
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

    @Throws(InterruptedException::class)
    fun findEntitySet(store: DataStore<EntitySet>, seconds: Int): DefaultKinveyReadEntitySetCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyReadEntitySetCallback(latch)
        val looperThread = LooperThread(Runnable { store.find(callback) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun findIdEntitySet(store: DataStore<EntitySet>, id: String, seconds: Int): DefaultKinveyClientEntitySetCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyClientEntitySetCallback(latch)
        val looperThread = LooperThread(Runnable { store.find(id, callback, null) })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    fun pendingSyncEntities(collectionName: String): List<SyncItem>? {
        return client?.syncManager?.popSingleItemQueue(collectionName)
    }

    @Throws(InterruptedException::class)
    fun createAndSavePerson(store: DataStore<Person>, username: String) {
        val person = createPerson(username)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
    }

    @Throws(InterruptedException::class, IOException::class)
    fun testFindCount(storeType: StoreType, isCachedCallbackUsed: Boolean) {
        val store = collection(COLLECTION, Person::class.java, storeType, client)
        if (storeType !== StoreType.NETWORK) {
            client?.syncManager?.clear(COLLECTION)
        }
        clearBackend(store)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        var cachedCallback: DefaultKinveyCachedCallback<Int>? = null
        if (storeType === StoreType.CACHE && isCachedCallbackUsed) {
            cachedCallback = DefaultKinveyCachedCallback()
        }
        val countCallback = findCount(store, DEFAULT_TIMEOUT, cachedCallback)
        assertNull(countCallback.error)
        assertNotNull(countCallback.result)
        if (storeType === StoreType.CACHE && isCachedCallbackUsed) {
            assertNotNull(cachedCallback?.result)
            assertNotNull(cachedCallback?.result == 1)
            assertNull(cachedCallback?.error)
        }
        assertTrue(countCallback.result == 1)
    }

    @Throws(InterruptedException::class, IOException::class)
    fun findCount(store: DataStore<Person>, seconds: Int, cachedClientCallback: DefaultKinveyCachedCallback<Int>?): DefaultKinveyCountCallback {
        val latch = CountDownLatch(if (cachedClientCallback != null) 2 else 1)
        if (cachedClientCallback != null) {
            cachedClientCallback.latch = latch
        }
        val callback = DefaultKinveyCountCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (cachedClientCallback != null) {
                store.count(callback, cachedClientCallback)
            } else {
                store.count(callback)
            }
        })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun delete(store: DataStore<Person>, id: String?, seconds: Int): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store.delete(id, callback) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun testDelete(storeType: StoreType) {
        val store = collection(COLLECTION, Person::class.java, storeType, client)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        val userId = saveCallback.result?.id
        val deleteCallback = delete(store, userId, DEFAULT_TIMEOUT)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        assertTrue(deleteCallback.result == 1)
    }

    @Throws(InterruptedException::class)
    fun testDeleteNullId(storeType: StoreType) {
        val store = collection(COLLECTION, Person::class.java, storeType, client)
        client?.syncManager?.clear(COLLECTION)
        val person = createPerson(TEST_USERNAME)
        val saveCallback = save(store, person)
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        val deleteCallback = delete(store, null, DEFAULT_TIMEOUT)
        assertNotNull(deleteCallback.error)
        assertNull(deleteCallback.result)
    }

    @Throws(InterruptedException::class)
    fun delete(store: DataStore<Person>, entityIDs: Iterable<String>): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store.delete(entityIDs, callback) })
        looperThread.start()
        latch.await(120, TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun testDeleteArray(storeType: StoreType) {
        val store = collection(COLLECTION, Person::class.java, storeType, client)
        client?.syncManager?.clear(COLLECTION)
        var saveCallback = save(store, createPerson(TEST_USERNAME))
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        val user1Id = saveCallback.result?.id ?: ""
        saveCallback = save(store, createPerson(TEST_USERNAME_2))
        assertNotNull(saveCallback.result)
        assertNull(saveCallback.error)
        assertNotNull(saveCallback.result?.id)
        val user2Id = saveCallback.result?.id ?: ""
        assertNotEquals(user1Id, user2Id)
        val list = listOf(user1Id, user2Id)
        val deleteCallback = delete(store, list)
        assertNull(deleteCallback.error)
        assertNotNull(deleteCallback.result)
        assertTrue(deleteCallback.result == list.size)
    }

    fun removeFiles(path: String) {
        var file = File(path)
        if (file.exists()) {
            file.delete()
        }
        val lockPath = "$path.lock"
        file = File(lockPath)
        if (file.exists()) {
            file.delete()
        }
        val logPath = "$path.management"
        file = File(logPath)
        if (file.exists()) {
            file.delete()
        }
    }

    @Throws(InterruptedException::class)
    fun purge(query: Query?, store: DataStore<Person>): DefaultKinveyPurgeCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyPurgeCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (query != null) {
                store.purge(query, callback)
            } else {
                store.purge(callback)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun sync(store: DataStore<Person>, seconds: Int): DefaultKinveySyncCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveySyncCallback(latch)
        val looperThread = LooperThread(Runnable { store.sync(callback) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun sync(store: DataStore<Person>, query: Query, seconds: Int): DefaultKinveySyncCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveySyncCallback(latch)
        val looperThread = LooperThread(Runnable { store.sync(query, callback) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun push(store: DataStore<Person>, seconds: Int): DefaultKinveyPushCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyPushCallback(latch)
        val looperThread = LooperThread(Runnable { store.push(callback) })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun pull(store: DataStore<Person>, query: Query?): DefaultKinveyPullCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyPullCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (query != null) {
                store.pull(query, callback)
            } else {
                store.pull(callback)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun pullEntitySet(store: DataStore<EntitySet>, query: Query?): DefaultKinveyPullCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyPullCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (query != null) {
                store.pull(query, callback)
            } else {
                store.pull(callback)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun pull(store: DataStore<Person>, query: Query?, pageSize: Int): DefaultKinveyPullCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyPullCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (query != null) {
                store.pull(query, pageSize, callback)
            } else {
                store.pull(pageSize, callback)
            }
        })
        looperThread.start()
        latch.await()
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    @Throws(InterruptedException::class)
    fun delete(store: DataStore<Person>, query: Query?): DefaultKinveyDeleteCallback {
        val latch = CountDownLatch(1)
        val callback = DefaultKinveyDeleteCallback(latch)
        val looperThread = LooperThread(Runnable { store.delete(query!!, callback) })
        looperThread.start()
        latch.await(120, TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    //cleaning backend store
    @Throws(InterruptedException::class)
    fun cleanBackendDataStore(store: DataStore<Person>) {
        val syncCallback = sync(store, 120)
        assertNull(syncCallback.error)
        var query = client?.query()
        query = query?.notEqual("age", "100500")
        val deleteCallback = delete(store, query)
        assertNull(deleteCallback.error)
        val pushCallback = push(store, 120)
        assertNull(pushCallback.error)
        assertTrue(pushCallback.result?.listOfExceptions?.size == 0)
        Log.d("testPull", " : clearing backend store successful")
    }

    @Throws(InterruptedException::class)
    fun clearBackend(store: DataStore<Person>) {
        var query = client?.query()
        query = query?.notEqual("age", "100500")
        val deleteCallback = delete(store, query)
    }

    //use for COLLECTION and for Person.class
    fun getCacheSize(storeType: StoreType): Long {
        return client?.cacheManager?.getCache(COLLECTION, Person::class.java, storeType.ttl)?.get()?.size?.toLong() ?: 0
    }

    fun isCollectionHasOneTable(collection: String, realm: DynamicRealm?): Boolean {
        val currentSchema = realm?.schema
        var className: String
        var schemaCounter = 0
        val schemas = currentSchema?.all!!
        for (schema in schemas) {
            className = schema.className
            if (className == TableNameManagerUtil.getShortName(collection, realm)) {
                schemaCounter++
            }
        }
        return schemaCounter == 1
    }

    /**
     * Check that main and each internal tables have correct items count
     * @param expectedItemsCount expected items count
     */
    fun checkInternalTablesHasItems(expectedItemsCount: Int, collection: String?, realm: DynamicRealm?) {
        val currentSchema = realm?.schema
        var originalName: String?
        var className: String
        val schemas = currentSchema?.all
        schemas?.forEach { schema ->
                className = schema.className
//search class
            if (className == TableNameManagerUtil.getShortName(collection, realm)) {
                assertTrue(realm?.where(TableNameManagerUtil.getShortName(collection, realm))?.count() == expectedItemsCount.toLong())
                //search sub-classes
                schemas.forEach { subClassSchema ->
                    originalName = TableNameManagerUtil.getOriginalName(subClassSchema.className, realm)
                    if (originalName != null && originalName?.startsWith(className + Constants.UNDERSCORE) == true) {
                        checkInternalTablesHasItems(expectedItemsCount, originalName, realm)
                    }
                }
            }
        }
    }

    @After
    fun tearDown() {
        client?.performLockDown()
        if (Client.kinveyHandlerThread != null) {
            try {
                client?.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    @Throws(InterruptedException::class)
    fun find(store: DataStore<Person>, ids: Iterable<String>, seconds: Int,
                     cachedClientCallback: CustomKinveyCachedCallback<KinveyReadResponse<Person>?>?): DefaultKinveyReadCallback {
        val latch = CountDownLatch(if (cachedClientCallback != null) 2 else 1)
        if (cachedClientCallback != null) {
            cachedClientCallback.latch = latch
        }
        val callback = DefaultKinveyReadCallback(latch)
        val looperThread = LooperThread(Runnable {
            if (cachedClientCallback != null) {
                store.find(ids, callback, cachedClientCallback)
            } else {
                store.find(ids, callback)
            }
        })
        looperThread.start()
        latch.await(seconds.toLong(), TimeUnit.SECONDS)
        looperThread.mHandler?.sendMessage(Message())
        return callback
    }

    companion object {
        const val COLLECTION = "PersonsNew"
        const val TEST_USERNAME = "Test_UserName"
        const val TEST_USERNAME_2 = "Test_UserName_2"
        const val TEST_TEMP_USERNAME = "Temp_UserName"
        const val USERNAME = "username"
        const val ID = "_id"
        const val KMD = "_kmd"
        const val SORT_FIELD = "_kmd.ect"
        const val LMT = "lmt"
        const val FIELD = "field"
        const val DEFAULT_TIMEOUT = 60
        const val LONG_TIMEOUT = 6 * DEFAULT_TIMEOUT
    }
}
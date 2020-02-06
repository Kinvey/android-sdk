package com.kinvey.androidTest.store.datastore

import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.Client.Companion.kinveyHandlerThread
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.TestManager.*
import com.kinvey.androidTest.TestManager.Companion.PASSWORD
import com.kinvey.androidTest.TestManager.Companion.TEST_USERNAME
import com.kinvey.androidTest.TestManager.Companion.TEST_USERNAME_2
import com.kinvey.androidTest.TestManager.Companion.USERNAME
import com.kinvey.androidTest.callback.CustomKinveyPullCallback
import com.kinvey.androidTest.model.Person
import com.kinvey.java.AbstractClient
import com.kinvey.java.core.AbstractKinveyClient
import com.kinvey.java.store.StoreType
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.lang.reflect.Field
import java.util.*

@RunWith(AndroidJUnit4::class)
@SmallTest
class PaginationTest {

    private var client: Client<*>? = null
    private var testManager: TestManager<Person>? = null

    @Before
    @Throws(InterruptedException::class)
    fun setUp() {
        val mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        testManager = TestManager()
        testManager?.login(USERNAME, PASSWORD, client)
    }

    @After
    fun tearDown() {
        client?.performLockDown()
        if (kinveyHandlerThread != null) {
            try {
                client?.stopKinveyHandlerThread()
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPagedPull() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        client?.syncManager?.clear(COLLECTION)
        testManager?.cleanBackendDataStore(store)

        // Arrange
        val curly = Person("Curly")
        val larry = Person("Larry")
        val moe = Person("Moe")
        testManager?.save(store, curly)
        testManager?.save(store, larry)
        testManager?.save(store, moe)

        val cacheSizeBefore = testManager?.getCacheSize(StoreType.CACHE, client)
        assertTrue(cacheSizeBefore == 3L)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.CACHE.ttl)?.clear()

        val cacheSizeBetween = testManager?.getCacheSize(StoreType.CACHE, client)
        assertTrue(cacheSizeBetween == 0L)

        // Act
        val pullCallback = testManager?.pullCustom(store, null, 2)

        // Assert
        assertNull(pullCallback?.error)
        assertNotNull(pullCallback?.result)
        assertTrue(pullCallback?.result?.count == 3)
        assertTrue(pullCallback?.result?.count?.toLong() == testManager?.getCacheSize(StoreType.CACHE, client))
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun syncErrorPaginationAsync() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        client?.syncManager?.clear(COLLECTION)

        testManager?.createPersons(store, 10)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 10L)
        mockInvalidConnection()
        val syncCallback = testManager?.sync(store, null, false)
        cancelMockInvalidConnection()
        assertNull(syncCallback?.kinveyPushResponse)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncPagedError() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        client?.syncManager?.clear(COLLECTION)

        testManager?.createPersons(store, 10)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 10L)
        mockInvalidConnection()
        val syncCallback = testManager?.sync(store, client?.query()?.equals("username", TestManager.TEST_USERNAME + 0)
                ?.or(client?.query()?.equals("username", TestManager.TEST_USERNAME + 1)), 1)
        cancelMockInvalidConnection()
        assertNull(syncCallback?.kinveyPushResponse)
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

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPagedPullBlocking() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        client?.syncManager?.clear(COLLECTION)
        testManager?.cleanBackendDataStore(store)

        // Arrange
        val persons = ArrayList<Person>()
        val alvin = Person("Alvin")
        val simon = Person("Simon")
        val theodore = Person("Theodore")
        testManager?.save(store, alvin)
        testManager?.save(store, simon)
        testManager?.save(store, theodore)
        val cacheSizeBefore = testManager?.getCacheSize(StoreType.CACHE, client)
        assertTrue(cacheSizeBefore == 3L)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.CACHE.ttl)?.clear()
        val cacheSizeBetween = testManager?.getCacheSize(StoreType.CACHE, client)
        assertTrue(cacheSizeBetween == 0L)

        // Act
        val response = store.pullBlocking(null, 2)
        assertEquals(3, response.count.toLong())
        assertTrue(response.count.toLong() == testManager?.getCacheSize(StoreType.CACHE, client))
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncBlockingPaged() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        client?.syncManager?.clear(COLLECTION)

        var person = Person(TEST_USERNAME)
        testManager?.save(store, person)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 1L)
        store.pushBlocking()

        person = Person(TEST_USERNAME_2)
        testManager?.save(store, person)
        store.syncBlocking(client?.query(), 1)

        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        assertTrue(testManager?.find(store, client?.query())?.result?.result?.size == 2)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncPaged() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        client?.syncManager?.clear(COLLECTION)

        testManager?.createPersons(store, 10)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 10L)

        val syncCallback = testManager?.sync(store, null, 1)
        assertNull(syncCallback?.error)
        assertNotNull(syncCallback?.kinveyPushResponse?.successCount)
        assertEquals(10, syncCallback?.kinveyPushResponse?.successCount)
        assertNotNull(syncCallback?.result)
        assertEquals(0, syncCallback?.result?.listOfExceptions?.size)
        assertEquals(10, syncCallback?.result?.count)

        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        assertEquals(10, store.find()?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncPagedWithQuery() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        client?.syncManager?.clear(COLLECTION)

        testManager?.createPersons(store, 10)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 10L)

        val syncCallback = testManager?.sync(store, client?.query()?.equals("username", TEST_USERNAME + 0)
                ?.or(client?.query()?.equals("username", TEST_USERNAME + 1)), 1)
        assertNull(syncCallback?.error)

        assertNotNull(syncCallback?.kinveyPushResponse?.successCount)
        assertEquals(10, syncCallback?.kinveyPushResponse?.successCount)
        assertNotNull(syncCallback?.result)
        assertEquals(0, syncCallback?.result?.listOfExceptions?.size)
        assertEquals(2, syncCallback?.result?.count)

        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        assertEquals(10, store.find()?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncWithAutoPagination() {
        syncAutoPaginationAsync(true)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSyncWithoutAutoPagination() {
        syncAutoPaginationAsync(false)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun syncAutoPaginationAsync(isAutoPagination: Boolean) {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        client?.syncManager?.clear(COLLECTION)

        testManager?.createPersons(store, 10)
        assertTrue(client?.syncManager?.getCount(COLLECTION) == 10L)

        val syncCallback = testManager?.sync(store, null, isAutoPagination)
        assertNull(syncCallback?.error)
        assertNotNull(syncCallback?.kinveyPushResponse?.successCount)
        assertEquals(10, syncCallback?.kinveyPushResponse?.successCount)
        assertNotNull(syncCallback?.result)
        assertEquals(0, syncCallback?.result?.listOfExceptions?.size)
        assertEquals(10, syncCallback?.result?.count)

        assertTrue(client?.syncManager?.getCount(COLLECTION) == 0L)
        assertEquals(10, store.find()?.result?.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testPagedPullNotCorrectItem() {
        val store = collection(Person.COLLECTION_WITH_EXCEPTION, Person::class.java, StoreType.SYNC, client)
        val pullCallback = testManager?.pullCustom(store, null, 2)
        assertTrue(pullCallback?.result?.listOfExceptions?.size == 1)
        assertTrue(pullCallback?.result?.count == 4)
        testManager?.cleanBackend(store, StoreType.SYNC)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullBlockingWithAutoPagination() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        testManager?.cleanBackend(store, StoreType.SYNC)
        testManager?.createPersons(store, 5)
        testManager?.push(store)

        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.clear()
        assertEquals(5, store.pullBlocking(client?.query(), true)?.count)
        assertEquals(5L, testManager?.getCacheSize(StoreType.SYNC, client))
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullBlockingWithoutAutoPagination() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        testManager?.cleanBackend(store, StoreType.SYNC)

        testManager?.createPersons(store, 5)
        testManager?.push(store)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.clear()

        assertEquals(5, store.pullBlocking(client?.query(), false).count)
        assertEquals(5L, testManager?.getCacheSize(StoreType.SYNC, client))
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullAsyncWithAutoPagination() {
        pullAutoPaginationAsync(true)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testPullAsyncWithoutAutoPagination() {
        pullAutoPaginationAsync(false)
    }

    @Throws(InterruptedException::class, IOException::class)
    private fun pullAutoPaginationAsync(isAutoPagination: Boolean) {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        client?.syncManager?.clear(COLLECTION)
        testManager?.cleanBackend(store, StoreType.SYNC)
        testManager?.createPersons(store, 5)
        testManager?.push(store)
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.clear()
        assertEquals(5, testManager?.pullCustom(store, client?.query(), isAutoPagination)?.result?.count)
        assertEquals(5L, testManager?.getCacheSize(StoreType.SYNC, client))
        client?.cacheManager?.getCache(COLLECTION, Person::class.java, StoreType.SYNC.ttl)?.clear()
        assertEquals(5, testManager?.pullCustom(store, null, isAutoPagination)?.result?.count)
        assertEquals(5L, testManager?.getCacheSize(StoreType.SYNC, client))
    }

    @Test
    fun testPagedPullPrecondition() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        try {
            store.pull(client?.query(), -1, null)
            assertFalse(true)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("pageSize mustn't be less than 0") == true)
        }
    }

    @Test
    fun testPagedPullPreconditionWithoutQuery() {
        val store = collection(COLLECTION, Person::class.java, StoreType.CACHE, client)
        try {
            store.pull(-1, null)
            assertFalse(true)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("pageSize mustn't be less than 0") == true)
        }
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun testDeletingItemsFromTheCacheIfItemsWereDeletedAtTheBackendUsingAP() {
        val store = collection(COLLECTION, Person::class.java, StoreType.SYNC, client)
        val networkStore = collection(COLLECTION, Person::class.java, StoreType.NETWORK, client)
        client?.syncManager?.clear(COLLECTION)
        testManager?.cleanBackend(networkStore, StoreType.NETWORK)

        (0..29).forEach { i ->
            testManager?.save(networkStore, Person(TEST_USERNAME_2))
        }
        (0..4).forEach { i ->
            testManager?.save(networkStore, Person(TEST_USERNAME))
        }
        assertEquals(35, testManager?.pullCustom(store, client?.query(), 10)?.result?.count)
        assertEquals(35, store.count())

        testManager?.delete(networkStore, client?.query()?.equals("username", TEST_USERNAME_2))
        assertEquals(5, testManager?.pullCustom(store, client?.query(), 10)?.result?.count)
        assertEquals(5, store.count())
    }

    companion object {
        const val COLLECTION = "PersonsNew"
    }
}
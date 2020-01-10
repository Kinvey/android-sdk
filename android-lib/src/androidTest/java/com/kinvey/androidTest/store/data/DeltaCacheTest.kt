package com.kinvey.androidTest.store.data

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.kinvey.android.Client
import com.kinvey.android.Client.Builder
import com.kinvey.android.model.User
import com.kinvey.android.store.DataStore
import com.kinvey.android.store.DataStore.Companion.collection
import com.kinvey.androidTest.TestManager
import com.kinvey.androidTest.TestManager.*
import com.kinvey.androidTest.TestManager.Companion.PASSWORD
import com.kinvey.androidTest.TestManager.Companion.TEST_USERNAME
import com.kinvey.androidTest.TestManager.Companion.TEST_USERNAME_2
import com.kinvey.androidTest.TestManager.Companion.USERNAME
import com.kinvey.androidTest.callback.CustomKinveyPullCallback
import com.kinvey.androidTest.callback.CustomKinveySyncCallback
import com.kinvey.androidTest.callback.DefaultKinveyClientCallback
import com.kinvey.androidTest.callback.DefaultKinveyDeleteCallback
import com.kinvey.androidTest.model.Person
import com.kinvey.java.Constants
import com.kinvey.java.Query
import com.kinvey.java.core.KinveyJsonError
import com.kinvey.java.core.KinveyJsonResponseException
import com.kinvey.java.model.KinveyQueryCacheResponse
import com.kinvey.java.network.NetworkManager
import com.kinvey.java.network.NetworkManager.QueryCacheGet
import com.kinvey.java.query.AbstractQuery.SortOrder
import com.kinvey.java.store.BaseDataStore
import com.kinvey.java.store.QueryCacheItem
import com.kinvey.java.store.StoreType
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * Created by yuliya on 12/27/17.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class DeltaCacheTest {
    private var client: Client<*>? = null
    private var testManager: TestManager<Person>? = null
    private var store: DataStore<Person>? = null

    //@Rule
    //val mockitoRule = MockitoJUnit.rule()

    @Before
    @Throws(InterruptedException::class)
    fun setUp() {
        val mMockContext: Context? = InstrumentationRegistry.getInstrumentation().targetContext
        client = Builder<User>(mMockContext).build()
        testManager = TestManager()
        testManager?.login(USERNAME, PASSWORD, client as Client<*>)
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

    /* MLIBZ-2470 Test Server-side Delta Set use cases */

    /* The goal of the test is to make sure delta set is not used when not enabled */
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDeltaSetIsNotUsed() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)
        testManager?.saveCustom(store, Person(TEST_USERNAME))
        store?.pushBlocking()

        var pullCallback = testManager?.pullCustom(store, client?.query())
        assertEquals(1, pullCallback?.result?.count)

        testManager?.saveCustom(store, Person(TEST_USERNAME_2))
        store?.pushBlocking()
        pullCallback = testManager?.pullCustom(store, client?.query())
        assertEquals(2, pullCallback?.result?.count)
        assertFalse(store?.isDeltaSetCachingEnabled == true)
    }

/* The goal of the test is to confirm that empty array will be returned if the user has no changes and the since is respected */
//    @Test
//    public void testEmptyArrayIsReturnedIfNoChanges() throws InterruptedException, IOException {
//        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, client);
//        testManager.cleanBackend(store, StoreType.SYNC);
//        store.setDeltaSetCachingEnabled(true);
//        testManager.saveCustom(store, new Person(TEST_USERNAME));
//        store.pushBlocking();
//
//        CustomKinveyPullCallback pullCallback = testManager.pullCustom(store, client.query());
//        assertEquals(0, pullCallback.getResult().getCount());
//    }
/* end */

    @Test
    @Throws(IOException::class, InvocationTargetException::class, NoSuchMethodException::class,
            ClassNotFoundException::class, InstantiationException::class, IllegalAccessException::class,
            NoSuchFieldException::class)
    fun testDeltaSyncPull() {
        val query = client?.query()
        val lastRequestTime = "Time"
        val mockCacheGet = mock(QueryCacheGet::class.java)
        val mockResponse = KinveyQueryCacheResponse<Person>()
        val people: MutableList<Person> = ArrayList()
        people.add(Person("name_1"))
        people.add(Person("name_2"))
        mockResponse.lastRequestTime = lastRequestTime
        mockResponse.changed = people
        mockResponse.listOfExceptions = ArrayList<Exception>()

        `when`<KinveyQueryCacheResponse<*>>(mockCacheGet.execute()).thenReturn(mockResponse)
        val spyNetworkManager = spy(NetworkManager(Person.DELTA_SET_COLLECTION, Person::class.java, client))
        `when`(spyNetworkManager.queryCacheGetBlocking(query, lastRequestTime))?.thenReturn(mockCacheGet as NetworkManager.QueryCacheGet<Person>?)

        val store = testManager?.mockBaseDataStore(client, Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, spyNetworkManager)
        store?.isDeltaSetCachingEnabled = true
//        Field field = BaseDataStore.class.getDeclaredField("queryCache");
//        field.setAccessible(true);

        val queryCache = client?.syncManager?.cacheManager
                ?.getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem::class.java, Long.MAX_VALUE)
        queryCache?.save(QueryCacheItem(Person.DELTA_SET_COLLECTION, query?.queryFilterMap.toString(), lastRequestTime))
        val response = store?.pullBlocking(query)

        assertNotNull(response)
        assertEquals(0, response?.listOfExceptions?.size)
        assertEquals(2, response?.count)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCreate() {
        client?.enableDebugLogging()
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val person = Person()
        person.username = TEST_USERNAME
        val callback = testManager?.save(store, person)
        assertNotNull(callback)
        assertNotNull(callback?.result)
        assertNull(callback?.error)
        assertNotNull(callback?.result?.username)
        assertEquals(TEST_USERNAME, callback?.result?.username)

        store?.pushBlocking()
        val pullResponse = store?.pullBlocking(client?.query())
        assertNotNull(pullResponse)
        assertEquals(1, pullResponse?.count)

        val syncCallback = testManager?.sync(store, client?.query())
        assertNotNull(syncCallback)
        assertNotNull(syncCallback?.result)
        assertNull(syncCallback?.error)
        assertEquals(0, syncCallback?.result?.count)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testCreateSync() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val person = Person()
        person.username = TEST_USERNAME
        person.set("CustomField", "CustomValue")
        val savedPerson = store?.save(person)
        assertNotNull(savedPerson)
        assertEquals(TEST_USERNAME, savedPerson?.username)

        store?.pushBlocking()
        assertEquals(1, store?.pullBlocking(client?.query())?.count)
        assertNotNull(store?.find()?.result?.get(0))
        assertEquals(TEST_USERNAME, store?.find()?.result?.get(0)?.username)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testRead() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val person = Person()
        person.username = TEST_USERNAME
        person.set("CustomField", "CustomValue")
        val savedPerson = store?.save(person)
        assertNotNull(savedPerson)
        assertEquals(TEST_USERNAME, savedPerson?.username)

        store?.pushBlocking()
        assertEquals(1, store?.pullBlocking(client?.query())?.count)
        assertNotNull(store?.find()?.result?.get(0))
        assertEquals(TEST_USERNAME, store?.find()?.result?.get(0)?.username)

        val personList = testManager?.find(store, client?.query())?.result?.result
        val person1 = personList?.get(0)
        assertNotNull(person1)
        assertNotNull(person1?.username)
        assertEquals(TEST_USERNAME, person1?.username)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testReadALotOfItems() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        testManager?.cleanBackend(store, StoreType.SYNC)
        testManager?.createPersons(store, TEN_ITEMS)
        store?.pushBlocking()

        val query = client?.query()?.addSort("_kmd.ect", SortOrder.ASC) as Query
        assertEquals(TEN_ITEMS, store?.pullBlocking(query)?.count)
        var pulledPersons = store?.find(query)?.result

        assertNotNull(pulledPersons)
        (0 until TEN_ITEMS).forEach { i ->
            val person = pulledPersons?.get(i)
            assertEquals(TEST_USERNAME + i, person?.username)
        }

        assertEquals(0, store?.pullBlocking(query)?.count)
        pulledPersons = store?.find(query)?.result
        (0 until TEN_ITEMS).forEach { i ->
            val person = pulledPersons?.get(i)
            assertEquals(TEST_USERNAME + i, person?.username)
        }

        val foundPersons = testManager?.find(store, query)?.result?.result
        assertNotNull(foundPersons)
        assertEquals(TEN_ITEMS, foundPersons?.size)
        (0 until TEN_ITEMS).forEach { i ->
            val person1 = foundPersons?.get(i)
            assertEquals(TEST_USERNAME + i, person1?.username)
        }
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testReadALotOfItemsWithClearCache() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        testManager?.cleanBackend(store, StoreType.SYNC)
        testManager?.createPersons(store, TEN_ITEMS)
        store?.pushBlocking()
        store?.clear()

        val query = client?.query()?.addSort("_kmd.ect", SortOrder.ASC) as Query
        assertEquals(TEN_ITEMS, store?.pullBlocking(query)?.count)
        var pulledPersons = store?.find(query)?.result
        assertNotNull(pulledPersons)
        (0 until TEN_ITEMS).forEach { i ->
            val person = pulledPersons?.get(i)
            assertEquals(TEST_USERNAME + i, person?.username)
        }

        assertEquals(0, store?.pullBlocking(query)?.count)
        pulledPersons = store?.find(query)?.result
        assertNotNull(pulledPersons)
        (0 until TEN_ITEMS).forEach { i ->
            val person = pulledPersons?.get(i)
            assertEquals(TEST_USERNAME + i, person?.username)
        }

        val foundPersons = testManager?.find(store, query)?.result?.result
        assertNotNull(foundPersons)
        assertEquals(TEN_ITEMS, foundPersons?.size)
        (0 until TEN_ITEMS).forEach { i ->
            val person1 = foundPersons?.get(i)
            assertEquals(TEST_USERNAME + i, person1?.username)
        }
    }

    /**
     * Check that Delta Sync Find works with StoreType.CACHE
     */
    @Test
    @Throws(IOException::class, InvocationTargetException::class, NoSuchMethodException::class,
            ClassNotFoundException::class, InstantiationException::class, IllegalAccessException::class,
            NoSuchFieldException::class)
    fun testDeltaSyncFindByEmptyQuery() {
        val query = client?.query() as Query
        val lastRequestTime = "Time"
        val mockCacheGet = mock(QueryCacheGet::class.java)
        val mockResponse = KinveyQueryCacheResponse<Person>()
        val people: MutableList<Person> = ArrayList()
        people.add(Person("name_1"))
        people.add(Person("name_2"))
        mockResponse.lastRequestTime = lastRequestTime
        mockResponse.changed = people
        mockResponse.listOfExceptions = ArrayList<Exception>()

        `when`<KinveyQueryCacheResponse<*>>(mockCacheGet.execute()).thenReturn(mockResponse)
        val spyNetworkManager: NetworkManager<Person> = spy(NetworkManager(Person.DELTA_SET_COLLECTION, Person::class.java, client))
        `when`(spyNetworkManager.queryCacheGetBlocking(query, lastRequestTime))
                .thenReturn(mockCacheGet as NetworkManager.QueryCacheGet<Person>)

        val store = testManager?.mockBaseDataStore(client, Person.DELTA_SET_COLLECTION,
                Person::class.java, StoreType.CACHE, spyNetworkManager)
        store?.isDeltaSetCachingEnabled = true
        val field = BaseDataStore::class.java.getDeclaredField("queryCache")
        field.isAccessible = true

        val queryCache = client?.syncManager?.cacheManager
                ?.getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem::class.java, Long.MAX_VALUE)
        queryCache?.save(QueryCacheItem(
                Person.DELTA_SET_COLLECTION,
                query?.queryFilterMap.toString(),
                lastRequestTime))
        val response = store?.find(query)?.result

        assertNotNull(response)
        assertEquals("name_1", response?.get(0)?.username)
        assertEquals("name_2", response?.get(1)?.username)
    }

    //ignore while test app won't support delta cache
    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUpdate() {

        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val person = Person()
        person.username = TEST_USERNAME
        person.set("CustomField", "CustomValue")

        val callback = testManager?.save(store, person)
        assertNotNull(callback)
        assertNotNull(callback?.result)
        assertNull(callback?.error)
        assertNotNull(callback?.result?.username)
        assertEquals(TEST_USERNAME, callback?.result?.username)

        var syncCallback = testManager?.sync(store, client?.query())
        assertNotNull(syncCallback)
        assertNotNull(syncCallback?.result)
        assertNull(syncCallback?.error)
        assertNotNull(syncCallback?.result?.count)
        assertEquals(1, syncCallback?.result?.count)

        var personList = testManager?.find(store, client?.query())?.result?.result
        val changedPerson = personList?.get(0)
        assertNotNull(changedPerson)
        assertNotNull(changedPerson?.username)
        assertEquals(TEST_USERNAME, changedPerson?.username)
        changedPerson?.username = "DeltaCacheUserName_changed"
        testManager?.save(store, changedPerson)

        personList = testManager?.find(store, client?.query())?.result?.result
        val changedPerson1 = personList?.get(0)
        assertNotNull(changedPerson1)
        assertNotNull(changedPerson1?.username)
        assertEquals("DeltaCacheUserName_changed", changedPerson1?.username)

        syncCallback = testManager?.sync(store, client?.query())
        assertNotNull(syncCallback)
        assertNotNull(syncCallback?.result)
        assertNull(syncCallback?.error)
        assertNotNull(syncCallback?.result?.count)

        personList = testManager?.find(store, client?.query())?.result?.result
        val person1 = personList?.get(0)
        assertNotNull(person1)
        assertNotNull(person1?.username)
        assertEquals("DeltaCacheUserName_changed", person1?.username)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUpdateSync() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val person = Person()
        person.username = TEST_USERNAME
        person.set("CustomField", "CustomValue")
        val savedPerson = store?.save(person)
        assertNotNull(savedPerson)
        assertEquals(TEST_USERNAME, savedPerson?.username)

        store?.pushBlocking()
        store?.pullBlocking(client?.query())
        var downloadedPerson = store?.find()?.result?.get(0)
        assertNotNull(downloadedPerson)

        var personList = store?.find()?.result
        val changedPerson = personList?.get(0)
        assertNotNull(changedPerson)
        assertNotNull(changedPerson?.username)
        assertEquals(TEST_USERNAME, changedPerson?.username)
        changedPerson?.username = "DeltaCacheUserName_changed"

        store?.save(changedPerson as Person)
        personList = store?.find()?.result
        var changedPerson1 = personList?.get(0)
        assertNotNull(changedPerson1)
        assertNotNull(changedPerson1?.username)
        assertEquals("DeltaCacheUserName_changed", changedPerson1?.username)

        store?.pushBlocking()
        store?.pullBlocking(client?.query())
        downloadedPerson = store?.find()?.result?.get(0)
        assertNotNull(downloadedPerson)
        assertEquals("DeltaCacheUserName_changed", downloadedPerson?.username)

        personList = store?.find()?.result
        changedPerson1 = personList?.get(0)
        assertNotNull(changedPerson1)
        assertNotNull(changedPerson1?.username)
        assertEquals("DeltaCacheUserName_changed", changedPerson1?.username)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDelete() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val person = Person()
        person.username = TEST_USERNAME
        person.set("CustomField", "CustomValue")
        val callback = testManager?.save(store, person)
        assertNotNull(callback)
        assertNotNull(callback?.result)
        assertNull(callback?.error)
        assertNotNull(callback?.result?.username)
        assertEquals(TEST_USERNAME, callback?.result?.username)

        var syncCallback = testManager?.sync(store, client?.query())
        assertNotNull(syncCallback)
        assertNotNull(syncCallback?.result)
        assertNull(syncCallback?.error)
        assertNotNull(syncCallback?.result?.count)
        assertEquals(1, syncCallback?.result?.count)

        var personList = testManager?.find(store, client?.query())?.result?.result
        val foundPerson = personList?.get(0)
        assertNotNull(foundPerson)
        assertNotNull(foundPerson?.username)
        assertEquals(TEST_USERNAME, foundPerson?.username)

        val deleteCallback = testManager?.delete(store, foundPerson?.id)
        assertNotNull(deleteCallback)
        assertNotNull(deleteCallback?.result)
        assertNull(deleteCallback?.error)
        personList = testManager?.find(store, client?.query())?.result?.result
        assertEquals(0, personList?.size)
        assertEquals(1L, store?.syncCount())

        syncCallback = testManager?.sync(store, client?.query())
        assertNotNull(syncCallback)
        assertNotNull(syncCallback?.result)
        assertNull(syncCallback?.error)
        assertEquals(0, syncCallback?.result?.count)
        personList = testManager?.find(store, client?.query())?.result?.result
        assertEquals(0, personList?.size)
        assertEquals(0, store?.count())
        assertEquals(0L, store?.syncCount())
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testDeleteSync() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val person = Person()
        person.username = TEST_USERNAME
        person.set("CustomField", "CustomValue")
        val savedPerson = store?.save(person)
        assertNotNull(savedPerson)
        assertEquals(TEST_USERNAME, savedPerson?.username)
        store?.pushBlocking()
        assertEquals(1, store?.pullBlocking(client?.query())?.count)

        assertNotNull(store?.find()?.result?.get(0))
        assertEquals(TEST_USERNAME, store?.find()?.result?.get(0)?.username)
        var personList = store?.find()?.result
        val foundPerson = personList?.get(0)
        assertNotNull(foundPerson)
        assertNotNull(foundPerson?.username)
        assertEquals(TEST_USERNAME, foundPerson?.username)

        val deletedItemsCount = store?.delete(foundPerson?.id ?: "")
        assertEquals(1, deletedItemsCount)
        personList = store?.find()?.result
        assertEquals(0, personList?.size)
        assertEquals(1L, store?.syncCount())
        store?.pushBlocking()
        store?.pullBlocking(client?.query())

        val downloadedPersons = store?.find()?.result
        assertNotNull(downloadedPersons)
        assertEquals(0, downloadedPersons?.size)
        personList = store?.find()?.result
        assertEquals(0, personList?.size)
        assertEquals(0, store?.count())
        assertEquals(0L, store?.syncCount())
    }

    /**
     * Check Delta Sync Find by Query
     */
    @Test
    @Throws(IOException::class, InvocationTargetException::class, NoSuchMethodException::class,
            ClassNotFoundException::class, InstantiationException::class, IllegalAccessException::class,
            NoSuchFieldException::class)
    fun testDeltaSyncFindByQuery() {
        val query = client?.query()?.equals("name", "name_1") as Query
        val lastRequestTime = "Time"
        val mockCacheGet = mock(QueryCacheGet::class.java)
        val mockResponse = KinveyQueryCacheResponse<Person>()
        val people: MutableList<Person> = ArrayList()
        people.add(Person("name_1"))
        people.add(Person("name_1"))
        mockResponse.lastRequestTime = lastRequestTime
        mockResponse.changed = people
        mockResponse.listOfExceptions = ArrayList<Exception>()

        `when`<KinveyQueryCacheResponse<*>>(mockCacheGet.execute()).thenReturn(mockResponse)
        val spyNetworkManager = spy(NetworkManager(Person.DELTA_SET_COLLECTION, Person::class.java, client))
        `when`(spyNetworkManager.queryCacheGetBlocking(query, lastRequestTime))
                .thenReturn(mockCacheGet as NetworkManager.QueryCacheGet<Person>)

        val store = testManager?.mockBaseDataStore(client, Person.DELTA_SET_COLLECTION,
                Person::class.java, StoreType.CACHE, spyNetworkManager)
        store?.isDeltaSetCachingEnabled = true
        val field = BaseDataStore::class.java.getDeclaredField("queryCache")
        field.isAccessible = true

        val queryCache = client?.syncManager?.cacheManager
                ?.getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem::class.java, Long.MAX_VALUE)
        queryCache?.save(QueryCacheItem(
                Person.DELTA_SET_COLLECTION,
                query?.queryFilterMap.toString(),
                lastRequestTime))
        val response = store?.find(query)?.result

        assertNotNull(response)
        assertEquals("name_1", response?.get(0)?.username)
        assertEquals("name_1", response?.get(1)?.username)
    }

//ignore while test app won't support delta cache

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testSaveWithTwoStorageTypes() {
        client?.enableDebugLogging()
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true

        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        assertFalse(networkStore.isDeltaSetCachingEnabled)
        testManager?.cleanBackend(store, StoreType.SYNC)
        testManager?.createPersons(store, TEN_ITEMS)
        testManager?.sync(store, client?.query())

        var pullCallback = testManager?.pullCustom(store, client?.query())
        assertNotNull(pullCallback)
        assertNotNull(pullCallback?.result)
        assertNull(pullCallback?.error)
        assertEquals(0, pullCallback?.result?.count)

        val saveCallback = testManager?.save(networkStore, Person(TEST_USERNAME + TEN_ITEMS))
        assertNotNull(saveCallback)
        assertNotNull(saveCallback?.result)
        assertNull(saveCallback?.error)
        assertEquals(TEST_USERNAME + TEN_ITEMS, saveCallback?.result?.username)

        pullCallback = testManager?.pullCustom(store, client?.query())
        assertNotNull(pullCallback)
        assertNotNull(pullCallback?.result)
        assertNull(pullCallback?.error)
        assertEquals(1, pullCallback?.result?.count)

        val personList = testManager?.find(store, client?.query())?.result?.result
        assertEquals(TEN_ITEMS + 1, personList?.size)
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun testUpdateWithTwoStorageTypes() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        val networkStore = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.NETWORK, client)
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        assertFalse(networkStore.isDeltaSetCachingEnabled)
        testManager?.cleanBackend(store, StoreType.SYNC)

        testManager?.createPersons(store, TEN_ITEMS)
        testManager?.sync(store, client?.query())
        val personForUpdate = testManager?.find(networkStore, client?.query()?.equals("username", TEST_USERNAME + 0))?.result?.result?.get(0)
        personForUpdate?.username = TEST_USERNAME + 100

        val saveCallback = testManager?.save(networkStore, personForUpdate)
        assertNotNull(saveCallback)
        assertNotNull(saveCallback?.result)
        assertNull(saveCallback?.error)
        assertEquals(TEST_USERNAME + 100, saveCallback?.result?.username)

        val query = client?.query()?.addSort("_kmd.lmt", SortOrder.ASC) as Query
        val pullCallback = testManager?.pullCustom(store, query)
        assertNotNull(pullCallback)
        assertNotNull(pullCallback?.result)
        assertNull(pullCallback?.error)
        assertEquals(1, pullCallback?.result?.count)
        assertEquals(TEST_USERNAME + 100, store?.find(query)?.result?.get(9)?.username)

        val personList = testManager?.find(store, client?.query()?.equals("username", TEST_USERNAME + 100))?.result?.result
        assertEquals(1, personList?.size)
        assertEquals(TEST_USERNAME + 100, personList?.get(0)?.username)
    }

    @Test
    fun testDefaultDeltaCacheValue() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertFalse(store?.isDeltaSetCachingEnabled == true)
    }

    @Test
    fun testChangeDeltaCache() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        store?.isDeltaSetCachingEnabled = true
        assertTrue(store?.isDeltaSetCachingEnabled == true)
        store?.isDeltaSetCachingEnabled = false
        assertFalse(store?.isDeltaSetCachingEnabled == true)
    }

    @Test
    fun testDeprecatedWay() {
        client?.isUseDeltaCache = true
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        assertTrue(store?.isDeltaSetCachingEnabled == true)
    }

/*
    @Test
    public void testResultSetSizeExceededErrorHandling() throws IOException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InterruptedException {
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        Query query = client.query();
        String lastRequestTime = "Time";
        NetworkManager<Person>.QueryCacheGet mockCacheGet = mock(NetworkManager.QueryCacheGet.class);
        KinveyJsonResponseException exception = mock(KinveyJsonResponseException.class);
        KinveyJsonError jsonError = new KinveyJsonError();
        jsonError.setError("ResultSetSizeExceeded");
        when(exception.getDetails()).thenReturn(jsonError);
        when(exception.getStatusCode()).thenReturn(400);
        when(mockCacheGet.execute()).thenThrow(exception);
        NetworkManager<Person> spyNetworkManager = spy(new NetworkManager<>(Person.DELTA_SET_COLLECTION, Person.class, client));
        when(spyNetworkManager.queryCacheGetBlocking(query, lastRequestTime)).thenReturn(mockCacheGet);
        BaseDataStore<Person> store = testManager.mockBaseDataStore(client, Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, spyNetworkManager);
        store.setDeltaSetCachingEnabled(true);
        store.save(new Person(TEST_USERNAME));
        store.save(new Person(TEST_USERNAME));
        store.save(new Person(TEST_USERNAME));
        store.pushBlocking();
        ICache<QueryCacheItem> queryCache = client.getSyncManager().getCacheManager().getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem.class, Long.MAX_VALUE);
        queryCache.save(new QueryCacheItem(
                Person.DELTA_SET_COLLECTION,
                query.getQueryFilterMap().toString(),
                lastRequestTime));
        KinveyPullResponse response = store.pullBlocking(query);
        assertNotNull(response);
        assertEquals(0, response.getListOfExceptions().size());
        assertEquals(3, response.getCount());
    }
*/

/*    @Test
    public void testMissingConfigurationErrorHandling() throws IOException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InterruptedException {
        store = DataStore.collection(Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, client);
        testManager.cleanBackend(store, StoreType.SYNC);
        Query query = client.query();
        String lastRequestTime = "Time";
        NetworkManager<Person>.QueryCacheGet mockCacheGet = mock(NetworkManager.QueryCacheGet.class);
        KinveyJsonResponseException exception = mock(KinveyJsonResponseException.class);
        KinveyJsonError jsonError = new KinveyJsonError();
        jsonError.setError("MissingConfiguration");
        when(exception.getDetails()).thenReturn(jsonError);
        when(exception.getStatusCode()).thenReturn(403);
        when(mockCacheGet.execute()).thenThrow(exception);
        NetworkManager<Person> spyNetworkManager = spy(new NetworkManager<>(Person.DELTA_SET_COLLECTION, Person.class, client));
        when(spyNetworkManager.queryCacheGetBlocking(query, lastRequestTime)).thenReturn(mockCacheGet);
        BaseDataStore<Person> store = testManager.mockBaseDataStore(client, Person.DELTA_SET_COLLECTION, Person.class, StoreType.SYNC, spyNetworkManager);
        store.setDeltaSetCachingEnabled(true);
        store.save(new Person(TEST_USERNAME));
        store.save(new Person(TEST_USERNAME));
        store.pushBlocking();
        ICache<QueryCacheItem> queryCache = client.getSyncManager().getCacheManager().getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem.class, Long.MAX_VALUE);
        queryCache.save(new QueryCacheItem(
                Person.DELTA_SET_COLLECTION,
                query.getQueryFilterMap().toString(),
                lastRequestTime));

        KinveyPullResponse response = store.pullBlocking(query);
        assertEquals(0, response.getListOfExceptions().size());
        assertEquals(2, response.getCount());
    }*/


    @Test
    @Throws(IOException::class, InvocationTargetException::class, NoSuchMethodException::class,
            ClassNotFoundException::class, InstantiationException::class, IllegalAccessException::class,
            InterruptedException::class)
    fun testKinveyErrorHandling() {
        store = collection(Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, client)
        testManager?.cleanBackend(store, StoreType.SYNC)

        val query = client?.query()
        val lastRequestTime = "Time"
        val mockCacheGet = mock(QueryCacheGet::class.java)
        val exception: KinveyJsonResponseException = mock(KinveyJsonResponseException::class.java)
        val jsonError = KinveyJsonError()
        jsonError.error = "KinveyException"
        jsonError.description = "Some Description."

        `when`(exception.details).thenReturn(jsonError)
        `when`(mockCacheGet.execute()).thenThrow(exception)
        val spyNetworkManager = spy(NetworkManager(Person.DELTA_SET_COLLECTION, Person::class.java, client))
        `when`(spyNetworkManager.queryCacheGetBlocking(query, lastRequestTime)).thenReturn(mockCacheGet as NetworkManager.QueryCacheGet<Person>)

        val store = testManager?.mockBaseDataStore(client, Person.DELTA_SET_COLLECTION, Person::class.java, StoreType.SYNC, spyNetworkManager)
        store?.isDeltaSetCachingEnabled = true
        store?.save(Person(TEST_USERNAME))
        store?.save(Person(TEST_USERNAME))
        store?.pushBlocking()

        val queryCache = client?.syncManager?.cacheManager
                ?.getCache(Constants.QUERY_CACHE_COLLECTION, QueryCacheItem::class.java, Long.MAX_VALUE)
        queryCache?.save(QueryCacheItem(
                Person.DELTA_SET_COLLECTION,
                query?.queryFilterMap.toString(),
                lastRequestTime))
        try {
            store?.pullBlocking(query)
        } catch (e: KinveyJsonResponseException) {
            assertEquals(jsonError.error, e.details?.error)
        }
    }

    companion object {
        private const val TEN_ITEMS = 10
    }
}